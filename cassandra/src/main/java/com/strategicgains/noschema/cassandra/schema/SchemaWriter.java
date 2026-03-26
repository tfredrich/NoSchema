package com.strategicgains.noschema.cassandra.schema;

public interface SchemaWriter
{
	void ensureTables();
	void dropTables();
}
