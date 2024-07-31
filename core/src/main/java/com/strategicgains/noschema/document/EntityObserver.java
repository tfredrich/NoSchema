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
	 * Called after an entity is created.
	 * 
	 * @param entity the entity that was created.
	 */
	public void afterCreate(T entity);

	/**
	 * Called after an entity is deleted.
	 * 
	 * @param entity the entity that was deleted.
	 */
	public void afterDelete(T entity);

	/**
	 * Called after an entity is read.
	 * 
	 * @param entity the entity that was read.
	 */
	public void afterRead(T entity);

	/**
	 * Called after an entity is updated.
	 * 
	 * @param entity the entity that was updated.
	 */
	public void afterUpdate(T entity);

	/**
	 * Called before an entity is created. Any changes made to the entity will be
	 * persisted.
	 * 
	 * @param entity the entity that will be created.
	 */
	public void beforeCreate(T entity);

	/**
	 * Called before an entity is deleted.
	 * 
	 * @param entity the entity that will be deleted.
	 */
	public void beforeDelete(T entity);

	/**
	 * Called before an entity is read. Any changes made to the identifier will be
	 * used to read the entity.
	 * 
	 * @param id the identifier of the entity that will be read.
	 */
	public void beforeRead(Identifier id);

	/**
	 * Called before an entity is updated. Any changes made to the entity will be
	 * persisted.
	 * 
	 * @param entity the entity that will be updated.
	 */
	public void beforeUpdate(T entity);
}
