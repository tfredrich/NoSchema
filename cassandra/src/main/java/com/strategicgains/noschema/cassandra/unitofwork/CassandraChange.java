package com.strategicgains.noschema.cassandra.unitofwork;

import java.util.Objects;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.unitofwork.Change;
import com.strategicgains.noschema.unitofwork.ChangeType;

public class CassandraChange<T extends Identifiable>
extends Change<T>
{
	private String tableName;

	public CassandraChange(String tableName, T entity, ChangeType state)
	{
		super(entity, state);
		this.tableName = tableName;
	}

	public String getTableName()
	{
		return tableName;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() + Objects.hash(tableName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object that)
	{
		return super.equals(that) && Objects.equals(this.tableName, ((CassandraChange<T>) that).tableName);
	}
}
