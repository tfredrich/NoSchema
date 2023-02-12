package com.strategicgains.noschema.cassandra;

import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.cassandra.key.KeyDefinitionParser;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public class Table
{
	private static final String DEFAULT_KEYS = "id:uuid";

	private String keyspace;
	private String name;
	private KeyDefinition keys;

	// How long should the table's data live? (0 implies forever)
	private long ttl;

	public Table()
	{
		super();
	}

	/**
	 * Creates a new Table instance with a primary key of 'id' with a type of 'uuid'
	 * 
	 * @param keyspace
	 * @param name
	 * @throws KeyDefinitionException
	 */
	public Table(String keyspace, String name)
	throws KeyDefinitionException
	{
		this(keyspace, name, DEFAULT_KEYS);
	}

	public Table(String keyspace, String name, String keys)
	throws KeyDefinitionException
	{
		this(keyspace, name, KeyDefinitionParser.parse(keys));
	}

	public Table(String keyspace, String name, KeyDefinition keys)
	{
		this(keyspace, name, keys, 0l);
	}

	public Table(String keyspace, String name, KeyDefinition keys, long ttl)
	{
		this();
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

	@Override
	public String toString()
	{
		return String.format("%s.%s=(keys=%s, ttl=%l)", keyspace(), name(), keys(), ttl());
	}
}
