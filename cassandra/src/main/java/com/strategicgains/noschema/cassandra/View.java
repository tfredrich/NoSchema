package com.strategicgains.noschema.cassandra;

import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.cassandra.key.KeyDefinitionParser;
import com.strategicgains.noschema.exception.KeyDefinitionException;

/**
 * A View is a table that is a child of a PrimaryTable. It is a table that is
 * created to support a query that is not supported by the parent table. Views
 * are read-only and are not updated directly. They are updated when the parent
 * table is updated (via the UnitOfWork).
 * 
 * A View is a denormalized table that is created to support a specific query and
 * contains all the same data that the primary table contains but with a different
 * key structure. The key structure of a view is defined by the query that it supports.
 * 
 * @author Todd Fredrich
 */
public class View
extends AbstractTable
{
	private PrimaryTable parent;

	public View()
	{
		super();
	}

	public View(PrimaryTable parent, String viewName, String keys)
	throws KeyDefinitionException
	{
		this(parent, viewName, KeyDefinitionParser.parse(keys), 0l);
	}

	public View(PrimaryTable parent, String viewName, KeyDefinition keys)
	{
		this(parent, viewName, keys, 0l);
	}

	public View(PrimaryTable parent, String viewName, String keys, long ttl)
	throws KeyDefinitionException
	{
		this(parent, viewName, KeyDefinitionParser.parse(keys), ttl);
	}

	public View(PrimaryTable parent, String viewName, KeyDefinition keys, long ttl)
	{
		super(parent.keyspace(), viewName, keys, ttl);
		parent(parent);
	}

	public PrimaryTable parent()
	{
		return parent;
	}

	public View parent(PrimaryTable parent)
	{
		this.parent = parent;
		return this;
	}

	@Override
	public String asTableName()
	{
		return String.format("%s_%s", parent.name(), name());
	}
}
