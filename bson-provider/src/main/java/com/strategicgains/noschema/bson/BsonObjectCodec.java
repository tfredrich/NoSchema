package com.strategicgains.noschema.bson;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bson.AbstractBsonReader.State;
import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.BsonInvalidOperationException;
import org.bson.BsonWriter;
import org.bson.UuidRepresentation;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.EnumCodecProvider;
import org.bson.codecs.IterableCodecProvider;
import org.bson.codecs.MapCodecProvider;
import org.bson.codecs.UuidCodecProvider;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.io.BasicOutputBuffer;

import com.strategicgains.noschema.document.ObjectCodec;
import com.strategicgains.noschema.exception.DescriptorException;

public final class BsonObjectCodec<T>
implements ObjectCodec<T>
{
	public static final CodecRegistry DEFAULT_CODEC_REGISTRY =
			fromProviders(Arrays.asList(
				new UuidCodecProvider(UuidRepresentation.STANDARD),
				new ValueCodecProvider(),
				new IterableCodecProvider(),
				new MapCodecProvider(),
				new EnumCodecProvider()
			));

	Map<Class<?>, EntityDescriptor> descriptorsByClass = new HashMap<>();

	@Override
	public byte[] serialize(T object)
	{
		BasicOutputBuffer output = new BasicOutputBuffer();

		try (BsonBinaryWriter bson = new BsonBinaryWriter(output))
		{
			bson.writeStartDocument();
			EncoderContext context = EncoderContext.builder().build();
			writeProperties(bson, object, context);
			bson.writeEndDocument();
			bson.flush();
		}

		return output.toByteArray();
	}

	@Override
	public  T deserialize(byte[] bytes, Class<T> clazz)
	{
		try (BsonBinaryReader bsonReader = new BsonBinaryReader(ByteBuffer.wrap(bytes)))
		{
			bsonReader.readStartDocument();
			DecoderContext context = DecoderContext.builder().build();
			T instance = readProperties(bsonReader, clazz, context);
			bsonReader.readEndDocument();
			return instance;
		}
	}

	private T readProperties(BsonBinaryReader bson, Class<T> classFor, DecoderContext context)
	{
		T entity;
		try
		{
			entity = classFor.getDeclaredConstructor().newInstance();
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e)
		{
			throw new DescriptorException(e);
		}

		EntityDescriptor descriptor = buildEntityDescriptor(entity);
		readProperties(bson, descriptor, entity, context);
		return entity;
	}

	private void readProperties(BsonBinaryReader bson, EntityDescriptor descriptor, Object entity, DecoderContext context)
	{
		try
		{
			while(State.END_OF_DOCUMENT != bson.getState())
			{
				String name = bson.readName();
				FieldDescriptor d = descriptor.getField(name);
		
				if (d == null)
				{
					bson.skipValue();
				}
				else if (d.isProperty())
				{
					Object value = context.decodeWithChildContext(d.getCodec(), bson);
					d.set(entity, value);
				}
				else
				{
					Object fieldValue = d.get(entity);

					if (fieldValue != null) {
						readReference(bson, d, fieldValue, context);
					}
				}
			}
		}
		catch(BsonInvalidOperationException e)
		{
			// Log the exception or rethrow it as a different exception.
		}
	}

	private void readReference(BsonBinaryReader bson, FieldDescriptor descriptor, Object value, DecoderContext context)
	{
		bson.readStartDocument();
		readProperties(bson, descriptor.getReference(), value, context);
		bson.readEndDocument();
	}

	private void writeProperties(BsonWriter bson, Object entity, EncoderContext context)
	{
		EntityDescriptor descriptor = buildEntityDescriptor(entity);
		writeProperties(bson, descriptor, entity, context);
	}

	private EntityDescriptor buildEntityDescriptor(Object entity)
	{
		EntityDescriptor descriptor = descriptorsByClass.get(entity.getClass());

		if (descriptor != null) return descriptor;
		descriptor = EntityDescriptor.from(entity, DEFAULT_CODEC_REGISTRY);
		descriptorsByClass.put(descriptor.getDescribedClass(), descriptor);
		return descriptor;
	}

	private void writeProperties(BsonWriter bson, EntityDescriptor descriptor, Object entity, EncoderContext context)
	{
		descriptor.fields().forEach(d -> {
			Object property = d.get(entity);

			if (d.isProperty())
			{
				bson.writeName(d.getName());
				context.encodeWithChildContext(d.getCodec(), bson, property);
			}
			else
			{
				writeReference(bson, d, property, context);
			}
		});
	}

	private void writeReference(BsonWriter bson, FieldDescriptor descriptor, Object value, EncoderContext context)
	{
		bson.writeStartDocument(descriptor.getName());
		writeProperties(bson, descriptor.getReference(), value, context);
		bson.writeEndDocument();
	}
}
