package com.strategicgains.noschema.cassandra.index;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.AbstractTable;
import com.strategicgains.noschema.cassandra.CqlStatementFactory;
import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.document.ObjectCodec;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.InvalidObjectIdException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public final class IndexStatementFactory<T extends Identifiable>
implements CqlStatementFactory<T>
{
	private static final String CREATE_CQL = "insert into %s.%s (%s) values (%s)";
	private static final String DELETE_CQL = "delete from %s.%s where %s";
	private static final String EXISTS_CQL = "select count(*) from %s.%s  where %s limit 1";
	private static final String READ_CQL = "select %s, %s from %s.%s where %s limit 1";
	private static final String READ_ALL_CQL = "select %s, %s from %s.%s where %s";
	private static final String UPDATE_CQL = "update %s.%s set %s where %s";

	private static final String CREATE = "create";
	private static final String DELETE = "delete";
	private static final String EXISTS = "exists";
	private static final String READ = "read";
	private static final String READ_ALL = "readAll_";
	private static final String UPDATE = "update";
	private static final String UPSERT = "upsert";

	private CqlSession session;
	private AbstractTable table;
	private Map<String, PreparedStatement> statements = new ConcurrentHashMap<>();
	private CassandraIndexEntryFactory<T> indexEntryFactory;

	public IndexStatementFactory(CqlSession session, AbstractTable table, ObjectCodec<T> codec)
	{
		this(session, table, new CassandraIndexEntryFactory<>(table.keys(), codec));
	}

	public IndexStatementFactory(CqlSession session, AbstractTable table, CassandraIndexEntryFactory<T> factory)
	{
		super();
		this.session = session;
		this.table = table;
		this.indexEntryFactory = factory;
	}

	private PreparedStatement prepareCreate()
	{
		return statements.computeIfAbsent(CREATE, k -> 
			session.prepare(
				String.format(CREATE_CQL,
					table.keyspace(),
					table.asTableName(),
					table.keys().asSelectProperties(),
					table.keys().asQuestionMarks(5)))
		);
	}

	private PreparedStatement prepareDelete()
	{
		return statements.computeIfAbsent(DELETE, k ->
			session.prepare(
				String.format(DELETE_CQL,
					table.keyspace(),
					table.asTableName(),
					table.keys().asIdentityClause()))
		);
	}

	private PreparedStatement prepareExists()
	{
		return statements.computeIfAbsent(EXISTS, k -> 
			session.prepare(
				String.format(EXISTS_CQL,
					table.keyspace(),
					table.asTableName(),
					table.keys().asIdentityClause()))
		);
	}

	private PreparedStatement prepareUpdate()
	{
		return statements.computeIfAbsent(UPDATE, k -> 
			session.prepare(
				String.format(UPDATE_CQL,
					table.keyspace(),
					table.asTableName(),
					table.keys().asIdentityClause()))
		);
	}

	private PreparedStatement prepareUpsert()
	{
		return statements.computeIfAbsent(UPSERT, k -> 
		session.prepare(
			String.format(CREATE_CQL,
				table.keyspace(),
				table.asTableName(),
				table.keys().asSelectProperties(),
				table.keys().asQuestionMarks(5)))
		);
	}

	private PreparedStatement prepareRead()
	{
		return statements.computeIfAbsent(READ, k -> 
		session.prepare(
			String.format(READ_CQL,
				table.keys().asSelectProperties(),
				table.keyspace(),
				table.asTableName(),
				table.keys().asIdentityClause()))
		);
	}

	private PreparedStatement prepareReadAll(int keyCount)
	{
		return statements.computeIfAbsent(READ_ALL + keyCount, k -> 
		session.prepare(
			String.format(READ_ALL_CQL,
				table.keys().asSelectProperties(),
				table.keyspace(),
				table.asTableName(),
				table.keys().asIdentityClause(keyCount)))
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
		return prepareReadAll(parameters.length).bind(parameters);
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
		Identifier entityId = entity.getIdentifier();
		Object[] values = new Object[id.size() + entityId.size()];
		fill(values, 0, id.components().toArray());
		fill(values, id.size(), entityId.components().toArray());
		return ps.bind(values);
	}

	protected BoundStatement bindUpdate(PreparedStatement ps, T entity)
	{
		Document document = asDocument(entity);
		document.setUpdatedAt(new Date());
		Identifier id = document.getIdentifier();
		Identifier entityId = entity.getIdentifier();
		Object[] values = new Object[id.size() + entityId.size()];
		fill(values, 0, entityId.components().toArray());
		fill(values, entityId.size(), id.components().toArray());
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
		if (entity instanceof Document entityDocument) return entityDocument;

		try
		{
			return indexEntryFactory.asDocument(entity);
		}
		catch (InvalidIdentifierException | KeyDefinitionException e)
		{
			throw new InvalidObjectIdException(e);
		}
	}
}
