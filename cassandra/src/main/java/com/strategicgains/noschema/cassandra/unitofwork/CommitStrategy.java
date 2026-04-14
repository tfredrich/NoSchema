package com.strategicgains.noschema.cassandra.unitofwork;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.strategicgains.noschema.unitofwork.UnitOfWorkCommitException;
import com.strategicgains.noschema.unitofwork.UnitOfWorkRollbackException;

/**
 * Defines the strategy for committing a set of statements to Cassandra as part of a UnitOfWork.
 * 
 * @author Todd Fredrich
 * @since Aug 17, 2024
 */
public interface CommitStrategy
{
	CompletableFuture<?> commit(List<BoundStatement> statements)
	throws UnitOfWorkCommitException;

	void rollback()
	throws UnitOfWorkRollbackException;
}
