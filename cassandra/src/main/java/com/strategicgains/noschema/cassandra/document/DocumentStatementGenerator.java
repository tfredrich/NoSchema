package com.strategicgains.noschema.cassandra.document;

import java.util.HashMap;
import java.util.Map;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.PrimaryTable;
import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.document.Document;

public class DocumentStatementGenerator
{
	private final Map<String, KeyDefinition> keysByView = new HashMap<>();
    private final Map<String, DocumentStatementFactory<Document>> factoriesByView = new HashMap<>();

	public DocumentStatementGenerator(CqlSession session, PrimaryTable table)
	{
		super();
		table.stream().forEach(view -> {
			put(view.name(), new DocumentStatementFactory<>(session, view));
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

	public BoundStatement create(String viewName, Document entity)
	{
		return get(viewName).create(entity);
	}

	public BoundStatement update(String viewName, Document entity)
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

	private void put(String viewName, DocumentStatementFactory<Document> factory)
	{
		factoriesByView.put(viewName, factory);
	}

	private void put(String viewName, KeyDefinition keys)
	{
		this.keysByView.put(viewName, keys);
	}

	private DocumentStatementFactory<Document> get(String viewName)
	{
		DocumentStatementFactory<Document> factory = factoriesByView.get(viewName);

//		if (factory == null) throw new InvalidViewNameException(viewName);

		return factory;
	}
}
