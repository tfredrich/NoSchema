package com.strategicgains.noschema.unitofwork;

import com.strategicgains.noschema.Identifiable;

public class DefaultChangeFactory<T extends Identifiable>
implements ChangeFactory<T>
{
	@Override
	public Change<T> create(T entity, EntityState state)
	{
		return new Change<>(entity, state);
	}
}
