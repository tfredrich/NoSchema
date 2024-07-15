package com.strategicgains.noschema.cassandra;

import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.exception.KeyDefinitionException;

/**
 * An Index is simply an Identifier (key structure) pointing to another Identifier (in the PrimaryTable).
 * 
 * An Index table is created to support a specific query but only contains the primary key of the parent
 * table as its data. Consequently, an Index table is read-only and is not updated directly. Instead, it is
 * updated when the parent table is updated (via the UnitOfWork).
 * 
 * Indexes incur a performance penalty when reading multiple rows from the parent table. They are not
 * recommended for high-throughput applications as reading rows from the index table will require a
 * subsequent read from the parent table to retrieve the actual data.
 * 
 * @see SecondaryTable
 * @see View
 * @see PrimaryTable
 * @author Todd Fredrich
 */
public class Index
extends SecondaryTable
{
	public Index()
	{
		super();
	}

	public Index(PrimaryTable parent, String tableName, String keys)
	throws KeyDefinitionException
	{
		super(parent, tableName, keys);
	}

	public Index(PrimaryTable parent, String tableName, KeyDefinition keys)
	{
		super(parent, tableName, keys);
	}

	public Index(PrimaryTable parent, String tableName, String keys, long ttl)
	throws KeyDefinitionException
	{
		super(parent, tableName, keys, ttl);
	}

	public Index(PrimaryTable parent, String tableName, KeyDefinition keys, long ttl)
	{
		super(parent, tableName, keys, ttl);
	}

	@Override
	public boolean isIndex()
	{
		return true;
	}
}
