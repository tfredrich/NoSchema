package com.strategicgains.noschema.cassandra;

import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.cassandra.key.KeyDefinitionParser;
import com.strategicgains.noschema.exception.KeyDefinitionException;

/**
 * A SecondaryTable is a child of a PrimaryTable. It is a table that is
 * created to support a query that is not supported by the parent table such as
 * views and indexes. They are read-only and are not updated directly. Instead,
 * they are updated when the parent table is updated (via the UnitOfWork).
 * 
 * A View is a denormalized table that is created to support a specific query and
 * contains all the same data that the primary table contains but with a different
 * key structure. The key structure of a view is defined by the query that it supports.
 * 
 * An Index is simply an Identifier pointing to another Identifier (in the PrimaryTable).
 * 
 * @author Todd Fredrich
 * @see View
 * @see Index
 */
public abstract class SecondaryTable
extends AbstractTable
{
	private PrimaryTable parent;

	protected SecondaryTable()
	{
		super();
	}

	protected SecondaryTable(PrimaryTable parent, String tableName, String keys)
	throws KeyDefinitionException
	{
		this(parent, tableName, KeyDefinitionParser.parse(keys), 0l);
	}

	protected SecondaryTable(PrimaryTable parent, String tableName, KeyDefinition keys)
	{
		this(parent, tableName, keys, 0l);
	}

	protected SecondaryTable(PrimaryTable parent, String tableName, String keys, long ttl)
	throws KeyDefinitionException
	{
		this(parent, tableName, KeyDefinitionParser.parse(keys), ttl);
	}

	protected SecondaryTable(PrimaryTable parent, String tableName, KeyDefinition keys, long ttl)
	{
		super(parent.keyspace(), tableName, keys, ttl);
		setParent(parent);
	}

	public PrimaryTable getParent()
	{
		return parent;
	}

	public SecondaryTable setParent(PrimaryTable parent)
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
