package com.strategicgains.noschema.cassandra;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.exception.KeyDefinitionException;

/**
 * A View is a denormalized table that is created to support a specific query and
 * contains all the same data that the primary table contains but with a different
 * key structure. The key structure of a view is defined by the query that it supports.
 * 
 * A View is a SecondaryTable that is read-only and is not updated directly. Instead,
 * it is updated when the parent table is updated (via the UnitOfWork).
 * 
 * @see SecondaryTable
 * @see Index
 * @see PrimaryTable
 * @author Todd Fredrich
 */
public class View<T extends Identifiable>
extends SecondaryTable<T>
{
	public View()
	{
		super();
	}

	public View(PrimaryTable<T> parent, String viewName, KeyDefinition keys, long ttl)
	{
		super(parent, viewName, keys, ttl);
	}

	public View(PrimaryTable<T> parent, String viewName, KeyDefinition keys)
	{
		super(parent, viewName, keys);
	}

	public View(PrimaryTable<T> parent, String viewName, String keys, long ttl)
	throws KeyDefinitionException
	{
		super(parent, viewName, keys, ttl);
	}

	public View(PrimaryTable<T> parent, String viewName, String keys)
	throws KeyDefinitionException
	{
		super(parent, viewName, keys);
	}

	@Override
	public View<T> withRowMapper(RowMapper<T> rowMapper)
	{
		super.withRowMapper(rowMapper);
		return this;
	}

	@Override
	public boolean hasRowMapper()
	{
		return (super.hasRowMapper() || getParent().hasRowMapper());
	}

	@Override
	public RowMapper<T> rowMapper()
	{
		return (super.hasRowMapper() ? super.rowMapper() : getParent().rowMapper());
	}
}
