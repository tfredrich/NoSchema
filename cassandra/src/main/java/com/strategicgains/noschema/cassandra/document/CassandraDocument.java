package com.strategicgains.noschema.cassandra.document;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.document.ByteArrayDocument;

public class CassandraDocument<T extends Identifiable>
extends ByteArrayDocument<T>
{
	public CassandraDocument()
	{
		super();
	}

	public CassandraDocument(Identifier id, byte[] value, Class<T> type)
	{
		super(id, value, type);
	}
}
