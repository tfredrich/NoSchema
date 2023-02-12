package com.strategicgains.noschema.dynamodb.schema;

import com.datastax.oss.driver.api.core.CqlSession;

public interface SchemaProvider
{
	public String asCreateScript();
	public String asDropScript();
	public boolean drop(CqlSession session);
	public boolean create(CqlSession session);
}
