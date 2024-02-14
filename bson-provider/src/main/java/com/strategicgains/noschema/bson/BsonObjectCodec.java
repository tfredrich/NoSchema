package com.strategicgains.noschema.bson;

import static java.lang.String.format;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bson.AbstractBsonReader.State;
import org.bson.BSONDecoder;
import org.bson.BSONEncoder;
import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;
import org.bson.BasicBSONEncoder;
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

import com.strategicgains.noschema.document.EntityDescriptor;
import com.strategicgains.noschema.document.FieldDescriptor;
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

	private static final BSONDecoder DECODER = new BasicBSONDecoder();
	private static final BSONEncoder ENCODER = new BasicBSONEncoder();

	private Map<String, Class<T>> discriminators = new HashMap<>();
	Map<Class<?>, EntityDescriptor> descriptorsByClass = new HashMap<>();

	@Override
	public BSONObject encode(T object)
	{
		return DECODER.readObject(asBytes(object));
	}

	@Override
	public T decode(BSONObject bsonObject, String className)
	{
		return fromBytes(ENCODER.encode(bsonObject), className);
	}

	@SuppressWarnings("unchecked")
	private byte[] asBytes(T object)
	{
		register((Class<T>) object.getClass());
		BasicOutputBuffer output = new BasicOutputBuffer();

		try (BsonBinaryWriter bson = new BsonBinaryWriter(output))
		{
			bson.writeStartDocument();
			EncoderContext context = EncoderContext.builder().build();
			writeProperties(bson, object, context);
			bson.writeEndDocument();
			bson.flush();
		}

		return output.getInternalBuffer();
	}

	private T fromBytes(byte[] bytes, String className)
	{
		try (BsonBinaryReader bsonReader = new BsonBinaryReader(ByteBuffer.wrap(bytes)))
		{
			bsonReader.readStartDocument();
			DecoderContext context = DecoderContext.builder().build();
			T instance = readProperties(bsonReader, getClassFor(className), context);
			bsonReader.readEndDocument();
			return instance;
		}
	}

	private T readProperties(BsonBinaryReader bson, Class<T> classFor, DecoderContext context)
	{
		T entity;
		try
		{
			entity = classFor.getDeclaredConstructor(new Class<?>[0]).newInstance();
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

	private Class<T> getClassFor(final String discriminator)
	{
		Class<T> clazz = discriminators.get(discriminator);

		if (clazz != null) return clazz;

		clazz = getClassForName(discriminator);

		if (clazz == null)
		{
			throw new DescriptorException(format("A class could not be found for the discriminator: '%s'.", discriminator));
		}

		register(clazz);
		return clazz;
	}

	@SuppressWarnings("unchecked")
	private Class<T> getClassForName(final String discriminator)
	{
		Class<T> clazz = null;

		try
		{
			clazz = (Class<T>) Class.forName(discriminator);
		}
		catch (final ClassNotFoundException e)
		{
			// Ignore
		}

		return clazz;
	}

	private void register(Class<T> clazz)
	{
		discriminators.putIfAbsent(clazz.getName(), clazz);
	}
}
