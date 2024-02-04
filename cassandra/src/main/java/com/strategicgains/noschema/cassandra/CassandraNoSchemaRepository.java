package com.strategicgains.noschema.cassandra;

import java.nio.ByteBuffer;
import java.sql.Date;
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
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.Row;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.NoSchemaRepository;
import com.strategicgains.noschema.cassandra.document.CassandraDocumentFactory;
import com.strategicgains.noschema.cassandra.document.DocumentSchemaProvider;
import com.strategicgains.noschema.cassandra.document.DocumentSchemaProvider.Columns;
import com.strategicgains.noschema.cassandra.document.DocumentStatementGenerator;
import com.strategicgains.noschema.cassandra.schema.SchemaWriter;
import com.strategicgains.noschema.document.Document;
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
	private DocumentStatementGenerator statementGenerator;
	private Map<String, CassandraDocumentFactory<T>> factoriesByView = new HashMap<>();
	private BatchType unitOfWorkType;


	protected CassandraNoSchemaRepository(CqlSession session, PrimaryTable table)
	{
		this(session, table, BatchType.LOGGED);
	}

	protected CassandraNoSchemaRepository(CqlSession session, PrimaryTable table, BatchType unitOfWorkType)
	{
		super();
		this.session = Objects.requireNonNull(session);
		this.table = Objects.requireNonNull(table);
		this.unitOfWorkType = Objects.requireNonNull(unitOfWorkType);
		this.statementGenerator = new DocumentStatementGenerator(session, table);
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
		new DocumentSchemaProvider(table).create(session);

		if (hasViews())
		{
			table.views().forEach(v -> new DocumentSchemaProvider(v).create(session));
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

	public T create(T entity)
	{
		CassandraNoSchemaUnitOfWork uow = createUnitOfWork();

		try
		{
			final AtomicReference<BSONObject> bson = new AtomicReference<>();

			table.stream().forEach(t -> {
				final Document d;

				if (bson.get() == null)
				{
					d = asDocument(t.name(), entity);
					bson.set(d.getObject());
				}
				else
				{
					d = asDocument(t.name(), entity, bson.get());
				}

				uow.registerNew(t.name(), d);
			});

			uow.commit();
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

			table.stream().forEach(t -> {
				final Document d;

				if (bson.get() == null)
				{
					d = asDocument(t.name(), entity);
					bson.set(d.getObject());
				}
				else
				{
					d = asDocument(t.name(), entity, bson.get());
				}

				uow.registerDeleted(t.name(), d);
			});

			uow.commit();
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
			Document originalDocument = asDocument(original);

			if (originalDocument == null)
			{
				originalDocument = readAsDocument(entity.getIdentifier());
				uow.registerClean(table.name(), originalDocument);
			}

			final T originalEntity = asEntity(table.name(), originalDocument);
			final BSONObject bson = originalDocument.getObject();

			table.stream().forEach(t -> {
				final Document updatedViewDocument = asDocument(t.name(), entity);
				final Document originalViewDocument = asDocument(t.name(), originalEntity, bson);

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

			table.stream().forEach(t -> {
				final Document d;

				if (bson.get() == null)
				{
					d = asDocument(t.name(), entity);
					bson.set(d.getObject());
				}
				else
				{
					d = asDocument(t.name(), entity, bson.get());
				}

				uow.registerDirty(t.name(), d);
			});

			uow.commit();
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
		d.setCreatedAt(new Date(row.getInstant(Columns.CREATED_AT).getEpochSecond()));
		d.setUpdatedAt(new Date(row.getInstant(Columns.UPDATED_AT).getEpochSecond()));
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
