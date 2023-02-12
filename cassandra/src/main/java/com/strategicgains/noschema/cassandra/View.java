package com.strategicgains.noschema.cassandra;

import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.cassandra.key.KeyDefinitionParser;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public class View
extends Table
{
	private Table parent;

	public View()
	{
		super();
	}

	public View(Table parent, String name, String keys)
	throws KeyDefinitionException
	{
		this(parent, name, KeyDefinitionParser.parse(keys));
	}

	public View(Table parent, String name, KeyDefinition keys)
	{
		this(parent, name, keys, 0l);
	}

	public View(Table parent, String name, KeyDefinition keys, long ttl)
	{
		super(parent.keyspace(), name, keys, ttl);
		parent(parent);
	}

	public Table parent()
	{
		return parent;
	}

	public void parent(Table parent)
	{
		this.parent = parent;
	}

	@Override
	public String name()
	{
		return parent.name() + "_" + super.name();
	}
}
