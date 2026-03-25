package com.strategicgains.noschema.cassandra;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

public interface TableStatementFactory
{
	BoundStatement create(String tableName, Identifiable entity);
	BoundStatement update(String tableName, Identifiable entity);
	BoundStatement delete(String tableName, Identifier id);
	BoundStatement exists(String tableName, Identifier id);
	BoundStatement read(String tableName, Identifier id);
	BoundStatement readAll(String tableName, int limit, String cursor, Object... parameters);
	boolean isViewUnique(String tableName);
}
