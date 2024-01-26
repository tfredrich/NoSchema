package com.strategicgains.noschema.cassandra;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.datastax.oss.driver.api.core.CqlSession;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.document.DocumentSchemaProvider;
import com.strategicgains.noschema.cassandra.schema.SchemaRegistry;
import com.strategicgains.noschema.exception.DuplicateItemException;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.ItemNotFoundException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public class SampleRun {

	private static final String FLOWERS_BY_NAME = "flowers_by_name";

	public static void main(String[] args)
	throws KeyDefinitionException, InvalidIdentifierException, DuplicateItemException, ItemNotFoundException
	{
		CqlSession session = createCassandraSession();
		String keyspace = "sample_run";
		PrimaryTable byId = new PrimaryTable(keyspace, "flowers_by_id", "id:UUID")
			.withView(FLOWERS_BY_NAME, "(account.id as account_id:UUID), name:text");

		SchemaRegistry schemas = SchemaRegistry.keyspace(keyspace);
		byId.views().forEach(v -> schemas.schema(new DocumentSchemaProvider(v)));
		schemas.initializeAll(session)
			.exportInitializeAll();

		CassandraNoSchemaRepository<Flower> flowers = new CassandraNoSchemaRepository<>(session, byId);
		flowers.ensureTables();

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
			System.out.println("*** CREATE ***");
			Flower written = flowers.create(flower);
			System.out.println(written.toString());

			System.out.println("*** READ ID ***");
			Flower read = flowers.read(new Identifier(id));
			System.out.println(read.toString());

			System.out.println("*** READ-NAME ***");
			read = flowers.readView(FLOWERS_BY_NAME, new Identifier(accountId, "rose"));
			System.out.println(read.toString());

			System.out.println("*** UPDATE ***");
			read.setName(read.getName() + "-updated");
			read.setIsBlooming(false);
			read.setColors(Arrays.asList("blue", "green", "yellow"));
			Flower updated = flowers.update(read);
			System.out.println(updated.toString());

			read = flowers.read(new Identifier(updated.getId()));
			System.out.println(read.toString());
		}
		finally
		{
			session.close();
		}
	}

	private static CqlSession createCassandraSession()
	{
		return CqlSession.builder()
		    .addContactPoint(new InetSocketAddress("0.0.0.0", 9042))
		    .withLocalDatacenter("datacenter1")
		    .build();
	}
}
