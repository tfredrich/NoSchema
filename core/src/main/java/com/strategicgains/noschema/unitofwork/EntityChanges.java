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

public class EntityChanges<T extends Identifiable>
{
	Map<EntityState, Change<T>> changes = new EnumMap<>(EntityState.class);

	public EntityChanges<T> add(Change<T> change)
	{
		changes.put(change.getState(), change);
		return this;
	}

	public Change<T> get(EntityState state)
	{
		return changes.get(state);
	}

	public Optional<Change<T>> asChange()
	{
		return changes.entrySet()
			.stream()
			.filter(e -> (e.getKey() != EntityState.CLEAN))
			.findFirst()
			.map(Entry::getValue);
	}
}
