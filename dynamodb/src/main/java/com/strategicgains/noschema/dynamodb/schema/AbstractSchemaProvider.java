package com.strategicgains.noschema.dynamodb.schema;

import com.datastax.oss.driver.api.core.CqlSession;

public abstract class AbstractSchemaProvider
implements SchemaProvider
{
	@Override
	public boolean drop(CqlSession session)
	{
		return session.execute(asDropScript()).wasApplied();
	}

	@Override
	public boolean create(CqlSession session)
	{
		return session.execute(asCreateScript()).wasApplied();
	}
}
