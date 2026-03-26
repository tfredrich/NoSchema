package com.strategicgains.noschema.cassandra;

import com.strategicgains.noschema.document.AbstractDocumentObserver;
import com.strategicgains.noschema.document.Document;

public class SampleDocumentObserver
extends AbstractDocumentObserver
{
	@Override
	public void afterCreate(Document document)
	{
		System.out.println("SampleObserver.afterCreate(): " + document.getIdentifier());
//		document
//			.withMetadata("todd", "Here? Check")
//			.withMetadata("foo", "bar");
	}

	@Override
	public void afterRead(Document document)
	{
		System.out.println("SampleObserver.afterRead(): " + document.getMetadata());
	}
}
