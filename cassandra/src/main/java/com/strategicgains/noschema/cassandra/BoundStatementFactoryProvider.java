package com.strategicgains.noschema.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;

public interface BoundStatementFactoryProvider<T>
{
	BoundStatementFactory<T> create(CqlSession session, AbstractTable<?> table);
}
