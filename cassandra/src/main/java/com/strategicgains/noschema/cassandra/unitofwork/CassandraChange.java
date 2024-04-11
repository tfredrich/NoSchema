package com.strategicgains.noschema.cassandra.unitofwork;

import java.util.Objects;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.unitofwork.Change;
import com.strategicgains.noschema.unitofwork.ChangeType;

public class CassandraChange<T extends Identifiable>
extends Change<T>
{
	private String view;

	public CassandraChange(String view, T entity, ChangeType state)
	{
		super(entity, state);
		this.view = view;
	}

	public String getView()
	{
		return view;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() + Objects.hash(view);
	}

	@Override
	public boolean equals(Object that)
	{
		return super.equals(that) && Objects.equals(this.view, ((CassandraChange<T>) that).view);
	}
}
