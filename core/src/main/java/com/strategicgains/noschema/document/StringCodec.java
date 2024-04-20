package com.strategicgains.noschema.document;

public interface StringCodec<T>
extends Serializer<T, String>, Deserializer<T, String>
{
	String serialize(T object);
	T deserialize(String value, Class<T> clazz);
}