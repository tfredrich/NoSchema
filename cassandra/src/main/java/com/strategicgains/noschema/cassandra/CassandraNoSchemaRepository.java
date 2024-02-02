package com.strategicgains.noschema.cassandra;

import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.NoSchemaRepository;
import com.strategicgains.noschema.cassandra.schema.SchemaWriter;

public interface CassandraNoSchemaRepository<T>
extends NoSchemaRepository<T>, SchemaWriter<T>
{
	T read(String viewName, Identifier id);
	boolean exists(String viewName, Identifier id);
}
