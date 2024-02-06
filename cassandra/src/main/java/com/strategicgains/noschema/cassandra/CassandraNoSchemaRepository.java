package com.strategicgains.noschema.cassandra;

import java.nio.ByteBuffer;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.bson.BSONDecoder;
import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.NoSchemaRepository;
import com.strategicgains.noschema.cassandra.document.CassandraDocumentFactory;
import com.strategicgains.noschema.cassandra.document.DocumentTableSchemaProvider;
import com.strategicgains.noschema.cassandra.document.DocumentTableSchemaProvider.Columns;
import com.strategicgains.noschema.cassandra.schema.SchemaWriter;
import com.strategicgains.noschema.cassandra.unitofwork.UnitOfWorkType;
import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.document.DocumentObserver;
import com.strategicgains.noschema.exception.DuplicateItemException;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.ItemNotFoundException;
import com.strategicgains.noschema.exception.KeyDefinitionException;
import com.strategicgains.noschema.exception.StorageException;
import com.strategicgains.noschema.unitofwork.UnitOfWorkCommitException;

public class CassandraNoSchemaRepository<T extends Identifiable>
implements NoSchemaRepository<T>, SchemaWriter<T>
{
	private static final BSONDecoder DECODER = new BasicBSONDecoder();

	private CqlSession session;
	private PrimaryTable table;
	private CassandraNoSchemaStatementFactory statementGenerator;
	private Map<String, CassandraDocumentFactory<T>> factoriesByView = new HashMap<>();
	private UnitOfWorkType unitOfWorkType;
	private List<DocumentObserver> observers = new ArrayList<>();


	protected CassandraNoSchemaRepository(CqlSession session, PrimaryTable table)
	{
		this(session, table, UnitOfWorkType.LOGGED);
	}

	protected CassandraNoSchemaRepository(CqlSession session, PrimaryTable table, UnitOfWorkType unitOfWorkType)
	{
		super();
		this.session = Objects.requireNonNull(session);
		this.table = Objects.requireNonNull(table);
		this.unitOfWorkType = Objects.requireNonNull(unitOfWorkType);
		this.statementGenerator = new CassandraNoSchemaStatementFactory(session, table);
		factoriesByView.put(table.name(), new CassandraDocumentFactory<>(table.keys()));
		table.views().forEach(view ->
			this.factoriesByView.put(view.name(), new CassandraDocumentFactory<>(view.keys()))
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
			final AtomicReference<BSONObject> bson = new AtomicReference<>();
			final AtomicReference<Document> primaryDocument = new AtomicReference<>();

			table.stream().forEach(t -> {
				final Document d;

				if (bson.get() == null)
				{
					observers.forEach(o -> o.beforeEncoding(entity));
					d = asDocument(t.name(), entity);
					primaryDocument.set(d);
					bson.set(d.getObject());
					observers.forEach(o -> o.afterEncoding(primaryDocument.get()));
					observers.forEach(o -> o.beforeCreate(primaryDocument.get()));
				}
				else
				{
					d = asDocument(t.name(), entity, bson.get());
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
			final AtomicReference<BSONObject> bson = new AtomicReference<>();
			final AtomicReference<Document> primaryDocument = new AtomicReference<>();

			table.stream().forEach(t -> {
				final Document d;

				if (bson.get() == null)
				{
					observers.forEach(o -> o.beforeEncoding(entity));
					d = asDocument(t.name(), entity);
					primaryDocument.set(d);
					bson.set(d.getObject());
					observers.forEach(o -> o.afterEncoding(primaryDocument.get()));
					observers.forEach(o -> o.beforeDelete(primaryDocument.get()));
				}
				else
				{
					d = asDocument(t.name(), entity, bson.get());
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
		return (session.execute(statementGenerator.exists(viewName, id)).one().getLong(0) > 0);
	}

	@Override
	public T read(Identifier id)
	{
		return read(table.name(), id);
	}
 
	public T read(String viewName, Identifier id)
	{
		Row row = readRow(viewName, id);
		return marshalEntity(viewName, row);
	}

	@Override
	public List<T> readAll(Object... parms)
	{
		// TODO Auto-generated method stub
		return Collections.emptyList();
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
				originalDocument.set(readAsDocument(entity.getIdentifier()));
				uow.registerClean(table.name(), originalDocument.get());
				originalEntity = asEntity(table.name(), originalDocument.get());
			}

			observers.forEach(o -> o.beforeUpdate(originalDocument.get()));
			final BSONObject bson = originalDocument.get().getObject();
			final AtomicReference<Document> updatedDocument = new AtomicReference<>();

			table.stream().forEach(t -> {
				if (updatedDocument.get() == null)
				{
					observers.forEach(o -> o.beforeEncoding(entity));
				}

				final Document updatedViewDocument = asDocument(t.name(), entity);
				final Document originalViewDocument = asDocument(t.name(), originalEntity, bson);

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
			final AtomicReference<BSONObject> bson = new AtomicReference<>();
			final AtomicReference<Document> updated = new AtomicReference<>();

			table.stream().forEach(table -> {
				final Document d;

				if (bson.get() == null)
				{
					observers.forEach(o -> o.beforeEncoding(entity));
					d = asDocument(table.name(), entity);
					observers.forEach(o -> o.afterEncoding(d));
					observers.forEach(o -> o.beforeUpdate(d));
					bson.set(d.getObject());
					updated.set(d);
				}
				else
				{
					d = asDocument(table.name(), entity, bson.get());
				}

				uow.registerDirty(table.name(), d);
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

	private Document readAsDocument(Identifier id)
	throws ItemNotFoundException
	{
		Row row = readRow(table.name(), id);
		Document document = marshalDocument(row);
		T entity = asEntity(table.name(), document);
		document.setIdentifier(entity.getIdentifier());
		return document;
	}

	private Row readRow(String viewName, Identifier id)
	{
		observers.forEach(o -> o.beforeRead(id));
		Row row = session.execute(statementGenerator.read(viewName, id)).one();

		if (row == null)
		{
			throw new ItemNotFoundException(id.toString());
		}

		return row;
	}

	private T marshalEntity(String viewName, Row row)
	{
		Document d = marshalDocument(row);

		if (d == null) return null;

		return asEntity(viewName, d);
	}

	//TODO: this should be in DocumentStatementFactory
	private Document marshalDocument(Row row)
	{
		if (row == null)
		{
			return null;
		}

		Document d = new Document();
		ByteBuffer b = row.getByteBuffer(Columns.OBJECT);

		if (b != null && b.hasArray())
		{
			//Force the reading of all the bytes.
			d.setObject(DECODER.readObject(b.array()));
		}

		d.setType(row.getString(Columns.TYPE));
		d.setMetadata(row.getMap(Columns.METADATA, String.class, String.class));
		d.setCreatedAt(new Date(row.getInstant(Columns.CREATED_AT).getEpochSecond()));
		d.setUpdatedAt(new Date(row.getInstant(Columns.UPDATED_AT).getEpochSecond()));
		observers.forEach(o -> o.afterRead(d));
		return d;
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

	private Document asDocument(String viewName, T entity, BSONObject bson)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		return factoriesByView.get(viewName).asDocument(entity, bson);
	}

	private void handleException(UnitOfWorkCommitException e)
	{
		if (e.getCause() instanceof DuplicateItemException duplicate)
		{
			throw duplicate;
		}

		if (e.getCause() instanceof InvalidIdentifierException invalidId)
		{
			throw invalidId;
		}

		throw new StorageException(e.getCause());
	}
}
