package com.strategicgains.noschema.cassandra.index;

import com.strategicgains.noschema.cassandra.Index;
import com.strategicgains.noschema.cassandra.PrimaryTable;
import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.cassandra.schema.AbstractSchemaProvider;

public class IndexSchemaProvider
extends AbstractSchemaProvider
{
	private static final String DROP_TABLE = "drop table if exists %s.%s;";
	private static final String CREATE_TABLE = "create table if not exists %s.%s" +
	"(" +
		"%s," +									// Identifying properties
	    "%s," +									// Document identifier.
		"%s" +									// primary key
	")" +
	" %s";										// clustering order (optional)

	private PrimaryTable parent;
	private String keyspace;
	private String table;
	private KeyDefinition keys;

	public IndexSchemaProvider(Index index) {
		super();
		this.parent = index.parent();
		this.keyspace = index.keyspace();
		this.table = index.asTableName();
		this.keys = index.keys();
	}

	@Override
	public String asCreateScript()
	{
		return String.format(CREATE_TABLE, keyspace, table,
			keys.asColumns(),
			parent.keys().asColumns(),
			keys.asPrimaryKey(),
			keys.asClusteringKey());
	}

	@Override
	public String asDropScript()
	{
		return String.format(DROP_TABLE, keyspace, table);
	}
}
