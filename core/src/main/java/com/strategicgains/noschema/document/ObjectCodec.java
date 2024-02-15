package com.strategicgains.noschema.document;

public interface ObjectCodec<T>
{
	byte[] serialize(T object);
	T deserialize(byte[] bytes, Class<T> clazz);
}