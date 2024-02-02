package com.strategicgains.noschema.cassandra.document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.exception.DuplicateItemException;
import com.strategicgains.noschema.exception.ItemNotFoundException;
import com.strategicgains.noschema.unitofwork.AbstractUnitOfWork;
import com.strategicgains.noschema.unitofwork.Change;
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
		List<CompletionStage<Boolean>> existence = new ArrayList<>();
		List<BoundStatement> statements = new ArrayList<>();

		changes().forEach(change -> {
			checkExistence(session, (DocumentChange) change).ifPresent(existence::add);
			generateStatementFor((DocumentChange) change).ifPresent(statements::add);
		});

		handleExistenceChecks(existence);

		// TODO: use an execution strategy: LOGGED, UNLOGGED, ASYNC
		BatchStatementBuilder batch = new BatchStatementBuilder(BatchType.LOGGED);
		statements.forEach(batch::addStatement);
		CompletionStage<AsyncResultSet> resultSet = session.executeAsync(batch.build());

		resultSet
			.thenAccept(r -> reset())
			.exceptionally(t -> {
				throw new UnitOfWorkCommitException("Commit failed", t);
			})
			.toCompletableFuture()
			.join();
	}

	@Override
    public void rollback()
    throws UnitOfWorkRollbackException
    {
        // No-op for CassandraUnitOfWork since we're using a logged batch
    }

	private void handleExistenceChecks(List<CompletionStage<Boolean>> futures)
	throws UnitOfWorkCommitException
	{
		CompletableFuture<?>[] futuresArray = futures.stream()
			.map(CompletionStage::toCompletableFuture)
			.toArray(CompletableFuture[]::new);

		CompletableFuture<Void> anyFailedFuture = CompletableFuture.allOf(futuresArray);

		try
		{
			anyFailedFuture.exceptionally(t -> {
				throw new UnitOfWorkCommitException(t.getCause());
			}).join();
		}
		catch (CompletionException e)
		{
			throw (UnitOfWorkCommitException) e.getCause();
		}
	}

	private Optional<CompletionStage<Boolean>> checkExistence(CqlSession session, final DocumentChange change)
	{
		String viewName = change.getView();

		if (generator.isViewUnique(viewName))
		{
			return Optional.of(session.executeAsync(generator.exists(viewName, change.getId()))
				.thenApply(r -> r.one().getLong(0) > 0L)
					.thenCompose(exists -> checkExistenceRules(change, exists)));
		}

		return Optional.empty();
	}

	private CompletionStage<Boolean> checkExistenceRules(Change<Document> change, boolean exists)
	{
		CompletableFuture<Boolean> result = new CompletableFuture<>();

		if (exists && change.isNew())
		{
			result.completeExceptionally(new DuplicateItemException(change.getId().toString()));
		}
		else if (!exists && (change.isDirty() || change.isDeleted()))
		{
			result.completeExceptionally(new ItemNotFoundException(change.getId().toString()));
		}
		else
		{
			result.complete(Boolean.TRUE);
		}

		return result;
	}

	private Optional<BoundStatement> generateStatementFor(DocumentChange change)
	{
		String viewName = change.getView();

		switch(change.getState())
		{
			case DELETED:
				return Optional.of(generator.delete(viewName, change.getId()));
			case DIRTY:
				return Optional.of(generator.update(viewName, change.getEntity()));
			case NEW:
				return Optional.of(generator.create(viewName, change.getEntity()));
			default:
				break;
		}

		return Optional.empty();
	}

	public Document readClean(Identifier id)
	{
		return findClean(id);
	}
}
