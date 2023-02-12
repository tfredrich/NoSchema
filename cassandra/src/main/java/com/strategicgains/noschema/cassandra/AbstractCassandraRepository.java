package com.strategicgains.noschema.cassandra;

import java.util.List;
import java.util.stream.Collectors;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.exception.DuplicateItemException;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.ItemNotFoundException;
import com.strategicgains.noschema.exception.StorageException;

public abstract class AbstractCassandraRepository<T, F extends StatementFactory<T>>
{
	private CqlSession session;
	private String keyspace;
	private String tableName;
	private F statementFactory;

	protected AbstractCassandraRepository(CqlSession session, String keyspace, String tableName, F statementFactory)
	{
		this.session = session;
		this.keyspace = keyspace;
		this.tableName = tableName;
		this.statementFactory = statementFactory;
	}

	public T doCreate(T entity)
	throws InvalidIdentifierException, DuplicateItemException
	{
		ResultSet result = submitCreate(entity);

		if (result.wasApplied())
		{
			return entity;
		}

		throw new DuplicateItemException(entity.toString());
	}

	public boolean exists(Identifier id)
	throws InvalidIdentifierException
	{
		return (submitExists(id).one().getLong(0) > 0);
	}

	public T doUpdate(T entity)
	throws ItemNotFoundException, InvalidIdentifierException
	{
		ResultSet results = submitUpdate(entity);

		if (results.wasApplied())
		{
			return entity;
		}

		throw new ItemNotFoundException(entity.toString());			
	}

	public T doUpsert(T entity)
	throws StorageException
	{
		ResultSet result = submitUpsert(entity);

		if (result.wasApplied())
		{
			return entity;
		}

		//TODO: This doesn't provide any informational value... what should it be?
		throw new StorageException(String.format("Table %s.%s failed to store entity: %s", keyspace(), tableName(), entity.toString()));
	}

	public boolean doDelete(Identifier id)
	throws ItemNotFoundException, InvalidIdentifierException
	{
		ResultSet results = submitDelete(id);

		//TODO: Determine if this is consistent with other repository implementations. It seems wonky.
		if (!results.wasApplied())
		{
			throw new ItemNotFoundException(id.toString());
		}

		return true;
	}

	public T doRead(Identifier id)
	throws ItemNotFoundException, InvalidIdentifierException
	{
		ResultSet results = submitRead(id);
		Row row = results.one();

		if (row == null)
		{
			throw new ItemNotFoundException(id.toString());
		}

		return marshalRow(row);
	}

	public List<T> doReadAll(Object... parameters)
	{
		BoundStatement bs = statementFactory.readAll(parameters);
		return marshalAll(session.execute(bs));
	}

	/**
	 * Read all given identifiers.
	 * 
	 * Leverages the token-awareness of the driver to optimally query each node directly instead of invoking a
	 * coordinator node. Sends an individual query for each partition key, so reaches the appropriate replica
	 * directly and collates the results client-side.
	 * 
	 * @param ids the partition keys (identifiers) to select.
	 */
//	public List<T> doReadIn(Identifier... ids)
//	{
//		List<ResultSet> results = submitReadIn(ids);
//		return results.stream().map(rs -> marshalRow(rs.one())).collect(Collectors.toList());
//	}

	public CqlSession session()
	{
		return session;
	}

	protected String keyspace()
	{
		return keyspace;
	}

	protected String tableName()
	{
		return tableName;
	}

	protected F statementFactory()
	{
		return statementFactory;
	}

	protected void statementFactory(F factory)
	{
		this.statementFactory = factory;
	}

	protected BoundStatement bindIdentity(PreparedStatement bs, Identifier id)
	{
		return bs.bind(id.components().toArray());
	}

	protected List<T> marshalAll(ResultSet rs)
	{
		return rs.all().stream().map(this::marshalRow).collect(Collectors.toList());
	}

	protected abstract T marshalRow(Row row);

	protected ResultSet submitCreate(T entity)
	throws InvalidIdentifierException
	{
		BoundStatement bs = statementFactory.create(entity);
		return session.execute(bs);
	}

	protected ResultSet submitDelete(Identifier id)
	throws InvalidIdentifierException
	{
		BoundStatement bs = statementFactory.delete(toForeignIdentifier(id));
		return session.execute(bs);
	}

	private ResultSet submitExists(Identifier id)
	throws InvalidIdentifierException
	{
		BoundStatement bs = statementFactory().exists(toForeignIdentifier(id));
		return session.execute(bs);
	}

	private ResultSet submitRead(Identifier id)
	throws InvalidIdentifierException
	{
		BoundStatement bs = statementFactory.read(toForeignIdentifier(id));
		return session.execute(bs);
	}

	protected ResultSet submitUpdate(T entity)
	throws InvalidIdentifierException
	{
		BoundStatement bs = statementFactory.update(entity);
		return session.execute(bs);
	}

	protected ResultSet submitUpsert(T entity)
	throws InvalidIdentifierException
	{
		BoundStatement bs = statementFactory().upsert(entity);
		return session.execute(bs);
	}

	private com.strategicgains.noschema.Identifier toForeignIdentifier(Identifier id)
	{
		return new com.strategicgains.noschema.Identifier(id.components().toArray());
	}

	/**
	 * Leverages the token-awareness of the driver to optimally query each node directly instead of invoking a
	 * coordinator node. Sends an individual query for each partition key, so reaches the appropriate replica
	 * directly and collates the results client-side.
	 * 
	 * @param ids the partition keys (identifiers) to select.
	 * @return a List of ListenableFuture instances for each underlying ResultSet--one for each ID.
	 */
//	private  List<ResultSet> submitReadIn(Identifier... ids)
//	{
//		if (ids == null) return Collections.emptyList();
//
//		List<BoundStatement> statements = new ArrayList<>(ids.length);
//		Arrays.asList(ids).stream().forEach(id -> {
//			BoundStatement bs = statementFactory.read(asForeignIdentifier(id));
//			statements.add(bs);
//		});
//
//		return submitStatementsAsync(statements);
//	}
//
//	protected List<ResultSet> submitStatementsAsync(List<BoundStatement> statements)
//	{
//		List<CompletionStage<AsyncResultSet>> futures = new ArrayList<>(statements.size());
//		statements.stream().forEach(s -> futures.add(session.execute(s)));
//		return futuresInCompletionOrder(futures);
//	}
//
//	private List<ResultSet> futuresInCompletionOrder(List<CompletableFuture<AsyncResultSet>> futures)
//	{
//		return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
//		CompletableFutures.allDone(futures).whenComplete((a, b) -> {
//			return futures.stream().map(f -> CompletableFutures.getCompleted(f)).collect(Collectors.toList());
//		});
//		return Futures.inCompletionOrder(futures).stream().map(t -> {
//			try {
//				return t.get();
//			} catch (InterruptedException e) {
//				Thread.currentThread().interrupt();
//				throw new RuntimeException(e);
//			} catch (ExecutionException e) {
//				throw new RuntimeException(e);
//			}
//		}).collect(Collectors.toList());
//	}
}
