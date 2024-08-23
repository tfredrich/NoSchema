package com.strategicgains.noschema.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.UUID;

import org.junit.Test;

import com.strategicgains.noschema.entity.PrimitiveEntity;

public class JacksonPrimitivesTest
{
	private JacksonObjectCodec<PrimitiveEntity> codec = new JacksonObjectCodec<>();

	@Test
	public void shouldEncodeAndDecode()
	{
		UUID id = UUID.fromString("8dbac965-a1c8-4ad6-a043-5f5a9a5ee8c0");
		UUID accountId = UUID.fromString("a87d3bff-6997-4739-ab4e-ded0cc85700f");
		Date createdAt = new Date(1648598130248L);
		Date updatedAt = new Date(1648598130233L);
		PrimitiveEntity entity = new PrimitiveEntity(id);
		entity.setAccountId(accountId);
		entity.setCreatedAt(createdAt);
		entity.setUpdatedAt(updatedAt);

		byte[] bytes = codec.serialize(entity);
		PrimitiveEntity decoded = codec.deserialize(bytes, PrimitiveEntity.class);
		makeEntityAssertions(id, accountId, createdAt, updatedAt, decoded);
	}

	private void makeEntityAssertions(UUID id, UUID accountId, Date created, Date updated, PrimitiveEntity entity)
	{
		assertNotNull(entity);
		assertEquals(id, entity.getId());
		assertEquals(accountId, entity.getAccountId());
		assertEquals(created, entity.getCreatedAt());
		assertEquals(updated, entity.getUpdatedAt());
		assertEquals(-1, entity.getPrimitiveInt());
		assertEquals(true, entity.isPrimitiveBoolean());
		assertEquals(-2.0, entity.getPrimitiveDouble(), 0.001);
		assertEquals(-3L, entity.getPrimitiveLong());
		assertEquals((short) -4, entity.getPrimitiveShort());
		assertEquals((byte) -5, entity.getPrimitiveByte());
		byte[] bytes = entity.getPrimitiveByteArray();
		assertNotNull(bytes);
		assertEquals(6, bytes.length);
		assertEquals('a', entity.getPrimitiveChar());
		assertEquals(-6.0f, entity.getPrimitiveFloat(), 0.001f);
	}
}
