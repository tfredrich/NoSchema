package com.strategicgains.noschema.cassandra;

import com.datastax.oss.driver.api.core.cql.Row;
import com.strategicgains.noschema.Identifiable;

public interface CassandraRowMapper<T extends Identifiable>
{
	T toEntity(Row row);
}
