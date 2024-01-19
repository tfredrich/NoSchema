package com.strategicgains.noschema.unitofwork;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import com.strategicgains.noschema.Identifiable;

/**
 * This class provides transactional context for managing database changes. It allows
 * registering new entities, marking entities as "dirty" or "deleted". Commit and
 * rollback are left for extenders of the class.
 */
public abstract class AbstractUnitOfWork<T extends Identifiable>
implements UnitOfWork<T>
{
	// This set is used to keep track of new entities that need to be persisted
	// during the transaction.
	private Set<T> newObjects = new HashSet<>();

	// This set is used to keep track of entities that have been updated during the
	// transaction.
	private Set<T> dirtyObjects = new HashSet<>();

	// This set is used to keep track of entities that have been marked for deletion
	// during the transaction.
	private Set<T> deletedObjects = new HashSet<>();

	/**
	 * Returns a stream containing all the new entities.
	 */
	protected Stream<T> newObjects() {
		return Collections.unmodifiableSet(newObjects).stream();
	}

	/**
	 * Returns a stream containing all the dirty entities.
	 */
	protected Stream<T> dirtyObjects() {
		return Collections.unmodifiableSet(dirtyObjects).stream();
	}

	/**
	 * Returns a stream containing all the deleted entities.
	 */
	protected Stream<T> deletedObjects() {
		return Collections.unmodifiableSet(deletedObjects).stream();
	}

	/**
	 * Determines the state of an entity within this unit of work.
	 *
	 * @param entity the entity to determine the state for.
	 * @return the current state of the object.
	 */
	@Override
	public EntityState getState(T entity) {
		if (newObjects.contains(entity))
			return EntityState.NEW;
		if (dirtyObjects.contains(entity))
			return EntityState.DIRTY;
		if (deletedObjects.contains(entity))
			return EntityState.DELETED;
		return EntityState.UNKNOWN;
	}

	/**
	 * Registers a new entity that needs to be persisted during the transaction.
	 *
	 * @param entity the new entity to register.
	 */
	@Override
	public void registerNew(T entity) {
		newObjects.add(entity);
	}

	/**
	 * Registers an entity that has been updated during the transaction.
	 *
	 * @param entity the entity that has been updated.
	 */
	@Override
	public void registerDirty(T entity)
	{
		dirtyObjects.add(entity);
	}

	/**
	 * Registers an entity that has been marked for deletion during the transaction.
	 *
	 * @param entity the entity that has been marked for deletion.
	 */
	@Override
	public void registerDeleted(T entity) {
		deletedObjects.add(entity);
	}

    /**
     * Clears or deregisters all the registered entities and resets the unit of work to it's initial, empty state.
     */
    protected void reset()
	{
		newObjects.clear();
		dirtyObjects.clear();
		deletedObjects.clear();
	}
}
