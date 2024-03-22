package com.strategicgains.noschema.cassandra;

import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.cassandra.key.KeyDefinitionParser;
import com.strategicgains.noschema.exception.KeyDefinitionException;

/**
 * An Index is a Cassandra Table that is defined as an index on a parent PrimaryTable.
 * It differs from a View in that it contains only the primary key and the identifier
 * of for the PrimaryTable being indexed.  It does not contain any other fields from
 * the entity being indexed.
 * 
 * Indexes are used to support queries for primary tables that contain large objects
 * and readAll queries are rare.  Indexes are used to quickly locate the primary table
 * identifier for a given query but requires two reads to get the full entity.
 */
public class Index
extends AbstractTable
{
	private PrimaryTable parent;

	public Index()
	{
		super();
	}

	public Index(PrimaryTable parent, String indexName, String keys)
	throws KeyDefinitionException
	{
		this(parent, indexName, KeyDefinitionParser.parse(keys), 0l);
	}

	public Index(PrimaryTable parent, String indexName, KeyDefinition keys)
	{
		this(parent, indexName, keys, 0l);
	}

	public Index(PrimaryTable parent, String indexName, String keys, long ttl)
	throws KeyDefinitionException
	{
		this(parent, indexName, KeyDefinitionParser.parse(keys), ttl);
	}

	public Index(PrimaryTable parent, String indexName, KeyDefinition keys, long ttl)
	{
		super(parent.keyspace(), indexName, keys, ttl);
		parent(parent);
	}

	public PrimaryTable parent()
	{
		return parent;
	}

	public Index parent(PrimaryTable parent)
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
