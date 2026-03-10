/*
    Copyright 2024-2026, Strategic Gains, Inc.

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package com.strategicgains.noschema.entity;

import java.util.UUID;

public abstract class AbstractEntity<T>
{
	private T id;
	private AccountReference account = new AccountReference();

	public AbstractEntity()
	{
		super();
	}

	public AbstractEntity(T id)
	{
		this();
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
