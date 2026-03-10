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

import java.util.Objects;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

public class Change<T extends Identifiable>
{
	private final T entity;
	private EntityState state;

	public Change(T entity, EntityState state)
	{
		this.entity = entity;
		this.state = state;
	}

	public T getEntity()
	{
		return entity;
	}

	public Identifier getId()
	{
		return entity.getIdentifier();
	}

	public EntityState getState()
	{
		return state;
	}

	public boolean isNew()
	{
		return EntityState.NEW == state;
	}

	public boolean isClean()
	{
		return EntityState.CLEAN == state;
	}

	public boolean isDirty()
	{
		return EntityState.DIRTY == state;
	}

	public boolean isDeleted()
	{
		return EntityState.DELETED == state;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(getId(), state);
	}

	@Override
	public boolean equals(Object that)
	{
		return Objects.equals(this, that);
	}
}
