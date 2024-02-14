package com.strategicgains.noschema.document;

import org.bson.BSONObject;

public interface ObjectCodec<T>
{
	BSONObject encode(T object);
	T decode(BSONObject bsonObject, String className);
}