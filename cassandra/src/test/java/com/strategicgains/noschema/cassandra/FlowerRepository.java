package com.strategicgains.noschema.cassandra;

import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import com.datastax.oss.driver.api.core.CqlSession;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.key.ClusteringKeyComponent;
import com.strategicgains.noschema.cassandra.key.DataTypes;
import com.strategicgains.noschema.cassandra.key.builder.KeyDefinitionBuilder;
import com.strategicgains.noschema.cassandra.unitofwork.UnitOfWorkType;
import com.strategicgains.noschema.document.ByteArrayCodec;

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
	private static final String FLOWERS_BY_DATE = "by_created_at";

	public FlowerRepository(CqlSession session, String keyspace, UnitOfWorkType unitOfWorkType, ByteArrayCodec<Flower> codec)
	{
		super(session,
			/**
			 * A primary table with a unique key that is the 'id' property of type UUID.
			 * The database table column and the entity property are the same name.
			 */
			new PrimaryTable(keyspace, "flowers", "id:UUID unique")

				/**
				 * A view of flowers by name, where the name is unique within an account.
				 * The partition key is extracted from the entity via dot notation (account.id)
				 * and the database column name is account_id of type UUID.
				 * The clustering key is the name, which is a text field and the column name matches
				 * the entity property name.
				 */
				.withView(FLOWERS_BY_NAME, "(account.id as account_id:UUID), name:text unique")

				/**
				 * A view of flowers by height, where the partition key is a bucket of heights (int)
				 * and the clustering key is the actual height (float).
				 * This example uses a lambda function to extract the partition key from the entity's
				 * height property--extracting the integer portion of the float. The clustering
				 * key is the height property of the entity of type float.
				 */
				.withView(FLOWERS_BY_HEIGHT, new KeyDefinitionBuilder()
					.withPartitionKey("height_bucket", "height", DataTypes.INTEGER)
						.withExtractor(f -> ((Float) f).intValue())
					.withClusteringKey("height", DataTypes.FLOAT, ClusteringKeyComponent.Ordering.ASC)
					.build()
				)

				/**
				 * An index of flowers by createdAt for an account. As dates could be a problem
				 * for the wide rows of Cassandra, this example uses a bucketed integer value
				 * for the day of the year (1-366) as the partition key and the actual date as the
				 * clustering key.
				 */
				.withIndex(FLOWERS_BY_DATE, new KeyDefinitionBuilder()
					.withPartitionKey("day", "createdAt", DataTypes.INTEGER)
						.withExtractor(d -> ((Date) d).toInstant().atZone(ZoneId.systemDefault()).getDayOfYear())
					.withClusteringKey("createdAt", DataTypes.TIMESTAMP, ClusteringKeyComponent.Ordering.ASC)
						.withExtractor(d -> ((Date) d).toInstant())
					.build()
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
