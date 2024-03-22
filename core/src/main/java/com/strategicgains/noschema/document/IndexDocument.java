package com.strategicgains.noschema.document;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

public class IndexDocument
implements Identifiable
{
	private Identifier identifier;
	private Identifier documentId;

	public IndexDocument()
	{
		super();
	}

	public IndexDocument(Identifier identifier, Document document)
	{
		this();
		setIdentifier(identifier);
		setDocumentId(document);
	}

	public Identifier getDocumentId()
	{
		return (hasDocumentId() ? new Identifier(documentId) : null);
	}

	public boolean hasDocumentId()
	{
		return (documentId != null);
	}

	public void setDocumentId(Identifier documentId)
	{
		this.documentId = documentId;
	}

	public void setDocumentId(Document document)
	{
		setDocumentId(document.getIdentifier());
	}

	@Override
	public Identifier getIdentifier()
	{
		return (hasIdentifier() ? new Identifier(identifier) : null);
	}

	public boolean hasIdentifier()
	{
		return (identifier != null);
	}

	public void setIdentifier(Identifier identifier)
	{
		this.identifier = identifier;
	}
}
