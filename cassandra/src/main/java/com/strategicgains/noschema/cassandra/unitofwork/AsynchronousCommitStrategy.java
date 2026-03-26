package com.strategicgains.noschema.cassandra.unitofwork;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.strategicgains.noschema.unitofwork.UnitOfWorkCommitException;
import com.strategicgains.noschema.unitofwork.UnitOfWorkRollbackException;

/**
 * This commit strategy executes each statement asynchronously and waits for all
 * to complete. The statements are executed asynchronously against the target
 * partition. This can be useful for scenarios where writing crosses multiple
 * partitions in parallel without the overhead of batching.
 * 
 * The consistency is eventual, and there is no real guarantee of atomicity
 * across the statements. If any statement fails, commit() will throw an
 * exception, but other statements may have already been applied.
 * 
 * The Repository (e.g., CassandraRepository) will execute rollback() if
 * commit() throws an exception. However, rollback in this context is made up of
 * compensating transactions, updates or deletes to undo the changes made by the
 * successfully-completed commit statements, since Cassandra doesn't support
 * traditional transactions or rollbacks.
 * 
 * While the possibility is narrow, it is definitely possible to have the
 * rollback also fail, leaving the system in an inconsistent state. This is left
 * as a known risk of using this commit strategy, and it is the responsibility
 * of the application to handle such scenarios appropriately, such as by
 * implementing additional monitoring, alerting, or manual intervention
 * processes to address any inconsistencies that may arise.
 *
 * @author toddf
 * @since 2024-06-01
 */
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
