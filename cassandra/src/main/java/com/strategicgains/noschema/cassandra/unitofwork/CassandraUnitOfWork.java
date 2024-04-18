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
import com.strategicgains.noschema.cassandra.CassandraStatementFactory;
import com.strategicgains.noschema.exception.DuplicateItemException;
import com.strategicgains.noschema.exception.ItemNotFoundException;
import com.strategicgains.noschema.unitofwork.AbstractUnitOfWork;
import com.strategicgains.noschema.unitofwork.Change;
import com.strategicgains.noschema.unitofwork.ChangeType;
import com.strategicgains.noschema.unitofwork.UnitOfWork;
import com.strategicgains.noschema.unitofwork.UnitOfWorkCommitException;
import com.strategicgains.noschema.unitofwork.UnitOfWorkRollbackException;

public class CassandraUnitOfWork<T extends Identifiable>
extends AbstractUnitOfWork<T>
implements UnitOfWork<T>
{
    private final CqlSession session;
    private final CassandraStatementFactory<T> statementFactory;
    private final UnitOfWorkCommitStrategy commitStrategy;

    public CassandraUnitOfWork(CqlSession session, CassandraStatementFactory<T> statementFactory)
    {
    	this(session, statementFactory, UnitOfWorkType.LOGGED);
    }

    public CassandraUnitOfWork(CqlSession session, CassandraStatementFactory<T> statementFactory, UnitOfWorkType unitOfWorkType)
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
	public CassandraUnitOfWork<T> registerNew(String viewName, T entity)
	{
		registerChange(viewName, entity, ChangeType.NEW);
		return this;
	}

	/**
	 * Registers an entity that has been updated during the transaction.
	 *
	 * @param entity the entity in its dirty state (after update).
	 */
	public CassandraUnitOfWork<T> registerDirty(String viewName, T entity)
	{
		registerChange(viewName, entity, ChangeType.DIRTY);
		return this;
	}

	/**
	 * Registers an entity for removal during the transaction.
	 *
	 * @param entity the entity in its clean state (before removal).
	 */
	public CassandraUnitOfWork<T> registerDeleted(String viewName, T entity)
	{
		registerChange(viewName, entity, ChangeType.DELETED);
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
	public CassandraUnitOfWork<T> registerClean(String viewName, T entity)
	{
		registerChange(viewName, entity, ChangeType.CLEAN);
		return this;
	}

	private CassandraUnitOfWork<T> registerChange(String viewName, T entity, ChangeType changeType)
	{
		registerChange(new CassandraChange<>(viewName, entity, changeType));
		return this;
	}

    @Override
	public void commit()
	throws UnitOfWorkCommitException
	{
		List<CompletionStage<Boolean>> existence = new ArrayList<>();
		List<BoundStatement> statements = new ArrayList<>();

		changeStream().forEach(change -> {
			checkExistence(session, (CassandraChange<T>) change).ifPresent(existence::add);
			generateStatementFor((CassandraChange<T>) change).ifPresent(statements::add);
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

	private Optional<CompletionStage<Boolean>> checkExistence(CqlSession session, final CassandraChange<T> change)
	{
		String viewName = change.getView();

		if (statementFactory.isViewUnique(viewName))
		{
			return Optional.of(session.executeAsync(statementFactory.exists(viewName, change.getId()))
				.thenApply(r -> r.one().getLong(0) > 0L)
					.thenCompose(exists -> checkExistenceRules(change, exists)));
		}

		return Optional.empty();
	}

	private CompletionStage<Boolean> checkExistenceRules(Change<T> change, boolean exists)
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

	private Optional<BoundStatement> generateStatementFor(CassandraChange<T> change)
	{
		String viewName = change.getView();

		switch(change.getState())
		{
			case DELETED:
				return Optional.of(statementFactory.delete(viewName, change.getId()));
			case DIRTY:
				return Optional.of(statementFactory.update(viewName, change.getEntity()));
			case NEW:
				return Optional.of(statementFactory.create(viewName, change.getEntity()));
			default:
				break;
		}

		return Optional.empty();
	}
}
