package com.strategicgains.noschema.gson;

import java.lang.reflect.Modifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.strategicgains.noschema.document.Deserializer;
import com.strategicgains.noschema.document.Serializer;

public class JsonStringSerialization<T>
implements Serializer<T, String>, Deserializer<T, String>
{
	private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private final Gson gson;

	public JsonStringSerialization()
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
