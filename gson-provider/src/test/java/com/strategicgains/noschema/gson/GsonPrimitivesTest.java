/*
    Copyright 2024-2026, Strategic Gains, Inc.

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package com.strategicgains.noschema.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.UUID;

import org.junit.Test;

import com.strategicgains.noschema.entity.PrimitiveEntity;

public class GsonPrimitivesTest
{
	private GsonObjectCodec<PrimitiveEntity> codec = new GsonObjectCodec<>();

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
