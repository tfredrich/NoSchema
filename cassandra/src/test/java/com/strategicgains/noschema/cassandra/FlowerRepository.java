package com.strategicgains.noschema.cassandra;

import java.util.UUID;

import com.datastax.oss.driver.api.core.CqlSession;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.unitofwork.UnitOfWorkType;
import com.strategicgains.noschema.document.ObjectCodec;

public class FlowerRepository
extends CassandraNoSchemaRepository<Flower>
{
	private static final String FLOWERS_BY_NAME = "_by_name";

	public FlowerRepository(CqlSession session, String keyspace, UnitOfWorkType unitOfWorkType, ObjectCodec<Flower> codec)
	{
		super(session,
			new PrimaryTable(keyspace, "flowers", "id:UUID unique")
				.withView(FLOWERS_BY_NAME, "(account.id as account_id:UUID), name:text unique"),
			unitOfWorkType,
			codec);
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
