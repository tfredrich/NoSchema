package com.strategicgains.noschema.cassandra.unitofwork;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.strategicgains.noschema.unitofwork.UnitOfWorkCommitException;
import com.strategicgains.noschema.unitofwork.UnitOfWorkRollbackException;

public class AsynchronousCommitStrategy
implements UnitOfWorkCommitStrategy
{
	private CqlSession session;

	
	public AsynchronousCommitStrategy(CqlSession session)
	{
		super();
		this.session = session;
	}


	@Override
	public CompletableFuture<AsyncResultSet> commit(List<BoundStatement> statements)
	throws UnitOfWorkCommitException
	{
		throw new UnitOfWorkCommitException("Not Implemented.");
		// TODO: Implement
//		List<CompletionStage<?>> existence = new ArrayList<>();
//		
//		CompletableFuture[] allStatements = statements.stream()
//				.map(s -> session.executeAsync(s).toCompletableFuture())
//				.toArray(CompletableFuture[]::new);
//
//			CompletableFuture<Void> allStatements = CompletableFuture.allOf(futuresArray);
	}


	@Override
	public void rollback() throws UnitOfWorkRollbackException
	{
		// No-op for CassandraUnitOfWork so far...
		throw new UnitOfWorkRollbackException("Not Implemented.");
	}
}