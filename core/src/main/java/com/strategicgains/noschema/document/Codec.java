package com.strategicgains.noschema.document;

public interface Codec<T1, T2>
extends Serializer<T1, T2>, Deserializer<T1, T2>
{
}
