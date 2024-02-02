package com.strategicgains.noschema.cassandra.schema;

public interface SchemaWriter<T>
{
	void ensureTables();
	void dropTables();
}
