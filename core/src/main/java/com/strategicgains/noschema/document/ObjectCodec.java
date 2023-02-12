package com.strategicgains.noschema.document;

import org.bson.BSONObject;

public interface ObjectCodec<T>
{
	BSONObject encode(T object);
	T decode(BSONObject bsonObject, String discriminator);

	byte[] asBytes(T object);
	T fromBytes(byte[] bytes, String className);
}