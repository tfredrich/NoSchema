package com.strategicgains.noschema.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bson.BSONDecoder;
import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;
import org.bson.BsonBinarySubType;
import org.bson.UuidRepresentation;
import org.bson.internal.UuidHelper;
import org.bson.types.Binary;
import org.junit.Test;

import com.strategicgains.noschema.entity.Flower;

public class JacksonFlowerTest
{
	private static final BSONDecoder DECODER = new BasicBSONDecoder();

	@Test
	public void shouldEncodeAndDecodeWithSameCodec()
	{
		UUID id = UUID.fromString("8dbac965-a1c8-4ad6-a043-5f5a9a5ee8c0");
		UUID accountId = UUID.fromString("a87d3bff-6997-4739-ab4e-ded0cc85700f");
		Date createdAt = new Date(1648598130248L);
		Date updatedAt = new Date(1648598130233L);
		List<String> colors = Arrays.asList("red", "white", "pink", "yellow");
		Flower flower = new Flower(id, "rose", true, 3.25f, colors);
		flower.setAccountId(accountId);
		flower.setCreatedAt(createdAt);
		flower.setUpdatedAt(updatedAt);

		JacksonObjectCodec<Flower> codec = new JacksonObjectCodec<>();
		byte[] bytes = codec.serialize(flower);
		makeBsonAssertions(id, accountId, createdAt, updatedAt, bytes);

		Flower decoded = codec.deserialize(bytes, Flower.class);
		makeFlowerAssertions(id, accountId, createdAt, updatedAt, decoded);
	}

	@Test
	public void shouldEncodeAndDecodeWithDifferentCodec()
	{
		UUID id = UUID.fromString("8dbac965-a1c8-4ad6-a043-5f5a9a5ee8c0");
		UUID accountId = UUID.fromString("a87d3bff-6997-4739-ab4e-ded0cc85700f");
		Date createdAt = new Date(1648598130248L);
		Date updatedAt = new Date(1648598130233L);
		List<String> colors = Arrays.asList("red", "white", "pink", "yellow");
		Flower flower = new Flower(id, "rose", true, 3.25f, colors);
		flower.setAccountId(accountId);
		flower.setCreatedAt(createdAt);
		flower.setUpdatedAt(updatedAt);

		JacksonObjectCodec<Flower> codec1 = new JacksonObjectCodec<>();
		byte[] bytes = codec1.serialize(flower);
		makeBsonAssertions(id, accountId, createdAt, updatedAt, bytes);

		JacksonObjectCodec<Flower> codec2 = new JacksonObjectCodec<>();
		Flower decoded = codec2.deserialize(bytes, Flower.class);
		makeFlowerAssertions(id, accountId, createdAt, updatedAt, decoded);
	}

	@Test
	public void shouldSkipNullValues()
	{
		Flower flower = new Flower();

		JacksonObjectCodec<Flower> codec = new JacksonObjectCodec<>();
		byte[] bytes = codec.serialize(flower);
		makeNullBsonAssertions(bytes);

		Flower decoded = codec.deserialize(bytes, Flower.class);
		makeNullFlowerAssertions(decoded);
	}

	private void makeNullBsonAssertions(byte[] bytes)
	{
        BSONObject bson = makeBsonIdAssertions(null, bytes);

        assertNull(bson.get("createdAt"));
        assertNull(bson.get("updatedAt"));
        assertNull(bson.get("name"));
        assertNull(bson.get("colors"));
        assertNull(((BSONObject) bson.get("account")).get("id"));
	}

	private void makeNullFlowerAssertions(Flower flower)
	{
		assertNotNull(flower);
		assertNull(flower.getId());
		assertNull(flower.getName());
		assertNull(flower.getColors());
		assertNull(flower.getAccountId());
		assertNull(flower.getCreatedAt());
		assertNull(flower.getUpdatedAt());
		assertTrue(flower.getIsBlooming());
		assertNull(flower.getHeight());
	}

	private void makeBsonAssertions(UUID id, UUID accountId, Date created, Date updated, byte[] bytes)
	{
		BSONObject bson = makeBsonIdAssertions(id, bytes);

		Binary bsonAccountId = (Binary) ((BSONObject) bson.get("account")).get("id");
		assertEquals(BsonBinarySubType.UUID_STANDARD.getValue(), bsonAccountId.getType());
		assertEquals(accountId, UuidHelper.decodeBinaryToUuid(bsonAccountId.getData(), bsonAccountId.getType(), UuidRepresentation.STANDARD));
	
		assertEquals("rose", bson.get("name"));

		@SuppressWarnings("unchecked")
		List<String> colors = (List<String>) bson.get("colors");
		assertEquals(4, colors.size());
		assertTrue(colors.contains("red"));
		assertTrue(colors.contains("white"));
		assertTrue(colors.contains("pink"));
		assertTrue(colors.contains("yellow"));
		assertEquals(created, bson.get("createdAt"));
		assertEquals(updated, bson.get("updatedAt"));
		assertTrue((Boolean) bson.get("isBlooming"));
		assertEquals(3.25f, (Double) bson.get("height"), 0.001f);
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

	private BSONObject makeBsonIdAssertions(UUID id, byte[] bytes)
	{
		BSONObject bson = DECODER.readObject(bytes);
        assertNotNull(bson);
        Binary bsonId = (Binary) bson.get("id");

		if (id == null)
		{
			assertNull(bsonId);
			return bson;
		}

        assertNotNull(bsonId);
        assertEquals(BsonBinarySubType.UUID_STANDARD.getValue(), bsonId.getType());
        assertEquals(id, UuidHelper.decodeBinaryToUuid(bsonId.getData(), bsonId.getType(), UuidRepresentation.STANDARD));
		return bson;
	}
}
