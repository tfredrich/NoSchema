/**
 * 
 */
package com.strategicgains.noschema.document;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

/**
 * A do-nothing implementation of EntityObserver to enable extenders
 * to simply override the method(s) of interest.
 * 
 * @author Todd Fredrich
 * @see EntityObserver
 * @param <T> the type of entity to observe.
 */
public abstract class AbstractEntityObserver<T extends Identifiable>
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
	public void beforeRead(Identifier id) {}

	@Override
	public void beforeUpdate(T entity) {}
}
