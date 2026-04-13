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
 * This class is useful for creating a {@link DocumentFilter} that only observes a few of the lifecycle points.
 * <p/>
 * This class provides empty methods for all of the observation points in the Document lifecycle. Subclasses override the
 * method(s) they want to observe.
 * <p/>
 * For example, if you only want to observe the creation of documents, you can create a subclass of this class and override
 * the {@link #afterCreate(Document) afterCreate()} method.
 * 
 * @author Todd Fredrich
 * @since Feb 5, 2024
 * @see DocumentFilter
 * @see Document
 * @see Identifiable
 * @see Identifier
 */
public abstract class AbstractDocumentFilter
implements DocumentFilter
{
    /**
     * This method is called after successfully reading a document.
     *
     * @param document The Document object that has been read.
     */
    @Override
    public void onRead(Document document) {}

    /**
     * This method is called before writing (creating or updating) a document.
     *
     * @param document The Document object that will be created.
     */
    @Override
    public void onWrite(Document document) {}
}
