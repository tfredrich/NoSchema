package com.strategicgains.noschema.cassandra.unitofwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.MutationStatementFactory;
import com.strategicgains.noschema.exception.DuplicateItemException;
import com.strategicgains.noschema.exception.ItemNotFoundException;
import com.strategicgains.noschema.unitofwork.Change;
import com.strategicgains.noschema.unitofwork.EntityState;
import com.strategicgains.noschema.unitofwork.RepositoryUnitOfWork;
import com.strategicgains.noschema.unitofwork.UnitOfWorkChangeSet;
import com.strategicgains.noschema.unitofwork.UnitOfWorkCommitException;
import com.strategicgains.noschema.unitofwork.UnitOfWorkRollbackException;

public class CassandraUnitOfWork
implements RepositoryUnitOfWork
{
    private final CqlSession session;
    private final MutationStatementFactory statementFactory;
    private final UnitOfWorkChangeSet changeSet = new UnitOfWorkChangeSet();
    private final UnitOfWorkCommitStrategy commitStrategy;

    public CassandraUnitOfWork(CqlSession session, MutationStatementFactory statementFactory)
    {
    	this(session, statementFactory, UnitOfWorkType.LOGGED);
    }

    public CassandraUnitOfWork(CqlSession session, MutationStatementFactory statementFactory, UnitOfWorkType unitOfWorkType)
    {
        this.session = Objects.requireNonNull(session);
        this.statementFactory = Objects.requireNonNull(statementFactory);
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
    @Override
	public CassandraUnitOfWork registerNew(String viewName, Identifiable entity)
	{
		changeSet.registerChange(new CassandraChange<>(viewName, entity, EntityState.NEW));
		return this;
	}

	/**
	 * Registers an entity that has been updated during the transaction.
	 *
	 * @param entity the entity in its dirty state (after update).
	 */
    @Override
	public CassandraUnitOfWork registerDirty(String viewName, Identifiable entity)
	{
		changeSet.registerChange(new CassandraChange<>(viewName, entity, EntityState.DIRTY));
		return this;
	}

	/**
	 * Registers an entity for removal during the transaction.
	 *
	 * @param entity the entity in its clean state (before removal).
	 */
    @Override
	public CassandraUnitOfWork registerDeleted(String viewName, Identifiable entity)
	{
		changeSet.registerChange(new CassandraChange<>(viewName, entity, EntityState.DELETED));
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
    @Override
	public CassandraUnitOfWork registerClean(String viewName, Identifiable entity)
	{
		changeSet.registerChange(new CassandraChange<>(viewName, entity, EntityState.CLEAN));
		return this;
	}

    @Override
	public void commit()
	throws UnitOfWorkCommitException
		{
			List<CompletionStage<Boolean>> existence = new ArrayList<>();
			List<BoundStatement> statements = new ArrayList<>();

			changeSet.stream()
				.map(change -> (CassandraChange<?>) change)
				.forEach(change -> {
					checkExistence(session, change).ifPresent(existence::add);
					generateStatementFor(change).ifPresent(statements::add);
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

	private Optional<CompletionStage<Boolean>> checkExistence(CqlSession session, final CassandraChange<?> change)
	{
		String viewName = change.getView();

		if (statementFactory.isViewUnique(viewName))
		{
			return Optional.of(session.executeAsync(statementFactory.exists(viewName, change.getIdentifier()))
				.thenApply(r -> r.one().getLong(0) > 0L)
					.thenCompose(exists -> checkExistenceRules(change, exists)));
		}

		return Optional.empty();
	}

	private CompletionStage<Boolean> checkExistenceRules(Change<?> change, boolean exists)
	{
		CompletableFuture<Boolean> result = new CompletableFuture<>();

		if (exists && change.isNew())
		{
			result.completeExceptionally(new DuplicateItemException(change.getIdentifier().toString()));
		}
		else if (!exists && (change.isDirty() || change.isDeleted()))
		{
			result.completeExceptionally(new ItemNotFoundException(change.getIdentifier().toString()));
		}
		else
		{
			result.complete(Boolean.TRUE);
		}

		return result;
	}

	private Optional<BoundStatement> generateStatementFor(CassandraChange<?> change)
	{
		String viewName = change.getView();

				switch(change.getState())
				{
					case DELETED:
						return Optional.of(statementFactory.delete(viewName, change.getIdentifier()));
					case DIRTY:
						return Optional.of(statementFactory.update(viewName, change.getEntity()));
					case NEW:
						return Optional.of(statementFactory.create(viewName, change.getEntity()));
					default:
						break;
				}

		return Optional.empty();
	}

	public Identifiable readClean(Identifier id)
	{
		return changeSet.findClean(id);
	}
}
