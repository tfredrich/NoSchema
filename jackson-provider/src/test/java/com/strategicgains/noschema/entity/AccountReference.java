package com.strategicgains.noschema.entity;

import java.util.UUID;

public class AccountReference
{
	private UUID id;

	public AccountReference()
	{
		super();
	}

	public AccountReference(UUID accountId)
	{
		this();
		setId(accountId);
	}

	public UUID getId()
	{
		return id;
	}

	public void setId(UUID accountId)
	{
		this.id = accountId;
	}
}
