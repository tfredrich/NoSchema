package com.strategicgains.noschema.dynamodb;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.strategicgains.noschema.Identifier;

public interface StatementFactory<T>
{
	BoundStatement create(T entity);
	BoundStatement delete(Identifier id);
	BoundStatement exists(Identifier id);
	BoundStatement update(T entity);
	BoundStatement upsert(T entity);
	BoundStatement read(Identifier id);
	BoundStatement readAll(Object... parameters);
}
