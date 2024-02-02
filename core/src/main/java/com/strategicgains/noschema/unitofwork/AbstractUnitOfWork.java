package com.strategicgains.noschema.unitofwork;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

/**
 * This class provides transactional context for managing database changes. It allows
 * registering new entities, marking entities as "dirty" or "deleted". Commit and
 * rollback are left for extenders of the class.
 */
public abstract class AbstractUnitOfWork<T extends Identifiable>
implements UnitOfWork<T>
{
	// This identity map is used to keep track of entities that have changed and
	// need to be persisted during the transaction.
	private Map<Identifier, ChangeSet<T>> changes = new HashMap<>();

	private ChangeFactory<T> factory = new DefaultChangeFactory<>();

	/**
	 * Returns a stream containing all the changed entities (excluding CLEAN).
	 */
	protected Stream<Change<T>> changes()
	{
	    return changes.values().stream().flatMap(s -> s.asChange().stream());
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
	public void registerNew(T entity)
	{
		ChangeSet<T> changeSet = getChangeSetFor(entity);
		changeSet.add(createChange(EntityState.NEW, entity));
	}

	/**
	 * Registers an entity that has been updated during the transaction.
	 *
	 * @param entity the entity in its dirty state (after update).
	 */
	@Override
	public void registerDirty(T entity)
	{
		ChangeSet<T> changeSet = getChangeSetFor(entity);
		changeSet.add(createChange(EntityState.DIRTY, entity));
	}

	/**
	 * Registers an entity that has been marked for deletion during the transaction.
	 *
	 * @param entity the entity that has been marked for deletion.
	 */
	@Override
	public void registerDeleted(T entity)
	{
		ChangeSet<T> changeSet = getChangeSetFor(entity);
		changeSet.add(createChange(EntityState.DELETED, entity));
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
	public void registerClean(T entity)
	{
		ChangeSet<T> changeSet = getChangeSetFor(entity);
		changeSet.add(createChange(EntityState.CLEAN, entity));
	}

    /**
     * Clears or deregisters all the previously-registered entities and resets the unit of work to it's initial, empty state.
     */
    protected void reset()
	{
		changes.clear();
	}

	private ChangeSet<T> getChangeSetFor(T entity)
	{
		return changes.computeIfAbsent(entity.getIdentifier(), a -> new ChangeSet<>());
	}

	public T findClean(Identifier id)
	{
		ChangeSet<T> s = changes.get(id);

		if (s != null)
		{
			Change<T> change = s.get(EntityState.CLEAN);

			if (change != null) return change.getEntity();
		}

		return null;
	}

	protected Change<T> createChange(EntityState state, T entity)
	{
		return factory.create(entity, state);
	}
}
