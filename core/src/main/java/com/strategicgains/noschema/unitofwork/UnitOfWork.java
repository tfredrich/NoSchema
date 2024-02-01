package com.strategicgains.noschema.unitofwork;

/**
 * This interface provides a transactional context for managing database
 * changes. It allows registering new entities, marking entities as "dirty" or
 * "deleted", and committing or rolling back the current transaction.
 */
public interface UnitOfWork<T> {
	/**
	 * Commits the current transaction and saves all changes made during the session
	 * to the database.
	 */
	void commit() throws UnitOfWorkCommitException;

	/**
	 * Undoes all changes made during the current session and rolls back the
	 * transaction.
	 */
	void rollback() throws UnitOfWorkRollbackException;

	/**
	 * Registers a new entity that has not been persisted yet.
	 * 
	 * @param entity The new entity to register.
	 */
	void registerNew(T entity);

	/**
	 * Marks the given entity as "dirty" and needs to be updated in the database
	 * during commit.
	 * 
	 * @param entity The entity after it is updated (dirty).
	 */
	void registerDirty(T entity);

	/**
	 * Marks the given entity as "deleted" and should be removed from the database
	 * during commit.
	 * 
	 * @param entity The entity to mark as deleted.
	 */
	void registerDeleted(T entity);

	/**
	 * Registers the entity as "clean" or freshly-read from the database before
	 * any modifications have occurred. This may be used during commit() to detect
	 * change between those marked dirty to minimize read-before-update impacts. 
	 * 
	 * @param entity The entity to mark as deleted.
	 */
	void registerClean(T entity);
}
