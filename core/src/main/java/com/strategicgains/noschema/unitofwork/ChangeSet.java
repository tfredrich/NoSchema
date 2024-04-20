package com.strategicgains.noschema.unitofwork;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

/**
 * ChangeSet provides an identity map to track the entities that
 * have changed and need to be persisted during the UnitOfWork.
 * 
 * @param <T> the type of entity being tracked.
 */
public class ChangeSet
{
	// This identity map is used to keep track of entities that have changed and
	// need to be persisted during the transaction.
	private Map<Identifier, Changes<?>> changes = new HashMap<>();

	/**
	 * Returns a stream containing all the changed entities (excluding CLEAN).
	 */
	@SuppressWarnings("unchecked")
	public <T extends Identifiable> Stream<Change<T>> stream()
	{
		return changes
            .values()
            .stream()
            .map(Changes::asOptional)
            .filter(Optional::isPresent)
            .map(c -> (Change<T>) c.get());
	}

	/**
	 * Registers a change with the ChangeSet. The change is registered
	 * based on the entity's identifier. Multiple changes registered for
	 * the same entity and change type will overwrite the previous change.
	 * 
	 * @param change the Change instance to register.
	 * @return this ChangeSet instance (for method chaining).
	 */
	public <T extends Identifiable> ChangeSet registerChange(Change<T> change)
	{
		Changes<T> changeSet = getChangesFor(change.getEntity());
		changeSet.put(change);
		return this;
	}

    /**
     * Clears or deregisters all the previously-registered changes and
     * resets the unit of work to it's initial, empty state.
     */
    public void reset()
	{
		changes.clear();
	}

	/**
	 * Returns the entity that has been registered as CLEAN with the given
	 * identifier. This clean entity is the entity that was retrieved from the
	 * data store and is considered the authoritative version of the entity that
	 * can be used to determine deltas during the UnitOfWork (e.g. if the keys
	 * changed).
	 * 
	 * @param id the identifier of the entity to retrieve.
	 * @return the entity that has been registered as CLEAN with the given identifier. May be null.
	 */
	public <T extends Identifiable> T getClean(Identifier id)
	{
		@SuppressWarnings("unchecked")
		Changes<T> s = (Changes<T>) changes.get(id);

		if (s != null)
		{
			Change<T> change = s.getClean();

			if (change != null) return change.getEntity();
		}

		return null;
	}

    /**
     * Returns the Changes instance for the given entity, creating it if it doesn't
     * already exist.
     * 
     * @param entity the entity whose Changes instance is being retrieved. Must have an identifier.
     * @return the Changes instance for the given entity. May be empty, but never null.
     */
	@SuppressWarnings("unchecked")
	private <T extends Identifiable> Changes<T> getChangesFor(T entity)
	{
		return (Changes<T>) changes.computeIfAbsent(entity.getIdentifier(), a -> new Changes<>());
	}

	/**
	 * Can hold two Change instances: one for the CLEAN state and one for any other state.
	 * 
	 * @param <I> the type of entity being tracked.
	 */
	private class Changes<T extends Identifiable>
	{
		private Change<T> clean;
		private Change<T> changed;

		public Changes<T> put(Change<T> change)
		{
			if (change.getState() == ChangeType.CLEAN)
			{
				clean = change;
			}
			else
			{
				changed = change;
			}

			return this;
		}

		public Change<T> getClean()
		{
			return clean;
		}

		public Optional<Change<T>> asOptional()
		{
			return Optional.ofNullable(changed);
		}
	}
}
