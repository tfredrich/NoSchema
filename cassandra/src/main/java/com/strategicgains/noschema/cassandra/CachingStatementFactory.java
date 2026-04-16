package com.strategicgains.noschema.cassandra;

import java.util.HashMap;
import java.util.Map;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.protocol.internal.util.Bytes;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.document.DocumentStatementFactory;
import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.document.ObjectCodec;

public class CachingStatementFactory<T extends Identifiable>
{
	private final Map<String, KeyDefinition> keysByTable = new HashMap<>();
    private final Map<String, CqlStatementFactory<T>> factoriesByTable = new HashMap<>();

	public CachingStatementFactory(CqlSession session, PrimaryTable table, ObjectCodec<T> codec)
	{
		super();
		table.stream().forEach(view -> {
			put(view.name(), new DocumentStatementFactory<>(session, view, codec));
			put(view.name(), view.keys());				
		});
	}

	public BoundStatement read(String tableName, Identifier id)
	{
		return get(tableName).read(id);
	}

	/**
	 * Read all records from the table, with paging support. If a cursor is
	 * provided, it will be used to set the paging state of the statement. If a
	 * limit is provided (greater-than zero), it will be used to set the page size
	 * of the statement.
	 * 
	 * @param tableName the name of the table to read from.
	 * @param limit the maximum number of records to return. If zero or negative, no limit will be applied.
	 * @param cursor the paging state to use for the query. If null, the query will start from the beginning.
	 * @param parameters any parital key parameters required for the query.
	 * @return a BoundStatement ready to be executed against the Cassandra session.
	 */
	public BoundStatement readAll(String tableName, int limit, String cursor, Object... parameters)
	{
		BoundStatement stmt = get(tableName).readAll(parameters);

		if (limit > 0)
		{
			stmt = stmt.setPageSize(limit);
		}

		if (cursor != null)
		{
			stmt = stmt.setPagingState(Bytes.fromHexString(cursor));
		}

		return stmt;
	}

	public BoundStatement delete(String tableName, Identifier id)
	{
		return get(tableName).delete(id);
	}

	public BoundStatement create(String tableName, T entity)
	{
		return get(tableName).create(entity);
	}

	public BoundStatement update(String tableName, T entity)
	{
		return get(tableName).update(entity);
	}

	public BoundStatement exists(String tableName, Identifier id)
	{
		return get(tableName).exists(id);
	}

	public boolean isViewUnique(String tableName)
	{
		return keysByTable.get(tableName).isUnique();
	}

	private void put(String tableName, CqlStatementFactory<T> factory)
	{
		factoriesByTable.put(tableName, factory);
	}

	private void put(String tableName, KeyDefinition keys)
	{
		this.keysByTable.put(tableName, keys);
	}

	private CqlStatementFactory<T> get(String tableName)
	{
		CqlStatementFactory<T> factory = factoriesByTable.get(tableName);

//		if (factory == null) throw new InvalidViewNameException(tableName);

		return factory;
	}
}
