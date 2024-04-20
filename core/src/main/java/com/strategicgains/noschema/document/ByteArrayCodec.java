package com.strategicgains.noschema.document;

public interface ByteArrayCodec<T>
extends Serializer<T, byte[]>, Deserializer<T, byte[]>
{
	byte[] serialize(T object);
	T deserialize(byte[] bytes, Class<T> clazz);
}