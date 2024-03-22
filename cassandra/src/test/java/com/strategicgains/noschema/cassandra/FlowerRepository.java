package com.strategicgains.noschema.cassandra;

import java.util.UUID;

import com.datastax.oss.driver.api.core.CqlSession;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.key.ClusteringKeyComponent;
import com.strategicgains.noschema.cassandra.key.DataTypes;
import com.strategicgains.noschema.cassandra.key.builder.KeyDefinitionBuilder;
import com.strategicgains.noschema.cassandra.unitofwork.UnitOfWorkType;
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
public class FlowerRepository
extends CassandraRepository<Flower>
{
	private static final String FLOWERS_BY_HEIGHT = "by_height";
	private static final String FLOWERS_BY_NAME = "by_name";

	public FlowerRepository(CqlSession session, String keyspace, UnitOfWorkType unitOfWorkType, ObjectCodec<Flower> codec)
	{
		super(session,
			/**
			 * A primary table with a unique key that is the 'id' property of type UUID. The column and the entity property are the same.
			 */
			new PrimaryTable(keyspace, "flowers", "id:UUID unique")

				/**
				 * A view by name, where the name is unique within an account.
				 * The partition key is extracted from the entity via dot notation (account.id) and the column is account_id of type UUID.
				 * The clustering key is the name, which is a text field.
				 */
				.withView(FLOWERS_BY_NAME, "(account.id as account_id:UUID), name:text unique")

				/**
				 * A view by height, where the partition key is a bucket of heights (int) and the clustering key is the height (float).
				 * This uses a lambda function to extract the partition key from the entity's height property.
				 * The clustering key is the height property of the entity of type float.
				 */
				.withView(FLOWERS_BY_HEIGHT, new KeyDefinitionBuilder()
					.withPartitionKey("height_bucket", "height", DataTypes.INTEGER)
						.withExtractor(f -> ((Float) f).intValue())
					.withClusteringKey("height", DataTypes.FLOAT, ClusteringKeyComponent.Ordering.ASC)
					.build()
				)

				/**
				 * An index that contains no data, only the key definition and an ID pointing to the Flower of the primary table.
				 */
				.withIndex(FLOWERS_BY_NAME + "_idx", "(account.id as account_id:UUID), name:text unique"),
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
