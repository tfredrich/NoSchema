package com.strategicgains.noschema.cassandra.document;

import java.nio.ByteBuffer;
import java.sql.Date;

import com.datastax.oss.driver.api.core.cql.Row;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.document.DocumentSchemaProvider.Columns;
import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.document.AbstractDocumentFactory;
import com.strategicgains.noschema.document.ByteArrayCodec;
import com.strategicgains.noschema.document.ByteArrayDocument;
import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public class CassandraDocumentFactory<T extends Identifiable>
extends AbstractDocumentFactory<T, byte[]>
{
	private KeyDefinition keys;

	public CassandraDocumentFactory(KeyDefinition keys, ByteArrayCodec<T> codec)
	{
		super(codec);
		setKeyDefinition(keys);
	}

	public CassandraDocumentFactory(ByteArrayCodec<T> codec, KeyDefinition keys)
	{
		super(codec);
		setKeyDefinition(keys);
	}

	public ByteArrayDocument<T> asDocument(Row row)
	{
		if (row == null)
		{
			return null;
		}

		ByteArrayDocument<T> d = new ByteArrayDocument<>();
		ByteBuffer b = row.getByteBuffer(Columns.OBJECT);

		if (b != null && b.hasArray())
		{
			//Force the reading of all the bytes.
			d.setValue((b.array()));
		}

		d.setType(row.getString(Columns.TYPE));
		d.setMetadata(row.getMap(Columns.METADATA, String.class, String.class));
		d.setCreatedAt(new Date(row.getInstant(Columns.CREATED_AT).getEpochSecond()));
		d.setUpdatedAt(new Date(row.getInstant(Columns.UPDATED_AT).getEpochSecond()));
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

	@Override
	public T asPojo(Document<byte[]> document)
	{
		return null;
	}

	@Override
	protected Document<byte[]> createDocument(Identifier id, byte[] bytes, Class<T> clazz)
	{
		return new ByteArrayDocument<>(id, bytes, clazz);
	}
}
