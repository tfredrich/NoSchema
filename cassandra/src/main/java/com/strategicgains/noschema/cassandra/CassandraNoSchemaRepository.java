package com.strategicgains.noschema.cassandra;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.BSONDecoder;
import org.bson.BasicBSONDecoder;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.NoSchemaRepository;
import com.strategicgains.noschema.cassandra.document.CassandraDocumentFactory;
import com.strategicgains.noschema.cassandra.document.DocumentSchemaProvider;
import com.strategicgains.noschema.cassandra.document.DocumentStatementFactory;
import com.strategicgains.noschema.cassandra.document.DocumentSchemaProvider.Columns;
import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.cassandra.key.KeyPropertyConverter;
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
	private static final BSONDecoder DECODER = new BasicBSONDecoder();
	private static final String PRIMARY_TABLE = "<|primary_table|>";

	private CqlSession session;
	private PrimaryTable table;
	private Map<String, DocumentStatementFactory<T>> statementsByView = new HashMap<>();
	private Map<String, CassandraDocumentFactory<T>> factoriesByView = new HashMap<>();

	public CassandraNoSchemaRepository(CqlSession session, PrimaryTable table)
	{
		super();
		this.table = table;
		statementsByView.put(PRIMARY_TABLE, new DocumentStatementFactory<>(session, table));
		table.views().forEach(view -> {
			this.statementsByView.put(view.name(), new DocumentStatementFactory<>(session, view));
			this.factoriesByView.put(view.name(), new CassandraDocumentFactory<>(view.keys()));				
		});
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
		CassandraUnitOfWork<T> uow = new CassandraUnitOfWork<>(session, statementsByView.get(PRIMARY_TABLE));
		uow.registerNew(entity);

		try
		{
			uow.commit();
		}
		catch (UnitOfWorkCommitException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		List<BoundStatement> statements = new ArrayList<>(views.size() + 1);
//		statements.add(statementsByView.get(PRIMARY_VIEW).create(entity));
//
//		if (hasView())
//		{
//			views.forEach(view -> statements.add(statementsByView.get(view.name()).create(entity)));
//			ExecResult creation = executeAll(statements);
//
//			if (!creation.succeeded())
//			{
//				creation.rollback().forEach(stmt -> session.executeAsync(stmt));
//				throw new DuplicateItemException(creation.message());
//			}
//		}

		return entity;
	}

	@Override
	public boolean delete(Identifier id)
	throws ItemNotFoundException, InvalidIdentifierException
	{
		return false;
	}

	@Override
	public boolean exists(Identifier id)
	throws InvalidIdentifierException
	{
		return existsInView(PRIMARY_TABLE, id);
	}

	public boolean existsInView(String viewName, Identifier id)
	throws InvalidIdentifierException
	{
		return (session.execute(statementsByView.get(viewName).exists(id)).one().getLong(0) > 0);
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
		Row row = session.execute(statementsByView.get(viewName).read(id)).one();

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
		// TODO Auto-generated method stub
		return null;
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

	private com.strategicgains.noschema.Identifier marshalId(KeyDefinition keyDefinition, Row row)
	{
		com.strategicgains.noschema.Identifier id = new com.strategicgains.noschema.Identifier();
		keyDefinition.components().forEach(t -> id.add(KeyPropertyConverter.marshal(t.column(), t.type(), row)));
		return id;
	}

	private ExecResult executeAll(List<BoundStatement> statements)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
