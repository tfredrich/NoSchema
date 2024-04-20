package com.strategicgains.noschema.gson;

import java.lang.reflect.Modifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.strategicgains.noschema.document.StringCodec;

public class GsonStringCodec<T>
implements StringCodec<T>
{
	private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private final Gson gson;

	public GsonStringCodec()
	{
		gson = new GsonBuilder()
				.disableHtmlEscaping()
				.setDateFormat(TIMESTAMP_FORMAT)
				.excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
				.create();
	}

	@Override
	public String serialize(T object)
	{
		return gson.toJson(object);
	}

	public T deserialize(String json, Class<T> clazz)
	{
		return gson.fromJson(json, clazz);
	}
}
