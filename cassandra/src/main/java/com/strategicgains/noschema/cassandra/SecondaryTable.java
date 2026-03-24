package com.strategicgains.noschema.cassandra;

import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.cassandra.key.KeyDefinitionParser;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.exception.KeyDefinitionException;

/**
 * A SecondaryTable is an abstract class describing a child of a PrimaryTable.
 * It is a table that is created to support a query that is not supported by
 * the parent table such as views and indexes. They are read-only and are not
 * updated directly. Instead, they are updated when the parent table is
 * updated (via the UnitOfWork).
 * 
 * @author Todd Fredrich
 * @see View
 * @see Index
 */
public abstract class SecondaryTable<T extends Identifiable>
extends AbstractTable<T>
{
	private PrimaryTable<T> parent;

	protected SecondaryTable()
	{
		super();
	}

	protected SecondaryTable(PrimaryTable<T> parent, String tableName, String keys)
	throws KeyDefinitionException
	{
		this(parent, tableName, KeyDefinitionParser.parse(keys), 0l);
	}

	protected SecondaryTable(PrimaryTable<T> parent, String tableName, KeyDefinition keys)
	{
		this(parent, tableName, keys, 0l);
	}

	protected SecondaryTable(PrimaryTable<T> parent, String tableName, String keys, long ttl)
	throws KeyDefinitionException
	{
		this(parent, tableName, KeyDefinitionParser.parse(keys), ttl);
	}

	protected SecondaryTable(PrimaryTable<T> parent, String tableName, KeyDefinition keys, long ttl)
	{
		super(parent.keyspace(), tableName, keys, ttl);
		setParent(parent);
	}

	public PrimaryTable<T> getParent()
	{
		return parent;
	}

	public SecondaryTable<T> setParent(PrimaryTable<T> parent)
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
