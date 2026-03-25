package com.strategicgains.noschema.cassandra;

import java.util.HashMap;
import java.util.Map;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.protocol.internal.util.Bytes;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.key.KeyDefinition;

public class CachingStatementFactory<T extends Identifiable>
implements MutationStatementFactory
{
	private final Map<String, KeyDefinition> keysByTable = new HashMap<>();
    private final Map<String, PreparedStatementFactory<T>> statementsByTable = new HashMap<>();

	public CachingStatementFactory(CqlSession session, PrimaryTable<?> table, PreparedStatementFactoryProvider<T> factoryProvider)
	{
		super();
		table.stream().forEach(view -> {
			put(view.name(), factoryProvider.create(session, view));
			put(view.name(), view.keys());				
		});
	}

	public BoundStatement read(String tableName, Identifier id)
	{
		return get(tableName).read(id);
	}

	public BoundStatement readAll(String tableName, int limit, String cursor, Object... parameters)
	{
		BoundStatement stmt = get(tableName)
			.readAll(parameters)
			.setPageSize(limit);

		if (cursor != null)
		{
			stmt = stmt.setPagingState(Bytes.fromHexString(cursor));
		}

		return stmt;
	}

	@Override
	public BoundStatement delete(String tableName, Identifier id)
	{
		return get(tableName).delete(id);
	}

	@Override
	public BoundStatement exists(String tableName, Identifier id)
	{
		return get(tableName).exists(id);
	}

	@Override
	public boolean isViewUnique(String tableName)
	{
		return keysByTable.get(tableName).isUnique();
	}

	@Override
	public BoundStatement create(String tableName, Identifiable entity)
	{
		return mutationFactory(tableName).create(entity);
	}

	@Override
	public BoundStatement update(String tableName, Identifiable entity)
	{
		return mutationFactory(tableName).update(entity);
	}

	private void put(String tableName, PreparedStatementFactory<T> factory)
	{
		statementsByTable.put(tableName, factory);
	}

	private void put(String tableName, KeyDefinition keys)
	{
		this.keysByTable.put(tableName, keys);
	}

	private PreparedStatementFactory<T> get(String tableName)
	{
		PreparedStatementFactory<T> factory = statementsByTable.get(tableName);

		if (factory == null) throw new InvalidViewNameException(tableName);

		return factory;
	}

	@SuppressWarnings("unchecked")
	private PreparedStatementFactory<Identifiable> mutationFactory(String tableName)
	{
		return (PreparedStatementFactory<Identifiable>) statementsByTable.get(tableName);
	}
}
