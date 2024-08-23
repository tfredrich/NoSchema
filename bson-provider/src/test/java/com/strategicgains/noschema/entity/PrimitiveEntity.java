package com.strategicgains.noschema.entity;

import java.util.UUID;

/**
 * This sample entity is in no way invaded by any classes in the NoSchema library. It's a plain POJO that
 * is mapped externally into a Document entity that can be stored in the database.
 * 
 * @author Todd Fredrich
 */
public class PrimitiveEntity
extends AbstractTimestampedEntity<UUID>
{
	private int primitiveInt = -1;
	private boolean isPrimitiveBoolean = true;
	private double primitiveDouble = -2.0;
	private long primitiveLong = -3L;
	private short primitiveShort = -4;
	private byte primitiveByte = -5;
	private byte[] primitiveByteArray = new byte[] { 0, 1, 2, 3, 4, 5 };
	private char primitiveChar = 'a';
	private float primitiveFloat = -6.0f;

	public PrimitiveEntity() {
		super();
	}

	public PrimitiveEntity(UUID id)
	{
		super(id);
	}

	public int getPrimitiveInt()
	{
		return primitiveInt;
	}

	public boolean isPrimitiveBoolean()
    {
        return isPrimitiveBoolean;
    }

	public double getPrimitiveDouble()
	{
		return primitiveDouble;
	}

	public long getPrimitiveLong()
	{
		return primitiveLong;
	}

	public short getPrimitiveShort()
	{
		return primitiveShort;
	}

	public byte getPrimitiveByte()
	{
		return primitiveByte;
	}

	public char getPrimitiveChar()
	{
		return primitiveChar;
	}

	public float getPrimitiveFloat()
	{
		return primitiveFloat;
	}

	public byte[] getPrimitiveByteArray()
	{
		return primitiveByteArray;
	}

	@Override
	public String toString()
	{
		return "Primitive [id=" + getId() + ", primitiveInt=" + primitiveInt + ", isPrimitiveBoolean="
			+ isPrimitiveBoolean + ", primitiveDouble=" + primitiveDouble + ", primitiveLong=" + primitiveLong
			+ ", primitiveShort=" + primitiveShort + ", primitiveByte=" + primitiveByte + ", primitiveChar="
			+ primitiveChar + ", primitiveFloat=" + primitiveFloat + "]";
	}
}