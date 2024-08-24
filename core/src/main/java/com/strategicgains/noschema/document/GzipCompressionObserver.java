package com.strategicgains.noschema.document;

import com.strategicgains.noschema.Identifiable;

public class GzipCompressionObserver
extends AbstractDocumentObserver
{

	@Override
	public <T extends Identifiable> void beforeEncoding(T entity)
	{
		// TODO Auto-generated method stub
		super.beforeEncoding(entity);
	}

	@Override
	public void afterEncoding(Document document) {
		// TODO Auto-generated method stub
		super.afterEncoding(document);
	}

}
