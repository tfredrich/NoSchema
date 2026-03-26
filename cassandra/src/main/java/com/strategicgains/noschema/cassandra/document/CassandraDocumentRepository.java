package com.strategicgains.noschema.cassandra.document;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.Repository;
import com.strategicgains.noschema.RepositoryObserver;
import com.strategicgains.noschema.cassandra.AbstractTable;
import com.strategicgains.noschema.cassandra.BoundStatementFactory;
import com.strategicgains.noschema.cassandra.BoundStatementFactoryProvider;
import com.strategicgains.noschema.cassandra.CachingStatementFactory;
import com.strategicgains.noschema.cassandra.CassandraRepository;
import com.strategicgains.noschema.cassandra.Index;
import com.strategicgains.noschema.cassandra.IndexDereferencePolicy;
import com.strategicgains.noschema.cassandra.PagedResponse;
import com.strategicgains.noschema.cassandra.PrimaryTable;
import com.strategicgains.noschema.cassandra.schema.SchemaWriter;
import com.strategicgains.noschema.cassandra.unitofwork.CassandraUnitOfWork;
import com.strategicgains.noschema.cassandra.unitofwork.UnitOfWorkType;
import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.document.DocumentCodec;
import com.strategicgains.noschema.document.DocumentFilter;
import com.strategicgains.noschema.document.DocumentObserver;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.ItemNotFoundException;
import com.strategicgains.noschema.exception.KeyDefinitionException;
import com.strategicgains.noschema.unitofwork.UnitOfWorkCommitException;

/**
 * A CassandraDocumentRepository persists entities as Documents in Cassandra.
 * It composes a generic CassandraRepository for read orchestration and entity
 * lifecycle observation, while handling document mapping and document filters
 * locally.
 *
 * T is the domain entity type exposed to clients.
 */
public class CassandraDocumentRepository<T extends Identifiable>
implements Repository<T>, SchemaWriter
{
	private final DelegateRepository delegate;
	private final CachingStatementFactory<Document> statementFactory;
	private final Map<String, CassandraDocumentMapper<T>> mappersByTable = new HashMap<>();
	private final List<DocumentFilter> documentFilters = new ArrayList<>();
	private final List<DocumentObserver> documentObservers = new ArrayList<>();

	protected CassandraDocumentRepository(CqlSession session, PrimaryTable<T> table, DocumentCodec<T> codec)
	{
		this(session, table, UnitOfWorkType.LOGGED, codec);
	}

	protected CassandraDocumentRepository(CqlSession session, PrimaryTable<T> table, UnitOfWorkType unitOfWorkType, DocumentCodec<T> codec)
	{
		this.delegate = new DelegateRepository(session, table, unitOfWorkType, entityStatementFactory(codec));
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
				public BoundStatement create(T entity)
				{
					return delegate.create(mapper.toDocument(entity));
				}

				@Override
				public BoundStatement delete(Identifier id)
				{
					return delegate.delete(id);
				}

				@Override
				public BoundStatement exists(Identifier id)
				{
					return delegate.exists(id);
				}

				@Override
				public BoundStatement update(T entity)
				{
					return delegate.update(mapper.toDocument(entity));
				}

				@Override
				public BoundStatement upsert(T entity)
				{
					return delegate.upsert(mapper.toDocument(entity));
				}

				@Override
				public BoundStatement read(Identifier id)
				{
					return delegate.read(id);
				}

				@Override
				public BoundStatement readAll(Object... parameters)
				{
					return delegate.readAll(parameters);
				}
			};
		};
	}

	@Override
	public void ensureTables()
	{
		new DocumentSchemaProvider(delegate.tableDef()).create(delegate.sessionDef());

		if (delegate.hasViewsDef())
		{
			delegate.tableDef().views().forEach(view -> new DocumentSchemaProvider(view).create(delegate.sessionDef()));
		}

		if (delegate.hasIndexesDef())
		{
			delegate.tableDef().indexes().forEach(idx -> new DocumentSchemaProvider(idx).create(delegate.sessionDef()));
		}
	}

	@Override
	public void dropTables()
	{
		new DocumentSchemaProvider(delegate.tableDef()).drop(delegate.sessionDef());

		if (delegate.hasViewsDef())
		{
			delegate.tableDef().views().forEach(view -> new DocumentSchemaProvider(view).drop(delegate.sessionDef()));
		}

		if (delegate.hasIndexesDef())
		{
			delegate.tableDef().indexes().forEach(idx -> new DocumentSchemaProvider(idx).drop(delegate.sessionDef()));
		}
	}

	public CassandraDocumentRepository<T> withDocumentObserver(DocumentObserver observer)
	{
		documentObservers.add(observer);
		return this;
	}

	public CassandraDocumentRepository<T> withDocumentFilter(DocumentFilter filter)
	{
		documentFilters.add(filter);
		return this;
	}

	public CassandraDocumentRepository<T> withEntityObserver(RepositoryObserver<T> observer)
	{
		delegate.withObserver(observer);
		return this;
	}

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
			delegate.handleRepositoryException(e);
		}

		return null;
	}

	public T create(T entity, CassandraUnitOfWork uow)
	{
		delegate.beforeCreateEntity(entity);
		final AtomicReference<Document> primaryDocument = new AtomicReference<>();
		final AtomicReference<byte[]> serializedBody = new AtomicReference<>();
		final AtomicReference<byte[]> serializedId = new AtomicReference<>();

		delegate.tableDef().stream().forEach(table -> {
			Document document;

			if (primaryDocument.get() == null)
			{
				document = encodeForWrite(entity, asDocument(table.name(), entity));
				primaryDocument.set(document);
				serializedBody.set(document.getObject());
				serializedId.set(identifierBytes(document));
			}
			else
			{
				document = asWriteDocument(table, entity, serializedBody.get(), serializedId.get());
				document.setMetadata(primaryDocument.get().getMetadata());
			}

			documentObservers.forEach(o -> o.beforeCreate(document));
			uow.registerNew(table.name(), document);
		});

		documentObservers.forEach(o -> o.afterCreate(primaryDocument.get()));
		return entity;
	}

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
			delegate.handleRepositoryException(e);
		}
	}

	public void delete(Identifier id, CassandraUnitOfWork uow)
	{
		T entity = read(id);
		delegate.beforeDeleteEntity(entity);
		final AtomicReference<Document> primaryDocument = new AtomicReference<>();
		final AtomicReference<byte[]> serializedBody = new AtomicReference<>();
		final AtomicReference<byte[]> serializedId = new AtomicReference<>();

		delegate.tableDef().stream().forEach(table -> {
			Document document;

			if (primaryDocument.get() == null)
			{
				document = encodeForWrite(entity, asDocument(table.name(), entity));
				primaryDocument.set(document);
				serializedBody.set(document.getObject());
				serializedId.set(identifierBytes(document));
			}
			else
			{
				document = asWriteDocument(table, entity, serializedBody.get(), serializedId.get());
				document.setMetadata(primaryDocument.get().getMetadata());
			}

			documentObservers.forEach(o -> o.beforeDelete(document));
			uow.registerDeleted(table.name(), document);
		});

		documentObservers.forEach(o -> o.afterDelete(primaryDocument.get()));
	}

	@Override
	public boolean exists(Identifier id)
	{
		return delegate.exists(id);
	}

	public boolean exists(String viewName, Identifier id)
	{
		return delegate.exists(viewName, id);
	}

	@Override
	public T read(Identifier id)
	{
		return delegate.read(id);
	}

	public T read(String viewName, Identifier id)
	{
		return delegate.read(viewName, id);
	}

	public PagedResponse<T> readAll(int limit, String cursor, Object... parms)
	{
		beforeReadDocuments(parms);
		return delegate.readAll(limit, cursor, parms);
	}

	public PagedResponse<T> readAll(String viewName, int limit, String cursor, Object... parms)
	{
		beforeReadDocuments(parms);
		return delegate.readAll(viewName, limit, cursor, parms);
	}

	@Override
	public List<T> readIn(List<Identifier> ids)
	{
		return delegate.readIn(ids);
	}

	public List<T> readIn(String viewName, List<Identifier> ids)
	{
		return delegate.readIn(viewName, ids);
	}

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
			delegate.handleRepositoryException(e);
		}

		return null;
	}

	public T update(T entity, T original, CassandraUnitOfWork uow)
	{
		Document originalDocument = (original != null)
			? asDocument(original)
			: readAsDocument(entity.getIdentifier()).join();
		T originalEntity = (original != null)
			? original
			: asEntity(delegate.tableDef().name(), originalDocument);
		if (original == null)
		{
			uow.registerClean(delegate.tableDef().name(), originalDocument);
		}

		documentObservers.forEach(o -> o.beforeUpdate(originalDocument));
		delegate.beforeUpdateEntity(entity);

		final AtomicReference<Document> updatedPrimaryDocument = new AtomicReference<>();
		final AtomicReference<byte[]> updatedBody = new AtomicReference<>();
		final AtomicReference<byte[]> updatedId = new AtomicReference<>();
		final byte[] originalBody = originalDocument.getObject();
		final byte[] originalId = identifierBytes(originalDocument);

		delegate.tableDef().stream().forEach(table -> {
			Document updatedDocument;
			Document originalViewDocument = asWriteDocument(table, originalEntity, originalBody, originalId);

			if (updatedPrimaryDocument.get() == null)
			{
				updatedDocument = encodeForWrite(entity, asDocument(table.name(), entity));
				updatedPrimaryDocument.set(updatedDocument);
				updatedBody.set(updatedDocument.getObject());
				updatedId.set(identifierBytes(updatedDocument));
			}
			else
			{
				updatedDocument = asWriteDocument(table, entity, updatedBody.get(), updatedId.get());
				updatedDocument.setMetadata(updatedPrimaryDocument.get().getMetadata());
			}

			if (!updatedDocument.getIdentifier().equals(originalViewDocument.getIdentifier()))
			{
				uow.registerDeleted(table.name(), originalViewDocument);
				uow.registerNew(table.name(), updatedDocument);
			}
			else
			{
				uow.registerDirty(table.name(), updatedDocument);
			}
		});

		documentObservers.forEach(o -> o.afterUpdate(updatedPrimaryDocument.get()));
		return entity;
	}

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
			delegate.handleRepositoryException(e);
		}

		return null;
	}

	public T upsert(T entity, CassandraUnitOfWork uow)
	{
		delegate.beforeUpdateEntity(entity);
		final AtomicReference<Document> primaryDocument = new AtomicReference<>();
		final AtomicReference<byte[]> serializedBody = new AtomicReference<>();
		final AtomicReference<byte[]> serializedId = new AtomicReference<>();

		delegate.tableDef().stream().forEach(table -> {
			Document document;

			if (primaryDocument.get() == null)
			{
				document = encodeForWrite(entity, asDocument(table.name(), entity));
				primaryDocument.set(document);
				serializedBody.set(document.getObject());
				serializedId.set(identifierBytes(document));
				documentObservers.forEach(o -> o.beforeUpdate(document));
			}
			else
			{
				document = asWriteDocument(table, entity, serializedBody.get(), serializedId.get());
				document.setMetadata(primaryDocument.get().getMetadata());
			}

			uow.registerDirty(table.name(), document);
		});

		documentObservers.forEach(o -> o.afterUpdate(primaryDocument.get()));
		return entity;
	}

	protected CassandraUnitOfWork createUnitOfWork()
	{
		return new CassandraUnitOfWork(delegate.sessionDef(), statementFactory, delegate.unitOfWorkTypeDef());
	}

	protected Document asDocument(T entity)
	{
		return asDocument(delegate.tableDef().name(), entity);
	}

	private Document asDocument(String tableName, T entity)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		return mappersByTable.get(tableName).toDocument(entity);
	}

	private Document asDocument(String tableName, T entity, byte[] bytes)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		return mappersByTable.get(tableName).toDocument(entity, bytes);
	}

	private Document asDocument(String tableName, Row row)
	{
		return mappersByTable.get(tableName).toDocument(row);
	}

	private T asEntity(String tableName, Document document)
	{
		return mappersByTable.get(tableName).toEntity(document);
	}

	private Document encodeForWrite(T entity, Document document)
	{
		documentFilters.forEach(f -> f.onWrite(document));
		return document;
	}

	private Document decodeAfterRead(Document document)
	{
		for (int i = documentFilters.size() - 1; i >= 0; --i)
		{
			documentFilters.get(i).onRead(document);
		}

		documentObservers.forEach(o -> o.afterRead(document));
		return document;
	}

	private Document asWriteDocument(AbstractTable<T> table, T entity, byte[] serializedBody, byte[] serializedId)
	{
		if (table.isIndex())
		{
			return asDocument(table.name(), entity, serializedId);
		}

		return asDocument(table.name(), entity, serializedBody);
	}

	private byte[] identifierBytes(Document document)
	{
		return document.getIdentifier().toString().getBytes(StandardCharsets.UTF_8);
	}

	private CompletableFuture<Document> readAsDocument(Identifier id)
	throws ItemNotFoundException
	{
		return delegate.readRowData(delegate.tableDef().name(), id)
			.thenApply(row -> {
				Document document = decodeAfterRead(asDocument(delegate.tableDef().name(), row));
				T entity = asEntity(delegate.tableDef().name(), document);
				document.setIdentifier(entity.getIdentifier());
				return document;
			});
	}

	private void beforeReadDocument(Identifier id)
	{
		documentObservers.forEach(o -> o.beforeRead(id));
	}

	private void beforeReadDocuments(Object... parameters)
	{
		documentObservers.forEach(o -> o.beforeRead(new Identifier(parameters)));
	}

	private T mapRow(String viewName, Row row)
	{
		AbstractTable<T> currentTable = delegate.tableDef().table(viewName);

		if (currentTable instanceof Index<T> index && IndexDereferencePolicy.ALWAYS.equals(index.dereferencePolicy()))
		{
			return read(index.getParent().name(), index.toPrimaryIdentifier(row));
		}

		Document document = asDocument(viewName, row);

		if (document == null)
		{
			return null;
		}

		return asEntity(viewName, decodeAfterRead(document));
	}

	private final class DelegateRepository
	extends CassandraRepository<T>
	{
		private DelegateRepository(CqlSession session, PrimaryTable<T> table, UnitOfWorkType unitOfWorkType, BoundStatementFactoryProvider<T> factoryProvider)
		{
			super(session, table, unitOfWorkType, factoryProvider);
		}

		@Override
		protected void beforeRead(Identifier id)
		{
			beforeReadDocument(id);
		}

		@Override
		protected T mapRow(String viewName, Row row)
		{
			return CassandraDocumentRepository.this.mapRow(viewName, row);
		}

		private PrimaryTable<T> tableDef()
		{
			return table();
		}

		private CqlSession sessionDef()
		{
			return session();
		}

		private UnitOfWorkType unitOfWorkTypeDef()
		{
			return unitOfWorkType();
		}

		private boolean hasViewsDef()
		{
			return hasViews();
		}

		private boolean hasIndexesDef()
		{
			return hasIndexes();
		}

		private void beforeCreateEntity(T entity)
		{
			beforeCreate(entity);
		}

		private void beforeDeleteEntity(T entity)
		{
			beforeDelete(entity);
		}

		private void beforeUpdateEntity(T entity)
		{
			beforeUpdate(entity);
		}

		private void handleRepositoryException(Exception e)
		{
			handleException(e);
		}

		private CompletableFuture<Row> readRowData(String viewName, Identifier id)
		{
			return readRow(viewName, id);
		}
	}
}
