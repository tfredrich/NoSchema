package com.strategicgains.noschema.unitofwork;

import java.util.stream.Stream;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

public abstract class AbstractUnitOfWork
implements UnitOfWork
{
    private final ChangeSet changeSet = new ChangeSet();

	@Override
	public <T extends Identifiable> UnitOfWork registerChange(Change<T> change)
	{
		changeSet.registerChange(change);
		return this;
	}

	public <T extends Identifiable> T readClean(Identifier id)
	{
		return changeSet.getClean(id);
	}

	protected <T extends Identifier> Stream<Change<T>> changeStream()
	{
		return changeSet.stream();
	}
}
