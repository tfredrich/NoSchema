package com.strategicgains.noschema.document;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public interface DocumentFactory<T extends Identifiable, U>
{
	void setCodec(Codec<U, T> codec);
	Document<U> asDocument(T entity) throws InvalidIdentifierException, KeyDefinitionException;
	Document<U> asDocument(T entity, byte[] bytes) throws InvalidIdentifierException, KeyDefinitionException;
	T asPojo(Document<U> document);
}
