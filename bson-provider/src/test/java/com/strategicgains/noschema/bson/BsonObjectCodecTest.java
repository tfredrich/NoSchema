package com.strategicgains.noschema.bson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bson.BSONDecoder;
import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;
import org.junit.Test;

import com.strategicgains.noschema.entity.Flower;

public class BsonObjectCodecTest
{
	private static final BSONDecoder DECODER = new BasicBSONDecoder();


	private BsonObjectCodec<Flower> codec = new BsonObjectCodec<>();

	@Test
	public void shouldEncodeAndDecode()
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

		byte[] bytes = codec.serialize(flower);
		makeBsonAssertions(id, accountId, createdAt, updatedAt, bytes);

		Flower decoded = codec.deserialize(bytes, Flower.class);
		makeFlowerAssertions(id, accountId, createdAt, updatedAt, decoded);
	}

	private void makeBsonAssertions(UUID id, UUID accountId, Date created, Date updated, byte[] bytes)
	{
		BSONObject bson = DECODER.readObject(bytes);
		assertNotNull(bson);
		assertNotNull(bson.get("id"));
		assertNotNull(((BSONObject) bson.get("account")).get("id"));
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
		assertEquals(0, bson.get("primitiveInt"));
		assertEquals(false, bson.get("isProtected"));
		assertEquals(0.0, bson.get("primitiveDouble"));
		assertEquals(0L, bson.get("primitiveLong"));
		assertEquals((short) 0, bson.get("primitiveShort"));
		assertEquals((byte) 0, bson.get("primitiveByte"));
		assertEquals(' ', bson.get("primitiveChar"));
		assertEquals(0.0f, (Double) bson.get("primitiveFloat"), 0.001f);
	}

	private void makeFlowerAssertions(UUID id, UUID accountId, Date created, Date updated, Flower flower) {
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
		assertEquals(0, flower.getPrimitiveInt());
		assertEquals(false, flower.isPrimitiveBoolean());
		assertEquals(0.0, flower.getPrimitiveDouble(), 0.001);
		assertEquals(0L, flower.getPrimitiveLong());
		assertEquals((short) 0, flower.getPrimitiveShort());
		assertEquals((byte) 0, flower.getPrimitiveByte());
		assertEquals(' ', flower.getPrimitiveChar());
		assertEquals(0.0f, flower.getPrimitiveFloat(), 0.001f);
	}
}
