package com.strategicgains.noschema.document;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

/**
 * EntityObserver is an interface for classes that wish to observe the lifecycle
 * of entities.
 * 
 * @param <T> the type of entity to observe.
 */
public interface EntityObserver<T extends Identifiable>
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
	 * Invoked before an entity is read. Any changes made to the identifier will be
	 * used to read the entity.
	 * 
	 * @param id the identifier of the entity that will be read.
	 */
	public void beforeRead(Identifier id);

	/**
	 * Invoked before an entity is updated. Any changes made to the entity will be
	 * persisted.
	 * 
	 * @param entity the entity that will be updated.
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
