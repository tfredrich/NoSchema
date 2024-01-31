package com.strategicgains.noschema.unitofwork;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import com.strategicgains.noschema.Identifiable;

public class ChangeSet<T extends Identifiable>
{
	Map<EntityState, T> changes = new EnumMap<>(EntityState.class);

	public ChangeSet<T> add(EntityState state, T entity)
	{
		changes.put(state, entity);
		return this;
	}

	public Optional<Change<T>> asChange()
	{
		return changes.entrySet().stream().map(e -> {
			switch (e.getKey())
			{
			case DIRTY:
				return new DirtyChange<>(changes.get(EntityState.CLEAN), changes.get(EntityState.DIRTY));
			case NEW:
				return new Change<>(changes.get(EntityState.NEW), EntityState.NEW);
			case DELETED:
				return new Change<>(changes.get(EntityState.DELETED), EntityState.DELETED);
			default:
				return null;
			}
		}).findFirst();
	}
}
