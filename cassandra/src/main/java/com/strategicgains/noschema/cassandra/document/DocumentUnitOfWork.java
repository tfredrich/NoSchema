package com.strategicgains.noschema.cassandra.document;

import java.util.ArrayList;
import java.util.Collections;
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
	{
		List<CompletionStage<Boolean>> existence = new ArrayList<>();
		List<BoundStatement> statements = new ArrayList<>();

		changes().forEach(change -> {
			checkExistence(session, change).ifPresent(existence::add);
			statements.addAll(generateStatementsFor(change));
		});

		handleExistenceInOrder(existence);
		// existence.forEach(this::handleExistence);

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

	private void handleExistenceInOrder(List<CompletionStage<Boolean>> futures)
	{
		CompletableFuture<?>[] futuresArray = futures.stream()
			.map(CompletionStage::toCompletableFuture)
			.toArray(CompletableFuture[]::new);

		CompletableFuture<Void> anyFailedFuture = CompletableFuture.allOf(futuresArray);

		anyFailedFuture.exceptionally(t -> {
			throw new UnitOfWorkCommitException("Existence check failed", t);
		}).join();
	}

	private void handleExistence(CompletionStage<Boolean> future)
	{
		try
		{
			Object v = future.toCompletableFuture().join();
		}
		catch (CompletionException e)
		{
			throw new UnitOfWorkCommitException(e.getCause());
		}
	}

	@Override
    public void rollback()
    throws UnitOfWorkRollbackException
    {
        // No-op for CassandraUnitOfWork since we're using a logged batch
    }

	private Optional<CompletionStage<Boolean>> checkExistence(CqlSession session, final Change<Document> change) {
		String viewName = change.getEntity().getView();

		if (generator.isViewUnique(viewName)) {
			return Optional.of(session.executeAsync(generator.exists(viewName, change.getId()))
				.thenApply(r -> r.one().getLong(0) > 0L)
				.thenCompose(exists -> checkExistenceRules(change, exists)));
		}

		return Optional.empty();
	}

	private CompletionStage<Boolean> checkExistenceRules(Change<Document> change, boolean exists) {
		CompletableFuture<Boolean> result = new CompletableFuture<>();

		if (exists && change.isNew()) {
			result.completeExceptionally(new DuplicateItemException(change.getId().toString()));
		} else if (!exists && (change.isDirty() || change.isDeleted())) {
			result.completeExceptionally(new ItemNotFoundException(change.getId().toString()));
		} else {
			result.complete(Boolean.TRUE);
		}

		return result;
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
