package com.strategicgains.noschema.document;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.strategicgains.noschema.Identifier;

public abstract class AbstractDocument<T>
implements Document<T>
{
	/**
	 * The key components to store in a key/value or columnar store.
	 */
	private Identifier identifier;

	/**
	 * The serialized contents of the entity contained by this document.
	 */
	private T value;

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
	protected AbstractDocument()
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
	protected AbstractDocument(Identifier id)
	{
		this();
		setIdentifier(id);
	}

	/**
	 * Constructor with type parameter.
	 * 
	 * @param type The class of the object to be stored as BSON in this document.
	 */
	protected AbstractDocument(Class<?> type)
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
	protected AbstractDocument(T value, Class<?> type)
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
	protected AbstractDocument(Identifier id, T value, Class<?> type)
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

	public AbstractDocument<T> withMetadata(String name, String value)
	{
		if (metadata == null) metadata = new HashMap<>();
		metadata.put(name, value);
		return this;
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
	public boolean hasValue()
	{
		return false;
	}

	@Override
	public T getValue()
	{
		return value;
	}

	@Override
	public void setValue(T value)
	{
		this.value = value;
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
