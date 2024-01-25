package com.strategicgains.noschema.cassandra;

import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public abstract class AbstractTable
{
	// The Cassandra keyspace in which this table is stored.
	private String keyspace;

	// The name of the table.
	private String name;

	// The keys that identify the primary identifier 
	private KeyDefinition keys;

	// How long should the table's data live? (0 implies forever)
	private long ttl;

	protected AbstractTable()
	{
		super();
	}

	protected AbstractTable(String keyspace, String name, KeyDefinition keys, long ttl)
	{
		super();
		keyspace(keyspace);
		name(name);
		keys(keys);
		ttl(ttl);
	}

	public String keyspace()
	{
		return keyspace;
	}

	public boolean hasKeyspace()
	{
		return (keyspace != null);
	}

	public void keyspace(String keyspace)
	{
		this.keyspace = keyspace;
	}

	public boolean hasName()
	{
		return (name != null);
	}

	public String name()
	{
		return name;
	}

	public void name(String name)
	{
		this.name = name;
	}

	public KeyDefinition keys()
	{
		return keys;
	}

	public void keys(KeyDefinition keys)
	{
		this.keys = keys;
	}

	public long ttl()
	{
		return ttl;
	}

	public void ttl(long ttl)
	{
		this.ttl = ttl;
	}

	public boolean isUnique()
	{
		return keys.isUnique();
	}

	public Identifier getIdentifier(Object entity)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		return keys.identifier(entity);
	}

	@Override
	public String toString()
	{
		return String.format("%s.%s=(keys=%s, ttl=%l)", keyspace(), name(), keys().toString(), ttl());
	}

	public String asTableName()
	{
		return name();
	}
}
