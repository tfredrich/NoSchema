package com.strategicgains.noschema.dynamodb;

import com.strategicgains.noschema.dynamodb.key.KeyDefinition;
import com.strategicgains.noschema.dynamodb.schema.AbstractSchemaProvider;

public class DocumentSchemaProvider
extends AbstractSchemaProvider
{
	public static class Columns
	{
		static final String OBJECT = "object";
		static final String TYPE = "type";
		static final String CREATED_AT = "created_at";
		static final String UPDATED_AT = "updated_at";
	}

	private static final String DROP_TABLE = "drop table if exists %s.%s;";
	private static final String CREATE_TABLE = "create table if not exists %s.%s" +
	"(" +
		"%s," +									// identifying properties
	    Columns.OBJECT + " blob," +
		Columns.TYPE + " text," +
	    // TODO: Add Location details to Document.
	    // TODO: Add Lucene index capability to Document.
		Columns.CREATED_AT + " timestamp," +
	    Columns.UPDATED_AT + " timestamp," +
		"%s" +									// primary key
	")" +
	" %s";										// clustering order (optional)

	private String keyspace;
	private String table;
	private KeyDefinition keys;

	public DocumentSchemaProvider(Table table)
	{
		super();
		this.keyspace = table.keyspace();
		this.table = table.name();
		this.keys = table.keys();
	}

	@Override
	public String asCreateScript()
	{
		return String.format(CREATE_TABLE, keyspace, table, keys.asColumns(), keys.asPrimaryKey(), keys.asClusteringKey());
	}

	@Override
	public String asDropScript()
	{
		return String.format(DROP_TABLE, keyspace, table);
	}
}
