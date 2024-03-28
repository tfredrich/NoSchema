package com.strategicgains.noschema.cassandra;

import com.strategicgains.noschema.document.AbstractRepositoryObserver;
import com.strategicgains.noschema.document.Document;

public class SampleObserver
extends AbstractRepositoryObserver
{
	@Override
	public void afterEncoding(Document document)
	{
		System.out.println("SampleObserver.afterEncoding(): " + document.getIdentifier());
		document
			.withMetadata("todd", "Here? Check")
			.withMetadata("foo", "bar");
	}

	@Override
	public void afterRead(Document document)
	{
		System.out.println("SampleObserver.afterRead(): " + document.getMetadata());
	}
}
