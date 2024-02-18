package com.strategicgains.noschema.cassandra;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.datastax.oss.driver.api.core.CqlSession;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.bson.BsonObjectCodec;
import com.strategicgains.noschema.cassandra.document.DocumentTableSchemaProvider;
import com.strategicgains.noschema.cassandra.schema.SchemaRegistry;
import com.strategicgains.noschema.cassandra.unitofwork.UnitOfWorkType;
import com.strategicgains.noschema.document.ObjectCodec;
import com.strategicgains.noschema.exception.DuplicateItemException;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.ItemNotFoundException;
import com.strategicgains.noschema.exception.KeyDefinitionException;
import com.strategicgains.noschema.gson.GsonObjectCodec;

public class SampleRun {

	private static final String FLOWERS_BY_NAME = "by_name";
	private static final ObjectCodec<Flower> GSON_CODEC = new GsonObjectCodec<>();
	private static final ObjectCodec<Flower> BSON_CODEC = new BsonObjectCodec<>();
	private static final UnitOfWorkType unitOfWorkType = UnitOfWorkType.ASYNC;

	public static void main(String[] args)
	throws KeyDefinitionException, InvalidIdentifierException, DuplicateItemException, ItemNotFoundException
	{
		CqlSession session = createCassandraSession();

		try
		{
			testBson(session, "sample_run_bson");
			testGson(session, "sample_run_gson");
		}
		finally
		{
			session.close();
		}
	}

	private static Flower instantiateFlower(UUID accountId, UUID id)
	{
		Date createdAt = new Date(1648598130248L);
		Date updatedAt = new Date(1648598130233L);
		List<String> colors = Arrays.asList("red", "white", "pink", "yellow");
		Flower flower = new Flower(id, "rose", true, 3.25f, colors);
		flower.setAccountId(accountId);
		flower.setCreatedAt(createdAt);
		flower.setUpdatedAt(updatedAt);
		return flower;
	}

	private static CqlSession createCassandraSession()
	{
		return CqlSession.builder()
		    .addContactPoint(new InetSocketAddress("0.0.0.0", 9042))
		    .withLocalDatacenter("datacenter1")
		    .build();
	}

	private static void testBson(CqlSession session, String keyspace)
	{
		SchemaRegistry.keyspace(keyspace);
		PrimaryTable flowersTable = new PrimaryTable(keyspace, "flowers", "id:UUID unique")
			.withView(FLOWERS_BY_NAME, "(account.id as account_id:UUID), name:text unique");
		testCassandra(keyspace, session, flowersTable, unitOfWorkType, BSON_CODEC);
		SchemaRegistry.clear();
	}

	private static void testGson(CqlSession session, String keyspace)
	{
		SchemaRegistry.keyspace(keyspace);
		PrimaryTable flowersTable = new PrimaryTable(keyspace, "flowers", "id:UUID unique")
			.withView(FLOWERS_BY_NAME, "(account.id as account_id:UUID), name:text unique");
		testCassandra(keyspace, session, flowersTable, unitOfWorkType, GSON_CODEC);
		SchemaRegistry.clear();
	}

	private static void testCassandra(String keyspace, CqlSession session, PrimaryTable flowersTable, UnitOfWorkType uowType, ObjectCodec<Flower> codec)
	{
		SchemaRegistry schemas = SchemaRegistry.instance();
		flowersTable.stream().forEach(v -> schemas.withProvider(new DocumentTableSchemaProvider(v)));
		schemas.initializeAll(session)
			.exportInitializeAll();

		CassandraNoSchemaRepository<Flower> flowers = new CassandraNoSchemaRepository<>(session, flowersTable, uowType, codec);
		flowers.withObserver(new SampleObserver());
		flowers.ensureTables();

		UUID id = UUID.fromString("8dbac965-a1c8-4ad6-a043-5f5a9a5ee8c0");
		UUID accountId = UUID.fromString("a87d3bff-6997-4739-ab4e-ded0cc85700f");
		Flower flower = instantiateFlower(accountId, id);

		System.out.println("*** READ ID (Not Found) ***");
		try
		{
			flowers.read(flower.getIdentifier());
			throw new RuntimeException("FAILED: Expected ItemNotFoundException (by ID)");
		}
		catch (ItemNotFoundException e)
		{
			System.out.println("Recieved expected exception: ItemNotFoundException: " + e.getMessage());
		}

		System.out.println("*** READ NAME (Not Found) ***");
		try
		{
			flowers.read(FLOWERS_BY_NAME, new Identifier(accountId, "rose"));
			throw new RuntimeException("FAILED: Expected ItemNotFoundException (by name)");
		}
		catch (ItemNotFoundException e)
		{
			System.out.println("Recieved expected exception: ItemNotFoundException: " + e.getMessage());
		}

		System.out.println("*** CREATE ***");
		Flower written = flowers.create(flower);
		System.out.println(written.toString());

		System.out.println("*** READ ID ***");
		Flower read = flowers.read(new Identifier(id));
		System.out.println(read.toString());

		System.out.println("*** READ NAME ***");
		read = flowers.read(FLOWERS_BY_NAME, new Identifier(accountId, "rose"));
		System.out.println(read.toString());

		System.out.println("*** CREATE DUPLICATE ***");
		try
		{
			flowers.create(flower);
			throw new RuntimeException("FAILED: Expected DuplicateItemException");
		}
		catch (DuplicateItemException e)
		{
			System.out.println("Recieved expected exception: DuplicateItemException: " + e.getMessage());
		}

		System.out.println("*** UPDATE ***");
		Flower updated = new Flower(read);
		updated.setName(read.getName() + "-updated");
		updated.setIsBlooming(false);
		updated.setColors(Arrays.asList("blue", "green", "yellow"));
		updated = flowers.update(updated, read);
		System.out.println(updated.toString());

		System.out.println("*** RE-READ ***");
		read = flowers.read(new Identifier(updated.getId()));
		System.out.println(read.toString());

		System.out.println("*** READ ALL ***");
		List<Flower> all = flowers.readAll(FLOWERS_BY_NAME, accountId);
		System.out.println("Size: " + all.size());
		System.out.println(all.get(0));
	}
}
