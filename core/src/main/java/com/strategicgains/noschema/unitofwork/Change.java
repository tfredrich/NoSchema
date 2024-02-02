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
