package com.strategicgains.noschema.cassandra;

import java.util.UUID;

import com.datastax.oss.driver.api.core.CqlSession;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.key.ClusteringKeyComponent;
import com.strategicgains.noschema.cassandra.key.DataTypes;
import com.strategicgains.noschema.cassandra.key.KeyComponent;
import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.cassandra.unitofwork.UnitOfWorkType;
import com.strategicgains.noschema.document.ObjectCodec;

public class FlowerRepository
extends CassandraNoSchemaRepository<Flower>
{
	private static final String FLOWERS_BY_HEIGHT = "by_height";
	private static final String FLOWERS_BY_NAME = "by_name";

	public FlowerRepository(CqlSession session, String keyspace, UnitOfWorkType unitOfWorkType, ObjectCodec<Flower> codec)
	{
		super(session,
			new PrimaryTable(keyspace, "flowers", "id:UUID unique")
				.withView(FLOWERS_BY_NAME, "(account.id as account_id:UUID), name:text unique")
				.withView(FLOWERS_BY_HEIGHT, new KeyDefinition()
					.addPartitionKey(
						new KeyComponent("height_bucket", "height", DataTypes.INTEGER)
							.extractor(f -> ((Float) f).intValue())
					)
					.addClusteringKey("height", DataTypes.FLOAT, ClusteringKeyComponent.Ordering.ASC)
				),
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
