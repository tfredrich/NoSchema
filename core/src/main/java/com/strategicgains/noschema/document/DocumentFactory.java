package com.strategicgains.noschema.document;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public interface DocumentFactory<T extends Identifiable, U>
{
	void setCodec(Codec<T, U> codec);

	Document<U> asDocument(T entity)
	throws InvalidIdentifierException, KeyDefinitionException;

	Document<U> asDocument(T entity, U bytes)
	throws InvalidIdentifierException, KeyDefinitionException;

	T asPojo(Document<U> document);
}
