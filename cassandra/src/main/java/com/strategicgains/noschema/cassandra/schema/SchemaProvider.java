package com.strategicgains.noschema.cassandra.schema;

import com.datastax.oss.driver.api.core.CqlSession;

public interface SchemaProvider
{
	boolean drop(CqlSession session);
	boolean create(CqlSession session);

	String asCreateScript();
	String asDropScript();
}
