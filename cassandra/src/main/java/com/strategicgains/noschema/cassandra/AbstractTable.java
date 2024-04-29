package com.strategicgains.noschema.cassandra;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
	//TODO: Implement TTL in Cassandra.
	private long ttl;

	// Any extra name/value tag-alongs the customer wants to include.
	//TODO: Handle null on persistence
	private Map<String, String> metadata;

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

	public Map<String, String> metadata()
	{
		return (metadata != null ? Collections.unmodifiableMap(metadata) : Collections.emptyMap());
	}

	public AbstractTable withMetadata(String name, String value)
	{
		if (metadata == null)
		{
			metadata = new HashMap<>();
		}

		metadata.put(name, value);
		return this;
	}

	public Identifier getIdentifier(Object entity)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		return keys.identifier(entity);
	}

	@Override
	public String toString()
	{
		return String.format("%s.%s:(keys=%s, ttl=%d)", keyspace(), name(), keys(), ttl());
	}

	public String asTableName()
	{
		return name();
	}

	public boolean isIndex()
	{
		return false;
	}
}
