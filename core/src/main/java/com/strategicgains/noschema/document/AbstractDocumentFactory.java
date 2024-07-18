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
 * @param <T> the type of POJO this factory creates Document instances for.
 */
public abstract class AbstractDocumentFactory<T extends Identifiable>
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

	public Document<T> asDocument(T entity)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		byte[] bson = codec.serialize(entity);
		return asDocument(entity, bson);
	}

	@SuppressWarnings("unchecked")
	public Document<T> asDocument(T entity, byte[] bytes)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		Identifier id = extractIdentifier(entity);
		return new Document<>(id, bytes, (Class<T>) entity.getClass());		
	}

	@SuppressWarnings("unchecked")
	public T asPojo(Document<T> document)
	{
		return codec.deserialize(document.getObject(), (Class<T>) document.getTypeAsClass());
	}

	protected abstract Identifier extractIdentifier(T entity)
	throws InvalidIdentifierException, KeyDefinitionException;
}
