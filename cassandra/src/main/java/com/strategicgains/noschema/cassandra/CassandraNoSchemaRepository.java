package com.strategicgains.noschema.cassandra;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.bson.BSONDecoder;
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
			asViewDocuments(entity).forEach(uow::registerNew);
			uow.commit();
		}
		catch (UnitOfWorkCommitException e)
		{
			// TODO Determine if it's unique violation, bad ID, or something else.
			e.printStackTrace();
		}

		return entity;
	}

	@Override
	public boolean delete(Identifier id)
	throws ItemNotFoundException, InvalidIdentifierException
	{
		try
		{
			T entity = read(id);
			DocumentUnitOfWork uow = new DocumentUnitOfWork(session, statementGenerator);
			asViewDocuments(entity).forEach(uow::registerDeleted);
			uow.commit();
		}
		catch (UnitOfWorkCommitException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	@Override
	public boolean exists(Identifier id)
	throws InvalidIdentifierException
	{
		return existsInView(PRIMARY_TABLE, id);
	}

	public boolean existsInView(String viewName, Identifier id)
	{
		return (session.execute(statementGenerator.exists(viewName, id)).one().getLong(0) > 0);
	}

	@Override
	public T read(Identifier id)
	throws ItemNotFoundException
	{
		return readView(PRIMARY_TABLE, id);
	}
 
	public T readView(String viewName, Identifier id)
	throws ItemNotFoundException
	{
		Row row = session.execute(statementGenerator.read(viewName, id)).one();

		if (row == null)
		{
			throw new ItemNotFoundException(id.toString());
		}

		return marshalRow(viewName, row);
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

	@Override
	public T upsert(T entity)
	throws InvalidIdentifierException, KeyDefinitionException, StorageException
	{
		// TODO Auto-generated method stub
		return null;
	}

	protected T marshalRow(String viewName, Row row)
	{
		Document d = marshalDocument(row);

		if (d == null) return null;

		return factoriesByView.get(viewName).asPojo(d);
	}

	// TODO: This is misplaced (depends on DocumentSchemaProvider.Columns)
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
		return d;
	}

	private Stream<Document> asViewDocuments(T entity)
	throws InvalidIdentifierException
	{
		try
		{
			return factoriesByView.entrySet().stream().map(entry -> {
				try
				{
					Document d = entry.getValue().asDocument(entity);
					d.setView(entry.getKey());
					return d;
				}
				catch (InvalidIdentifierException | KeyDefinitionException e)
				{
					throw new RuntimeException(e);
				}
			});
		}
		catch (RuntimeException e)
		{
			if (e.getCause() instanceof InvalidIdentifierException)
			{
				throw (InvalidIdentifierException) e.getCause();
			}
			else if (e.getCause() instanceof KeyDefinitionException)
			{
				throw new InvalidIdentifierException(e.getCause());
			}

			throw e; // rethrow the original RuntimeException if the cause is not what we expected
		}
	}
}
