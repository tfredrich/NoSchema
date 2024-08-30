package com.strategicgains.noschema.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.strategicgains.noschema.entity.Flower;
import com.strategicgains.noschema.gson.GsonObjectCodec;

public class GsonFlowerTest
{
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

		GsonObjectCodec<Flower> codec = new GsonObjectCodec<>();
		byte[] bytes = codec.serialize(flower);
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

		GsonObjectCodec<Flower> codec1 = new GsonObjectCodec<>();
		GsonObjectCodec<Flower> codec2 = new GsonObjectCodec<>();
		byte[] bytes = codec1.serialize(flower);
		Flower decoded = codec2.deserialize(bytes, Flower.class);
		makeFlowerAssertions(id, accountId, createdAt, updatedAt, decoded);
	}

	@Test
	public void shouldSkipNullValues()
	{
		Flower flower = new Flower();

		GsonObjectCodec<Flower> codec = new GsonObjectCodec<>();
		byte[] bytes = codec.serialize(flower);
		Flower decoded = codec.deserialize(bytes, Flower.class);
		makeNullFlowerAssertions(decoded);
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
		assertNull(flower.getIsBlooming());
		assertNull(flower.getHeight());
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
