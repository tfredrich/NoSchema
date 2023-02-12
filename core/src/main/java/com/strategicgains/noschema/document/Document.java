package com.strategicgains.noschema.document;

import java.util.Date;

import org.bson.BSONObject;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

public class Document
implements Identifiable
{
	// The key components to store in a key/value or columnar store.
	private Identifier identifier;
	private Date createdAt;
	private Date updatedAt;

	// The BSON document.
	private BSONObject bson;

	// The fully-qualified class name of the object stored as BSON.
	// This is used to instantiate the BSON as a POJO
	private String type;

	public Document()
	{
		super();
	}

	public Document(Class<?> type)
	{
		super();
		this.type = type.getName();
	}

	public Document(BSONObject bson, Class<?> type)
	{
		this(type);
		setObject(bson);
	}

	public Document(Identifier id, BSONObject bson, Class<?> type)
	{
		this(bson, type);
		setIdentifier(id);
		setType(type.getName());
	}

	@Override
	public Identifier getIdentifier()
	{
		return (hasIdentifier() ? new Identifier(identifier) : null);
	}

	public boolean hasIdentifier()
	{
		return (identifier != null);
	}

	public void setIdentifier(Identifier id)
	{
		this.identifier = (id != null ? new Identifier(id) : null);
	}

	public boolean hasObject()
	{
		return (bson != null);
	}

	public BSONObject getObject()
	{
		return bson;
	}

	public void setObject(BSONObject bson)
	{
		this.bson = bson;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public Date getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(Date createdAt)
	{
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt()
	{
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt)
	{
		this.updatedAt = updatedAt;
	}

	@Override
	public String toString()
	{
		return "Document{" + "id=" + identifier.toString() + ", object=" + getObject() + ", type=" + type + ", createdAt=" + getCreatedAt() + ", updatedAt=" + getUpdatedAt() + "}";
	}
}
