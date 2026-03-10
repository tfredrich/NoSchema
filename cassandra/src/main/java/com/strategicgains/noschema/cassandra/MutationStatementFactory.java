package com.strategicgains.noschema.cassandra;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

public interface MutationStatementFactory
{
	BoundStatement create(String viewName, Identifiable entity);
	BoundStatement update(String viewName, Identifiable entity);
	BoundStatement delete(String viewName, Identifier id);
	BoundStatement exists(String viewName, Identifier id);
	boolean isViewUnique(String viewName);
}
