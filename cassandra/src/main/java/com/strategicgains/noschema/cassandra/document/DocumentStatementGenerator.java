package com.strategicgains.noschema.cassandra.document;

import static com.strategicgains.noschema.cassandra.CassandraNoSchemaRepository.PRIMARY_TABLE;

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
		factoriesByView.put(PRIMARY_TABLE, new DocumentStatementFactory<>(session, table));
		table.views().forEach(view -> {
			this.factoriesByView.put(view.name(), new DocumentStatementFactory<>(session, view));
			this.keysByView.put(view.name(), view.keys());				
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

	private DocumentStatementFactory<Document> get(String viewName)
	{
		DocumentStatementFactory<Document> factory = factoriesByView.get(viewName);

//		if (factory == null) throw new InvalidViewNameException(viewName);

		return factory;
	}
}
