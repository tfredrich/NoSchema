package com.strategicgains.noschema.cassandra;

import java.util.Date;
import java.util.UUID;

public abstract class AbstractEntity
{
	private UUID id;
	private AccountReference account = new AccountReference();
	private Date createdAt;
	private Date updatedAt;

	public AbstractEntity()
	{
		this(UUID.randomUUID());
	}

	public AbstractEntity(UUID id)
	{
		super();
		this.id = id;
		Date now = new Date();
		this.setCreatedAt(now);
		this.setUpdatedAt(now);
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public AccountReference getAccount()
	{
		return account;
	}

	public void setAccount(AccountReference accountRef)
	{
		this.account = accountRef;
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
