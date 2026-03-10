/*
    Copyright 2024-2026, Strategic Gains, Inc.

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
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