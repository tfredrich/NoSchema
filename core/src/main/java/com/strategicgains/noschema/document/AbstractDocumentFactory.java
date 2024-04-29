package com.strategicgains.noschema.document;

import com.strategicgains.noschema.Identifiable;
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
public abstract class AbstractDocumentFactory<T extends Identifiable, U>
implements DocumentFactory<T, U>
{
	private Codec<T, U> codec;

	protected AbstractDocumentFactory(Codec<T, U> codec)
	{
		super();
		setCodec(codec);
	}

	@Override
	public void setCodec(Codec<T, U> objectCodec)
	{
		this.codec = objectCodec;
	}

	@Override
	public Document<U> asDocument(T entity)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		U bson = codec.serialize(entity);
		return asDocument(entity, bson);
	}

	@Override
	public Document<U> asDocument(T entity, U bytes)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		Identifier id = extractIdentifier(entity);
		return createDocument(id, bytes, (Class<T>) entity.getClass());		
	}

	protected abstract Document<U> createDocument(Identifier id, U bytes, Class<T> clazz);

	protected abstract Identifier extractIdentifier(T entity)
	throws InvalidIdentifierException, KeyDefinitionException;
}
