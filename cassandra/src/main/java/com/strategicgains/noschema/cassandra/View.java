package com.strategicgains.noschema.cassandra;

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
public class View
extends SecondaryTable
{
	public View()
	{
		super();
	}

	public View(PrimaryTable parent, String viewName, KeyDefinition keys, long ttl)
	{
		super(parent, viewName, keys, ttl);
	}

	public View(PrimaryTable parent, String viewName, KeyDefinition keys)
	{
		super(parent, viewName, keys);
	}

	public View(PrimaryTable parent, String viewName, String keys, long ttl)
	throws KeyDefinitionException
	{
		super(parent, viewName, keys, ttl);
	}

	public View(PrimaryTable parent, String viewName, String keys)
	throws KeyDefinitionException
	{
		super(parent, viewName, keys);
	}
}
