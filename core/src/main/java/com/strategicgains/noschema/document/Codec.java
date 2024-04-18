package com.strategicgains.noschema.document;

/**
 * Contract for serializing and deserializing from a source object into a target type.
 * 
 * @param <T> the target type.
 * @param <S> the source type.
 * 
 * @author Todd Fredrich
 * @see Serializer
 * @see Deserializer
 */
public interface Codec<T, S>
extends Serializer<T, S>, Deserializer<T, S>
{
}
