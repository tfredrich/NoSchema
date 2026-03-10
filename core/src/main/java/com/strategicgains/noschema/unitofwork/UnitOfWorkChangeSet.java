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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

/**
 * This class provides transactional context for managing database changesByState. It allows
 * registering new entities, marking entities as "dirty" or "deleted". Commit and
 * rollback are left for extenders of the class.
 */
public class UnitOfWorkChangeSet
{
	// This identity map is used to keep track of entities that have changed and
	// need to be persisted during the transaction.
	private Map<Identifier, EntityChanges<?>> changesByIdentifier = new HashMap<>();

	/**
	 * Returns a stream containing all the changed entities (excluding CLEAN).
	 */
	public Stream<Change<?>> stream()
	{
	    return changesByIdentifier.values().stream().flatMap(s -> s.asChange().stream());
	}

	public <T extends Identifiable> UnitOfWorkChangeSet registerChange(Change<T> change)
	{
		@SuppressWarnings("unchecked")
		EntityChanges<T> entityChanges = (EntityChanges<T>) changesByIdentifier.computeIfAbsent(change.getIdentifier(), a -> new EntityChanges<>());
		entityChanges.add(change);
		return this;
	}

    /**
     * Clears or deregisters all the previously-registered changesByIdentifier in the change set and
     * resets the unit of work to it's initial, empty state.
     */
    public void reset()
	{
		changesByIdentifier.clear();
	}

	public <T extends Identifiable> T findClean(Identifier id)
	{
		@SuppressWarnings("unchecked")
		EntityChanges<T> s = (EntityChanges<T>) changesByIdentifier.get(id);

		if (s != null)
		{
			Change<T> change = s.get(EntityState.CLEAN);

			if (change != null) return change.getEntity();
		}

		return null;
	}
}
