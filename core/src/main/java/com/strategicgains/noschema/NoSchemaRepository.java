package com.strategicgains.noschema;

import java.util.List;

import com.strategicgains.noschema.exception.DuplicateItemException;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.ItemNotFoundException;
import com.strategicgains.noschema.exception.KeyDefinitionException;
import com.strategicgains.noschema.exception.StorageException;

public interface NoSchemaRepository<T>
{
	/**
	 * Store a new entity in the database as a Document.
	 * An IdentityMapper must be registered for the Class in the singleton DocumentFactory.
	 * 
	 * @param entity
	 * @return
	 * @throws DuplicateItemException
	 * @throws InvalidIdentifierException 
	 * @throws KeyDefinitionException 
	 * @see DocumentFactory
	 */
	T create(T entity)
	throws DuplicateItemException, InvalidIdentifierException, KeyDefinitionException, StorageException;

	/**
	 * Remove an entity from the database by the given identifier.
	 * 
	 * @param id
	 * @return
	 * @throws ItemNotFoundException
	 * @throws InvalidIdentifierException 
	 */
	void delete(Identifier id)
	throws ItemNotFoundException, InvalidIdentifierException, StorageException;

	/**
	 * Check to see if the given identifier exists in the database.
	 * 
	 * @param id
	 * @return
	 */
	boolean exists(Identifier id)
	throws InvalidIdentifierException;

	/**
	 * Retrieve an entity from the database using the given identifier.
	 * 
	 * @param id An Identifier instance with values to use as the table keys.
	 * @return An instance of an entity.
	 * @throws ItemNotFoundException if the identifier is not found.
	 * @throws InvalidIdentifierException 
	 */
	T read(Identifier id)
	throws ItemNotFoundException, InvalidIdentifierException;

	/**
	 * Retrieve many entities from the table using the given parameters.
	 * 
	 * @param parms
	 * @return
	 */
	List<T> readAll(Object... parms);

	/**
	 * Read all given identifiers.
	 * 
	 * Leverages the token-awareness of the driver to optimally query each node directly instead of invoking a
	 * coordinator node. Sends an individual query for each partition key, so reaches the appropriate replica
	 * directly and collates the results client-side.
	 * 
	 * @param ids the partition keys (identifiers) to select.
	 */
	List<T> readIn(Identifier... ids);

	/**
	 * Rewrite an entity to the database. The entity identifier must already exist
	 * otherwise an ItemNotFoundException is thrown.
	 * 
	 * @param entity
	 * @return
	 * @throws ItemNotFoundException
	 * @throws InvalidIdentifierException 
	 * @throws KeyDefinitionException 
	 */
	T update(T updated, T original);

	/**
	 * Write an entity to the database not caring whether it exists or not.
	 * 
	 * @param entity
	 * @return
	 * @throws InvalidIdentifierException 
	 * @throws KeyDefinitionException 
	 * @throws StorageException 
	 */
	T upsert(T entity);
}