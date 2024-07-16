package com.strategicgains.noschema.document;

public interface EntityObserver<T>
{
	public void afterCreate(T entity);
	public void afterDelete(T entity);
	public void afterRead(T entity);
	public void afterUpdate(T entity);

	public void beforeCreate(T entity);
	public void beforeDelete(T entity);
	public void beforeUpdate(T entity);
}
