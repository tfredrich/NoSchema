package com.strategicgains.noschema.cassandra.unitofwork;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchType;

/**
 * Defines the type of commit strategy to use for a CassandraUnitOfWork.
 * 
 * @author Todd Fredrich
 * @since Aug 17, 2024
 */
public enum CommitType
{
	ASYNC,
	LOGGED,
	UNLOGGED;

	public CommitStrategy asCommitStrategy(CqlSession session)
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
