package com.strategicgains.noschema.cassandra;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.BSONEncoder;
import org.bson.BasicBSONEncoder;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.DocumentSchemaProvider.Columns;
import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.InvalidObjectIdException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public final class DocumentStatementFactory<T>
implements StatementFactory<T>
{
	private static final BSONEncoder ENCODER = new BasicBSONEncoder();

	private static final String SELECT_COLUMNS = String.join(",", Columns.OBJECT, Columns.TYPE, Columns.CREATED_AT, Columns.UPDATED_AT);
	private static final String CREATE_CQL = "insert into %s.%s (%s, %s, %s, %s, %s) values (%s) if not exists";
	private static final String DELETE_CQL = "delete from %s.%s where %s";
	private static final String EXISTS_CQL = "select count(*) from %s.%s  where %s limit 1";
	private static final String READ_CQL = "select %s, " + SELECT_COLUMNS + " from %s.%s where %s limit 1";
	private static final String READ_ALL_CQL = "select %s " + SELECT_COLUMNS + " from %s.%s where %s";
	private static final String UPDATE_CQL = "update %s.%s set %s = ?, %s = ?, %s = ? where %s if exists";
	private static final String UPSERT_CQL = "insert into %s.%s (%s, %s, %s, %s, %s) values (%s)";

	private static final String CREATE = "create";
	private static final String DELETE = "delete";
	private static final String EXISTS = "exists";
	private static final String READ = "read";
	private static final String READ_ALL = "readAll";
	private static final String UPDATE = "update";
	private static final String UPSERT = "upsert";

	private CqlSession session;
	private Table table;
	private Map<String, PreparedStatement> statements = new ConcurrentHashMap<>();
	private CassandraDocumentFactory<T> documentFactory;

	public DocumentStatementFactory(CqlSession session, Table table)
	{
		this(session, table, new CassandraDocumentFactory<>(table.keys()));
	}

	public DocumentStatementFactory(CqlSession session, Table table, CassandraDocumentFactory<T> factory)
	{
		super();
		this.session = session;
		this.table = table;
		this.documentFactory = factory;
	}

	private PreparedStatement prepareCreate()
	{
		return statements.computeIfAbsent(CREATE, k -> 
			session.prepare(
				String.format(CREATE_CQL,
					table.keyspace(),
					table.name(),
					table.keys().asSelectProperties(),
					Columns.OBJECT,
					Columns.TYPE,
					Columns.CREATED_AT,
					Columns.UPDATED_AT,
					table.keys().asQuestionMarks(4)))
		);
	}

	private PreparedStatement prepareDelete()
	{
		return statements.computeIfAbsent(DELETE, k ->
			session.prepare(
				String.format(DELETE_CQL,
					table.keyspace(),
					table.name(),
					table.keys().asIdentityClause()))
		);
	}

	private PreparedStatement prepareExists()
	{
		return statements.computeIfAbsent(EXISTS, k -> 
			session.prepare(
				String.format(EXISTS_CQL,
					table.keyspace(),
					table.name(),
					table.keys().asIdentityClause()))
		);
	}

	private PreparedStatement prepareUpdate()
	{
		return statements.computeIfAbsent(UPDATE, k -> 
			session.prepare(
				String.format(UPDATE_CQL,
					table.keyspace(),
					table.name(),
					Columns.OBJECT,
					Columns.TYPE,
					Columns.UPDATED_AT,
					table.keys().asIdentityClause()))
		);
	}

	private PreparedStatement prepareUpsert()
	{
		return statements.computeIfAbsent(UPSERT, k -> 
		session.prepare(
			String.format(UPSERT_CQL,
				table.keyspace(),
				table.name(),
				table.keys().asSelectProperties(),
				Columns.OBJECT,
				Columns.TYPE,
				Columns.CREATED_AT,
				Columns.UPDATED_AT,
				table.keys().asQuestionMarks(4)))
		);
	}

	private PreparedStatement prepareRead()
	{
		return statements.computeIfAbsent(READ, k -> 
		session.prepare(
			String.format(READ_CQL,
				table.keys().asSelectProperties(),
				table.keyspace(),
				table.name(),
				table.keys().asIdentityClause()))
		);
	}

	private PreparedStatement prepareReadAll()
	{
		return statements.computeIfAbsent(READ_ALL, k -> 
		session.prepare(
			String.format(READ_ALL_CQL,
				table.keys().asSelectProperties(),
				table.keyspace(),
				table.name(),
				table.keys().asPartitionIdentityClause()))
		);
	}

	@Override
	public BoundStatement create(T entity)
	{
		return bindCreate(prepareCreate(), entity);
	}

	@Override
	public BoundStatement delete(Identifier id)
	{
		return bindIdentity(prepareDelete(), id);
	}

	@Override
	public BoundStatement exists(Identifier id)
	{
		return bindIdentity(prepareExists(), id);
	}

	@Override
	public BoundStatement update(T entity)
	{
		return bindUpdate(prepareUpdate(), entity);
	}

	@Override
	public BoundStatement upsert(T entity)
	{
		return bindCreate(prepareUpsert(), entity);
	}

	@Override
	public BoundStatement read(Identifier id)
	{
		return bindIdentity(prepareRead(), id);
	}

	@Override
	public BoundStatement readAll(Object... parameters)
	{
		return prepareReadAll().bind(parameters);
	}

	protected BoundStatement bindIdentity(PreparedStatement bs, Identifier id)
	{
		return bs.bind(id.components().toArray());
	}

	protected BoundStatement bindCreate(PreparedStatement ps, T entity)
	{
		Document document = asDocument(entity);
		Date now = new Date();
		document.setCreatedAt(now);
		document.setUpdatedAt(now);
		Identifier id = document.getIdentifier();
		Object[] values = new Object[id.size() + 4]; // Identifier + object + createdAt + updatedAt.
		fill(values, 0, id.components().toArray());
		fill(values, id.size(),
			(document.hasObject() ? ByteBuffer.wrap(ENCODER.encode(document.getObject())) : null),
				document.getType(),
				document.getCreatedAt().toInstant(),
			    document.getUpdatedAt().toInstant());
		return ps.bind(values);
	}

	protected BoundStatement bindUpdate(PreparedStatement ps, T entity)
	{
		Document document = asDocument(entity);
		document.setUpdatedAt(new Date());
		Identifier id = document.getIdentifier();
		Object[] values = new Object[id.size() + 3];
			fill(values, 0,
				(document.hasObject() ? ByteBuffer.wrap(ENCODER.encode(document.getObject())) : null),
					document.getType(),
				    document.getUpdatedAt().toInstant());
			fill(values, 3, id.components().toArray());
			return ps.bind(values);
	}

	private void fill(Object[] array, int offset, Object... values)
	{
		for (int i = offset; i < values.length + offset; i++)
		{
			array[i] = values[i - offset];
		}
	}

	private Document asDocument(T entity)
	{
		try
		{
			return documentFactory.asDocument(entity);
		}
		catch (InvalidIdentifierException | KeyDefinitionException e)
		{
			throw new InvalidObjectIdException(e);
		}
	}
}
