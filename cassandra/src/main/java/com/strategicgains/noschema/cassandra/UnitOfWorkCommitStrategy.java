package com.strategicgains.noschema.cassandra;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.strategicgains.noschema.unitofwork.UnitOfWorkCommitException;
import com.strategicgains.noschema.unitofwork.UnitOfWorkRollbackException;

public interface UnitOfWorkCommitStrategy
{
	CompletableFuture<AsyncResultSet> commit(List<BoundStatement> statements)
	throws UnitOfWorkCommitException;

	void rollback()
	throws UnitOfWorkRollbackException;
}
