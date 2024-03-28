package com.strategicgains.noschema.cassandra.schema;

import com.datastax.oss.driver.api.core.CqlSession;

public interface CassandraSchemaProvider
{
	public String asCreateScript();
	public String asDropScript();
	public boolean drop(CqlSession session);
	public boolean create(CqlSession session);
}
