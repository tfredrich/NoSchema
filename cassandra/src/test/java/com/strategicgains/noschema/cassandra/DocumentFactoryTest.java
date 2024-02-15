package com.strategicgains.noschema.cassandra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.bson.BsonObjectCodec;
import com.strategicgains.noschema.cassandra.document.CassandraDocumentFactory;
import com.strategicgains.noschema.cassandra.key.KeyDefinitionParser;
import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.document.ObjectCodec;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public class DocumentFactoryTest
{
	private static final ObjectCodec<Flower> CODEC = new BsonObjectCodec<>();

	private UUID id = UUID.fromString("8dbac965-a1c8-4ad6-a043-5f5a9a5ee8c0");
	private UUID accountId = UUID.fromString("a87d3bff-6997-4739-ab4e-ded0cc85700f");
	private Date createdAt = new Date(1648598130248L);
	private Date updatedAt = new Date(1648598130233L);
	private List<String> colors = Arrays.asList("red", "white", "pink", "yellow");
	private Flower flower;

	@Before
	public void setup()
	{
		flower = new Flower(id, "rose", true, 3.25f, colors);
		flower.setAccountId(accountId);
		flower.setCreatedAt(createdAt);
		flower.setUpdatedAt(updatedAt);		
	}

	@Test
	public void testUuidIdentifier()
	throws KeyDefinitionException, InvalidIdentifierException
	{
		CassandraDocumentFactory<Flower> factory = new CassandraDocumentFactory<>(KeyDefinitionParser.parse("id:UUID"), CODEC);
		Document document = factory.asDocument(flower);
		assertEquals(new Identifier(id), document.getIdentifier());
		makeDocumentAssertions(new Identifier(id), document);

		Flower deserialized = factory.asPojo(document);
		makeFlowerAssertions(id, accountId, createdAt, updatedAt, deserialized);
	}

	@Test
	public void testMultipartIdentifier()
	throws KeyDefinitionException, InvalidIdentifierException
	{
		CassandraDocumentFactory<Flower> factory = new CassandraDocumentFactory<>(KeyDefinitionParser.parse("account.id as account_id:UUID, name:text"), CODEC);
		Document document = factory.asDocument(flower);
		assertEquals(new Identifier(accountId, "rose"), document.getIdentifier());
		makeDocumentAssertions(new Identifier(accountId, "rose"), document);
		Flower deserialized = factory.asPojo(document);
		makeFlowerAssertions(id, accountId, createdAt, updatedAt, deserialized);
	}

	private void makeDocumentAssertions(Identifier id, Document document)
	{
		assertEquals(id, document.getIdentifier());
		assertNotNull(document.getObject());
		assertNotNull(document.getCreatedAt());
		assertNotNull(document.getUpdatedAt());
	}

	private void makeFlowerAssertions(UUID id, UUID accountId, Date created, Date updated, Flower flower)
	{
		assertNotNull(flower);
		assertEquals(id, flower.getId());
		assertEquals(accountId, flower.getAccountId());
		assertEquals("rose", flower.getName());
		assertNotNull(flower.getColors());
		assertEquals(4, flower.getColors().size());
		assertTrue(flower.getColors().contains("red"));
		assertTrue(flower.getColors().contains("white"));
		assertTrue(flower.getColors().contains("pink"));
		assertTrue(flower.getColors().contains("yellow"));
		assertEquals(created, flower.getCreatedAt());
		assertEquals(updated, flower.getUpdatedAt());
		assertTrue(flower.getIsBlooming());
		assertEquals(3.25f, flower.getHeight(), 0.001f);
	}
}
