package com.strategicgains.noschema.document;

/**
 * Interface for deserializing a source object into a target object.
 * 
 * @param <T> the target type to deserialize into.
 * @param <S> the source type to deserialize from.
 */
public interface Deserializer<T, S>
{
	T deserialize(S source, Class<T> clazz);
}
