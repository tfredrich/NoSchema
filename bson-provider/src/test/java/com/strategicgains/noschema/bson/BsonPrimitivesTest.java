package com.strategicgains.noschema.bson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.UUID;

import org.bson.BSONDecoder;
import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;
import org.bson.BsonBinarySubType;
import org.bson.UuidRepresentation;
import org.bson.internal.UuidHelper;
import org.bson.types.Binary;
import org.junit.Test;

import com.strategicgains.noschema.entity.PrimitiveEntity;

public class BsonPrimitivesTest
{
	private static final BSONDecoder DECODER = new BasicBSONDecoder();


	private BsonObjectCodec<PrimitiveEntity> codec = new BsonObjectCodec<>();

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
		makeBsonAssertions(id, accountId, createdAt, updatedAt, bytes);

		PrimitiveEntity decoded = codec.deserialize(bytes, PrimitiveEntity.class);
		makeEntityAssertions(id, accountId, createdAt, updatedAt, decoded);
	}

	private void makeBsonAssertions(UUID id, UUID accountId, Date created, Date updated, byte[] bytes)
	{
		BSONObject bson = DECODER.readObject(bytes);
		assertNotNull(bson);
		Binary bsonId = (Binary) bson.get("id");
		assertEquals(BsonBinarySubType.UUID_STANDARD.getValue(), bsonId.getType());
		assertEquals(id, UuidHelper.decodeBinaryToUuid(bsonId.getData(), bsonId.getType(), UuidRepresentation.STANDARD));

		Binary bsonAccountId = (Binary) bson.get("accountId");
		assertEquals(BsonBinarySubType.UUID_STANDARD.getValue(), bsonAccountId.getType());
		assertEquals(accountId, UuidHelper.decodeBinaryToUuid(bsonAccountId.getData(), bsonAccountId.getType(), UuidRepresentation.STANDARD));
	
		assertEquals(created, bson.get("createdAt"));
		assertEquals(updated, bson.get("updatedAt"));
		assertEquals(0, bson.get("primitiveInt"));
		assertEquals(false, bson.get("primitiveBoolean"));
		assertEquals(0.0, bson.get("primitiveDouble"));
		assertEquals(0L, bson.get("primitiveLong"));
		assertEquals(0, bson.get("primitiveShort"));
		assertEquals(0, bson.get("primitiveByte"));
		byte[] byteArray = (byte[]) bson.get("primitiveByteArray");
		assertNotNull(byteArray);
		assertEquals(0, byteArray.length);
		assertEquals(" ", bson.get("primitiveChar"));
		assertEquals(0.0f, (Double) bson.get("primitiveFloat"), 0.001f);
	}

	private void makeEntityAssertions(UUID id, UUID accountId, Date created, Date updated, PrimitiveEntity entity)
	{
		assertNotNull(entity);
		assertEquals(id, entity.getId());
		assertEquals(accountId, entity.getAccountId());
		assertEquals(created, entity.getCreatedAt());
		assertEquals(updated, entity.getUpdatedAt());
		assertEquals(0, entity.getPrimitiveInt());
		assertEquals(false, entity.isPrimitiveBoolean());
		assertEquals(0.0, entity.getPrimitiveDouble(), 0.001);
		assertEquals(0L, entity.getPrimitiveLong());
		assertEquals((short) 0, entity.getPrimitiveShort());
		assertEquals((byte) 0, entity.getPrimitiveByte());
		byte[] bytes = entity.getPrimitiveByteArray();
		assertNotNull(bytes);
		assertEquals(0, bytes.length);
		assertEquals(' ', entity.getPrimitiveChar());
		assertEquals(0.0f, entity.getPrimitiveFloat(), 0.001f);
	}
}
