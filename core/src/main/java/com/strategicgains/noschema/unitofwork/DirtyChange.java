package com.strategicgains.noschema.unitofwork;

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
}
