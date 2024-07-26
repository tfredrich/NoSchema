package com.strategicgains.noschema.cassandra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.protocol.internal.util.Bytes;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.NoSchemaRepository;
import com.strategicgains.noschema.cassandra.document.CassandraDocumentFactory;
import com.strategicgains.noschema.cassandra.document.DocumentSchemaProvider;
import com.strategicgains.noschema.cassandra.schema.SchemaWriter;
import com.strategicgains.noschema.cassandra.unitofwork.CassandraUnitOfWork;
import com.strategicgains.noschema.cassandra.unitofwork.UnitOfWorkType;
import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.document.DocumentObserver;
import com.strategicgains.noschema.document.EntityObserver;
import com.strategicgains.noschema.document.ObjectCodec;
import com.strategicgains.noschema.exception.DuplicateItemException;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.ItemNotFoundException;
import com.strategicgains.noschema.exception.KeyDefinitionException;
import com.strategicgains.noschema.exception.StorageException;
import com.strategicgains.noschema.unitofwork.UnitOfWorkCommitException;

/**
 * A CassandraRepository is a NoSchemaRepository implementation that uses
 * Cassandra as its underlying data store. It is responsible for creating,
 * reading, updating, and deleting entities in the database. It also provides
 * methods for checking if an entity exists, and for reading multiple entities
 * at once.
 * 
 * This class is abstract and is meant to be extended by concrete
 * implementations that provide the necessary information to connect to the
 * Cassandra cluster and to define the schema for the entities to be stored.
 * 
 * Also, the repository can create and drop the underlying tables necessary to
 * store the entities.
 * 
 * T is the type of entity to be stored in the database, which must implement
 * the Identifiable interface.
 */
public class CassandraRepository<T extends Identifiable>
implements NoSchemaRepository<T>, SchemaWriter<T>
{
	// The session used to connect to the Cassandra cluster.
	private CqlSession session;
	// The primary table and its views.
	private PrimaryTable table;
	// The statement factory used to create the CQL statements within the UnitOfWork.
	private CassandraStatementFactory<Document<T>> statementFactory;
	// The factories used to encode and decode entities.
	private Map<String, CassandraDocumentFactory<T>> factoriesByTable = new HashMap<>();
	// The type of UnitOfWork to create.
	private UnitOfWorkType unitOfWorkType;
	// The observers used to observe the encoding, creation, update, and deletion of entities.
	private List<DocumentObserver<T>> documentObservers = new ArrayList<>();
	private List<EntityObserver<T>> entityObservers = new ArrayList<>();


	protected CassandraRepository(CqlSession session, PrimaryTable table, ObjectCodec<T> codec)
	{
		this(session, table, UnitOfWorkType.LOGGED, codec);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected CassandraRepository(CqlSession session, PrimaryTable table, UnitOfWorkType unitOfWorkType, ObjectCodec<T> codec)
	{
		super();
		this.session = Objects.requireNonNull(session);
		this.table = Objects.requireNonNull(table);
		this.unitOfWorkType = Objects.requireNonNull(unitOfWorkType);
		this.statementFactory = new CassandraStatementFactory(session, table, codec);
		factoriesByTable.put(table.name(), new CassandraDocumentFactory<>(table.keys(), codec));
		table.views().forEach(view ->
			this.factoriesByTable.put(view.name(), new CassandraDocumentFactory<>(view.keys(), codec))
		);
		table.indexes().forEach(index ->
			this.factoriesByTable.put(index.name(), new CassandraDocumentFactory<>(index.keys(), codec))
		);
	}

	protected boolean hasViews()
	{
		return table.hasViews();
	}

	protected boolean hasIndexes()
	{
		return table.hasIndexes();
	}

	@Override
	public void ensureTables()
	{
		new DocumentSchemaProvider(table).create(session);

		if (hasViews())
		{
			table.views().forEach(view -> new DocumentSchemaProvider(view).create(session));
		}

		if (hasIndexes())
		{
			table.indexes().forEach(idx -> new DocumentSchemaProvider(idx).create(session));
		}
	}

	@Override
	public void dropTables()
	{
		new DocumentSchemaProvider(table).drop(session);

		if (hasViews())
		{
			table.views().forEach(v -> new DocumentSchemaProvider(v).drop(session));
		}
	}

	protected String tableName()
	{
		return table.name();
	}

	public CassandraRepository<T> withDocumentObserver(DocumentObserver<T> observer)
	{
		documentObservers.add(observer);
		return this;
	}

	public CassandraRepository<T> withEntityObserver(EntityObserver<T> observer)
	{
		entityObservers.add(observer);
		return this;
	}

	/**
	 * This method is responsible for creating a new entity in the database.
	 * It first serializes the entity, then registers it in the UnitOfWork for 
	 * the primary table each view, then commits the UnitOfWork.
	 *
	 * @param entity The entity to be created.
	 * @return The created entity.
	 * @throws UnitOfWorkCommitException If there is an error during the commit operation.
	 */
	@Override
	public T create(T entity)
	{
		try
		{
			CassandraUnitOfWork<T> uow = createUnitOfWork();
			T created = create(entity, uow);
			uow.commit();
			return created;
		}
		catch (UnitOfWorkCommitException e)
		{
			handleException(e);
		}

		return null;
	}

	public T create(T entity, CassandraUnitOfWork<T> uow)
	{
		entityObservers.forEach(o -> o.beforeCreate(entity));
		final AtomicReference<byte[]> serialized = new AtomicReference<>();
		final AtomicReference<byte[]> serializedId = new AtomicReference<>();
		final AtomicReference<Document<T>> primaryDocument = new AtomicReference<>();

		table.stream().forEach(t -> {
			final Document<T> d;

			if (serialized.get() == null)
			{
				documentObservers.forEach(o -> o.beforeEncoding(entity));
				d = asDocument(t.name(), entity);
				primaryDocument.set(d);
				serialized.set(d.getObject());
				serializedId.set(d.getIdentifier().toString().getBytes());
				documentObservers.forEach(o -> o.afterEncoding(primaryDocument.get()));
				documentObservers.forEach(o -> o.beforeCreate(primaryDocument.get()));
			}
			else
			{
				if (t.isIndex())
				{
					d = asDocument(t.name(), entity, serializedId.get());
				}
				else
				{
					d = asDocument(t.name(), entity, serialized.get());
				}

				d.setMetadata(primaryDocument.get().getMetadata());
			}

			uow.registerNew(t.name(), d);
		});

		documentObservers.forEach(o -> o.afterCreate(primaryDocument.get()));
		entityObservers.forEach(o -> o.afterCreate(entity));
		return entity;
	}

	/**
	 * This method is responsible for deleting an entity from the database.
	 * It first identifies the entity by its Identifier, then registers it
	 * for deletion in the UnitOfWork for the primary table and each view,
	 * and finally commits the UnitOfWork.
	 *
	 * @param id The Identifier of the entity to be deleted.
	 * @throws UnitOfWorkCommitException If there is an error during the commit operation.
	 */
	@Override
	public void delete(Identifier id)
	{
		try
		{
			CassandraUnitOfWork<T> uow = createUnitOfWork();
			delete(id, uow);
			uow.commit();
		}
		catch (UnitOfWorkCommitException e)
		{
			handleException(e);
		}
	}

	public void delete(Identifier id, CassandraUnitOfWork<T> uow)
	{
		final T entity = read(id);
		entityObservers.forEach(o -> o.beforeDelete(entity));
		final AtomicReference<byte[]> serialized = new AtomicReference<>();
		final AtomicReference<Document<T>> primaryDocument = new AtomicReference<>();

		table.stream().forEach(t -> {
			final Document<T> d;

			if (serialized.get() == null)
			{
				documentObservers.forEach(o -> o.beforeEncoding(entity));
				d = asDocument(t.name(), entity);
				primaryDocument.set(d);
				serialized.set(d.getObject());
				documentObservers.forEach(o -> o.afterEncoding(primaryDocument.get()));
				documentObservers.forEach(o -> o.beforeDelete(primaryDocument.get()));
			}
			else
			{
				d = asDocument(t.name(), entity, serialized.get());
			}

			uow.registerDeleted(t.name(), d);
		});

		documentObservers.forEach(o -> o.afterDelete(primaryDocument.get()));
		entityObservers.forEach(o -> o.afterDelete(entity));
	}

	/**
	 * This method checks if an entity exists in the primary table.
	 * It executes an asynchronous query to check if the entity exists,
	 * returning true if the id exists, and false otherwise.
	 *
	 * @param id The Identifier of the entity to check.
	 * @return true if the entity exists, false otherwise.
	 * @throws InvalidIdentifierException If the provided Identifier is invalid.
	 */
	@Override
	public boolean exists(Identifier id)
	throws InvalidIdentifierException
	{
		return exists(table.name(), id);
	}

	/**
	 * This method checks if an entity exists in a specific view of the database.
	 * It executes an asynchronous query to check if the entity exists in the
	 * view, returning true if the id exists, and false otherwise.
	 *
	 * @param viewName The name of the view to check.
	 * @param id The Identifier of the entity to check.
	 * @return true if the entity exists in the view, false otherwise.
	 */
	public boolean exists(String viewName, Identifier id)
	{
		return session.executeAsync(statementFactory.exists(viewName, id))
			.thenApply(r -> (Boolean.valueOf(r.one().getLong(0) > 0)))
			.toCompletableFuture()
			.join();
	}

	/**
	 * This method reads an entity from the primary table.
	 * It executes an asynchronous query to read the entity, and then returns the result.
	 *
	 * @param id The Identifier of the entity to read.
	 * @return The read entity.
	 * @throws ItemNotFoundException If the entity is not found.
	 */
	@Override
	public T read(Identifier id)
	{
		return read(table.name(), id);
	}
 
	/**
	 * This method reads an entity from a specific view in the database.
	 * It executes an asynchronous query to read the entity from the view, and then returns the result.
	 *
	 * @param viewName The name of the view to read from.
	 * @param id The Identifier of the entity to read.
	 * @return The read entity.
	 * @throws ItemNotFoundException If the entity is not found.
	 */
	public T read(String viewName, Identifier id)
	{
		try
		{
			T read = readRow(viewName, id)
				.thenApply(row -> asEntity(viewName, row))
				.join();
			entityObservers.forEach(o -> o.afterRead(read));
			return read;
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
					page.iterator().forEachRemaining(row -> {
						T entity = asEntity(viewName, row);
						entityObservers.forEach(o -> o.afterRead(entity));
                        response.add(entity);
                    });
				})
				.join();
		}
		catch (CompletionException e)
		{
			handleException(e);
		}

		return response;
	}

	/**
	 * Reads multiple entities from the primary table.
	 * It executes asynchronous queries to read the entities,
	 * waits for all of them to complete, and then returns the results.
	 * Entities that are not found are not included in the result.
	 * 
	 * Note: the order of the returned entities is not guaranteed.
	 *
	 * @param ids The Identifiers of the entities to read.
	 * @return The list of read entities.
	 */
	@Override
	public List<T> readIn(List<Identifier> ids)
	{
		return readIn(table.name(), ids);
	}

	/**
	 * This method reads multiple entities from a specific view.
	 * It executes asynchronous queries to read the entities from the view,
	 * waits for all of them to complete, and then returns the results.
	 * Entities that are not found are not included in the result.
	 * 
	 * This method uses asynchronous queries to read the entities, which
	 * means the client itself is the coordinator instead of issuing all
	 * the queries to a single node. This is more efficient from the
	 * server perspective, but it can load up the client.
	 * 
	 * Note: the order of the returned entities is not guaranteed.
	 *
	 * @param viewName The name of the view to read from.
	 * @param ids The Identifiers of the entities to read.
	 * @return The list of read entities.
	 */
	public List<T> readIn(String viewName, List<Identifier> ids)
	{
		if (ids == null) return Collections.emptyList();

		List<CompletableFuture<T>> futures = ids.stream().map(id -> 
			session.executeAsync(statementFactory.read(viewName, id))
				.thenApply(rs -> rs.one())
				.thenApply(row -> {
					T entity = asEntity(viewName, row);
					entityObservers.forEach(o -> o.afterRead(entity));
					return entity;
				})
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

	/**
	 * This method updates an entity in the database.
	 * It first creates a UnitOfWork, then registers the original entity in it
	 * for the primary table and all its views. If the original entity is not
	 * provided (null), it reads it from the database using the identifier for the
	 * provided entity.
	 *
	 * @param entity The new entity data.
	 * @param original The original entity data. If null, the method will read it from the database.
	 * @return The updated entity.
	 */
	@Override
	public T update(T entity, T original)
	{
		try
		{
			CassandraUnitOfWork<T> uow = createUnitOfWork();
			T updated = update(entity, original, uow);
			uow.commit();
			return updated;
		}
		catch (UnitOfWorkCommitException e)
		{
			handleException(e);
		}

		return null;
	}

	public T update(T entity, T original, CassandraUnitOfWork<T> uow)
	{
		AtomicReference<Document<T>> originalDocument = new AtomicReference<>();
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

		documentObservers.forEach(o -> o.beforeUpdate(originalDocument.get()));
		final byte[] serialized = originalDocument.get().getObject();
		final AtomicReference<Document<T>> updatedDocument = new AtomicReference<>();

		table.stream().forEach(t -> {
			if (updatedDocument.get() == null)
			{
				documentObservers.forEach(o -> o.beforeEncoding(entity));
			}

			final Document<T> updatedViewDocument = asDocument(t.name(), entity);
			final Document<T> originalViewDocument = asDocument(t.name(), originalEntity, serialized);

			if (updatedDocument.get() == null)
			{
				updatedDocument.set(updatedViewDocument);
				documentObservers.forEach(o-> o.afterEncoding(updatedViewDocument));
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

		documentObservers.forEach(o -> o.afterUpdate(updatedDocument.get()));
		return entity;
	}

	/**
	 * This method upserts (updates or inserts) an entity into the database.
	 * It has the benefit of not incurring any reads before update, as it 
	 * doesn't check for existence before updating. If the entity already
	 * exists in the database, it is updated; otherwise, it is inserted.
	 *
	 * @param entity The entity to be upserted.
	 * @return The upserted entity.
	 */
	@Override
	public T upsert(T entity)
	{
		try
		{
			CassandraUnitOfWork<T> uow = createUnitOfWork();
			T upserted = upsert(entity, uow);
			uow.commit();
			return upserted;
		}
		catch (UnitOfWorkCommitException e)
		{
			handleException(e);
		}

		return null;
	}

	public T upsert(T entity, CassandraUnitOfWork<T> uow)
	{
		final AtomicReference<byte[]> bson = new AtomicReference<>();
		final AtomicReference<Document<T>> updated = new AtomicReference<>();

		table.stream().forEach(view -> {
			final Document<T> d;

			if (bson.get() == null)
			{
				documentObservers.forEach(o -> o.beforeEncoding(entity));
				d = asDocument(view.name(), entity);
				documentObservers.forEach(o -> o.afterEncoding(d));
				documentObservers.forEach(o -> o.beforeUpdate(d));
				bson.set(d.getObject());
				updated.set(d);
			}
			else
			{
				d = asDocument(view.name(), entity, bson.get());
			}

			uow.registerDirty(view.name(), d);
		});

		documentObservers.forEach(o -> o.afterUpdate(updated.get()));
		return entity;
	}

	protected CassandraUnitOfWork<T> createUnitOfWork()
	{
		return new CassandraUnitOfWork<>(session, statementFactory, unitOfWorkType);
	}

	private CompletableFuture<Document<T>> readAsDocument(Identifier id)
	throws ItemNotFoundException
	{
		return readRow(table.name(), id)
			.thenApply(row -> {
				Document<T> document = asDocument(table.name(), row);
				T entity = asEntity(table.name(), document);
				// TODO: This is a hack. Need to load this from the database.
				document.setIdentifier(entity.getIdentifier());
				return document;				
			});
	}

	private CompletableFuture<Row> readRow(String viewName, Identifier id)
	{
		documentObservers.forEach(o -> o.beforeRead(id));
		return session.executeAsync(statementFactory.read(viewName, id))
			.thenApply(rs -> rs.one())
			.thenApply(row -> {
				if (row == null) throw new ItemNotFoundException(id.toString());
				return row;
			})
			.toCompletableFuture();
	}

	private CompletableFuture<PagedRows> readRows(String viewName, int limit, String cursor, Object... parameters)
	{
		documentObservers.forEach(o -> o.beforeRead(new Identifier(parameters)));
		return session.executeAsync(statementFactory.readAll(viewName, limit, cursor, parameters))
			.thenApply(rs -> {
				PagedRows rows = new PagedRows();
				rows.cursor(Bytes.toHexString(rs.getExecutionInfo().getPagingState()));
				rows.currentPage(rs.currentPage());
				return rows;
			})
			.toCompletableFuture();
	}

	private T asEntity(String viewName, Row row)
	{
		Document<T> d = asDocument(viewName, row);

		if (d == null) return null;

		return asEntity(viewName, d);
	}

	private Document<T> asDocument(String viewName, Row row)
	{
		return factoriesByTable.get(viewName).asDocument(row);
	}

	private T asEntity(String viewName, Document<T> d)
	{
		return factoriesByTable.get(viewName).asPojo(d);
	}

	protected Document<T> asDocument(T entity)
	{
		return asDocument(table.name(), entity);
	}

	private Document<T> asDocument(String viewName, T entity)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		return factoriesByTable.get(viewName).asDocument(entity);
	}

	private Document<T> asDocument(String viewName, T entity, byte[] bytes)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		return factoriesByTable.get(viewName).asDocument(entity, bytes);
	}

	private void handleException(Exception e)
	throws StorageException
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
		private Iterable<Row> currentPage;

		void cursor(String hexString)
		{
			this.cursor = hexString;
		}

		String cursor()
		{
			return cursor;
		}

		void currentPage(Iterable<Row> rows)
		{
			this.currentPage = rows;
		}

		Iterator<Row> iterator()
        {
			return currentPage.iterator();
		}
	}
}
