package com.strategicgains.noschema.bson;

import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.configuration.CodecRegistries.withUuidRepresentation;
import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;
import static org.bson.codecs.pojo.Conventions.SET_PRIVATE_FIELDS_CONVENTION;

import java.nio.ByteBuffer;

import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.UuidRepresentation;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.Encoder;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.codecs.pojo.PojoCodecProvider.Builder;
import org.bson.conversions.Bson;
import org.bson.io.BasicOutputBuffer;

import com.strategicgains.noschema.document.ObjectCodec;

/**
 * A BSON-based implementation of ObjectCodec that serializes and
 * deserializes objects to and from BSON.
 * 
 * This implementation uses the MongoDB Java driver's BSON library
 * to serialize and deserialize objects.
 * 
 * @param <T> the type of object to serialize and deserialize.
 */
public final class BsonObjectCodec<T>
implements ObjectCodec<T>
{
	private CodecRegistry codecRegistry;

	public BsonObjectCodec()
	{
		this(createCodecBuilder().build());
	}

	public BsonObjectCodec(Class<?>... classes)
	{
		this(createCodecBuilder()
			.register(classes)
			.build()
		);
	}

	BsonObjectCodec(CodecProvider codecProvider)
    {
		this.codecRegistry = withUuidRepresentation(
			fromRegistries(
				Bson.DEFAULT_CODEC_REGISTRY, 
				fromProviders(asList(codecProvider))
			),
			UuidRepresentation.STANDARD
		);
    }

	@SuppressWarnings("unchecked")
	@Override
	public byte[] serialize(T object)
	{
		BasicOutputBuffer buffer = new BasicOutputBuffer();

		try (BsonBinaryWriter writer = new BsonBinaryWriter(buffer))
		{
			((Encoder<T>) codecRegistry.get(object.getClass()))
				.encode(writer, object, EncoderContext.builder().build());
		}

		return buffer.toByteArray();
	}

	@Override
	public T deserialize(byte[] bytes, Class<T> clazz)
	{
		try (BsonBinaryReader bsonReader = new BsonBinaryReader(ByteBuffer.wrap(bytes)))
		{
			return codecRegistry.get(clazz).decode(bsonReader, DecoderContext.builder().build());
		}
	}

	private static Builder createCodecBuilder()
	{
		return PojoCodecProvider
			.builder()
				.conventions(asList(SET_PRIVATE_FIELDS_CONVENTION, ANNOTATION_CONVENTION))
				.automatic(true);
	}
}
