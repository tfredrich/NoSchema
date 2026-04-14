package com.strategicgains.noschema.cassandra.unitofwork;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.strategicgains.noschema.unitofwork.UnitOfWorkCommitException;
import com.strategicgains.noschema.unitofwork.UnitOfWorkRollbackException;

/**
 * Executes statements asynchronously, returning a CompletableFuture that completes when all statements have completed.
 * There is no batch execution, so statements are executed individually.
 */
public class AsynchronousCommitStrategy
implements CommitStrategy
{
	private CqlSession session;

	
	public AsynchronousCommitStrategy(CqlSession session)
	{
		super();
		this.session = session;
	}


	@Override
	public CompletableFuture<Void> commit(List<BoundStatement> statements)
	throws UnitOfWorkCommitException
	{
		CompletableFuture<?>[] futures = statements.stream()
				.map(s -> session.executeAsync(s).toCompletableFuture())
				.toArray(CompletableFuture[]::new);

			return CompletableFuture.allOf(futures);
	}


	@Override
	public void rollback() throws UnitOfWorkRollbackException
	{
		// No-op for CassandraUnitOfWork so far...
		throw new UnitOfWorkRollbackException("Not Implemented.");
	}
}
