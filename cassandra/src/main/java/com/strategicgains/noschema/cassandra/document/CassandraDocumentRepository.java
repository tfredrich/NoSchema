package com.strategicgains.noschema.cassandra.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.RepositoryObserver;
import com.strategicgains.noschema.cassandra.CachingStatementFactory;
import com.strategicgains.noschema.cassandra.CassandraRepository;
import com.strategicgains.noschema.cassandra.BoundStatementFactory;
import com.strategicgains.noschema.cassandra.BoundStatementFactoryProvider;
import com.strategicgains.noschema.cassandra.PrimaryTable;
import com.strategicgains.noschema.cassandra.schema.SchemaWriter;
import com.strategicgains.noschema.cassandra.unitofwork.CassandraUnitOfWork;
import com.strategicgains.noschema.cassandra.unitofwork.UnitOfWorkType;
import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.document.DocumentCodec;
import com.strategicgains.noschema.document.DocumentObserver;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.ItemNotFoundException;
import com.strategicgains.noschema.exception.KeyDefinitionException;
import com.strategicgains.noschema.unitofwork.UnitOfWorkCommitException;

/**
 * A CassandraDocumentRepository is a Repository implementation that uses
 * Cassandra as its underlying data store and stores Identifiable 
 * instances wrapped in Document instances in the datastore.
 * 
 * This class is meant to be extended to provide the necessary information
 * to connect to the Cassandra cluster (e.g., CqlSession) and specify the
 * table configuration and document codec.
 * 
 * Also, the repository can create and drop the underlying tables necessary to
 * store the entities.
 * 
 * T is the type of entity to be stored in the database.
 */
public class CassandraDocumentRepository<T extends Identifiable>
extends CassandraRepository<T>
implements SchemaWriter<T>
{
	// The lifecycleObservers used to observe the encoding, creation, update, and deletion of entities.
	private final CachingStatementFactory<Document> statementFactory;
	private final Map<String, CassandraDocumentMapper<T>> mappersByTable = new HashMap<>();
	private final List<DocumentObserver> documentObservers = new ArrayList<>();

	protected CassandraDocumentRepository(CqlSession session, PrimaryTable<T> table, DocumentCodec<T> codec)
	{
		this(session, table, UnitOfWorkType.LOGGED, codec);
	}

	protected CassandraDocumentRepository(CqlSession session, PrimaryTable<T> table, UnitOfWorkType unitOfWorkType, DocumentCodec<T> codec)
	{
		super(session, table, unitOfWorkType, entityStatementFactory(codec));
		this.statementFactory = new CachingStatementFactory<>(session, table, DocumentStatementFactory::new);
		mappersByTable.put(table.name(), new CassandraDocumentMapper<>(table.keys(), codec));
		table.views().forEach(view -> mappersByTable.put(view.name(), new CassandraDocumentMapper<>(view.keys(), codec)));
		table.indexes().forEach(index -> mappersByTable.put(index.name(), new CassandraDocumentMapper<>(index.keys(), codec)));
	}

	private static <T extends Identifiable> BoundStatementFactoryProvider<T> entityStatementFactory(DocumentCodec<T> codec)
	{
		return (session, table) -> {
			CassandraDocumentMapper<T> mapper = new CassandraDocumentMapper<>(table.keys(), codec);
			DocumentStatementFactory delegate = new DocumentStatementFactory(session, table);

			return new BoundStatementFactory<>()
			{
				@Override
				public com.datastax.oss.driver.api.core.cql.BoundStatement create(T entity)
				{
					return delegate.create(mapper.toDocument(entity));
				}

				@Override
				public com.datastax.oss.driver.api.core.cql.BoundStatement delete(Identifier id)
				{
					return delegate.delete(id);
				}

				@Override
				public com.datastax.oss.driver.api.core.cql.BoundStatement exists(Identifier id)
				{
					return delegate.exists(id);
				}

				@Override
				public com.datastax.oss.driver.api.core.cql.BoundStatement update(T entity)
				{
					return delegate.update(mapper.toDocument(entity));
				}

				@Override
				public com.datastax.oss.driver.api.core.cql.BoundStatement upsert(T entity)
				{
					return delegate.upsert(mapper.toDocument(entity));
				}

				@Override
				public com.datastax.oss.driver.api.core.cql.BoundStatement read(Identifier id)
				{
					return delegate.read(id);
				}

				@Override
				public com.datastax.oss.driver.api.core.cql.BoundStatement readAll(Object... parameters)
				{
					return delegate.readAll(parameters);
				}
			};
		};
	}

	@Override
	public void ensureTables()
	{
		new DocumentSchemaProvider(table()).create(session());

		if (hasViews())
		{
			table().views().forEach(view -> new DocumentSchemaProvider(view).create(session()));
		}

		if (hasIndexes())
		{
			table().indexes().forEach(idx -> new DocumentSchemaProvider(idx).create(session()));
		}
	}

	@Override
	public void dropTables()
	{
		new DocumentSchemaProvider(table()).drop(session());

		if (hasViews())
		{
			table().views().forEach(view -> new DocumentSchemaProvider(view).drop(session()));
		}

		if (hasIndexes())
		{
			table().indexes().forEach(idx -> new DocumentSchemaProvider(idx).drop(session()));
		}
	}

	public CassandraDocumentRepository<T> withDocumentObserver(DocumentObserver observer)
	{
		documentObservers.add(observer);
		return this;
	}

	public CassandraDocumentRepository<T> withEntityObserver(RepositoryObserver<T> observer)
	{
		super.withObserver(observer);
		return this;
	}

	/**
	 * This method is responsible for creating a new entity in the database.
	 * It first serializes the entity, then registers it in the UnitOfWork for 
	 * the primary table each view, then commits the UnitOfWork.
	 *
	 * @param entity The entity to be created.
	 * @return The created entity.
	 * @throws UnitOfWorkCommitException If there is an error during the commit operation.
	 */
	@Override
	public T create(T entity)
	{
		try
		{
			CassandraUnitOfWork uow = createUnitOfWork();
			T created = create(entity, uow);
			uow.commit();
			return created;
		}
		catch (UnitOfWorkCommitException e)
		{
			handleException(e);
		}

		return null;
	}

	public T create(T entity, CassandraUnitOfWork uow)
	{
		beforeCreate(entity);
		final AtomicReference<byte[]> serialized = new AtomicReference<>();
		final AtomicReference<byte[]> serializedId = new AtomicReference<>();
		final AtomicReference<Document> primaryDocument = new AtomicReference<>();

		table().stream().forEach(t -> {
			final Document d;

			if (serialized.get() == null)
			{
				documentObservers.forEach(o -> o.beforeEncoding(entity));
				d = asDocument(t.name(), entity);
				primaryDocument.set(d);
				serialized.set(d.getObject());
				serializedId.set(d.getIdentifier().toString().getBytes());
			}
			else
			{
				if (t.isIndex())
				{
					d = asDocument(t.name(), entity, serializedId.get());
				}
				else
				{
					d = asDocument(t.name(), entity, serialized.get());
				}

				d.setMetadata(primaryDocument.get().getMetadata());
			}

			documentObservers.forEach(o -> o.afterEncoding(d));
			documentObservers.forEach(o -> o.beforeCreate(d));
			uow.registerNew(t.name(), d);
		});

		documentObservers.forEach(o -> o.afterCreate(primaryDocument.get()));
		afterCreate(entity);
		return entity;
	}

	/**
	 * This method is responsible for deleting an entity from the database.
	 * It first identifies the entity by its Identifier, then registers it
	 * for deletion in the UnitOfWork for the primary table and each view,
	 * and finally commits the UnitOfWork.
	 *
	 * @param id The Identifier of the entity to be deleted.
	 * @throws UnitOfWorkCommitException If there is an error during the commit operation.
	 */
	@Override
	public void delete(Identifier id)
	{
		try
		{
			CassandraUnitOfWork uow = createUnitOfWork();
			delete(id, uow);
			uow.commit();
		}
		catch (UnitOfWorkCommitException e)
		{
			handleException(e);
		}
	}

	public void delete(Identifier id, CassandraUnitOfWork uow)
	{
		final T entity = read(id);
		beforeDelete(entity);
		final AtomicReference<byte[]> serialized = new AtomicReference<>();
		final AtomicReference<Document> primaryDocument = new AtomicReference<>();

		table().stream().forEach(t -> {
			final Document d;

			if (serialized.get() == null)
			{
				documentObservers.forEach(o -> o.beforeEncoding(entity));
				d = asDocument(t.name(), entity);
				primaryDocument.set(d);
				serialized.set(d.getObject());
			}
			else
			{
				d = asDocument(t.name(), entity, serialized.get());
			}

			documentObservers.forEach(o -> o.afterEncoding(d));
			documentObservers.forEach(o -> o.beforeDelete(d));
			uow.registerDeleted(t.name(), d);
		});

		documentObservers.forEach(o -> o.afterDelete(primaryDocument.get()));
		afterDelete(entity);
	}

	/**
	 * This method updates an entity in the database.
	 * It first creates a UnitOfWork, then registers the original entity in it
	 * for the primary table and all its views. If the original entity is not
	 * provided (null), it reads it from the database using the identifier for the
	 * provided entity.
	 *
	 * @param entity The new entity data.
	 * @param original The original entity data. If null, the method will read it from the database.
	 * @return The updated entity.
	 */
	@Override
	public T update(T entity, T original)
	{
		try
		{
			CassandraUnitOfWork uow = createUnitOfWork();
			T updated = update(entity, original, uow);
			uow.commit();
			return updated;
		}
		catch (UnitOfWorkCommitException e)
		{
			handleException(e);
		}

		return null;
	}

	public T update(T entity, T original, CassandraUnitOfWork uow)
	{
		AtomicReference<Document> originalDocument = new AtomicReference<>();
		final T originalEntity;

		if (original != null)
		{
			originalDocument.set(asDocument(original));
			originalEntity = original;
		}
		else
		{
			originalDocument.set(readAsDocument(entity.getIdentifier()).join());
			uow.registerClean(table().name(), originalDocument.get());
			originalEntity = asEntity(table().name(), originalDocument.get());
		}

		documentObservers.forEach(o -> o.beforeUpdate(originalDocument.get()));
		beforeUpdate(entity);
		final byte[] serialized = originalDocument.get().getObject();
		final AtomicReference<Document> updatedDocument = new AtomicReference<>();

		table().stream().forEach(t -> {
			if (updatedDocument.get() == null)
			{
				documentObservers.forEach(o -> o.beforeEncoding(entity));
			}

			final Document updatedViewDocument = asDocument(t.name(), entity);
			final Document originalViewDocument = asDocument(t.name(), originalEntity, serialized);

			if (updatedDocument.get() == null)
			{
				updatedDocument.set(updatedViewDocument);
				documentObservers.forEach(o-> o.afterEncoding(updatedViewDocument));
			}
			else
			{
				updatedViewDocument.setMetadata(updatedDocument.get().getMetadata());
			}

			// If identifier changed, must perform delete and create.
			if (!updatedViewDocument.getIdentifier().equals(originalViewDocument.getIdentifier()))
			{
				uow.registerDeleted(t.name(), originalViewDocument);
				uow.registerNew(t.name(), updatedViewDocument);
			}
			// Otherwise it is simply an update.
			else
			{
				uow.registerDirty(t.name(), updatedViewDocument);
			}
		});

		documentObservers.forEach(o -> o.afterUpdate(updatedDocument.get()));
		afterUpdate(entity);
		return entity;
	}

	/**
	 * This method upserts (updates or inserts) an entity into the database.
	 * It has the benefit of not incurring any reads before update, as it 
	 * doesn't check for existence before updating. If the entity already
	 * exists in the database, it is updated; otherwise, it is inserted.
	 *
	 * @param entity The entity to be upserted.
	 * @return The upserted entity.
	 */
	@Override
	public T upsert(T entity)
	{
		try
		{
			CassandraUnitOfWork uow = createUnitOfWork();
			T upserted = upsert(entity, uow);
			uow.commit();
			return upserted;
		}
		catch (UnitOfWorkCommitException e)
		{
			handleException(e);
		}

		return null;
	}

	public T upsert(T entity, CassandraUnitOfWork uow)
	{
		final AtomicReference<byte[]> bson = new AtomicReference<>();
		final AtomicReference<Document> updated = new AtomicReference<>();

		beforeUpdate(entity);
		table().stream().forEach(view -> {
			final Document d;

			if (bson.get() == null)
			{
				documentObservers.forEach(o -> o.beforeEncoding(entity));
				d = asDocument(view.name(), entity);
				documentObservers.forEach(o -> o.afterEncoding(d));
				documentObservers.forEach(o -> o.beforeUpdate(d));
				bson.set(d.getObject());
				updated.set(d);
			}
			else
			{
				d = asDocument(view.name(), entity, bson.get());
			}

			uow.registerDirty(view.name(), d);
		});

		documentObservers.forEach(o -> o.afterUpdate(updated.get()));
		afterUpdate(entity);
		return entity;
	}

	private CompletableFuture<Document> readAsDocument(Identifier id)
	throws ItemNotFoundException
	{
		return readRow(table().name(), id)
			.thenApply(row -> {
				Document document = asDocument(table().name(), row);
				T entity = asEntity(table().name(), document);
				// TODO: This is a hack. Need to load this from the database.
				document.setIdentifier(entity.getIdentifier());
				return document;				
			});
	}

	@Override
	protected void beforeRead(Identifier id)
	{
		documentObservers.forEach(o -> o.beforeRead(id));
	}

	@Override
	protected void beforeReadAll(Object... parameters)
	{
		documentObservers.forEach(o -> o.beforeRead(new Identifier(parameters)));
	}

	@Override
	protected T mapRow(String viewName, Row row)
	{
		Document d = asDocument(viewName, row);

		if (d == null) return null;

		documentObservers.forEach(o -> o.afterEncoding(d));
		return asEntity(viewName, d);
	}

	private Document asDocument(String viewName, Row row)
	{
		return mappersByTable.get(viewName).toDocument(row);
	}

	private T asEntity(String viewName, Document d)
	{
		return mappersByTable.get(viewName).toEntity(d);
	}

	protected Document asDocument(T entity)
	{
		return asDocument(table().name(), entity);
	}

	protected CassandraUnitOfWork createUnitOfWork()
	{
		return new CassandraUnitOfWork(session(), statementFactory, unitOfWorkType());
	}

	private Document asDocument(String viewName, T entity)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		return mappersByTable.get(viewName).toDocument(entity);
	}

	private Document asDocument(String viewName, T entity, byte[] bytes)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		return mappersByTable.get(viewName).toDocument(entity, bytes);
	}
}
