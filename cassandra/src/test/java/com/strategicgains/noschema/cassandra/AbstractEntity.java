package com.strategicgains.noschema.cassandra;

import java.util.Date;
import java.util.UUID;

import com.strategicgains.noschema.Identifiable;

public abstract class AbstractEntity
implements Identifiable
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

	public AbstractEntity(AbstractEntity that)
	{
		this(that.id);
		setAccountId(that.getAccountId());
		setCreatedAt(that.createdAt);
		setUpdatedAt(that.updatedAt);
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
