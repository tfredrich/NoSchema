package com.strategicgains.noschema.cassandra;

import java.util.ArrayList;
import java.util.Collections;
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
 * @see View
 */
public class PrimaryTable
extends AbstractTable
{
	private static final String DEFAULT_KEYS = "id:uuid unique";
	private List<View> views;

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
		this(keyspaceName, name, DEFAULT_KEYS);
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

		view.parent(this);
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

	public Stream<View> uniqueViews()
	{
		return (hasViews()
			? Collections.unmodifiableList(views.stream().filter(View::isUnique).toList()).stream()
			: Stream.empty()
		);
	}

	public int getViewCount()
	{
		return (hasViews() ? views.size() : 0);
	}

	/**
	 * A stream of the primary table and all its views. Indexes are not included.
	 * 
	 * @return a Stream of the primary table and its views.
	 */
	public Stream<AbstractTable> stream()
	{
		return Stream.concat(Stream.of(this), views());
	}
}
