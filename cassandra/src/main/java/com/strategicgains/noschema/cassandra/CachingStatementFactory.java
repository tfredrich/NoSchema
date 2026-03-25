package com.strategicgains.noschema.cassandra;

import java.util.HashMap;
import java.util.Map;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.protocol.internal.util.Bytes;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.key.KeyDefinition;

/**
 * A ${link TableStatementFactory} that creates and caches prepared statements for each table in the hierarchy.
 * It is used by the ${link UnitOfWork} to execute statements against the database.
 *
 * @param <T> The type of Identifiable entities this factory will handle.
 * @author Todd Fredrich
 */
public class CachingStatementFactory<T extends Identifiable>
implements TableStatementFactory
{
	private final Map<String, KeyDefinition> keysByTable = new HashMap<>();
    private final Map<String, BoundStatementFactory<T>> statementsByTable = new HashMap<>();

	public CachingStatementFactory(CqlSession session, PrimaryTable<?> table, BoundStatementFactoryProvider<T> factoryProvider)
	{
		super();
		table.stream().forEach(view -> {
			putFactory(view.name(), factoryProvider.create(session, view));
			putKey(view.name(), view.keys());				
		});
	}

	@Override
	public BoundStatement read(String tableName, Identifier id)
	{
		return factoryFor(tableName).read(id);
	}

	@Override
	public BoundStatement readAll(String tableName, int limit, String cursor, Object... parameters)
	{
		BoundStatement stmt = factoryFor(tableName)
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
		return factoryFor(tableName).delete(id);
	}

	@Override
	public BoundStatement exists(String tableName, Identifier id)
	{
		return factoryFor(tableName).exists(id);
	}

	@Override
	public boolean isViewUnique(String tableName)
	{
		KeyDefinition keys = keysByTable.get(tableName);

		if (keys == null) throw new InvalidTableNameException(tableName);

		return keys.isUnique();
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

	private void putFactory(String tableName, BoundStatementFactory<T> factory)
	{
		statementsByTable.put(tableName, factory);
	}

	private void putKey(String tableName, KeyDefinition keys)
	{
		this.keysByTable.put(tableName, keys);
	}

	private BoundStatementFactory<T> factoryFor(String tableName)
	{
		BoundStatementFactory<T> factory = statementsByTable.get(tableName);

		if (factory == null) throw new InvalidTableNameException(tableName);

		return factory;
	}

	@SuppressWarnings("unchecked")
	private BoundStatementFactory<Identifiable> mutationFactory(String tableName)
	{
		return (BoundStatementFactory<Identifiable>) factoryFor(tableName);
	}
}
