package com.strategicgains.noschema.document;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;

/**
 * This class is useful for creating a {@link DocumentObserver} that only observes a few of the lifecycle points.
 * <p/>
 * This class provides empty methods for all of the observation points in the Document lifecycle. Subclasses override the
 * method(s) they want to observe.
 * <p/>
 * For example, if you only want to observe the creation of documents, you can create a subclass of this class and override
 * the {@link #afterCreate(Document) afterCreate()} method.
 * 
 * @author Todd Fredrich
 * @since Feb 5, 2024
 * @see DocumentObserver
 * @see Document
 * @see Identifiable
 * @see Identifier
 */
public abstract class AbstractDocumentObserver
implements DocumentObserver
{
    /**
     * This method is called before reading a document with the given identifier. It allows you to perform any necessary
     * actions before retrieving a document from storage.
     *
     * @param identifier The unique identifier of the document to be read.
     */
    @Override
    public void beforeRead(Identifier identifier) {}

    /**
     * This method is called after successfully reading a document. It allows you to perform any necessary actions after
     * retrieving a document from storage.
     *
     * @param document The Document object that has been read.
     */
    @Override
    public void afterRead(Document document) {}

    /**
     * This method is called before creating a new document. It allows you to perform any necessary actions before creating a
     * new document in the storage.
     *
     * @param document The Document object that will be created.
     */
    @Override
    public void beforeCreate(Document document) {}

    /**
     * This method is called after successfully creating a new document. It allows you to perform any necessary actions after
     * successfully creating a new document in the storage.
     *
     * @param document The Document object that has been created.
     */
    @Override
    public void afterCreate(Document document) {}

    /**
     * This method is called before deleting an existing document. It allows you to perform any necessary actions before
     * deleting a document from the storage.
     *
     * @param document The Document object that will be deleted.
     */
    @Override
    public void beforeDelete(Document document) {}

    /**
     * This method is called after successfully deleting a document. It allows you to perform any necessary actions after
     * successfully deleting a document from the storage.
     *
     * @param document The Document object that has been deleted.
     */
    @Override
    public void afterDelete(Document document) {}

    /**
     * This method is called before updating an existing document. It allows you to perform any necessary actions before
     * updating a document in the storage.
     *
     * @param document The Document object that will be updated.
     */
    @Override
    public void beforeUpdate(Document document) {}

    /**
     * This method is called after successfully updating a document. It allows you to perform any necessary actions after
     * successfully updating a document in the storage.
     *
     * @param document The Document object that has been updated.
     */
    @Override
    public void afterUpdate(Document document) {}

    /**
     * This method is called before encoding an entity into BSON. It allows you to perform any necessary actions before
     * converting an object into binary format for storage.
     *
     * @param <T>       The type of {@link Identifiable} entity being encoded.
     * @param entity    The identifiable object that will be encoded into BSON.
     */
    @Override
    public <T extends Identifiable> void beforeEncoding(T entity) {}

    /**
     * This method is called after successfully converting (encoding) an object into BSON. It allows you to perform any
     * necessary actions after converting an object into binary format for storage.
     *
     * @param document The Document object that has been encoded into BSON.
     */
    @Override
    public void afterEncoding(Document document) {}
}
