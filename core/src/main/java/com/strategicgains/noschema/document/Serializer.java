package com.strategicgains.noschema.document;

/**
 * Interface for serializing objects to a specific type.
 * 
 * @param <T> the type of object to serialize.
 * @param <S> the type of the serialized object.
 */
public interface Serializer<T, S>
{
	S serialize(T object);
}
