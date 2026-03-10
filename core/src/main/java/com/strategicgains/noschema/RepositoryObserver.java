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
package com.strategicgains.noschema;

/**
 * Interface for observing entity lifecycle events such as creation, deletion, updates, and reads.
 * 
 * @param <T> the type of entity being observed.
 */
public interface RepositoryObserver<T extends Identifiable>
{
	/**
     * Invoked before an entity is created in the database.
     * 
     * @param entity the entity being written.
     */
	public void beforeCreate(T entity);

	/**
	 * Invoked before an entity is deleted from the database.
	 * 
	 * @param entity the entity being deleted.
	 */
	public void beforeDelete(T entity);

	/**
	 * Invoked before an entity is updated in the database.
	 * 
	 * @param entity the entity being updated.
	 */
	public void beforeUpdate(T entity);

	/**
	 * Invoked after an entity is created in the database.
	 * 
	 * @param entity the entity that was written.
	 */
	public void afterCreate(T entity);

	/**
	 * Invoked after an entity is deleted from the database.
	 * 
	 * @param entity the entity that was deleted.
	 */
	public void afterDelete(T entity);

	/**
	 * Invoked after an entity is read from the database.
	 * 
	 * @param entity the entity that was read.
	 */
	public void afterRead(T entity);

	/**
	 * Invoked after an entity is updated in the database.
	 * 
	 * @param entity the entity that was updated.
	 */
	public void afterUpdate(T entity);
}
