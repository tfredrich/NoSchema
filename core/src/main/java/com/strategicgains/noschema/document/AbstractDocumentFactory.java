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
public abstract class AbstractDocumentFactory<T> implements DocumentFactory<T>
{
	private ByteArrayCodec<T> codec;

	protected AbstractDocumentFactory(ByteArrayCodec<T> codec)
	{
		super();
		setCodec(codec);
	}

	@Override
	public void setCodec(ByteArrayCodec<T> objectCodec)
	{
		this.codec = objectCodec;
	}

	@Override
	public Document asDocument(T entity)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		byte[] bson = codec.serialize(entity);
		return asDocument(entity, bson);
	}

	@Override
	public Document asDocument(T entity, byte[] bytes)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		Identifier id = extractIdentifier(entity);
		return new Document(id, bytes, entity.getClass());		
	}

	@Override
	@SuppressWarnings("unchecked")
	public T asPojo(Document document)
	{
		return codec.deserialize(document.getObject(), (Class<T>) document.getTypeAsClass());
	}

	protected abstract Identifier extractIdentifier(T entity)
	throws InvalidIdentifierException, KeyDefinitionException;
}
