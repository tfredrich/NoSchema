package com.strategicgains.noschema.unitofwork;

import java.util.stream.Stream;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

public abstract class AbstractUnitOfWork<T extends Identifiable>
implements UnitOfWork<T>
{
    private final ChangeSet<T> changeSet = new ChangeSet<>();

	@Override
	public UnitOfWork<T> registerChange(Change<T> change)
	{
		changeSet.registerChange(change);
		return this;
	}

	public T readClean(Identifier id)
	{
		return changeSet.findClean(id);
	}

	protected Stream<Change<T>> changeStream()
	{
		return changeSet.stream();
	}
}
