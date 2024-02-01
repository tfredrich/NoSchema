package com.strategicgains.noschema.unitofwork;

import java.util.Objects;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

public class Change<T extends Identifiable>
{
	private final T entity;
	private EntityState state;
	private boolean isCommitted;

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

	public boolean isCommitted()
	{
		return isCommitted;
	}

	public void setCommitted(boolean value)
	{
		this.isCommitted = value;
	}

	@Override
	public int hashCode()
	{
		return 11 * Objects.hash(getId(), state, isCommitted);
	}

	@Override
	public boolean equals(Object that)
	{
		return Objects.equals(this, that);
	}
}
