package com.strategicgains.noschema.cassandra;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.datastax.oss.driver.api.core.CqlSession;
import com.strategicgains.noschema.bson.BsonObjectCodec;
import com.strategicgains.noschema.cassandra.schema.CassandraSchemaRegistry;
import com.strategicgains.noschema.cassandra.unitofwork.UnitOfWorkType;
import com.strategicgains.noschema.document.ObjectCodec;
import com.strategicgains.noschema.exception.DuplicateItemException;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.ItemNotFoundException;
import com.strategicgains.noschema.exception.KeyDefinitionException;
import com.strategicgains.noschema.gson.GsonObjectCodec;

public class SampleRun
{
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

	private static Flower newRose(UUID accountId, UUID id)
	{
		List<String> colors = Arrays.asList("red", "white", "pink", "yellow");
		return instantiateFlower(accountId, id, "rose", true, 3.25f, colors);
	}

	private static Flower newSunflower(UUID accountId, UUID id)
	{
		List<String> colors = Arrays.asList("yellow", "orange");
		return instantiateFlower(accountId, id, "sunflower", true, 3.75f, colors);
	}

	private static Flower instantiateFlower(UUID accountId, UUID id, String name, Boolean isBlooming, Float height, List<String> colors)
	{
		Flower flower = new Flower(id, name, isBlooming, height, colors);
		flower.setAccountId(accountId);
		Date createdAt = new Date(1648598130248L);
		Date updatedAt = new Date(1648598130233L);
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
		CassandraSchemaRegistry.keyspace(keyspace);
		testCassandra(keyspace, session, unitOfWorkType, BSON_CODEC);
		CassandraSchemaRegistry.clear();
	}

	private static void testGson(CqlSession session, String keyspace)
	{
		CassandraSchemaRegistry.keyspace(keyspace);
		testCassandra(keyspace, session, unitOfWorkType, GSON_CODEC);
		CassandraSchemaRegistry.clear();
	}

	private static void testCassandra(String keyspace, CqlSession session, UnitOfWorkType uowType, ObjectCodec<Flower> codec)
	{
		// Create the keyspace, if needed.
		CassandraSchemaRegistry.initialize(session);

		FlowerRepository flowers = new FlowerRepository(session, keyspace, uowType, codec);
		flowers.withObserver(new SampleObserver());

		// Ensure the tables exist.
		flowers.ensureTables();

		UUID id = UUID.fromString("8dbac965-a1c8-4ad6-a043-5f5a9a5ee8c0");
		UUID accountId = UUID.fromString("a87d3bff-6997-4739-ab4e-ded0cc85700f");
		Flower flower = newRose(accountId, id);

		shouldThrowOnNotFoundById(flowers, flower.getId());

		shouldThrowOnNotFoundByName(flowers, accountId, flower.getName());

		shouldCreate(flowers, flower);

		Flower read = shouldReadById(flowers, id);

		shouldReadByName(flowers, accountId, "rose");

		shouldThrowOnDuplicate(flowers, flower);

		Flower updated = shouldUpdate(flowers, read);
		shouldReadUpdated(flowers, updated.getAccountId(), "rose-updated");

		UUID id2 = UUID.fromString("c9e71479-3b47-4aa6-84e7-97044e7f2a3c");
		flowers.create(newSunflower(accountId, id2));

		shouldReadAll(flowers, accountId);
	}

	private static void shouldThrowOnNotFoundById(FlowerRepository flowers, UUID id) {
		System.out.println("*** READ ID (Not Found) ***");
		try
		{
			flowers.read(id);
			throw new RuntimeException("FAILED: Expected ItemNotFoundException (by ID)");
		}
		catch (ItemNotFoundException e)
		{
			System.out.println("Recieved expected exception: ItemNotFoundException: " + e.getMessage());
		}
	}

	private static void shouldThrowOnNotFoundByName(FlowerRepository flowers, UUID accountId, String name) {
		System.out.println("*** READ NAME (Not Found) ***");
		try
		{
			flowers.readByName(accountId, name);
			throw new RuntimeException("FAILED: Expected ItemNotFoundException (by name)");
		}
		catch (ItemNotFoundException e)
		{
			System.out.println("Recieved expected exception: ItemNotFoundException: " + e.getMessage());
		}
	}

	private static void shouldCreate(FlowerRepository flowers, Flower flower) {
		System.out.println("*** CREATE ***");
		Flower written = flowers.create(flower);
		System.out.println(written.toString());
	}

	private static Flower shouldReadById(FlowerRepository flowers, UUID id) {
		System.out.println("*** READ ID ***");
		Flower read = flowers.read(id);
		System.out.println(read.toString());
		return read;
	}

	private static Flower shouldReadByName(FlowerRepository flowers, UUID accountId, String name) {
		Flower read;
		System.out.println("*** READ NAME ***");
		read = flowers.readByName(accountId, name);
		System.out.println(read.toString());
		return read;
	}

	private static void shouldThrowOnDuplicate(FlowerRepository flowers, Flower flower) {
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
	}

	private static Flower shouldUpdate(FlowerRepository flowers, Flower read) {
		System.out.println("*** UPDATE ***");
		Flower updated = new Flower(read);
		updated.setName(read.getName() + "-updated");
		updated.setIsBlooming(false);
		updated.setColors(Arrays.asList("blue", "green", "yellow"));
		updated = flowers.update(updated, read);
		System.out.println(updated.toString());
		return updated;
	}

	private static void shouldReadUpdated(FlowerRepository flowers, UUID accountId, String name) {
		Flower read;
		System.out.println("*** RE-READ ***");
		read = flowers.readByName(accountId, name);
		System.out.println(read.toString());
	}

	private static void shouldReadAll(FlowerRepository flowers, UUID accountId) {
		System.out.println("*** READ ALL ***");
		PagedResponse<Flower> all = flowers.readAllByName(20, null, accountId);
		System.out.println("Size: " + all.size());
		System.out.println(String.join(" | ", all.get(0).toString(), all.get(1).toString()));
	}
}
