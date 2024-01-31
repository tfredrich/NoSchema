package com.strategicgains.noschema.cassandra.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.shaded.guava.common.util.concurrent.Futures;
import com.datastax.oss.driver.shaded.guava.common.util.concurrent.ListenableFuture;
import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.exception.DuplicateItemException;
import com.strategicgains.noschema.exception.ItemNotFoundException;
import com.strategicgains.noschema.unitofwork.AbstractUnitOfWork;
import com.strategicgains.noschema.unitofwork.Change;
import com.strategicgains.noschema.unitofwork.DirtyChange;
import com.strategicgains.noschema.unitofwork.UnitOfWorkCommitException;
import com.strategicgains.noschema.unitofwork.UnitOfWorkRollbackException;

public class DocumentUnitOfWork
extends AbstractUnitOfWork<Document>
{
    private final CqlSession session;
    private final DocumentStatementGenerator generator;

    public DocumentUnitOfWork(CqlSession session, DocumentStatementGenerator statementGenerator)
    {
        this.session = Objects.requireNonNull(session);
        this.generator = Objects.requireNonNull(statementGenerator);
    }

    @Override
    public void commit()
    throws UnitOfWorkCommitException
    {
    	List<ListenableFuture<Boolean>> existence = new ArrayList<>();
    	List<BoundStatement> statements = new ArrayList<>();

		changes().forEach(change -> {
			checkExistence(session, change).ifPresent(existence::add);
			statements.addAll(generateStatementsFor(change));
		});

		if (!existence.isEmpty())
		{
			Futures.inCompletionOrder(existence);
		}

		// TODO: use an execution strategy: LOGGED, UNLOGGED, ASYNC
        BatchStatementBuilder batch = new BatchStatementBuilder(BatchType.LOGGED);
        statements.stream().forEach(batch::addStatement);
        CompletionStage<AsyncResultSet> resultSet = session.executeAsync(batch.build());

        if (resultSet.wasApplied())
        {
            reset();
        }
        else
        {
            throw new UnitOfWorkCommitException("Commit failed", resultSet.getExecutionInfo().getErrors().get(0).getValue());
        }
    }

	@Override
    public void rollback()
    throws UnitOfWorkRollbackException
    {
        // No-op for CassandraUnitOfWork since we're using a logged batch
    }

	private Optional<ListenableFuture<Boolean>> checkExistence(CqlSession session, Change<Document> change)
	{
		String viewName = change.getEntity().getView();

		// Updates and deletes on unique views require pre-existence.
		// Creation on unique views requires non-existence.
		if (generator.isViewUnique(viewName))
		{
			return Optional.of(session.executeAsync(generator.exists(viewName, change.getId())).thenApply(r -> 
				Boolean.valueOf(r.one().getInt(0) > 0))
					.thenApply(exists -> {
						if (Boolean.TRUE == exists && change.isNew())
						{
							return Futures.immediateFailedFuture(new DuplicateItemException());
						}
						else if (Boolean.FALSE == exists && (change.isDirty() || change.isDeleted()))
						{
							return Futures.immediateFailedFuture(new ItemNotFoundException());
						}

						return true;
					}).toCompletableFuture());
		}

		return Optional.empty();
	}

	private List<BoundStatement> generateStatementsFor(Change<Document> change)
	{
		String viewName = change.getEntity().getView();

		switch(change.getState())
		{
			case DELETED:
				return Collections.singletonList(generator.delete(viewName, change.getId()));
			case DIRTY:
				DirtyChange<Document> update = (DirtyChange<Document>) change;

				if (update.identityChanged())
				{
					ArrayList<BoundStatement> statements = new ArrayList<>(2);
					statements.add(generator.delete(viewName, update.getOriginal().getIdentifier()));
					statements.add(generator.create(viewName, update.getEntity()));
					return statements;
				}
				else
				{
					return Collections.singletonList(generator.update(viewName, change.getEntity()));
				}
			case NEW:
				return Collections.singletonList(generator.create(viewName, change.getEntity()));
			default:
				break;
		}

		return Collections.emptyList();
	}
}
