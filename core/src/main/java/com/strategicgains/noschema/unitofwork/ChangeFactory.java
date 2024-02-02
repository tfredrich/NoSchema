package com.strategicgains.noschema.unitofwork;

import com.strategicgains.noschema.Identifiable;

public interface ChangeFactory<T extends Identifiable>
{
	Change<T> create(T entity, EntityState state);
}
