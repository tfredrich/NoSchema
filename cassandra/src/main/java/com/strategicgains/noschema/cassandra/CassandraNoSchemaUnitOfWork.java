package com.strategicgains.noschema.cassandra;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.document.DocumentChange;
import com.strategicgains.noschema.cassandra.document.DocumentStatementGenerator;
import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.exception.DuplicateItemException;
import com.strategicgains.noschema.exception.ItemNotFoundException;
import com.strategicgains.noschema.unitofwork.Change;
import com.strategicgains.noschema.unitofwork.EntityState;
import com.strategicgains.noschema.unitofwork.UnitOfWork;
import com.strategicgains.noschema.unitofwork.UnitOfWorkChangeSet;
import com.strategicgains.noschema.unitofwork.UnitOfWorkCommitException;
import com.strategicgains.noschema.unitofwork.UnitOfWorkRollbackException;

public class CassandraNoSchemaUnitOfWork
implements UnitOfWork
{
    private final CqlSession session;
    private final DocumentStatementGenerator generator;
    private final UnitOfWorkChangeSet<Document> changeSet = new UnitOfWorkChangeSet<>();
    private final UnitOfWorkCommitStrategy commitStrategy;

    public CassandraNoSchemaUnitOfWork(CqlSession session, DocumentStatementGenerator statementGenerator)
    {
    	this(session, statementGenerator, UnitOfWorkType.LOGGED);
    }

    public CassandraNoSchemaUnitOfWork(CqlSession session, DocumentStatementGenerator statementGenerator, UnitOfWorkType unitOfWorkType)
    {
        this.session = Objects.requireNonNull(session);
        this.generator = Objects.requireNonNull(statementGenerator);
        this.commitStrategy = Objects.requireNonNull(unitOfWorkType)
        	.asCommitStrategy(session);
    }

	/**
	 * Registers a new entity that doesn't exist in the database and needs to be
	 * persisted during the transaction.
	 * 
	 * NOTE: Entities MUST be fully-populated across all identifier properties before
	 * registering them.
	 *
	 * @param entity the new entity to register.
	 */
	public CassandraNoSchemaUnitOfWork registerNew(String viewName, Document entity)
	{
		changeSet.registerChange(new DocumentChange(viewName, entity, EntityState.NEW));
		return this;
	}

	/**
	 * Registers an entity that has been updated during the transaction.
	 *
	 * @param entity the entity in its dirty state (after update).
	 */
	public CassandraNoSchemaUnitOfWork registerDirty(String viewName, Document entity)
	{
		changeSet.registerChange(new DocumentChange(viewName, entity, EntityState.DIRTY));
		return this;
	}

	/**
	 * Registers an entity for removal during the transaction.
	 *
	 * @param entity the entity in its clean state (before removal).
	 */
	public CassandraNoSchemaUnitOfWork registerDeleted(String viewName, Document entity)
	{
		changeSet.registerChange(new DocumentChange(viewName, entity, EntityState.DELETED));
		return this;
	}

	/**
	 * Registers an entity as clean, freshly-read from the database. These objects are used
	 * to determine deltas between dirty objects during commit().
	 * 
	 * NOTE: this method does NOT perform any copy operations so updating the object will
	 * change the copy that is registered as clean, making registration useless. Copy your
	 * own objects either before registering them as clean or before mutating them.
	 */
	public CassandraNoSchemaUnitOfWork registerClean(String viewName, Document entity)
	{
		changeSet.registerChange(new DocumentChange(viewName, entity, EntityState.CLEAN));
		return this;
	}

    @Override
	public void commit()
	throws UnitOfWorkCommitException
	{
		List<CompletionStage<Boolean>> existence = new ArrayList<>();
		List<BoundStatement> statements = new ArrayList<>();

		changeSet.stream().forEach(change -> {
			checkExistence(session, (DocumentChange) change).ifPresent(existence::add);
			generateStatementFor((DocumentChange) change).ifPresent(statements::add);
		});

		handleExistenceChecks(existence);

		commitStrategy
			.commit(statements)
			.join();
	}

	@Override
    public void rollback()
    throws UnitOfWorkRollbackException
    {
        commitStrategy.rollback();
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
		return changeSet.findClean(id);
	}
}
