package com.strategicgains.noschema.gson;

import java.nio.charset.StandardCharsets;

import com.strategicgains.noschema.document.ByteArrayCodec;

public class GsonByteArrayCodec<T>
implements ByteArrayCodec<T>
{
	private final GsonStringCodec<T> gson;

	public GsonByteArrayCodec()
	{
		gson = new GsonStringCodec<>();
	}

	@Override
	public byte[] serialize(T object)
	{
		String gsonStr = gson.serialize(object);
		return gsonStr.getBytes(StandardCharsets.UTF_8);
	}

	public T deserialize(byte[] bytes, Class<T> clazz)
	{
		String jsonStr = new String(bytes, StandardCharsets.UTF_8);
		return gson.deserialize(jsonStr, clazz);
	}
}
