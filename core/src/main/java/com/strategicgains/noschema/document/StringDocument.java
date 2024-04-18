package com.strategicgains.noschema.document;

import com.strategicgains.noschema.Identifier;

/**
 * A Document concrete class that [de]serializes the POJO as a string such as JSON text.
 */
public class StringDocument
extends AbstractFullDocument<String>
{
	/**
	 * Default constructor.
	 */
	public StringDocument()
	{
		super();
	}

	/**
	 * Constructor with type parameter.
	 * 
	 * @param type The class of the object to be stored as BSON in this document.
	 */
	public StringDocument(Class<?> type)
	{
		super(type);
	}

	/**
	 * Constructor with BSON object and type parameters.
	 * 
	 * @param json The serialized object as a JSON string.
	 * @param type The class of the object that is serialized.
	 */
	public StringDocument(String json, Class<?> type)
	{
		super(json, type);
	}

	/**
	 * Constructor with identifier, BSON object, and type parameters.
	 * 
	 * @param id   The identifier of the document.
	 * @param json The serialized object as a JSON string.
	 * @param type The class of the object to be stored.
	 */
	public StringDocument(Identifier id, String json, Class<?> type)
	{
		super(id, json, type);
	}

	/**
	 * Returns a string representation of the document.
	 * 
	 * @return A string representation of the document.
	 */
	@Override
	public String toString()
	{
		return "StringDocument{" + "id=" + getIdentifier() + ", value=" + getValue() + ", type=" + getType()
			+ ", createdAt=" + getCreatedAt() + ", updatedAt=" + getUpdatedAt() + "}";
	}
}
