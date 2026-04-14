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
package com.strategicgains.noschema.jackson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.strategicgains.noschema.document.ObjectCodec;
import com.strategicgains.noschema.exception.ConfigurationException;

/**
 * A Jackson-based implementation of ObjectCodec that serializes and deserializes
 * objects based on the configuration of the ObjectMapper passed into the constructor.
 * 
 * @param <T> the type of object to serialize and deserialize.
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @author Todd Fredrich
 */
public final class JacksonObjectCodec<T>
implements ObjectCodec<T>
{
	private static final String TIME_POINT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	private ObjectMapper mapper;

	/**
	 * Create a new JacksonObjectCodec instance using a default ObjectMapper.
	 */
	public JacksonObjectCodec()
    {
        this(createDefaultObjectMapper());
    }

	/**
	 * Create a new JacksonObjectCodec instance using the given ObjectMapper.
	 * 
	 * @param mapper an ObjectMapper instance
	 */
	public JacksonObjectCodec(ObjectMapper mapper)
	{
		super();
		this.mapper = mapper;
	}

	@Override
	public byte[] serialize(T object)
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			mapper.writeValue(baos, object);
			return baos.toByteArray();
		}
		catch (IOException e)
		{
			throw new ConfigurationException(e);
		}		
	}

	public T deserialize(byte[] bytes, Class<T> clazz)
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		try
		{
			return mapper.readValue(bais, clazz);
		}
		catch (IOException e)
		{
			throw new ConfigurationException(e);
		}
	}

	private static ObjectMapper createDefaultObjectMapper()
	{
		ObjectMapper mapper = new ObjectMapper();
		mapper
			.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
	
			// Ignore additional/unknown properties in a payload.
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			
			// Only serialize populated properties (do no serialize nulls)
			.setSerializationInclusion(JsonInclude.Include.NON_NULL)
			
			// Use fields directly.
			.setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
			
			// Ignore accessor and mutator methods (use fields per above).
			.setVisibility(PropertyAccessor.GETTER, Visibility.NONE)
			.setVisibility(PropertyAccessor.SETTER, Visibility.NONE)
			.setVisibility(PropertyAccessor.IS_GETTER, Visibility.NONE)
			
			// Set default date output format.
			.setDateFormat(new SimpleDateFormat(TIME_POINT_FORMAT));
		return mapper;
	}
}
