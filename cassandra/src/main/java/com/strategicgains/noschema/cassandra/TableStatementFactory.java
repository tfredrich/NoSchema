package com.strategicgains.noschema.cassandra;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

/**
 * Defines the contract for a factory that creates C* statements for a given table and entity.
 * The factory is used by the CassandraRepository to create statements for CRUD operations.
 * 
 * @author Todd Fredrich
 */
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
