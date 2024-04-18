package com.strategicgains.noschema.document;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

/**
 * The DocumentObserver interface defines a set of methods that are called
 * before and after certain lifecycle events on a Document. These operations include
 * reading, creating, deleting, updating, and encoding (an entity into BSON) a Document.
 *
 * Implementations of this interface can be used to perform actions such as
 * logging, eventing, encryption/decryption or validation before or after
 * these operations.
 * 
 * @author Todd Fredrich
 * @since Feb 5, 2024
 * @see Document
 * @see Identifiable
 * @see Identifier
 */
public interface RepositoryObserver<T extends Identifiable>
{
	/**
	 * Called before a Document is read.
	 *
	 * @param identifier the identifier of the Document to be read
	 */
	void beforeRead(Identifier identifier);

	/**
	 * Called after an Identifiable entity is read.
	 *
	 * @param entity the Identifiable entity that was read
	 */
	void afterRead(T entity);

	/**
	 * Called before an entity is written to the database.
	 *
	 * @param entity the Identifiable entity to be created
	 */
	void beforeCreate(T entity);

	/**
	 * Called after an entity is created in the database.
	 *
	 * @param entity the Identifiable entity that was created
	 */
	void afterCreate(T entity);

	/**
	 * Called before an entity is deleted from the database.
	 *
	 * @param entity the Identifiable entity to be deleted
	 */
	void beforeDelete(T entity);

	/**
	 * Called after an entity is deleted from the database.
	 *
	 * @param entity the Identifiable entity that was deleted.
	 */
	void afterDelete(T entity);

	/**
	 * Called before an entity is updated in the database.
	 *
	 * @param entity the Identifiable entity that will be updated
	 */
	void beforeUpdate(T entity);

	/**
	 * Called after an entity is updated in [written to] the database.
	 *
	 * @param entity the Identifiable entity that was updated
	 */
	void afterUpdate(T entity);
}