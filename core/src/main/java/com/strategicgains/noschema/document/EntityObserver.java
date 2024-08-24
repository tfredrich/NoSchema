package com.strategicgains.noschema.document;

public interface EntityObserver<T>
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
