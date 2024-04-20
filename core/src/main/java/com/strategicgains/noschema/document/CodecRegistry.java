package com.strategicgains.noschema.document;

import java.util.HashMap;
import java.util.Map;

public class CodecRegistry
{
	private Map<Class<?>, Codec<?, ?>> codecs = new HashMap<>();

	public CodecRegistry()
	{
		super();
	}

	public void register(Class<?> clazz, Codec<?, ?> codec)
	{
		codecs.put(clazz, codec);
	}

	@SuppressWarnings("unchecked")
	public <T, S> Codec<T, S> get(Class<T> clazz)
	{
		return (Codec<T, S>) codecs.get(clazz);
	}
}
