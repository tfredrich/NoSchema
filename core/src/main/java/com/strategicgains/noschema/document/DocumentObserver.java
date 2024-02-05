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
public interface DocumentObserver
{
	/**
	 * Called before a Document is read.
	 *
	 * @param identifier the identifier of the Document to be read
	 */
	void beforeRead(Identifier identifier);

	/**
	 * Called after a Document is read.
	 *
	 * @param document the Document that was read
	 */
	void afterRead(Document document);

	/**
	 * Called before a Document is created.
	 *
	 * @param document the Document to be created
	 */
	void beforeCreate(Document document);

	/**
	 * Called after a Document is created.
	 *
	 * @param document the Document that was created
	 */
	void afterCreate(Document document);

	/**
	 * Called before a Document is deleted.
	 *
	 * @param document the Document to be deleted
	 */
	void beforeDelete(Document document);

	/**
	 * Called after a Document is deleted.
	 *
	 * @param document the Document that was deleted
	 */
	void afterDelete(Document document);

	/**
	 * Called before a Document is updated.
	 *
	 * @param document the Document to be updated
	 */
	void beforeUpdate(Document document);

	/**
	 * Called after a Document is updated.
	 *
	 * @param document the Document that was updated
	 */
	void afterUpdate(Document document);

	/**
	 * Called before an entity is encoded into a Document.
	 * <p/>
	 * This occurs essentially the same time as beforeCreate and beforeUpdate, but
	 * can be used to perform actions that are specific to encoding an entity into a Document.
	 *
	 * @param entity the entity to be encoded
	 * @param <T> the type of the entity, which must be Identifiable
	 */
	<T extends Identifiable> void beforeEncoding(T entity);

	/**
	 * Called after an entity is encoded into a Document.
	 *
	 * @param document the Document that was created from the entity
	 */
	void afterEncoding(Document document);
}