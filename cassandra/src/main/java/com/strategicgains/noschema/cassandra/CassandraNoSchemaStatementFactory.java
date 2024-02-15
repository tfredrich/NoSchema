package com.strategicgains.noschema.cassandra;

import java.util.HashMap;
import java.util.Map;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.document.DocumentStatementFactory;
import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.document.ObjectCodec;

public class CassandraNoSchemaStatementFactory<T extends Identifiable>
{
	private final Map<String, KeyDefinition> keysByView = new HashMap<>();
    private final Map<String, DocumentStatementFactory<T>> factoriesByView = new HashMap<>();

	public CassandraNoSchemaStatementFactory(CqlSession session, PrimaryTable table, ObjectCodec<T> codec)
	{
		super();
		table.stream().forEach(view -> {
			put(view.name(), new DocumentStatementFactory<>(session, view, codec));
			put(view.name(), view.keys());				
		});
	}

	public BoundStatement read(String viewName, Identifier id)
	{
		return get(viewName).read(id);
	}

	public BoundStatement delete(String viewName, Identifier id)
	{
		return get(viewName).delete(id);
	}

	public BoundStatement create(String viewName, T entity)
	{
		return get(viewName).create(entity);
	}

	public BoundStatement update(String viewName, T entity)
	{
		return get(viewName).update(entity);
	}

	public BoundStatement exists(String viewName, Identifier id)
	{
		return get(viewName).exists(id);
	}

	public boolean isViewUnique(String viewName)
	{
		return keysByView.get(viewName).isUnique();
	}

	private void put(String viewName, DocumentStatementFactory<T> factory)
	{
		factoriesByView.put(viewName, factory);
	}

	private void put(String viewName, KeyDefinition keys)
	{
		this.keysByView.put(viewName, keys);
	}

	private DocumentStatementFactory<T> get(String viewName)
	{
		DocumentStatementFactory<T> factory = factoriesByView.get(viewName);

//		if (factory == null) throw new InvalidViewNameException(viewName);

		return factory;
	}
}
