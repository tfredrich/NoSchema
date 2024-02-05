package com.strategicgains.noschema.document;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

public interface DocumentObserver
{
	void beforeRead(Identifier identifier);
	void afterRead(Document document);

	void beforeCreate(Document document);
	void afterCreate(Document document);

	void beforeDelete(Document document);
	void afterDelete(Document document);

	void beforeUpdate(Document document);
	void afterUpdate(Document document);

	<T extends Identifiable> void beforeEncoding(T entity);
	void afterEncoding(Document document);
}
