package com.strategicgains.noschema.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;

/**
 * Defines a factory for creating BoundStatementFactory instances for a given entity type.
 * 
 * @author Todd Fredrich
 * @param <T> the entity type to create BoundStatementFactory instances for.
 */
public interface BoundStatementFactoryProvider<T>
{
	BoundStatementFactory<T> create(CqlSession session, AbstractTable<?> table);
}
