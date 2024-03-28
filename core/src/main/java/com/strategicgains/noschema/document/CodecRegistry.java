package com.strategicgains.noschema.document;

import java.util.HashMap;
import java.util.Map;

public class CodecRegistry<T extends Document>
{
	private Map<Class<?>, Codec<T, ?>> codecs = new HashMap<>();

	public CodecRegistry()
	{
		super();
	}

	public void register(Class<?> clazz, Codec<T, ?> codec)
	{
		codecs.put(clazz, codec);
	}

	public Codec<T, ?> get(Class<?> clazz)
	{
		return codecs.get(clazz);
	}
}
