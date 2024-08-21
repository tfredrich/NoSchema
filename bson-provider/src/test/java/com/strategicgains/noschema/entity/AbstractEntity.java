package com.strategicgains.noschema.entity;

import java.util.Date;
import java.util.UUID;

public abstract class AbstractEntity<T>
{
	private T id;
	private AccountReference account = new AccountReference();
	private Date createdAt;
	private Date updatedAt;

	public AbstractEntity(T id)
	{
		super();
		this.id = id;
		Date now = new Date();
		this.setCreatedAt(now);
		this.setUpdatedAt(now);
	}
	public T getId() {
		return id;
	}

	public void setId(T id) {
		this.id = id;
	}

	public UUID getAccountId() {
		return account.getId();
	}

	public void setAccountId(UUID accountId) {
		this.account.setId(accountId);
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}
}
