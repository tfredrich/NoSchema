package com.strategicgains.noschema.bson;

import java.util.HashMap;
import java.util.Map;

import org.bson.codecs.BooleanCodec;
import org.bson.codecs.ByteArrayCodec;
import org.bson.codecs.ByteCodec;
import org.bson.codecs.CharacterCodec;
import org.bson.codecs.Codec;
import org.bson.codecs.DoubleCodec;
import org.bson.codecs.FloatCodec;
import org.bson.codecs.IntegerCodec;
import org.bson.codecs.LongCodec;
import org.bson.codecs.ShortCodec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class PrimitiveCodecProvider
implements CodecProvider
{
	private Map<Class<?>, Codec<?>> codecs = new HashMap<>();

	public PrimitiveCodecProvider()
	{
		addCodecs();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry)
	{
		return (Codec<T>) codecs.get(clazz);
	}

    private void addCodecs() {
        addCodec(boolean.class, new BooleanCodec());
        addCodec(double.class, new DoubleCodec());
        addCodec(int.class, new IntegerCodec());
        addCodec(long.class, new LongCodec());
        addCodec(char.class, new CharacterCodec());
        addCodec(byte.class, new ByteCodec());
        addCodec(short.class, new ShortCodec());
        addCodec(byte[].class, new ByteArrayCodec());
        addCodec(float.class, new FloatCodec());
    }

    private <T> void addCodec(Class<T> clazz, final Codec<T> codec) {
        codecs.put(clazz, codec);
    }
}
