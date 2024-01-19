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
	// This set is used to keep track of entities that have changed and
	// need to be persisted during the transaction.
	private Set<Change<T>> changes = new HashSet<>();

	/**
	 * Returns a stream containing all the changed entities.
	 */
	protected Stream<Change<T>> changes()
	{
		return Collections.unmodifiableSet(changes).stream();
	}

	/**
	 * Registers a new entity that needs to be persisted during the transaction.
	 *
	 * @param entity the new entity to register.
	 */
	@Override
	public void registerNew(T entity) {
		changes.add(new Change<>(entity, EntityState.NEW));
	}

	/**
	 * Registers an entity that has been updated during the transaction.
	 *
	 * @param entity the entity that has been updated.
	 */
	@Override
	public void registerDirty(T entity)
	{
		changes.add(new Change<>(entity, EntityState.DIRTY));
	}

	/**
	 * Registers an entity that has been marked for deletion during the transaction.
	 *
	 * @param entity the entity that has been marked for deletion.
	 */
	@Override
	public void registerDeleted(T entity) {
		changes.add(new Change<>(entity, EntityState.DELETED));
	}

    /**
     * Clears or deregisters all the registered entities and resets the unit of work to it's initial, empty state.
     */
    protected void reset()
	{
		changes.clear();
	}
}
