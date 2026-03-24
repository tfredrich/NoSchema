package com.strategicgains.noschema.cassandra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Proxy;

import org.junit.Test;

import com.datastax.oss.driver.api.core.CqlSession;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.unitofwork.UnitOfWorkType;

public class CassandraRepositoryTest
{
	@Test
	public void shouldInheritPrimaryRowMapperForViews()
	throws Exception
	{
		NamedRowMapper rowMapper = new NamedRowMapper(1);
		PrimaryTable<TestEntity> table = new PrimaryTable<TestEntity>("ks", "flowers", "id:UUID unique")
			.withRowMapper(rowMapper)
			.withView("by_name", "name:text unique");

		assertSame(rowMapper, table.rowMapper());
		assertSame(rowMapper, table.views().findFirst().orElseThrow().effectiveRowMapper());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldRejectIndexesWithoutMapperOrDereference()
	throws Exception
	{
		PrimaryTable<TestEntity> table = new PrimaryTable<TestEntity>("ks", "flowers", "id:UUID unique")
			.withRowMapper(new NamedRowMapper(1))
			.withView("by_name", "name:text unique");

		table.withIndex("by_created_at", "createdAt:timestamp");

		new TestRepository(table);
	}

	@Test
	public void shouldAllowIndexesToDereferenceWithoutExplicitMapper()
	throws Exception
	{
		PrimaryTable<TestEntity> table = new PrimaryTable<TestEntity>("ks", "flowers", "id:UUID unique")
			.withRowMapper(new NamedRowMapper(1))
			.withIndex("by_created_at", "createdAt:timestamp");
		table.indexes().findFirst().orElseThrow().alwaysDereference();

		new TestRepository(table);
		assertEquals(1, table.getIndexCount());
	}

	private static CqlSession fakeSession()
	{
		return (CqlSession) Proxy.newProxyInstance(
			CqlSession.class.getClassLoader(),
			new Class<?>[] {CqlSession.class},
			(proxy, method, args) -> {
				throw new UnsupportedOperationException(method.getName());
			});
	}

	private static class TestRepository
	extends CassandraRepository<TestEntity>
	{
		TestRepository(PrimaryTable<TestEntity> table)
		{
			super(fakeSession(), table, UnitOfWorkType.LOGGED, new CachingStatementFactory<>(fakeSession(), table, (session, t) -> new NoOpPreparedStatementFactory<>()));
		}
	}

	private static class NamedRowMapper
	implements RowMapper<TestEntity>
	{
		private final Integer id;

		NamedRowMapper(Integer id)
		{
			this.id = id;
		}

		@Override
		public TestEntity toEntity(com.datastax.oss.driver.api.core.cql.Row row)
		{
			return null;
		}
	}

	private static class NoOpPreparedStatementFactory<T extends Identifiable>
	implements PreparedStatementFactory<T>
	{
		@Override
		public com.datastax.oss.driver.api.core.cql.BoundStatement create(T entity)
		{
			return null;
		}

		@Override
		public com.datastax.oss.driver.api.core.cql.BoundStatement delete(Identifier id)
		{
			return null;
		}

		@Override
		public com.datastax.oss.driver.api.core.cql.BoundStatement exists(Identifier id)
		{
			return null;
		}

		@Override
		public com.datastax.oss.driver.api.core.cql.BoundStatement update(T entity)
		{
			return null;
		}

		@Override
		public com.datastax.oss.driver.api.core.cql.BoundStatement upsert(T entity)
		{
			return null;
		}

		@Override
		public com.datastax.oss.driver.api.core.cql.BoundStatement read(Identifier id)
		{
			return null;
		}

		@Override
		public com.datastax.oss.driver.api.core.cql.BoundStatement readAll(Object... parameters)
		{
			return null;
		}
	}

	private static class TestEntity
	implements Identifiable
	{
		@Override
		public Identifier getIdentifier()
		{
			return new Identifier("id");
		}
	}
}
