package com.strategicgains.noschema.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchType;

public enum UnitOfWorkType
{
	ASYNC,
	LOGGED,
	UNLOGGED;

	public UnitOfWorkCommitStrategy asCommitStrategy(CqlSession session)
	{
		switch(this)
		{
		case ASYNC:
			return new AsynchronousCommitStrategy(session);
		case LOGGED:
			return new BatchCommitStrategy(BatchType.LOGGED, session);
		case UNLOGGED:
			return new BatchCommitStrategy(BatchType.UNLOGGED, session);
		}

		return null;
	}
}
