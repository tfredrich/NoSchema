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
package com.strategicgains.noschema.document;

import com.strategicgains.noschema.Identifier;

/**
 * The DocumentObserver interface defines a set of methods that are called
 * before and after certain lifecycle events on a Document. These operations include
 * reading, creating, deleting, and updating a Document.
 *
 * Implementations of this interface can be used to perform actions such as
 * logging, eventing, or validation before or after
 * these operations.
 * 
 * @author Todd Fredrich
 * @since Feb 5, 2024
 * @see Document
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

}
