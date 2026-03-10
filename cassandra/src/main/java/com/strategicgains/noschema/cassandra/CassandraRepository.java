package com.strategicgains.noschema.cassandra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.protocol.internal.util.Bytes;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.Repository;
import com.strategicgains.noschema.RepositoryObserver;
import com.strategicgains.noschema.cassandra.unitofwork.CassandraUnitOfWork;
import com.strategicgains.noschema.cassandra.unitofwork.UnitOfWorkType;
import com.strategicgains.noschema.exception.DuplicateItemException;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.ItemNotFoundException;
import com.strategicgains.noschema.exception.StorageException;
import com.strategicgains.noschema.unitofwork.UnitOfWorkCommitException;

/**
 * Generic Cassandra repository that persists Identifiable instances across a
 * primary table and its related views/indexes using per-view CQL factories.
 *
 * Concrete repositories provide the row-to-entity mapping strategy by
 * implementing asEntity().
 */
public abstract class CassandraRepository<T extends Identifiable>
implements Repository<T>
{
	private final CqlSession session;
	private final PrimaryTable table;
	private final CachingStatementFactory<T> statementFactory;
	private final Map<String, RowMapper<T>> rowMappersByTable = new HashMap<>();
	private final UnitOfWorkType unitOfWorkType;
	private final List<RepositoryObserver<T>> lifecycleObservers = new ArrayList<>();

	protected CassandraRepository(CqlSession session, PrimaryTable table, PreparedStatementFactoryProvider<T> factoryProvider, RowMapper<T> rowMapper)
	{
		this(session, table, UnitOfWorkType.LOGGED, factoryProvider, rowMapper);
	}

	protected CassandraRepository(CqlSession session, PrimaryTable table, UnitOfWorkType unitOfWorkType, PreparedStatementFactoryProvider<T> factoryProvider, RowMapper<T> rowMapper)
	{
		this(session, table, unitOfWorkType, new CachingStatementFactory<>(session, table, factoryProvider), rowMapper);
	}

	protected CassandraRepository(CqlSession session, PrimaryTable table, CachingStatementFactory<T> statementFactory, RowMapper<T> rowMapper)
	{
		this(session, table, UnitOfWorkType.LOGGED, statementFactory, rowMapper);
	}

	protected CassandraRepository(CqlSession session, PrimaryTable table, UnitOfWorkType unitOfWorkType, CachingStatementFactory<T> statementFactory, RowMapper<T> rowMapper)
	{
		this(session, table, unitOfWorkType, statementFactory, toViewMap(table, rowMapper));
	}

	protected CassandraRepository(CqlSession session, PrimaryTable table, UnitOfWorkType unitOfWorkType, CachingStatementFactory<T> statementFactory, Map<String, ? extends RowMapper<T>> rowMappersByTable)
	{
		this.session = Objects.requireNonNull(session);
		this.table = Objects.requireNonNull(table);
		this.unitOfWorkType = Objects.requireNonNull(unitOfWorkType);
		this.statementFactory = Objects.requireNonNull(statementFactory);
		this.rowMappersByTable.putAll(Objects.requireNonNull(rowMappersByTable));
	}

	protected String tableName()
	{
		return table.name();
	}

	public CassandraRepository<T> withObserver(RepositoryObserver<T> observer)
	{
		lifecycleObservers.add(observer);
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
			handleException(e);
		}

		return null;
	}

	public T create(T entity, CassandraUnitOfWork uow)
	{
		lifecycleObservers.forEach(o -> o.beforeCreate(entity));
		table.stream().forEach(t -> uow.registerNew(t.name(), entity));
		lifecycleObservers.forEach(o -> o.afterCreate(entity));
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
			handleException(e);
		}
	}

	public void delete(Identifier id, CassandraUnitOfWork uow)
	{
		T entity = read(id);
		lifecycleObservers.forEach(o -> o.beforeDelete(entity));
		table.stream().forEach(t -> uow.registerDeleted(t.name(), entity));
		lifecycleObservers.forEach(o -> o.afterDelete(entity));
	}

	@Override
	public boolean exists(Identifier id)
	throws InvalidIdentifierException
	{
		return exists(table.name(), id);
	}

	public boolean exists(String viewName, Identifier id)
	{
		return session.executeAsync(statementFactory.exists(viewName, id))
			.thenApply(r -> (Boolean.valueOf(r.one().getLong(0) > 0)))
			.toCompletableFuture()
			.join();
	}

	@Override
	public T read(Identifier id)
	{
		return read(table.name(), id);
	}

	public T read(String viewName, Identifier id)
	{
		try
		{
			T read = readRow(viewName, id)
				.thenApply(row -> asEntity(viewName, row))
				.join();
			lifecycleObservers.forEach(o -> o.afterRead(read));
			return read;
		}
		catch (CompletionException e)
		{
			handleException(e);
		}

		return null;
	}

	public PagedResponse<T> readAll(int limit, String cursor, Object... parms)
	{
		return readAll(table.name(), limit, cursor, parms);
	}

	public PagedResponse<T> readAll(String viewName, int limit, String cursor, Object... parms)
	{
		final PagedResponse<T> response = new PagedResponse<>();

		try
		{
			readRows(viewName, limit, cursor, parms)
				.thenAccept(page -> {
					response.cursor(page.cursor());
					page.iterator().forEachRemaining(row -> {
						T entity = asEntity(viewName, row);
						lifecycleObservers.forEach(o -> o.afterRead(entity));
						response.add(entity);
					});
				})
				.join();
		}
		catch (CompletionException e)
		{
			handleException(e);
		}

		return response;
	}

	@Override
	public List<T> readIn(List<Identifier> ids)
	{
		return readIn(table.name(), ids);
	}

	public List<T> readIn(String viewName, List<Identifier> ids)
	{
		if (ids == null) return Collections.emptyList();

		List<CompletableFuture<T>> futures = ids.stream()
			.<CompletableFuture<T>>map(id -> session.executeAsync(statementFactory.read(viewName, id))
				.thenApply(rs -> rs.one())
				.thenApply(row -> {
					T entity = asEntity(viewName, row);
					lifecycleObservers.forEach(o -> o.afterRead(entity));
					return entity;
				})
				.toCompletableFuture())
			.toList();

		CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
		CompletableFuture<List<T>> allCompletableFuture = allFutures.thenApply(v -> futures.stream()
			.map(CompletableFuture::join)
			.toList());

		try
		{
			return allCompletableFuture.get().stream()
				.filter(Objects::nonNull)
				.toList();
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
		catch (ExecutionException e)
		{
			throw new RuntimeException(e);
		}
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
			handleException(e);
		}

		return null;
	}

	public T update(T entity, T original, CassandraUnitOfWork uow)
	{
		T originalEntity = (original != null ? original : read(entity.getIdentifier()));

		if (originalEntity != null)
		{
			uow.registerClean(table.name(), originalEntity);
		}

		table.stream().forEach(t -> {
			if ((originalEntity != null) && !Objects.equals(originalEntity.getIdentifier(), entity.getIdentifier()))
			{
				uow.registerDeleted(t.name(), originalEntity);
				uow.registerNew(t.name(), entity);
			}
			else
			{
				uow.registerDirty(t.name(), entity);
			}
		});

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
			handleException(e);
		}

		return null;
	}

	public T upsert(T entity, CassandraUnitOfWork uow)
	{
		table.stream().forEach(view -> uow.registerDirty(view.name(), entity));
		return entity;
	}

	protected CassandraUnitOfWork createUnitOfWork()
	{
		return new CassandraUnitOfWork(session, statementFactory, unitOfWorkType);
	}

	private CompletableFuture<Row> readRow(String viewName, Identifier id)
	{
		return session.executeAsync(statementFactory.read(viewName, id))
			.thenApply(rs -> rs.one())
			.thenApply(row -> {
				if (row == null) throw new ItemNotFoundException(id.toString());
				return row;
			})
			.toCompletableFuture();
	}

	private CompletableFuture<PagedRows> readRows(String viewName, int limit, String cursor, Object... parameters)
	{
		return session.executeAsync(statementFactory.readAll(viewName, limit, cursor, parameters))
			.thenApply(rs -> {
				PagedRows rows = new PagedRows();
				rows.cursor(Bytes.toHexString(rs.getExecutionInfo().getPagingState()));
				rows.currentPage(rs.currentPage());
				return rows;
			})
			.toCompletableFuture();
	}

	private void handleException(Exception e)
	throws StorageException
	{
		if (e.getCause() instanceof DuplicateItemException duplicate)
		{
			throw duplicate;
		}

		if (e.getCause() instanceof InvalidIdentifierException invalidId)
		{
			throw invalidId;
		}

		if (e.getCause() instanceof ItemNotFoundException notFound)
		{
			throw notFound;
		}

		throw new StorageException(e.getCause());
	}

	private T asEntity(String viewName, Row row)
	{
		RowMapper<T> mapper = rowMappersByTable.get(viewName);

		if (mapper == null)
		{
			throw new IllegalStateException("No RowMapper configured for view: " + viewName);
		}

		return mapper.toEntity(row);
	}

	private static <T extends Identifiable> Map<String, RowMapper<T>> toViewMap(PrimaryTable table, RowMapper<T> rowMapper)
	{
		Map<String, RowMapper<T>> rowMappersByTable = new HashMap<>();
		table.stream().forEach(view -> rowMappersByTable.put(view.name(), rowMapper));
		return rowMappersByTable;
	}

	private class PagedRows
	{
		private String cursor;
		private Iterable<Row> currentPage;

		void cursor(String hexString)
		{
			this.cursor = hexString;
		}

		String cursor()
		{
			return cursor;
		}

		void currentPage(Iterable<Row> rows)
		{
			this.currentPage = rows;
		}

		Iterator<Row> iterator()
		{
			return currentPage.iterator();
		}
	}
}
