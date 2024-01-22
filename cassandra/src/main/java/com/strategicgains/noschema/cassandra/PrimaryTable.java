package com.strategicgains.noschema.cassandra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.cassandra.key.KeyDefinitionParser;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public class PrimaryTable
extends AbstractTable
{
	private static final String DEFAULT_KEYS = "id:uuid";
	private List<View> views;

	public PrimaryTable()
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
	public PrimaryTable(String keyspace, String name)
	throws KeyDefinitionException
	{
		this(keyspace, name, DEFAULT_KEYS);
		keyspace(keyspace);

	}

	public PrimaryTable(String keyspace, String name, String keys)
	throws KeyDefinitionException
	{
		this(keyspace, name, KeyDefinitionParser.parse(keys));
	}

	public PrimaryTable(String keyspace, String name, String keys, long ttl)
	throws KeyDefinitionException
	{
		this(keyspace, name, KeyDefinitionParser.parse(keys), ttl);
	}

	public PrimaryTable(String keyspace, String name, KeyDefinition keys)
	{
		this(keyspace, name, keys, 0l);
	}

	public PrimaryTable(String keyspace, String name, KeyDefinition keys, long ttl)
	{
		super(keyspace, name, keys, ttl);
	}

	public PrimaryTable withView(String name, String keys, long ttl)
	throws KeyDefinitionException
	{
		addView(new View(this, name, keys, ttl));
		return this;
	}

	public PrimaryTable withView(String name, KeyDefinition keys, long ttl)
	{
		addView(new View(this, name, keys, ttl));
		return this;
	}

	private void addView(View view)
	{
		if (views == null)
		{
			views = new ArrayList<>();
		}

		views.add(view);
	}

	public boolean hasViews()
	{
		return (views != null && !views.isEmpty());
	}

	public Stream<View> views()
	{
		return (hasViews() ? Collections.unmodifiableList(views).stream() : Stream.empty());
	}

	@Override
	public String toString()
	{
		return String.format("%s.%s=(keys=%s, ttl=%l)", keyspace(), name(), keys(), ttl());
	}

	public int getViewCount()
	{
		return (hasViews() ? views.size() : 0);
	}
}
