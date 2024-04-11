package com.strategicgains.noschema.document;

import com.strategicgains.noschema.Identifier;

/**
 * A concrete Document class that [de]serializes to a byte array, such as a BSON-encoded serialization which is efficient
 * for storage as it's a small, binary format--much smaller than JSON but is not human-readable.
 */
public class ByteArrayDocument
extends AbstractDocument<byte[]>
{
	/**
	 * Default constructor.
	 */
	public ByteArrayDocument()
	{
		super();
	}

	/**
	 * Constructor with type parameter.
	 * 
	 * @param type The class of the object to be stored as BSON in this document.
	 */
	public ByteArrayDocument(Class<?> type)
	{
		super(type);
	}

	/**
	 * Constructor with BSON object and type parameters.
	 * 
	 * @param bytes The serialized object as a byte array.
	 * @param type The class of the object that is serialized.
	 */
	public ByteArrayDocument(byte[] bytes, Class<?> type)
	{
		super(bytes, type);
	}

	/**
	 * Constructor with identifier, BSON object, and type parameters.
	 * 
	 * @param id   The identifier of the document.
	 * @param bytes The serialized object as a byte array.
	 * @param type The class of the object to be stored.
	 */
	public ByteArrayDocument(Identifier id, byte[] bytes, Class<?> type)
	{
		super(id, bytes, type);
	}
}
