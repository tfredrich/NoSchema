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
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

/**
 * Produces Document instances from a POJO and POJO instances from Document instances.
 * Extenders must implement the extractIdentifier() method to produce an Identifier.
 *  
 * @author toddfredrich
 *
 * @param <T>
 */
public abstract class AbstractDocumentFactory<T>
{
	private ObjectCodec<T> codec;

	protected AbstractDocumentFactory(ObjectCodec<T> codec)
	{
		super();
		setCodec(codec);
	}

	public void setCodec(ObjectCodec<T> objectCodec)
	{
		this.codec = objectCodec;
	}

	public Document asDocument(T entity)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		byte[] bson = codec.serialize(entity);
		return asDocument(entity, bson);
	}

	public Document asDocument(T entity, byte[] bytes)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		Identifier id = extractIdentifier(entity);
		return new Document(id, bytes, entity.getClass());		
	}

	@SuppressWarnings("unchecked")
	public T asPojo(Document document)
	{
		return codec.deserialize(document.getObject(), (Class<T>) document.getTypeAsClass());
	}

	protected abstract Identifier extractIdentifier(T entity)
	throws InvalidIdentifierException, KeyDefinitionException;
}
