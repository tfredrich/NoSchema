package com.strategicgains.noschema.cassandra;

import java.nio.ByteBuffer;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.bson.BSONDecoder;
import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.NoSchemaRepository;
import com.strategicgains.noschema.cassandra.document.CassandraDocumentFactory;
import com.strategicgains.noschema.cassandra.document.DocumentSchemaProvider;
import com.strategicgains.noschema.cassandra.document.DocumentSchemaProvider.Columns;
import com.strategicgains.noschema.cassandra.document.DocumentStatementGenerator;
import com.strategicgains.noschema.cassandra.document.DocumentUnitOfWork;
import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.exception.DuplicateItemException;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.ItemNotFoundException;
import com.strategicgains.noschema.exception.KeyDefinitionException;
import com.strategicgains.noschema.exception.StorageException;
import com.strategicgains.noschema.unitofwork.UnitOfWorkCommitException;

public class CassandraNoSchemaRepository<T extends Identifiable>
implements NoSchemaRepository<T>
{
	public static final String PRIMARY_TABLE = "<|primary_table|>";
	private static final BSONDecoder DECODER = new BasicBSONDecoder();

	private CqlSession session;
	private PrimaryTable table;
	private DocumentStatementGenerator statementGenerator;
	private Map<String, CassandraDocumentFactory<T>> factoriesByView = new HashMap<>();

	public CassandraNoSchemaRepository(CqlSession session, PrimaryTable table)
	{
		super();
		this.session = session;
		this.table = table;
		this.statementGenerator = new DocumentStatementGenerator(session, table);
		factoriesByView.put(PRIMARY_TABLE, new CassandraDocumentFactory<>(table.keys()));
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

	@Override
	public T create(T entity)
	throws DuplicateItemException, InvalidIdentifierException
	{
		DocumentUnitOfWork uow = new DocumentUnitOfWork(session, statementGenerator);

		try
		{
			AtomicReference<BSONObject> bson = new AtomicReference<>();

			table.stream().map(t -> {
				Document d;
				if (bson.get() == null)
				{
					d = asDocument(t.name(), entity);
					bson.set(d.getObject());
				}
				else
				{
					d = asDocument(t.name(), entity, bson.get());
				}

				return asDocument(t.name(), entity);
			}).forEach(uow::registerNew);

			uow.commit();
		}
		catch (UnitOfWorkCommitException e)
		{
			if (e.getCause() instanceof DuplicateItemException)
			{
				throw (DuplicateItemException) e.getCause();
			}

			if (e.getCause() instanceof InvalidIdentifierException)
			{
				throw (InvalidIdentifierException) e.getCause();
			}

			throw new StorageException(e.getCause());
		}

		return entity;
	}

	@Override
	public boolean delete(Identifier id)
	throws ItemNotFoundException, InvalidIdentifierException
	{
		try
		{
			// TODO: enable end-customer creation of the unit of work. Perhaps a thread local?
			DocumentUnitOfWork uow = new DocumentUnitOfWork(session, statementGenerator);
			Document original = uow.readClean(id);

			if(original == null)
			{
				original = readAsDocument(id);
				uow.registerClean(original);
			}

			asViewDocuments(original).forEach(uow::registerDeleted);
			uow.commit();
		}
		catch (UnitOfWorkCommitException e)
		{
			if (e.getCause() instanceof ItemNotFoundException)
			{
				throw (ItemNotFoundException) e.getCause();
			}

			if (e.getCause() instanceof InvalidIdentifierException)
			{
				throw (InvalidIdentifierException) e.getCause();
			}

			throw e;
		}

		return true;
	}

	@Override
	public boolean exists(Identifier id)
	throws InvalidIdentifierException
	{
		return exists(PRIMARY_TABLE, id);
	}

	public boolean exists(String viewName, Identifier id)
	{
		return (session.execute(statementGenerator.exists(viewName, id)).one().getLong(0) > 0);
	}

	@Override
	public T read(Identifier id)
	throws ItemNotFoundException
	{
		return read(PRIMARY_TABLE, id);
	}
 
	public T read(String viewName, Identifier id)
	throws ItemNotFoundException
	{
		Row row = readRow(viewName, id);
		return marshalEntity(viewName, row);
	}

	@Override
	public List<T> readAll(Object... parms)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T update(T entity)
	throws ItemNotFoundException, InvalidIdentifierException, KeyDefinitionException
	{
		try
		{
			// TODO: enable end-customer creation of the unit of work. Perhaps a thread local?
			DocumentUnitOfWork uow = new DocumentUnitOfWork(session, statementGenerator);
			Document originalDocument = uow.readClean(entity.getIdentifier());

			if (originalDocument == null)
			{
				originalDocument = readAsDocument(entity.getIdentifier());
				uow.registerClean(originalDocument);
			}

			final T originalEntity = asEntity(PRIMARY_TABLE, originalDocument);
			final BSONObject bson = originalDocument.getObject();

			table.stream().forEach(t -> {
				Document updatedDocument = asDocument(t.name(), entity);
				Document viewOriginal = asDocument(t.name(), originalEntity, bson);

				// If identifier changed, must perform delete and create.
				if (!updatedDocument.getIdentifier().equals(viewOriginal.getIdentifier()))
				{
					uow.registerDeleted(viewOriginal);
					uow.registerNew(updatedDocument);
				}
				// Otherwise it is simply an update.
				else
				{
					uow.registerDirty(updatedDocument);
				}
			});

			uow.commit();
		}
		catch (UnitOfWorkCommitException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return entity;
	}

	@Override
	public T upsert(T entity)
	throws InvalidIdentifierException, KeyDefinitionException, StorageException
	{
		try
		{
			// TODO: enable end-customer creation of the unit of work. Perhaps a thread local?
			DocumentUnitOfWork uow = new DocumentUnitOfWork(session, statementGenerator);
			asViewDocuments(entity).forEach(uow::registerDirty);
			uow.commit();
		}
		catch (UnitOfWorkCommitException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return entity;
	}

	private Document readAsDocument(Identifier id)
	throws ItemNotFoundException
	{
		Row row = readRow(PRIMARY_TABLE, id);
		Document document = marshalDocument(row);
		T entity = asEntity(PRIMARY_TABLE, document);
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

	protected T marshalEntity(String viewName, Row row)
	{
		Document d = marshalDocument(row);

		if (d == null) return null;

		return asEntity(viewName, d);
	}

	protected Document marshalDocument(Row row)
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

	private Stream<Document> asViewDocuments(Document document)
	{
		return asViewDocuments(asEntity(PRIMARY_TABLE, document));
	}

	private Stream<Document> asViewDocuments(T entity)
	{
		AtomicReference<BSONObject> bson = new AtomicReference<>();

		return table.stream().map(t -> {
			Document d;
			if (bson.get() == null)
			{
				d = asDocument(t.name(), entity);
				bson.set(d.getObject());
			}
			else
			{
				d = asDocument(t.name(), entity, bson.get());
			}

			return asDocument(t.name(), entity);
		});
	}

	private T asEntity(String viewName, Document d)
	{
		return factoriesByView.get(viewName).asPojo(d);
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
}
