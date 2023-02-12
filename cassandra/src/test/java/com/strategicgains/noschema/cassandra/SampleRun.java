package com.strategicgains.noschema.cassandra;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.datastax.oss.driver.api.core.CqlSession;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.schema.SchemaRegistry;
import com.strategicgains.noschema.exception.DuplicateItemException;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.ItemNotFoundException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public class SampleRun {

	public static void main(String[] args)
	throws KeyDefinitionException, InvalidIdentifierException, DuplicateItemException, ItemNotFoundException
	{
		CqlSession session = createCassandraSession();
		String keyspace = "sample_run";
		Table byId = new Table(keyspace, "flowers_by_id", "id:UUID");
		Table byName = new Table(keyspace, "flowers_by_name", "(account.id as account_id:UUID), name:text");
		SchemaRegistry
			.keyspace(keyspace)
			.schema(new DocumentSchemaProvider(byId))
			.schema(new DocumentSchemaProvider(byName))
			.initializeAll(session)
			.exportInitializeAll();

		CassandraDocumentRepository<Flower> flowersById = new CassandraDocumentRepository<>(session, byId);
		flowersById.ensureTable();

		CassandraDocumentRepository<Flower> flowersByName = new CassandraDocumentRepository<>(session, byName);
		flowersByName.ensureTable();

		UUID id = UUID.fromString("8dbac965-a1c8-4ad6-a043-5f5a9a5ee8c0");
		UUID accountId = UUID.fromString("a87d3bff-6997-4739-ab4e-ded0cc85700f");
		Date createdAt = new Date(1648598130248L);
		Date updatedAt = new Date(1648598130233L);
		List<String> colors = Arrays.asList("red", "white", "pink", "yellow");
		Flower flower = new Flower(id, "rose", true, 3.25f, colors);
		flower.setAccountId(accountId);
		flower.setCreatedAt(createdAt);
		flower.setUpdatedAt(updatedAt);

		try
		{
			writeById(flowersById, flower);
			readAndUpdateById(flowersById, id);
			writeThenReadByName(flowersByName, accountId, flower);
		}
		finally
		{
			session.close();
		}
	}

	private static void writeById(CassandraDocumentRepository<Flower> flowersById, Flower flower)
	throws InvalidIdentifierException, DuplicateItemException, KeyDefinitionException
	{
		System.out.println("*** CREATE by ID ***");
		Flower written = flowersById.create(flower);
		System.out.println(written.toString());
	}

	private static void readAndUpdateById(CassandraDocumentRepository<Flower> flowersById, UUID id)
	throws ItemNotFoundException, InvalidIdentifierException, KeyDefinitionException
	{
		Flower read = flowersById.read(new Identifier(id));
		System.out.println(read.toString());

		read.setName(read.getName() + "-updated");
		read.setIsBlooming(false);
		read.setColors(Arrays.asList("blue", "green", "yellow"));
		System.out.println("*** UPDATE by ID ***");
		Flower updated = flowersById.update(read);
		System.out.println(updated.toString());
		read = flowersById.read(new Identifier(updated.getId()));
		System.out.println(read.toString());
	}

	private static void writeThenReadByName(CassandraDocumentRepository<Flower> flowersByName, UUID accountId, Flower flower)
	throws InvalidIdentifierException, DuplicateItemException, KeyDefinitionException, ItemNotFoundException
	{
		Flower written;
		Flower read;
		System.out.println("*** CREATE by NAME ***");
		written = flowersByName.create(flower);
		System.out.println(written.toString());
		read = flowersByName.read(new Identifier(accountId, "rose"));
		System.out.println(read.toString());
	}

	private static CqlSession createCassandraSession()
	{
		return CqlSession.builder()
		    .addContactPoint(new InetSocketAddress("0.0.0.0", 9042))
		    .withLocalDatacenter("datacenter1")
		    .build();
	}
}
