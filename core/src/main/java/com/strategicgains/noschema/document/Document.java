package com.strategicgains.noschema.document;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bson.BSONObject;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

/**
 * The Document class implements the Identifiable interface. It represents a document that can be stored in a key/value
 * or columnar store. The document is represented as a BSON object and has an associated type.
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
	 * The contents of the entity contained by this document as BSON.
	 */
	private BSONObject bson;

	/**
	 * The fully-qualified class name of the object stored in this document as BSON. This is used to instantiate the
	 * BSON as a POJO
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
	 * @param bson The BSON object to be stored.
	 * @param type The class of the object that is serialized as BSON.
	 */
	public Document(BSONObject bson, Class<?> type)
	{
		this(type);
		setObject(bson);
	}

	/**
	 * Constructor with identifier, BSON object, and type parameters.
	 * 
	 * @param id   The identifier of the document.
	 * @param bson The BSON object to be stored.
	 * @param type The class of the object to be stored as BSON.
	 */
	public Document(Identifier id, BSONObject bson, Class<?> type)
	{
		this(bson, type);
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
		return (bson != null);
	}

	/**
	 * Returns the BSON object of the document.
	 * 
	 * @return The BSON object of the document.
	 */
	public BSONObject getObject()
	{
		return bson;
	}

	/**
	 * Sets the BSON object of the document.
	 * 
	 * @param bson The BSON object to be set.
	 */
	public void setObject(BSONObject bson)
	{
		this.bson = bson;
	}

	/**
	 * Returns the type of the document.
	 * 
	 * @return The type of the document.
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * Sets the type of the document.
	 * 
	 * @param type The type to be set.
	 */
	public void setType(String type)
	{
		this.type = type;
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
