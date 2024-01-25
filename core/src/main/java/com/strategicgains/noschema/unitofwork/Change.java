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

	@Override
	public int hashCode()
	{
		return 11 * Objects.hash(getId(), state);
	}

	@Override
	public boolean equals(Object that)
	{
		return Objects.equals(this, that);
	}
}
