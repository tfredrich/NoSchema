package com.strategicgains.noschema.cassandra.document;

import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.document.AbstractDocument;

public class CassandraIndexDocument
extends AbstractDocument<byte[]>
{
	public CassandraIndexDocument()
	{
		super();
	}

	public CassandraIndexDocument(Identifier id, byte[] value)
	{
		super(id, value);
	}
}
