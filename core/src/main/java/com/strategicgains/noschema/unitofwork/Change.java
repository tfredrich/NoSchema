package com.strategicgains.noschema.unitofwork;

import java.util.Objects;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

/**
 * Represents a change to an entity.  The change includes the entity itself
 * and the state of the entity (NEW, CLEAN, DIRTY, DELETED).
 * 
 * @param <T>
 */
public class Change<T extends Identifiable>
{
	private final T entity;
	private ChangeType state;

	public Change(T entity, ChangeType state)
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

	public ChangeType getState()
	{
		return state;
	}

	public boolean isNew()
	{
		return ChangeType.NEW == state;
	}

	public boolean isClean()
	{
		return ChangeType.CLEAN == state;
	}

	public boolean isDirty()
	{
		return ChangeType.DIRTY == state;
	}

	public boolean isDeleted()
	{
		return ChangeType.DELETED == state;
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
