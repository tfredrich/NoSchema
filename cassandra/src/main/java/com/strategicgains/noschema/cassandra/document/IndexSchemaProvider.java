package com.strategicgains.noschema.cassandra.document;

import com.strategicgains.noschema.cassandra.AbstractTable;
import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.cassandra.schema.AbstractSchemaProvider;

public class IndexSchemaProvider
extends AbstractSchemaProvider
{
	public static class Columns
	{
		public static final String REFERENCE = "reference";
		public static final String METADATA = "metadata";

		private Columns()
		{
			// prevents instantiation.
		}
	}

	private static final String DROP_TABLE = "drop table if exists %s.%s;";
	private static final String CREATE_TABLE = "create table if not exists %s.%s" +
	"(" +
		"%s," +									// identifying properties
	    Columns.REFERENCE + " blob," +
	    Columns.METADATA + " map<text,text>," +
		"%s" +									// primary key
	")" +
	" %s";										// clustering order (optional)

	private String keyspace;
	private String table;
	private KeyDefinition keys;

	public IndexSchemaProvider(AbstractTable table)
	{
		super();
		this.keyspace = table.keyspace();
		this.table = table.asTableName();
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
