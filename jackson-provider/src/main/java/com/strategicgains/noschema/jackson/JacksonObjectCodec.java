package com.strategicgains.noschema.jackson;

import com.strategicgains.noschema.document.ObjectCodec;

public final class JacksonObjectCodec<T>
implements ObjectCodec<T>
{
	public JacksonObjectCodec()
	{
	}

	@Override
	public byte[] serialize(T object)
	{
		return null;
	}

	public T deserialize(byte[] bytes, Class<T> clazz)
	{
		return null;
	}
}
