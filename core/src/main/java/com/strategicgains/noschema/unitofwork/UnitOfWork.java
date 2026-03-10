/*
    Copyright 2024-2026, Strategic Gains, Inc.

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package com.strategicgains.noschema.unitofwork;

/**
 * This interface provides a transactional context for managing database
 * changesByState. It allows registering new entities, marking entities as "dirty" or
 * "deleted", and committing or rolling back the current transaction.
 */
public interface UnitOfWork
{
	/**
	 * Commits the current transaction and saves all changesByState made during the session
	 * to the database.
	 */
	void commit() throws UnitOfWorkCommitException;

	/**
	 * Undoes all changesByState made during the current session and rolls back the
	 * transaction.
	 */
	void rollback() throws UnitOfWorkRollbackException;
}
