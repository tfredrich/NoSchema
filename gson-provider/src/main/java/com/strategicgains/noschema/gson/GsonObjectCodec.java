package com.strategicgains.noschema.gson;

import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.strategicgains.noschema.document.ObjectCodec;

public class GsonObjectCodec<T>
	implements ObjectCodec<T>
{
	private final Gson gson;

	public GsonObjectCodec()
	{
		gson = new Gson();
	}

	@Override
	public byte[] serialize(T object)
	{
		String gsonStr = gson.toJson(object);
		return gsonStr.getBytes(StandardCharsets.UTF_8);
	}

	public T deserialize(byte[] bytes, Class<T> clazz)
	{
		String jsonStr = new String(bytes, StandardCharsets.UTF_8);
		return gson.fromJson(jsonStr, clazz);
	}
}
