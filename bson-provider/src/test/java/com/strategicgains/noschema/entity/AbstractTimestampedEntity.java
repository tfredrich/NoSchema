package com.strategicgains.noschema.entity;

import java.util.Date;

public abstract class AbstractTimestampedEntity<T>
extends AbstractEntity<T>
{
	private Date createdAt;
	private Date updatedAt;

	public AbstractTimestampedEntity(T id)
	{
		super(id);
		Date now = new Date();
		this.setCreatedAt(now);
		this.setUpdatedAt(now);
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
}
