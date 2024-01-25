package com.strategicgains.noschema.unitofwork;

import java.util.Objects;

import com.strategicgains.noschema.Identifiable;

public class DirtyChange<T extends Identifiable>
extends Change<T>
{
	private T original;

	public DirtyChange(T original, T dirty)
	{
		super(dirty, EntityState.DIRTY);
		setOriginal(original);
	}

	public T getOriginal()
	{
		return original;
	}

	private void setOriginal(T original)
	{
		this.original = original;
	}

	public boolean identityEqual()
	{
		return getId().equals(original.getIdentifier());
	}

	public boolean identityChanged()
	{
		return !identityEqual();
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(getId(), getState(), original);
	}

	@Override
	public boolean equals(Object that)
	{
		return Objects.equals(this, that);
	}
}
