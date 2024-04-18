package com.strategicgains.noschema.document;

import com.strategicgains.noschema.Identifier;

public class IndexDocument
extends AbstractDocument<Identifier>
{
	/**
	 * Default constructor.
	 */
	protected IndexDocument()
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
	protected IndexDocument(Identifier id)
	{
		super(id);
	}
}
