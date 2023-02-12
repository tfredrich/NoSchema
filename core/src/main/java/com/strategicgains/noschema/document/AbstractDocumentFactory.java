package com.strategicgains.noschema.document;

import java.util.Date;

import org.bson.BSONObject;

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

	protected AbstractDocumentFactory()
	{
		this(new ObjectCodecImpl<>());
	}

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
		Identifier id = extractIdentifier(entity);
		BSONObject bson = codec.encode(entity);
		Document document = new Document(id, bson, entity.getClass());
		Date now = new Date();
		document.setCreatedAt(now);
		document.setUpdatedAt(now);
		return document;
	}

	public T asPojo(Document document)
	{
		return codec.decode(document.getObject(), document.getType());
	}

	protected abstract Identifier extractIdentifier(T entity)
	throws InvalidIdentifierException, KeyDefinitionException;
}
