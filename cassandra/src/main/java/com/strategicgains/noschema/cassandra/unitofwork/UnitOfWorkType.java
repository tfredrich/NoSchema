package com.strategicgains.noschema.cassandra.unitofwork;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchType;

/**
 * Indicates which commit strategy to use for a UnitOfWork. ASYNC will execute
 * each statement asynchronously, while LOGGED and UNLOGGED will execute
 * statements in a batch.
 * 
 * This effects the rollback and consistency guarantees of the UnitOfWork. ASYNC
 * provides no consistency guarantees nor rollback (presently - coming soon),
 * but enables the highest performance. LOGGED and UNLOGGED provide no rollback
 * but have consistency guarantees as defined by Cassandra for logged and
 * unlogged batches, respectively.
 * 
 * @author Todd Fredrich
 */
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
