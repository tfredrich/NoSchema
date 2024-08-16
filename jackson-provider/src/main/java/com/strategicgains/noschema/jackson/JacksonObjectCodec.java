package com.strategicgains.noschema.jackson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strategicgains.noschema.document.ObjectCodec;
import com.strategicgains.noschema.exception.ConfigurationException;

import de.undercouch.bson4jackson.BsonFactory;

/**
 * A Jackson-based implementation of ObjectCodec that serializes and deserializes
 * objects based on the configuration of the ObjectMapper passed into the constructor.
 * 
 * The default constructor creates a new ObjectMapper using the bson4jackson BsonFactory
 * to support BSON serialization.
 * 
 * @param <T> the type of object to serialize and deserialize.
 */
public final class JacksonObjectCodec<T>
implements ObjectCodec<T>
{
	private ObjectMapper mapper;

	public JacksonObjectCodec()
	{
		this(new ObjectMapper(new BsonFactory()));
	}

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
}
