package com.strategicgains.noschema.bson;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.withUuidRepresentation;

import java.lang.reflect.Field;

import org.bson.UuidRepresentation;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;

import com.strategicgains.noschema.exception.DescriptorException;

/**
 * Describes the codec to use for a field value or the EntityDescriptor if the value is a reference
 * to another class.
 * 
 * @author tfredrich
 */
public class FieldDescriptor
{
	public static final CodecRegistry PRIMITIVE_CODEC_REGISTRY = 
		withUuidRepresentation(
			fromProviders(new PrimitiveCodecProvider()),
			UuidRepresentation.STANDARD
		);

	/**
	 * The field being mapped.
	 */
	private Field field;

	/**
	 * The codec to process the field value. If null, there is no appropriate codec and is likely a referenced class.
	 * In this case, the referenced class will be described by an EntityDescriptor in the 'reference' property.
	 */
	private Codec<? super Object> codec;

	/**
	 * If there is no codec to process the field, then the referenced class will be treated as nestec, individual
	 * properties described by this EntityDescriptor.
	 */
	private EntityDescriptor reference;

	public FieldDescriptor(Field field)
	{
		this(field, null);
	}

	public FieldDescriptor(Field field, Codec<? super Object> codec)
	{
		super();
		this.field = field;
		setCodec(codec);
	}

	public Object get(Object model)
	{
		try
		{
			return field.get(model);
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			throw new DescriptorException(e);
		}
	}

	public void set(Object model, Object value)
	{
		try
		{
			field.set(model, value);
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			throw new DescriptorException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public Codec<? super Object> getCodec()
	{
		if (isPrimitive())
		{
			return (Codec<? super Object>) PRIMITIVE_CODEC_REGISTRY.get(field.getType());
		}

		return codec;
	}

	public boolean isProperty()
	{
		return (hasCodec() || isPrimitive());
	}

	public boolean isPrimitive()
	{
        return field.getType().isPrimitive();
	}

	public boolean isGeneric()
	{
		return (!hasCodec() && !isReference() && field.getType().isAssignableFrom(Object.class));
	}

	public boolean hasCodec()
	{
		return (codec != null);
	}

	public void setCodec(Codec<? super Object> codec)
	{
		this.codec = codec;
	}

	public String getName()
	{
		return field.getName();
	}

	public EntityDescriptor getReference()
	{
		return reference;
	}

	public boolean isReference()
	{
		return (reference != null);
	}

	public void setReference(EntityDescriptor reference)
	{
		this.reference = reference;
	}

	public String toString()
	{
		return String.format("field=%s, codec={%s}, reference={%s}", field.getName(), codec, reference);
	}
}
