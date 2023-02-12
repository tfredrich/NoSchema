package com.strategicgains.noschema.dynamodb;

import com.strategicgains.noschema.AbstractDocumentFactory;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.ObjectCodec;
import com.strategicgains.noschema.dynamodb.key.KeyDefinition;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public class DynamodbDocumentFactory<T>
extends AbstractDocumentFactory<T>
{
	private KeyDefinition keys;

	public DynamodbDocumentFactory(KeyDefinition keys)
	{
		super();
		setKeyDefinition(keys);
	}

	public DynamodbDocumentFactory(ObjectCodec<T> codec, KeyDefinition keys)
	{
		super(codec);
		setKeyDefinition(keys);
	}

	private void setKeyDefinition(KeyDefinition keys)
	{
		this.keys = keys;
	}

	@Override
	protected Identifier extractIdentifier(T entity)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		return keys.identifier(entity);
	}
}
