package com.strategicgains.noschema.unitofwork;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Stream;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

/**
 * ChangeSet provides an identity map to track the entities that
 * have changed and need to be persisted during the UnitOfWork.
 * 
 * @param <T> the type of entity being tracked.
 */
public class ChangeSet<T extends Identifiable>
{
	// This identity map is used to keep track of entities that have changed and
	// need to be persisted during the transaction.
	private Map<Identifier, Changes<T>> changes = new HashMap<>();

	/**
	 * Returns a stream containing all the changed entities (excluding CLEAN).
	 */
	public Stream<Change<T>> stream()
	{
	    return changes.values().stream().flatMap(s -> s.asChange().stream());
	}

	/**
	 * Registers a change with the ChangeSet. The change is registered
	 * based on the entity's identifier. Multiple changes registered for
	 * the same entity and change type will overwrite the previous change.
	 * 
	 * @param change the Change instance to register.
	 * @return this ChangeSet instance (for method chaining).
	 */
	public ChangeSet<T> registerChange(Change<T> change)
	{
		Changes<T> changeSet = getChangesFor(change.getEntity());
		changeSet.add(change);
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
	public T findClean(Identifier id)
	{
		Changes<T> s = changes.get(id);

		if (s != null)
		{
			Change<T> change = s.get(ChangeType.CLEAN);

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
	private Changes<T> getChangesFor(T entity)
	{
		return changes.computeIfAbsent(entity.getIdentifier(), a -> new Changes<>());
	}

	/**
	 * Provides an EnumMap of Change instances for the given entity, keyed by ChangeType.
	 * 
	 * @param <I> the type of entity being tracked.
	 */
	private class Changes<I extends Identifiable>
	{
		Map<ChangeType, Change<I>> changesByState = new EnumMap<>(ChangeType.class);

		public Changes<I> add(Change<I> change)
		{
			changesByState.put(change.getState(), change);
			return this;
		}

		public Change<I> get(ChangeType state)
		{
			return changesByState.get(state);
		}

		public Optional<Change<I>> asChange()
		{
			return changesByState.entrySet()
				.stream()
				.filter(e -> (e.getKey() != ChangeType.CLEAN))
				.findFirst()
				.map(Entry::getValue);
		}
	}
}
