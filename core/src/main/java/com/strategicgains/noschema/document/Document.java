package com.strategicgains.noschema.document;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

/**
 * The Document class implements the Identifiable interface. It represents a document that can be stored in a key/value
 * or columnar store. The document represents an object serialized as a byte array and has an associated type so the
 * object property, stored as a byte array, can be deserialized into a POJO.
 */
public class Document
implements Identifiable
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
	 * The serialized contents of the entity contained by this document.
	 */
	private byte[] bytes;

	/**
	 * The fully-qualified class name of the object stored in this document. This is used to instantiate the
	 * bytes as a POJO
	 */
	private String type;

	/**
	 * Name/value pairs that can tag along with the document in the database that can be applied to the entity.
	 */
	private Map<String, String> metadata;

	/**
	 * Default constructor.
	 */
	public Document()
	{
		super();
	}

	/**
	 * Constructor with type parameter.
	 * 
	 * @param type The class of the object to be stored as BSON in this document.
	 */
	public Document(Class<?> type)
	{
		super();
		this.type = type.getName();
	}

	/**
	 * Constructor with BSON object and type parameters.
	 * 
	 * @param bytes The serialized object as a byte array.
	 * @param type The class of the object that is serialized.
	 */
	public Document(byte[] bytes, Class<?> type)
	{
		this(type);
		setObject(bytes);
	}

	/**
	 * Constructor with identifier, BSON object, and type parameters.
	 * 
	 * @param id   The identifier of the document.
	 * @param bytes The serialized object as a byte array.
	 * @param type The class of the object to be stored.
	 */
	public Document(Identifier id, byte[] bytes, Class<?> type)
	{
		this(bytes, type);
		setIdentifier(id);
		setType(type.getName());

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
	public boolean hasIdentifier()
	{
		return (identifier != null);
	}

	/**
	 * Sets the identifier of the document.
	 * 
	 * @param id The identifier to be set.
	 */
	public void setIdentifier(Identifier id)
	{
		this.identifier = (id != null ? new Identifier(id) : null);
	}

	/**
	 * Checks if the document has a BSON object.
	 * 
	 * @return True if the document has a BSON object, false otherwise.
	 */
	public boolean hasObject()
	{
		return (bytes != null);
	}

	/**
	 * Returns the serialized data contained in this document.
	 * 
	 * @return The serialized object in the document.
	 */
	public byte[] getObject()
	{
		return bytes;
	}

	/**
	 * Sets the serialized object for the document.
	 * 
	 * @param serialized The serialized byte data to be set.
	 */
	public void setObject(byte[] serialized)
	{
		this.bytes = serialized;
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
	public Date getCreatedAt()
	{
		return createdAt;
	}

	/**
	 * Sets the creation date of the document.
	 * 
	 * @param createdAt The creation date to be set.
	 */
	public void setCreatedAt(Date createdAt)
	{
		this.createdAt = createdAt;
	}

	/**
	 * Returns the update date of the document.
	 * 
	 * @return The update date of the document.
	 */
	public Date getUpdatedAt()
	{
		return updatedAt;
	}

	/**
	 * Sets the update date of the document.
	 * 
	 * @param updatedAt The update date to be set.
	 */
	public void setUpdatedAt(Date updatedAt)
	{
		this.updatedAt = updatedAt;
	}

	public boolean hasMetadata()
	{
		return (metadata != null && !metadata.isEmpty());
	}

	public Map<String, String> getMetadata()
	{
		return (hasMetadata() ? Collections.unmodifiableMap(metadata) : Collections.emptyMap());
	}

	public void setMetadata(Map<String, String> map)
	{
		metadata = new HashMap<>(map);
	}

	public Document withMetadata(String name, String value)
	{
		if (metadata == null) metadata = new HashMap<>();
		metadata.put(name, value);
		return this;
	}

	/**
	 * Returns a string representation of the document.
	 * 
	 * @return A string representation of the document.
	 */
	@Override
	public String toString()
	{
		return "Document{" + "id=" + identifier.toString() + ", object=" + getObject() + ", type=" + type
			+ ", createdAt=" + getCreatedAt() + ", updatedAt=" + getUpdatedAt() + "}";
	}
}
