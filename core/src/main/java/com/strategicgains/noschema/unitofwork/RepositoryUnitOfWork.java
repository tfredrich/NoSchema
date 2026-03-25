package com.strategicgains.noschema.unitofwork;

import com.strategicgains.noschema.Identifiable;

/**
 * This interface extends the basic UnitOfWork contract to include methods for registering
 * entities in various states (new, dirty, deleted, clean) during a transaction. It is
 * designed to work across repository boundaries, so the registered entities are
 * accepted as Identifiable instead of being constrained to a single repository type.
 * 
 * @author Todd Fredrich
 */
public interface RepositoryUnitOfWork
extends UnitOfWork
{
	/**
	 * Registers a new entity that doesn't exist in the database and needs to be
	 * persisted during the transaction.
	 * 
	 * NOTE: Entities MUST be fully-populated across all identifier properties before
	 * registering them.
	 *
	 * @param entity the new entity to register.
	 */
	RepositoryUnitOfWork registerNew(String viewName, Identifiable entity);

	/**
	 * Registers an entity that has been updated during the transaction.
	 *
	 * @param entity the entity in its dirty state (after update).
	 */
	RepositoryUnitOfWork registerDirty(String viewName, Identifiable entity);

	/**
	 * Registers an entity for removal during the transaction.
	 *
	 * @param entity the entity in its clean state (before removal).
	 */
	RepositoryUnitOfWork registerDeleted(String viewName, Identifiable entity);

	/**
	 * Registers an entity that has been read from the database and is being tracked
	 * during the transaction.
	 *
	 * @param entity the entity in its clean state (after read).
	 */
	RepositoryUnitOfWork registerClean(String viewName, Identifiable entity);
}
