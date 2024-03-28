package com.strategicgains.noschema.document;

import java.util.Date;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

public interface Document
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
	 * Returns the creation date of the document.
	 * 
	 * @return The creation date of the document.
	 */
	Date getCreatedAt();

	/**
	 * Sets the creation date of the document.
	 * 
	 * @param createdAt The creation date to be set.
	 */
	public void setCreatedAt(Date createdAt);

	/**
	 * Returns the update date of the document.
	 * 
	 * @return The update date of the document.
	 */
	Date getUpdatedAt();

	/**
	 * Sets the update date of the document.
	 * 
	 * @param updatedAt The update date to be set.
	 */
	void setUpdatedAt(Date updatedAt);

	/**
	 * Returns whether the document has an object or not.
	 * 
	 * @return True if the document has an object, false otherwise.
	 */
	boolean hasObject();
}