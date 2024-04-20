package com.strategicgains.noschema.document;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

/**
 * A concrete Document class that [de]serializes to a byte array, such as a BSON-encoded serialization which is efficient
 * for storage as it's a small, binary format--much smaller than JSON but is not human-readable.
 * 
 * @param <T> The type of the object being wrapped by this document.
 */
public class ByteArrayDocument<T extends Identifiable>
extends AbstractFullDocument<byte[]>
{
	/**
	 * Default constructor.
	 */
	public ByteArrayDocument()
	{
		super();
	}

	/**
	 * Constructor with identifier, BSON object, and type parameters.
	 * 
	 * @param id   The identifier of the document.
	 * @param bytes The serialized object as a byte array.
	 * @param type The class of the object to be stored.
	 */
	public ByteArrayDocument(Identifier id, byte[] bytes, Class<T> type)
	{
		super(id, bytes, type);
	}
}
