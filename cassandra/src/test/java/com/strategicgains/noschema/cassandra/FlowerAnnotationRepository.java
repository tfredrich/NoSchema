package com.strategicgains.noschema.cassandra;

import java.util.UUID;

import com.datastax.oss.driver.api.core.CqlSession;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.document.ObjectCodec;

/**
 * A repository for the Flower entity. It manages three tables: flowers, flowers_by_name, and flowers_by_height,
 * all of which are in the same keyspace but have different partition keys and clustering keys.
 * 
 * This particular implementation is a little more complex than it needs to be as it support the SampleRun class
 * which demonstrates how to use this repository with both Gson and Bson codecs in different namespaces.
 * 
 * In all likelihood, Repository implementations could use containment instead of inheritance to reduce the scope of the 
 * methods exposed to the client. But, for the purposes of this example, it's easier to demonstrate the use of the
 * underlying methods by extending the CassandraRepository class.
 * 
 * @author Todd Fredrich
 */
public class FlowerAnnotationRepository
extends AnnotationRepository<Flower>
{
	private static final String FLOWERS_BY_NAME = "by_name";

	public FlowerAnnotationRepository(CqlSession session, String keyspace, ObjectCodec<Flower> codec)
	{
		super(session, keyspace, Flower.class, codec);
	}

	public Flower readByName(UUID accountId, String name)
	{
		return read(FLOWERS_BY_NAME, new Identifier(accountId, name));
	}

	public Flower read(UUID id)
	{
		return read(new Identifier(id));
	}

	public PagedResponse<Flower> readAllByName(int max, String cursor, UUID accountId)
	{
		return readAll(FLOWERS_BY_NAME, 20, cursor, accountId);
	}
}
