package com.strategicgains.noschema.document;

import java.util.Date;

import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.Timestamped;

public abstract class AbstractFullDocument<T>
extends AbstractDocument<T>
implements Timestamped
{
	/**
	 * The fully-qualified class name of the object stored in this document. This is used to instantiate the
	 * bytes as a POJO
	 */
	private String type;

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
	protected AbstractFullDocument()
	{
		super();

		Date now = new Date();
		setCreatedAt(now);
		setUpdatedAt(now);
	}

	/**
	 * Constructor with identifier, BSON object, and type parameters.
	 * 
	 * @param id   The identifier of the document.
	 * @param bytes The serialized object as a byte array.
	 * @param type The class of the object to be stored.
	 */
	protected AbstractFullDocument(Identifier id)
	{
		this();
		setIdentifier(id);
	}

	/**
	 * Constructor with type parameter.
	 * 
	 * @param type The class of the object to be stored as BSON in this document.
	 */
	protected AbstractFullDocument(Class<?> type)
	{
		this();
		this.type = type.getName();
	}

	/**
	 * Constructor with serialized object and type parameters.
	 * 
	 * @param value The serialized object at type T.
	 * @param type The class of the object that is serialized.
	 */
	protected AbstractFullDocument(T value, Class<?> type)
	{
		this(type);
		setValue(value);
	}

	/**
	 * Constructor with identifier, BSON object, and type parameters.
	 * 
	 * @param id   The identifier of the document.
	 * @param value The serialized object as type T.
	 * @param type The class of the object to be stored.
	 */
	protected AbstractFullDocument(Identifier id, T value, Class<?> type)
	{
		this();
		setIdentifier(id);
		setValue(value);
		setType(type.getName());
	}

	/**
	 * Returns the type of the document. This is the fully-qualified class name of the serialized object
	 * so it can be deserialized into a POJO.
	 * 
	 * @return The type of the document.
	 */
	public String getType()
	{
		return type;
	}

	public Class<?> getTypeAsClass()
	{
		try
		{
			return Class.forName(type);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Sets the type of the document. This is the fully-qualified class name of the serialized object
	 * so it can be deserialized into a POJO.
	 * 
	 * @param type The type to be set.
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * Sets the type of the document from a Class instance (instead of String).
	 * 
	 * @param type The class of the object to be set.
	 */
	public void setType(Class<?> type)
	{
		setType(type.getClass());
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

	/**
	 * Returns a string representation of the document.
	 * 
	 * @return A string representation of the document.
	 */
	@Override
	public String toString()
	{
		return "Document{" + "id=" + getIdentifier() + ", value=" + getValue() + ", type=" + type
			+ ", createdAt=" + getCreatedAt() + ", updatedAt=" + getUpdatedAt() + "}";
	}

}
