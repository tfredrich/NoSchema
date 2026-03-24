package com.strategicgains.noschema.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;

public interface PreparedStatementFactoryProvider<T>
{
	PreparedStatementFactory<T> create(CqlSession session, AbstractTable<?> table);
}
