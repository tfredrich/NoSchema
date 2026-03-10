package com.strategicgains.noschema.cassandra.document;

import java.nio.ByteBuffer;
import java.sql.Date;

import com.datastax.oss.driver.api.core.cql.Row;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.RowMapper;
import com.strategicgains.noschema.cassandra.document.DocumentSchemaProvider.Columns;
import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.document.AbstractDocumentMapper;
import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.document.DocumentCodec;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public class CassandraDocumentMapper<T extends Identifiable>
extends AbstractDocumentMapper<T>
implements RowMapper<T>
{
	private final KeyDefinition keys;

	public CassandraDocumentMapper(KeyDefinition keys, DocumentCodec<T> codec)
	{
		super(codec);
		this.keys = keys;
	}

	public Document toDocument(Row row)
	{
		if (row == null)
		{
			return null;
		}

		Document doc = new Document();
		ByteBuffer b = row.getByteBuffer(Columns.OBJECT);

		if (b != null && b.hasArray())
		{
			//Force the reading of all the bytes.
			doc.setObject((b.array()));
		}

		doc.setType(row.getString(Columns.TYPE));
		doc.setMetadata(row.getMap(Columns.METADATA, String.class, String.class));
		doc.setCreatedAt(new Date(row.getInstant(Columns.CREATED_AT).getEpochSecond()));
		doc.setUpdatedAt(new Date(row.getInstant(Columns.UPDATED_AT).getEpochSecond()));
		return doc;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T toEntity(Row row)
	{
		if (row == null)
		{
			return null;
		}

		ByteBuffer b = row.getByteBuffer(Columns.OBJECT);

		if (b != null && b.hasArray())
		{
			try
			{
				return fromBytes(b.array(), (Class<T>) Class.forName(row.getString(Columns.TYPE)));
			}
			catch (ClassNotFoundException e)
			{
				throw new RuntimeException(e);
			}
		}

		return null;
	}

	@Override
	protected Identifier extractIdentifier(T entity)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		return keys.identifier(entity);
	}
}
