package com.strategicgains.noschema.cassandra;

import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.exception.KeyDefinitionException;

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
