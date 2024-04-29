package com.strategicgains.noschema.cassandra;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.cassandra.key.KeyDefinitionParser;
import com.strategicgains.noschema.exception.KeyDefinitionException;

/**
 * A PrimaryTable is a Cassandra table that has a primary key. It may also have one or more views.
 * 
 * A view can be attached to a PrimaryTable and is a table that is defined with a different primary
 * key than the parent table. It is a different table that is defined within the same keyspace. It
 * is not a materialized view, but a separate table with its own primary key and related to the 
 * PrimaryTable only in that operations against the PrimaryTable are also performed against the view
 * via a UnitOfWork instance.
 * 
 * The primary table and its views are all defined within the same keyspace. Create, update, and
 * delete operations are managed via a UnitOfWork instance, which is responsible for ensuring that
 * all operations are performed against all the tables at once.
 * 
 * Time-to-live (TTL) is supported at the table level. If a table has a TTL defined, then all rows
 * inserted into the table and its views will have that TTL applied.
 * 
 * The primary key of tables and views is defined by a KeyDefinition instance, which defines which
 * columns are part of the primary key and their types.
 * 
 * @author Todd Fredrich
 * @see KeyDefinition
 * @see SecondaryTable
 */
public class PrimaryTable
extends AbstractTable
{
	private static final String DEFAULT_KEY = "id:uuid unique";
	private List<View> views;
	private List<Index> indexes;

	public PrimaryTable()
	{
		super();
	}

	/**
	 * Creates a new Table instance with a primary key of 'id' of type 'uuid'
	 * 
	 * @param keyspace
	 * @param name
	 * @throws KeyDefinitionException
	 */
	public PrimaryTable(String keyspaceName, String name)
	throws KeyDefinitionException
	{
		this(keyspaceName, name, DEFAULT_KEY);
		keyspace(keyspaceName);

	}

	public PrimaryTable(String keyspaceName, String tableName, String keys)
	throws KeyDefinitionException
	{
		this(keyspaceName, tableName, KeyDefinitionParser.parse(keys));
	}

	public PrimaryTable(String keyspaceName, String tableName, String keys, long ttl)
	throws KeyDefinitionException
	{
		this(keyspaceName, tableName, KeyDefinitionParser.parse(keys), ttl);
	}

	public PrimaryTable(String keyspaceName, String tableName, KeyDefinition keys)
	{
		this(keyspaceName, tableName, keys, 0l);
	}

	public PrimaryTable(String keyspaceName, String tableName, KeyDefinition keys, long ttl)
	{
		super(keyspaceName, tableName, keys, ttl);
	}

	public PrimaryTable withView(String viewName, String keys)
	throws KeyDefinitionException
	{
		return withView(viewName, keys, 0l);
	}

	public PrimaryTable withView(String viewName, String keys, long ttl)
	throws KeyDefinitionException
	{
		addView(new View(this, viewName, keys, ttl));
		return this;
	}

	public PrimaryTable withView(String viewName, KeyDefinition keys)
	{
		return withView(viewName, keys, 0l);
	}

	public PrimaryTable withView(String viewName, KeyDefinition keys, long ttl)
	{
		addView(new View(this, viewName, keys, ttl));
		return this;
	}

	public void addView(View view)
	{
		if (views == null)
		{
			views = new ArrayList<>();
		}

		view.setParent(this);
		views.add(view);
	}

	public boolean hasViews()
	{
		return (views != null && !views.isEmpty());
	}

	public Stream<View> views()
	{
		return (hasViews() ? views.stream() : Stream.empty());
	}

	public Stream<View> uniqueViews()
	{
		return (hasViews()
			? views.stream().filter(View::isUnique)
			: Stream.empty()
		);
	}

	public int getViewCount()
	{
		return (hasViews() ? views.size() : 0);
	}

	public PrimaryTable withIndex(String indexName, String keys)
	throws KeyDefinitionException
	{
		return withIndex(indexName, keys, 0l);
	}

	public PrimaryTable withIndex(String indexName, String keys, long ttl)
	throws KeyDefinitionException
	{
		addIndex(new Index(this, indexName, keys, ttl));
		return this;
	}

	public PrimaryTable withIndex(String indexName, KeyDefinition keys)
	{
		return withIndex(indexName, keys, 0l);
	}

	public PrimaryTable withIndex(String indexName, KeyDefinition keys, long ttl)
	{
		addIndex(new Index(this, indexName, keys, ttl));
		return this;
	}

	public void addIndex(Index index)
	{
		if (indexes == null)
		{
			indexes = new ArrayList<>();
		}

		index.setParent(this);
		indexes.add(index);
	}

	public boolean hasIndexes()
	{
		return (indexes != null && !indexes.isEmpty());
	}

	public Stream<Index> indexes()
	{
		return (hasIndexes() ? indexes.stream() : Stream.empty());
	}

	public Stream<Index> uniqueIndexes()
	{
		return (hasIndexes()
			? indexes.stream().filter(Index::isUnique)
			: Stream.empty()
		);
	}

	public int getIndexCount()
	{
		return (hasIndexes() ? indexes.size() : 0);
	}

	/**
	 * A stream of the primary table with all its views and indexes.
	 * 
	 * @return a Stream of the primary table, its views, and indexes.
	 */
	public Stream<AbstractTable> stream()
	{
		return Stream.of(Stream.of(this), views(), indexes()).flatMap(s -> s);
	}}
