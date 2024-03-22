package com.strategicgains.noschema.cassandra.index;

import com.datastax.oss.driver.api.core.cql.Row;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.document.AbstractDocumentFactory;
import com.strategicgains.noschema.document.IndexDocument;
import com.strategicgains.noschema.document.ObjectCodec;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public class CassandraIndexDocumentFactory<T>
extends AbstractDocumentFactory<T>
{
	private KeyDefinition keys;

	public CassandraIndexDocumentFactory(KeyDefinition keys, ObjectCodec<T> codec)
	{
		super(codec);
		setKeyDefinition(keys);
	}

	public CassandraIndexDocumentFactory(ObjectCodec<T> codec, KeyDefinition keys)
	{
		super(codec);
		setKeyDefinition(keys);
	}

	public IndexDocument asDocument(Row row)
	{
		if (row == null)
		{
			return null;
		}

		IndexDocument d = new IndexDocument();

		//TODO: map the columns to the Document Identifier.
		return d;
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
