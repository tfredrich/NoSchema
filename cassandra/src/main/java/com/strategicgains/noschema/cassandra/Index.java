package com.strategicgains.noschema.cassandra;

import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.exception.KeyDefinitionException;

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
