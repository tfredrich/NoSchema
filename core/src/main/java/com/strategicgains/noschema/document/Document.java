package com.strategicgains.noschema.document;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

/**
 * Document is a wrapper for serialized POJOs that are to be stored in a database. It contains
 * metadata about the document, such as creation and update dates, and the primary identifier
 * of the document.
 * 
 * A Document is essentially a key-value pair, where the key is the primary identifier of the
 * document and the value is the serialized POJO.
 * 
 * @param <T> The type of the serialized POJO wrapped by the document (e.g. byte array or string).
 */
public interface Document<T>
extends Identifiable
{
	/**
	 * Checks if the document has an identifier.
	 * 
	 * @return True if the document has an identifier, false otherwise.
	 */
	public boolean hasIdentifier();

	/**
	 * Sets the identifier of the document.
	 * 
	 * @param id The identifier to be set.
	 */
	public void setIdentifier(Identifier id);

	/**
	 * Returns whether the document has a value object or not.
	 * 
	 * @return True if the document has a value object, false otherwise.
	 */
	boolean hasValue();

	/**
	 * Returns the value object from the document.
	 * 
	 * @return The value object of the document.
	 */
	T getValue();

	/**
	 * Sets the value object on the document.
	 * 
	 * @param value The value object to be set.
	 */
	void setValue(T value);
}