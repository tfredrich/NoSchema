package com.strategicgains.noschema.unitofwork;

import com.strategicgains.noschema.Identifiable;

/**
 * This interface provides a transactional context for managing database
 * changes. It allows registering new entities, marking entities as "dirty" or
 * "deleted", and committing or rolling back the current transaction.
 */
public interface UnitOfWork
{
	/**
	 * Registers an entity change with the UnitOfWork that needs to be
	 * persisted during the transaction.
	 *
	 * @param change the change description to register.
	 */
	<T extends Identifiable> UnitOfWork registerChange(Change<T> change);

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
}
