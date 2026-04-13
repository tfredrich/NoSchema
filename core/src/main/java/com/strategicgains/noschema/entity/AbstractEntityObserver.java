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

import com.strategicgains.noschema.EntityObserver;

/**
 * Default implementation of EntityObserver that provides empty method bodies. Subclasses can override
 * only the methods they are interested in.
 * 
 * @author Todd Fredrich
 */
public abstract class AbstractEntityObserver<T extends Entity<?>>
implements EntityObserver<T>
{
	protected AbstractEntityObserver() 
	{
		// Prevents instantiation.
		super();
	}

	@Override
	public void afterCreate(T entity) {}

	@Override
	public void afterDelete(T entity) {}

	@Override
	public void afterRead(T entity) {}

	@Override
	public void afterUpdate(T entity) {}

	@Override
	public void beforeCreate(T entity) {}

	@Override
	public void beforeDelete(T entity) {}

	@Override
	public void beforeUpdate(T entity) {}
}
