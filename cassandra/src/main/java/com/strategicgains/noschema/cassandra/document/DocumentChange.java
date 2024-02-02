package com.strategicgains.noschema.cassandra.document;

import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.unitofwork.Change;
import com.strategicgains.noschema.unitofwork.EntityState;

public class DocumentChange
extends Change<Document>
{
	private String view;

	public DocumentChange(Document entity, EntityState state, String view)
	{
		super(entity, state);
		this.view = view;
	}

	public String getView()
	{
		return view;
	}
}
