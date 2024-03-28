package com.strategicgains.noschema.document;

import java.util.Date;

import com.strategicgains.noschema.Identifier;

public class IndexDocument
implements Document
{
	/**
	 * The key components to store in a key/value or columnar store.
	 */
	private Identifier identifier;

	/**
	 * The timestamp this document was originally created.
	 */
	private Date createdAt;

	/**
	 * The timestamp the document was last updated.
	 */
	private Date updatedAt;

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
		this();
		setIdentifier(id);

		Date now = new Date();
		setCreatedAt(now);
		setUpdatedAt(now);
	}

	/**
	 * Returns the identifier of the document.
	 * 
	 * @return The identifier of the document.
	 */
	@Override
	public Identifier getIdentifier()
	{
		return (hasIdentifier() ? new Identifier(identifier) : null);
	}

	/**
	 * Checks if the document has an identifier.
	 * 
	 * @return True if the document has an identifier, false otherwise.
	 */
	@Override
	public boolean hasIdentifier()
	{
		return (identifier != null);
	}

	/**
	 * Sets the identifier of the document.
	 * 
	 * @param id The identifier to be set.
	 */
	@Override
	public void setIdentifier(Identifier id)
	{
		this.identifier = (id != null ? new Identifier(id) : null);
	}

	/**
	 * Returns the creation date of the document.
	 * 
	 * @return The creation date of the document.
	 */
	@Override
	public Date getCreatedAt()
	{
		return createdAt;
	}

	/**
	 * Sets the creation date of the document.
	 * 
	 * @param createdAt The creation date to be set.
	 */
	@Override
	public void setCreatedAt(Date createdAt)
	{
		this.createdAt = createdAt;
	}

	/**
	 * Returns the update date of the document.
	 * 
	 * @return The update date of the document.
	 */
	@Override
	public Date getUpdatedAt()
	{
		return updatedAt;
	}

	/**
	 * Sets the update date of the document.
	 * 
	 * @param updatedAt The update date to be set.
	 */
	@Override
	public void setUpdatedAt(Date updatedAt)
	{
		this.updatedAt = updatedAt;
	}

	@Override
	public boolean hasObject()
	{
		return false;
	}

	/**
	 * Returns a string representation of the document.
	 * 
	 * @return A string representation of the document.
	 */
	@Override
	public String toString()
	{
		return "AbstractDocument{" + "id=" + identifier.toString()
			+ ", createdAt=" + getCreatedAt() + ", updatedAt=" + getUpdatedAt() + "}";
	}
}
