/*
    Copyright 2024-2026, Strategic Gains, Inc.

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package com.strategicgains.noschema.unitofwork;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.strategicgains.noschema.Identifiable;

/**
 * Because an entity can be registered with a UnitOfWork for both CLEAN  and a dirty state, this class
 * tracks and maintains both, allowing the UnitOfWork to determine the final state of the entity at
 * commit or rollback time.
 * 
 * The rules for registering changes are:
 * - If an entity is registered as CLEAN or NEW, any previous dirty state will be ignored (e.g., removed).
 * - If an entity is registered as NEW, it cannot be registered with any other state. To update the state of a NEW entity, it must be re-registered as NEW.
 * - If an entity is registered with a dirty state (e.g., DIRTY or DELETED), it must already be registered as CLEAN. Otherwise, an IllegalStateException will be thrown.
 * 
 * @param <T> the type of entity being tracked.
 */
public class EntityChanges<T extends Identifiable>
{
	Map<EntityState, Change<T>> changesByState = new EnumMap<>(EntityState.class);

	/**
	 * Registers a change for the entity. If a change is already registered for the same state, it will be replaced.
	 * If a change is being registered for a state that conflicts with the existing state (e.g., registering a dirty
	 * state for an entity that is not registered as CLEAN), an IllegalStateException will be thrown.
	 * 
	 * @param change the change to register for the entity.
	 * @return this EntityChanges instance, allowing for method chaining.
	 * @throws IllegalStateException if the change being registered conflicts with the existing state of the entity.
	 */
	public EntityChanges<T> add(Change<T> change)
	{
		if (change.isClean() || change.isNew())
		{
			// If the entity is being registered as CLEAN or NEW, we want to ignore any previous dirty state.
			changesByState.clear();
		}
		else if (changesByState.containsKey(EntityState.NEW))
		{
			throw new IllegalStateException("Cannot register a change for a NEW entity. Re-register it as NEW to update the state.");
		}
		else if (!changesByState.containsKey(EntityState.CLEAN))
		{
			throw new IllegalStateException("Cannot register a dirty state for an entity that is not registered as CLEAN.");
		}

		changesByState.put(change.getState(), change);
		return this;
	}

	/**
	 * Returns the registered change for the specified state, if any.
	 * If no change is registered for the specified state, null is returned.
	 * 
	 * @param state the state for which to retrieve the change.
	 * @return the registered change for the specified state, or null if no change is registered for that state.
	 */
	public Change<T> get(EntityState state)
	{
		return changesByState.get(state);
	}

	/**
	 * Returns an Optional containing the first non-CLEAN change for the entity, if any.
	 * If the entity is only registered as CLEAN, an empty Optional is returned.
	 * 
	 * @return an Optional containing the first non-CLEAN change for the entity, or an empty Optional if only CLEAN is registered.
	 */
	public Optional<Change<T>> asChange()
	{
		return changesByState.entrySet()
			.stream()
			.filter(e -> (e.getKey() != EntityState.CLEAN))
			.findFirst()
			.map(Entry::getValue);
	}
}
