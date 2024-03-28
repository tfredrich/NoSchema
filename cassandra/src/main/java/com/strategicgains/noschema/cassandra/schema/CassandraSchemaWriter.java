package com.strategicgains.noschema.cassandra.schema;

public interface CassandraSchemaWriter<T>
{
	void ensureTables();
	void dropTables();
}
