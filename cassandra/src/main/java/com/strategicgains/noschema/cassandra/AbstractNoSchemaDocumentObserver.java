package com.strategicgains.noschema.cassandra;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.document.DocumentObserver;

public abstract class AbstractNoSchemaDocumentObserver
implements DocumentObserver {

	@Override
	public void beforeRead(Identifier identifier) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterRead(Document document) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeCreate(Document document) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterCreate(Document document) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeDelete(Document document) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterDelete(Document document) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeUpdate(Document document) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterUpdate(Document document) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends Identifiable> void beforeEncoding(T entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterEncoding(Document document) {
		// TODO Auto-generated method stub

	}

}
