package com.strategicgains.noschema.cassandra;

import com.strategicgains.noschema.document.AbstractDocumentFilter;
import com.strategicgains.noschema.document.Document;

public class SampleDocumentFilter
extends AbstractDocumentFilter
{
	@Override
	public void onWrite(Document document)
	{
		System.out.println("SampleDocumentFilter.onWrite(): " + document.getIdentifier());
//		document
//			.withMetadata("todd", "Here? Check")
//			.withMetadata("foo", "bar");
	}

	@Override
	public void onRead(Document document)
	{
		System.out.println("SampleDocumentFilter.onRead(): " + document.getMetadata());
	}
}
