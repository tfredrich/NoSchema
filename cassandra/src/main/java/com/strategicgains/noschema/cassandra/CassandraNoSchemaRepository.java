package com.strategicgains.noschema.cassandra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.protocol.internal.util.Bytes;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.NoSchemaRepository;
import com.strategicgains.noschema.cassandra.document.CassandraDocumentFactory;
import com.strategicgains.noschema.cassandra.document.DocumentTableSchemaProvider;
import com.strategicgains.noschema.cassandra.schema.SchemaWriter;
import com.strategicgains.noschema.cassandra.unitofwork.UnitOfWorkType;
import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.document.DocumentObserver;
import com.strategicgains.noschema.document.ObjectCodec;
import com.strategicgains.noschema.exception.DuplicateItemException;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.ItemNotFoundException;
import com.strategicgains.noschema.exception.KeyDefinitionException;
import com.strategicgains.noschema.exception.StorageException;
import com.strategicgains.noschema.unitofwork.UnitOfWorkCommitException;

public class CassandraNoSchemaRepository<T extends Identifiable>
implements NoSchemaRepository<T>, SchemaWriter<T>
{
	private CqlSession session;
	private PrimaryTable table;
	private CassandraNoSchemaStatementFactory<T> statementGenerator;
	private Map<String, CassandraDocumentFactory<T>> factoriesByView = new HashMap<>();
	private UnitOfWorkType unitOfWorkType;
	private List<DocumentObserver> observers = new ArrayList<>();


	protected CassandraNoSchemaRepository(CqlSession session, PrimaryTable table, ObjectCodec<T> codec)
	{
		this(session, table, UnitOfWorkType.LOGGED, codec);
	}

	protected CassandraNoSchemaRepository(CqlSession session, PrimaryTable table, UnitOfWorkType unitOfWorkType, ObjectCodec<T> codec)
	{
		super();
		this.session = Objects.requireNonNull(session);
		this.table = Objects.requireNonNull(table);
		this.unitOfWorkType = Objects.requireNonNull(unitOfWorkType);
		this.statementGenerator = new CassandraNoSchemaStatementFactory<>(session, table, codec);
		factoriesByView.put(table.name(), new CassandraDocumentFactory<>(table.keys(), codec));
		table.views().forEach(view ->
			this.factoriesByView.put(view.name(), new CassandraDocumentFactory<>(view.keys(), codec))
		);
	}

	protected boolean hasViews()
	{
		return table.hasViews();
	}

	@Override
	public void ensureTables()
	{
		new DocumentTableSchemaProvider(table).create(session);

		if (hasViews())
		{
			table.views().forEach(v -> new DocumentTableSchemaProvider(v).create(session));
		}
	}

	@Override
	public void dropTables()
	{
		new DocumentTableSchemaProvider(table).drop(session);

		if (hasViews())
		{
			table.views().forEach(v -> new DocumentTableSchemaProvider(v).drop(session));
		}
	}

	protected String tableName()
	{
		return table.name();
	}

	public CassandraNoSchemaRepository<T> withObserver(DocumentObserver observer)
	{
		observers.add(observer);
		return this;
	}

	public T create(T entity)
	{
		CassandraNoSchemaUnitOfWork uow = createUnitOfWork();

		try
		{
			final AtomicReference<byte[]> serialized = new AtomicReference<>();
			final AtomicReference<Document> primaryDocument = new AtomicReference<>();

			table.stream().forEach(t -> {
				final Document d;

				if (serialized.get() == null)
				{
					observers.forEach(o -> o.beforeEncoding(entity));
					d = asDocument(t.name(), entity);
					primaryDocument.set(d);
					serialized.set(d.getObject());
					observers.forEach(o -> o.afterEncoding(primaryDocument.get()));
					observers.forEach(o -> o.beforeCreate(primaryDocument.get()));
				}
				else
				{
					d = asDocument(t.name(), entity, serialized.get());
					d.setMetadata(primaryDocument.get().getMetadata());
				}

				uow.registerNew(t.name(), d);
			});

			uow.commit();
			observers.forEach(o -> o.afterCreate(primaryDocument.get()));
		}
		catch (UnitOfWorkCommitException e)
		{
			handleException(e);
		}

		return entity;
	}

	public void delete(Identifier id)
	{
		try
		{
			CassandraNoSchemaUnitOfWork uow = createUnitOfWork();
			final T entity = read(id);
			final AtomicReference<byte[]> serialized = new AtomicReference<>();
			final AtomicReference<Document> primaryDocument = new AtomicReference<>();

			table.stream().forEach(t -> {
				final Document d;

				if (serialized.get() == null)
				{
					observers.forEach(o -> o.beforeEncoding(entity));
					d = asDocument(t.name(), entity);
					primaryDocument.set(d);
					serialized.set(d.getObject());
					observers.forEach(o -> o.afterEncoding(primaryDocument.get()));
					observers.forEach(o -> o.beforeDelete(primaryDocument.get()));
				}
				else
				{
					d = asDocument(t.name(), entity, serialized.get());
				}

				uow.registerDeleted(t.name(), d);
			});

			uow.commit();
			observers.forEach(o -> o.afterDelete(primaryDocument.get()));
		}
		catch (UnitOfWorkCommitException e)
		{
			handleException(e);
		}
	}

	@Override
	public boolean exists(Identifier id)
	throws InvalidIdentifierException
	{
		return exists(table.name(), id);
	}

	public boolean exists(String viewName, Identifier id)
	{
		return session.executeAsync(statementGenerator.exists(viewName, id))
			.thenApply(r -> (Boolean.valueOf(r.one().getLong(0) > 0)))
			.toCompletableFuture()
			.join();
	}

	@Override
	public T read(Identifier id)
	{
		return read(table.name(), id);
	}
 
	public T read(String viewName, Identifier id)
	{
		try
		{
			return readRow(viewName, id)
				.thenApply(row -> asEntity(viewName, row))
				.join();
		}
		catch (CompletionException e)
		{
			handleException(e);
		}

		return null;
	}

	/**
	 * Retrieve many entities from the primary table using the given [partial] identifier.
	 * Note that values for the partition key portion MUST be included.
	 * 
	 * @param limit the maximum number of rows to return.
	 * @param cursor a hex string representing the page state to start the query.
	 * @param parms properties making up a partial key or identifier.
	 * @return
	 */
	public PagedResponse<T> readAll(int limit, String cursor, Object... parms)
	{
		return readAll(table.name(), limit, cursor, parms);
	}

	/**
	 * Retrieve many entities from a view using the given [partial] identifier.
	 * Note that values for the partition key portion MUST be included.
	 * 
	 * @param viewName the name of the view to query.
	 * @param limit the maximum number of rows to return.
	 * @param cursor a hex string representing the page state to start the query.
	 * @param parms properties making up a partial key or identifier.
	 * @return
	 */
	public PagedResponse<T> readAll(String viewName, int limit, String cursor, Object... parms)
	{
		final PagedResponse<T> response = new PagedResponse<>();
		try
		{
			readRows(viewName, limit, cursor, parms)
				.thenAccept(page -> {
					response.cursor(page.cursor());
					page.stream().forEach(row -> response.add(asEntity(viewName, row)));
				})
				.join();
		}
		catch (CompletionException e)
		{
			handleException(e);
		}

		return response;
	}

	@Override
	public List<T> readIn(List<Identifier> ids)
	{
		return readIn(table.name(), ids);
	}

	public List<T> readIn(String viewName, List<Identifier> ids)
	{
		if (ids == null) return Collections.emptyList();

		List<CompletableFuture<T>> futures = ids.stream().map(id -> 
			session.executeAsync(statementGenerator.read(viewName, id))
				.thenApply(rs -> rs.one())
				.thenApply(row -> asEntity(viewName, row))
				.toCompletableFuture()
		).toList();

		CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

		CompletableFuture<List<T>> allCompletableFuture = allFutures.thenApply(v ->
			futures.stream()
				.map(CompletableFuture::join)
				.toList()
		);

		try
		{
			return allCompletableFuture.get();
		}
		catch (InterruptedException i)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(i);
		}
		catch (ExecutionException e)
		{
			throw new RuntimeException(e);
		}
	}

	public T update(T entity, T original)
	{
		try
		{
			CassandraNoSchemaUnitOfWork uow = createUnitOfWork();
			AtomicReference<Document> originalDocument = new AtomicReference<>();
			final T originalEntity;

			if (original != null)
			{
				originalDocument.set(asDocument(original));
				originalEntity = original;
			}
			else
			{
				originalDocument.set(readAsDocument(entity.getIdentifier()).join());
				uow.registerClean(table.name(), originalDocument.get());
				originalEntity = asEntity(table.name(), originalDocument.get());
			}

			observers.forEach(o -> o.beforeUpdate(originalDocument.get()));
			final byte[] serialized = originalDocument.get().getObject();
			final AtomicReference<Document> updatedDocument = new AtomicReference<>();

			table.stream().forEach(t -> {
				if (updatedDocument.get() == null)
				{
					observers.forEach(o -> o.beforeEncoding(entity));
				}

				final Document updatedViewDocument = asDocument(t.name(), entity);
				final Document originalViewDocument = asDocument(t.name(), originalEntity, serialized);

				if (updatedDocument.get() == null)
				{
					updatedDocument.set(updatedViewDocument);
					observers.forEach(o-> o.afterEncoding(updatedViewDocument));
				}
				else
				{
					updatedViewDocument.setMetadata(updatedDocument.get().getMetadata());
				}

				// If identifier changed, must perform delete and create.
				if (!updatedViewDocument.getIdentifier().equals(originalViewDocument.getIdentifier()))
				{
					uow.registerDeleted(t.name(), originalViewDocument);
					uow.registerNew(t.name(), updatedViewDocument);
				}
				// Otherwise it is simply an update.
				else
				{
					uow.registerDirty(t.name(), updatedViewDocument);
				}
			});

			uow.commit();
			observers.forEach(o -> o.afterUpdate(updatedDocument.get()));
		}
		catch (UnitOfWorkCommitException e)
		{
			handleException(e);
		}

		return entity;
	}

	public T upsert(T entity)
	{
		CassandraNoSchemaUnitOfWork uow = createUnitOfWork();

		try
		{
			final AtomicReference<byte[]> bson = new AtomicReference<>();
			final AtomicReference<Document> updated = new AtomicReference<>();

			table.stream().forEach(view -> {
				final Document d;

				if (bson.get() == null)
				{
					observers.forEach(o -> o.beforeEncoding(entity));
					d = asDocument(view.name(), entity);
					observers.forEach(o -> o.afterEncoding(d));
					observers.forEach(o -> o.beforeUpdate(d));
					bson.set(d.getObject());
					updated.set(d);
				}
				else
				{
					d = asDocument(view.name(), entity, bson.get());
				}

				uow.registerDirty(view.name(), d);
			});

			uow.commit();
			observers.forEach(o -> o.afterUpdate(updated.get()));
		}
		catch (UnitOfWorkCommitException e)
		{
			handleException(e);
		}

		return entity;
	}

	protected CassandraNoSchemaUnitOfWork createUnitOfWork()
	{
		return new CassandraNoSchemaUnitOfWork(session, statementGenerator, unitOfWorkType);
	}

	private CompletableFuture<Document> readAsDocument(Identifier id)
	throws ItemNotFoundException
	{
		return readRow(table.name(), id)
			.thenApply(row -> {
				Document document = asDocument(table.name(), row);
				T entity = asEntity(table.name(), document);
				// TODO: This is a hack. Need to load this from the database.
				document.setIdentifier(entity.getIdentifier());
				return document;				
			});
	}

	private CompletableFuture<Row> readRow(String viewName, Identifier id)
	{
		observers.forEach(o -> o.beforeRead(id));
		return session.executeAsync(statementGenerator.read(viewName, id))
			.thenApply(rs -> rs.one())
			.thenApply(row -> {
				if (row == null) throw new ItemNotFoundException(id.toString());
				return row;
			})
			.toCompletableFuture();
	}

	private CompletableFuture<PagedRows> readRows(String viewName, int limit, String cursor, Object... parameters)
	{
		observers.forEach(o -> o.beforeRead(new Identifier(parameters)));
		return session.executeAsync(statementGenerator.readAll(viewName, limit, cursor, parameters))
			.thenApply(rs -> {
				PagedRows rows = new PagedRows();
				rows.cursor(Bytes.toHexString(rs.getExecutionInfo().getPagingState()));
				rs.currentPage().iterator().forEachRemaining(rows::add);
				return rows;
			})
			.toCompletableFuture();
	}

	private T asEntity(String viewName, Row row)
	{
		Document d = asDocument(viewName, row);

		if (d == null) return null;

		return asEntity(viewName, d);
	}

	private Document asDocument(String viewName, Row row)
	{
		return factoriesByView.get(viewName).asDocument(row);
	}

	private T asEntity(String viewName, Document d)
	{
		return factoriesByView.get(viewName).asPojo(d);
	}

	protected Document asDocument(T entity)
	{
		return asDocument(table.name(), entity);
	}

	private Document asDocument(String viewName, T entity)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		return factoriesByView.get(viewName).asDocument(entity);
	}

	private Document asDocument(String viewName, T entity, byte[] bytes)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		return factoriesByView.get(viewName).asDocument(entity, bytes);
	}

	private void handleException(Exception e)
	throws DuplicateItemException, InvalidIdentifierException, ItemNotFoundException, StorageException
	{
		if (e.getCause() instanceof DuplicateItemException duplicate)
		{
			throw duplicate;
		}

		if (e.getCause() instanceof InvalidIdentifierException invalidId)
		{
			throw invalidId;
		}

		if (e.getCause() instanceof ItemNotFoundException notFound)
		{
			throw notFound;
		}

		throw new StorageException(e.getCause());
	}

	private class PagedRows
	{
		private String cursor;
		private List<Row> rows = new ArrayList<>();

		void cursor(String hexString)
		{
			this.cursor = hexString;
		}

		String cursor()
		{
			return cursor;
		}

		void add(Row row)
		{
			rows.add(row);
		}

		Stream<Row> stream()
		{
			return rows.stream();
		}
	}
}
