package com.strategicgains.noschema.cassandra.document;

import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.document.AbstractDocumentFactory;
import com.strategicgains.noschema.document.ObjectCodec;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public class CassandraDocumentFactory<T>
extends AbstractDocumentFactory<T>
{
	private KeyDefinition keys;

	public CassandraDocumentFactory(KeyDefinition keys, ObjectCodec<T> codec)
	{
		super(codec);
		setKeyDefinition(keys);
	}

	public CassandraDocumentFactory(ObjectCodec<T> codec, KeyDefinition keys)
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
