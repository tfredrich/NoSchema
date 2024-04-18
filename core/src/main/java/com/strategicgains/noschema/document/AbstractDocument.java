package com.strategicgains.noschema.document;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.strategicgains.noschema.Identifier;

/**
 * An abstract implementation of the Document interface that provides a base
 * implementation.
 * 
 * @param <T> The type of the serialized POJO wrapped by the document.
 */
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
	 * Name/value pairs that can tag along with the document in the database that can be applied to the entity.
	 */
	private Map<String, String> metadata;

	/**
	 * Default constructor.
	 */
	protected AbstractDocument()
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
	protected AbstractDocument(Identifier id)
	{
		this();
		setIdentifier(id);
	}

	/**
	 * Constructor with identifier, BSON object, and type parameters.
	 * 
	 * @param id   The identifier of the document.
	 * @param value The serialized object as type T.
	 */
	protected AbstractDocument(Identifier id, T value)
	{
		this();
		setIdentifier(id);
		setValue(value);
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
		return "AbstractDocument{" + "id=" + getIdentifier() + ", value=" + getValue() + "}";
	}

}
