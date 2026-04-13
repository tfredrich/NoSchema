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

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

/**
 * The DocumentFilter interface defines a set of methods that are called
 * before write and after read lifecycle events on a Document like a 
 * filter chain. They are called in-order on write and in reverse order on read.
 *
 * Implementations of this interface can be used to perform actions such as
 * logging, eventing, encryption/decryption, signing, or validation before or after
 * these operations.
 * 
 * @author Todd Fredrich
 * @since Feb 5, 2024
 * @see Document
 * @see Identifiable
 * @see Identifier
 */
public interface DocumentFilter
{
	/**
	 * Called before a Document is written to the database, during
	 * create, update, or upsert operations.
	 * 
	 * @param document
	 */
	public void onWrite(Document document);

	/**
	 * Called after a Document is read from the database, during
	 * read, readAll, readIn operations.
	 * 
	 * @param document
	 */
	public void onRead(Document document);
}