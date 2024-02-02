package com.strategicgains.noschema.cassandra;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.strategicgains.noschema.Identifier;

public interface CqlStatementFactory<T>
{
	BoundStatement create(T entity);
	BoundStatement delete(Identifier id);
	BoundStatement exists(Identifier id);
	BoundStatement update(T entity);
	BoundStatement upsert(T entity);
	BoundStatement read(Identifier id);
	BoundStatement readAll(Object... parameters);
}
