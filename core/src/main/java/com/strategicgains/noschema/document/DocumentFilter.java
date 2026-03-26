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

/**
 * A DocumentFilter transforms a {@link Document} as it is written to and read
 * from storage.
 * <p/>
 * Implementations are executed in registration order on writes and reverse
 * registration order on reads so reversible transformations such as
 * encryption/decryption, compression/decompression, signing/verification
 * semantics can be composed safely.
 *
 * @author Todd Fredrich
 * @since Mar 26, 2026
 * @see Document
 */
public interface DocumentFilter
{
	void onWrite(Document document);
	void onRead(Document document);
}
