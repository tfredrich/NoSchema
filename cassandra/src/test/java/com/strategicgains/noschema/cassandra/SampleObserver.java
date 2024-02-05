package com.strategicgains.noschema.cassandra;

import com.strategicgains.noschema.document.AbstractDocumentObserver;
import com.strategicgains.noschema.document.Document;

public class SampleObserver
extends AbstractDocumentObserver
{
	@Override
	public void afterEncoding(Document document)
	{
		document
			.withMetadata("todd", "Here? Check")
			.withMetadata("foo", "bar");
	}

	@Override
	public void afterRead(Document document)
	{
		System.out.println(document.getMetadata());
	}
}
