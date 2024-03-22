package com.strategicgains.noschema.unitofwork;

import java.util.Objects;

import com.strategicgains.noschema.document.Document;

public class DocumentChange
extends Change<Document>
{
	private String view;

	public DocumentChange(String view, Document entity, EntityState state)
	{
		super(entity, state);
		this.view = view;
	}

	public String getView()
	{
		return view;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() + Objects.hash(view);
	}

	@Override
	public boolean equals(Object that)
	{
		return super.equals(that) && Objects.equals(this.view, ((DocumentChange) that).view);
	}
}
