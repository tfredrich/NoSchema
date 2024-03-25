package com.strategicgains.noschema.document;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

/**
 * An IndexEntry is a mapping between an Identifier and a Document.  It is used to
 * index large documents when the origin document contains too much data to be
 * denormalized into a view.  The IndexEntry is used to store the Identifier of the
 * alternate key along with a reference to the original document.
 * <p/>
 * Performing a lookup on an IndexEntry will return the original document but incurs
 * the overhead of an additional lookup. Therefore, it is best to use IndexEntry only
 * when the original document is too large to denormalize into a view and readAll() or
 * other cursor-based queries are not expected.
 * 
 * @author Todd Fredrich
 */
public class IndexEntry
implements Identifiable
{
	private Identifier identifier;
	private Identifier documentId;

	public IndexEntry()
	{
		super();
	}

	public IndexEntry(Identifier identifier, Document document)
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
