package com.strategicgains.noschema.gson;

import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.strategicgains.noschema.document.ByteArrayCodec;

public class GsonObjectCodec<T>
	implements ByteArrayCodec<T>
{
	private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private final Gson gson;

	public GsonObjectCodec()
	{
		gson = new GsonBuilder()
			.disableHtmlEscaping()
			.setDateFormat(TIMESTAMP_FORMAT)
			.excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
			.create();
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
