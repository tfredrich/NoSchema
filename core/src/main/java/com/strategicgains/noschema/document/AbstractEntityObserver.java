/**
 * 
 */
package com.strategicgains.noschema.document;

/**
 * 
 */
public abstract class AbstractEntityObserver<T>
implements EntityObserver<T>
{
	protected AbstractEntityObserver() 
	{
		// Prevent instantiation.
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
