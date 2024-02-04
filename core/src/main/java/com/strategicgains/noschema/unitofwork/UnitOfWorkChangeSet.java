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
public class UnitOfWorkChangeSet<T extends Identifiable>
{
	// This identity map is used to keep track of entities that have changed and
	// need to be persisted during the transaction.
	private Map<Identifier, EntityChangeSet<T>> changes = new HashMap<>();

	/**
	 * Returns a stream containing all the changed entities (excluding CLEAN).
	 */
	public Stream<Change<T>> stream()
	{
	    return changes.values().stream().flatMap(s -> s.asChange().stream());
	}

	public UnitOfWorkChangeSet<T> registerChange(Change<T> change)
	{
		EntityChangeSet<T> changeSet = getChangeSetFor(change.getEntity());
		changeSet.add(change);
		return this;
	}

    /**
     * Clears or deregisters all the previously-registered changes and resets the unit of work to it's initial, empty state.
     */
    public void reset()
	{
		changes.clear();
	}

	private EntityChangeSet<T> getChangeSetFor(T entity)
	{
		return changes.computeIfAbsent(entity.getIdentifier(), a -> new EntityChangeSet<>());
	}

	public T findClean(Identifier id)
	{
		EntityChangeSet<T> s = changes.get(id);

		if (s != null)
		{
			Change<T> change = s.get(EntityState.CLEAN);

			if (change != null) return change.getEntity();
		}

		return null;
	}
}
