package com.strategicgains.noschema.cassandra.unitofwork;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.strategicgains.noschema.unitofwork.UnitOfWorkCommitException;
import com.strategicgains.noschema.unitofwork.UnitOfWorkRollbackException;

/**
 * A commit strategy that executes all statements in a single batch, which can
 * be either LOGGED, UNLOGGED.
 * 
 * Using batches is generally discouraged in Cassandra for large numbers of
 * statements or for statements that span multiple partitions, as it can lead to
 * performance issues. However, for small batches of statements that target the
 * same partition, using a batch can provide atomicity guarantees and improve
 * performance.
 * 
 * NOTE: This strategy does not support ROLLBACK since Cassandra doesn't support
 * transactions in the traditional sense. Instead, Cassandra's logged batches
 * provide atomicity guarantees for the statements within the batch, but if any
 * statement fails, the entire batch will fail and no changes will be applied.
 * Therefore, if a commit fails, it will throw an exception, and there is no
 * need for a separate rollback mechanism.
 *
 * @author toddf
 * @since 2024-06-01
 */
public class BatchCommitStrategy
implements UnitOfWorkCommitStrategy
{
	private BatchType batchType = BatchType.LOGGED;
	private CqlSession session;

	public BatchCommitStrategy(CqlSession session)
	{
		this(BatchType.LOGGED, session);
	}

	public BatchCommitStrategy(BatchType batchType, CqlSession session)
	{
		super();
		this.batchType = batchType;
		this.session = session;
	}

	@Override
	public CompletableFuture<AsyncResultSet> commit(List<BoundStatement> statements)
	throws UnitOfWorkCommitException
	{
		BatchStatementBuilder batch = new BatchStatementBuilder(batchType);
		statements.forEach(batch::addStatement);
		CompletionStage<AsyncResultSet> resultSet = session.executeAsync(batch.build());

		return resultSet
			.exceptionally(t -> {
				throw new UnitOfWorkCommitException("Commit failed", t);
			})
			.toCompletableFuture();
	}

	/**
	 * ROLLBACK is not supported for BatchCommitStrategy since Cassandra doesn't support
	 * transactions in the traditional sense. If a commit fails, it will throw an exception,
	 * and there is no need for a separate rollback mechanism.
	 * 
	 * @throws UnitOfWorkRollbackException always, since rollback is not supported.
	 */
	@Override
	public void rollback()
	throws UnitOfWorkRollbackException
	{
		// No-op for CassandraUnitOfWork since we're using a logged batch
		throw new UnitOfWorkRollbackException("Not Implemented.");
	}
}
