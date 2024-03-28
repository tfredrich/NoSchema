package com.strategicgains.noschema.document;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.strategicgains.noschema.Identifier;

/**
 * The SerializedDocument represents an abstract Document class that
 * contains a serialized POJO in format of type T.
 * 
 * The document also contains metadata that can be used to tag along with the
 * document in the database.
 * 
 * @param <T> The type of the serialized format of the POJO (e.g. byte[], String, etc.).
 */
public abstract class SerializedDocument<T>
extends IndexDocument
{
	/**
	 * The serialized contents of the entity contained by this document.
	 */
	private T object;

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
	protected SerializedDocument()
	{
		super();
	}

	/**
	 * Constructor with type parameter.
	 * 
	 * @param type The class of the object to be stored as BSON in this document.
	 */
	protected SerializedDocument(Class<?> type)
	{
		super();
		this.type = type.getName();
	}

	/**
	 * Constructor with serialized object and type parameters.
	 * 
	 * @param object The serialized object at type T.
	 * @param type The class of the object that is serialized.
	 */
	protected SerializedDocument(T object, Class<?> type)
	{
		this(type);
		setObject(object);
	}

	/**
	 * Constructor with identifier, BSON object, and type parameters.
	 * 
	 * @param id   The identifier of the document.
	 * @param object The serialized object as type T.
	 * @param type The class of the object to be stored.
	 */
	protected SerializedDocument(Identifier id, T object, Class<?> type)
	{
		super(id);
		setObject(object);
		setType(type.getName());
	}

	/**
	 * Checks if the document has a BSON object.
	 * 
	 * @return True if the document has a BSON object, false otherwise.
	 */
	public boolean hasObject()
	{
		return (object != null);
	}

	/**
	 * Returns the serialized data contained in this document.
	 * 
	 * @return The serialized object in the document.
	 */
	public T getObject()
	{
		return object;
	}

	/**
	 * Sets the serialized object for the document.
	 * 
	 * @param serialized The serialized byte data to be set.
	 */
	public void setObject(T serialized)
	{
		this.object = serialized;
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

	public SerializedDocument<T> withMetadata(String name, String value)
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
		return "SerializedDocument{" + "id=" + getIdentifier() + ", object=" + getObject() + ", type=" + type
			+ ", createdAt=" + getCreatedAt() + ", updatedAt=" + getUpdatedAt() + "}";
	}
}
