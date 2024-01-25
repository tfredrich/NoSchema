package com.strategicgains.noschema.cassandra;

import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.cassandra.key.KeyDefinitionParser;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public class View
extends AbstractTable
{
	private PrimaryTable parent;

	public View()
	{
		super();
	}

	public View(PrimaryTable parent, String name, String keys)
	throws KeyDefinitionException
	{
		this(parent, name, KeyDefinitionParser.parse(keys), 0l);
	}

	public View(PrimaryTable parent, String name, KeyDefinition keys)
	{
		this(parent, name, keys, 0l);
	}

	public View(PrimaryTable parent, String name, String keys, long ttl)
	throws KeyDefinitionException
	{
		this(parent, name, KeyDefinitionParser.parse(keys), ttl);
	}

	public View(PrimaryTable parent, String name, KeyDefinition keys, long ttl)
	{
		super(parent.keyspace(), name, keys, ttl);
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
