package com.strategicgains.noschema.bson;

import java.lang.reflect.Field;

import org.bson.codecs.Codec;

import com.strategicgains.noschema.exception.DescriptorException;

/**
 * Describes the codec to use for a field value or the EntityDescriptor if the value is a reference
 * to another class.
 * 
 * @author tfredrich
 */
public class FieldDescriptor
{
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

	public Codec<? super Object> getCodec()
	{
		return codec;
	}

	public boolean isProperty()
	{
		return hasCodec();
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
