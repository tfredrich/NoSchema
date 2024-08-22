package com.strategicgains.noschema.entity;

import java.util.UUID;

public abstract class AbstractEntity<T>
{
	private T id;
	private AccountReference account = new AccountReference();

	public AbstractEntity(T id)
	{
		super();
		this.id = id;
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
}
