package com.strategicgains.noschema.cassandra.document;

import java.util.HashMap;
import java.util.Map;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.document.Document;

public class DocumentStatementGenerator
{
    private final Map<String, DocumentStatementFactory<Document>> factories = new HashMap<>();

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

	public void put(String viewName, DocumentStatementFactory<Document> factory)
	{
		factories.put(viewName, factory);
	}

	public DocumentStatementFactory<Document> get(String viewName)
	{
		DocumentStatementFactory<Document> factory = factories.get(viewName);

//		if (factory == null) throw new InvalidViewNameException(viewName);

		return factory;
	}

}
