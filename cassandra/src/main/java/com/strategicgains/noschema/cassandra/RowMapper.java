package com.strategicgains.noschema.cassandra;

import com.datastax.oss.driver.api.core.cql.Row;
import com.strategicgains.noschema.Identifiable;

/**
 * Defines a contract for mapping a Cassandra Row to an Entity.
 * Used by the CassandraRepository to convert query results into entities.
 * 
 * @param <T> the type of entity to map to, which must implement Identifiable.
 */
public interface RowMapper<T extends Identifiable>
{
	T toEntity(Row row);
}
