package com.strategicgains.noschema.unitofwork;

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
}
