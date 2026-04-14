/*
    Copyright 2024-2026, Strategic Gains, Inc.

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package com.strategicgains.noschema.gson;

import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.strategicgains.noschema.document.ObjectCodec;

/**
 * A Gson-based implementation of ObjectCodec that serializes and deserializes objects based on the configuration of the
 * Gson instance passed into the constructor.
 * 
 * @param <T> the type of object to serialize and deserialize.
 * @see com.google.gson.Gson
 * @author Todd Fredrich
 */
public class GsonObjectCodec<T>
implements ObjectCodec<T>
{
	private static final String TIME_POINT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	private final Gson gson;

	/**
	 * Create a new GsonObjectCodec instance using a default Gson instance.
	 */
	public GsonObjectCodec()
	{
		this(createDefaultGson());
	}

	/**
	 * Create a new GsonObjectCodec instance using the given Gson instance.
	 * 
	 * @param gson a pre-configured Gson instance.
	 */
	public GsonObjectCodec(Gson gson)
	{
		this.gson = gson;
	}

	@Override
	public byte[] serialize(T object)
	{
		String gsonStr = gson.toJson(object);
		return gsonStr.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public T deserialize(byte[] bytes, Class<T> clazz)
	{
		String jsonStr = new String(bytes, StandardCharsets.UTF_8);
		return gson.fromJson(jsonStr, clazz);
	}

	private static Gson createDefaultGson()
	{
		return new GsonBuilder()
			.disableHtmlEscaping()
			.setDateFormat(TIME_POINT_FORMAT)
			.excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
			.create();
	}
}
