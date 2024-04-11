package com.strategicgains.noschema.unitofwork;

/**
 * Represents the type of change that has occurred to an entity during a UnitOfWork.
 */
public enum ChangeType
{
	/**
	 * The entity was not changed and does not need to be persisted.
	 * It can be used to determine deltas for dirty entities.
	 */
	CLEAN,

	/**
	 * The entity was created and needs to be inserted into the database.
	 */
	NEW,

	/**
	 * The entity was updated and needs to be changed in the database.
	 */
	DIRTY,

	/**
	 * The entity was removed and needs to be deleted from the database.
	 */
	DELETED,
	
	/**
	 * The entity was not recognized by the UnitOfWork and its state is unknown.
	 * This is typically an error condition.
	 */
	UNKNOWN
}
