package com.strategicgains.noschema.document;

import com.strategicgains.noschema.Identifiable;

public interface DocumentObserver<T extends Identifiable>
{
	/**
	 * Called before an entity is encoded into a Document.
	 *
	 * This occurs essentially the same time as beforeCreate and beforeUpdate, but
	 * can be used to perform actions that are specific to encoding an entity
	 * into a Document, such as data transformation, derived data calculation, etc.
	 *
	 * @param entity the entity to be encoded
	 * @param <I> the type of the entity, which must be and Identifiable implementation.
	 */
	void beforeEncoding(T entity);

	/**
	 * Called after an entity is encoded into a Document.
	 * 
	 * This occurs essentially the same time as beforeCreate and beforeUpdate, but
	 * can be used to perform actions that are needed after encoding an entity
	 * into a Document, such as encryption, data transformation, etc.
	 *
	 * @param entity the newly-encoded entity
	 */
	void afterEncoding(Document<T> entity);
}
