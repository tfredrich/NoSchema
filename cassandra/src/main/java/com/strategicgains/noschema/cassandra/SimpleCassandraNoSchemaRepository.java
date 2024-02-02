package com.strategicgains.noschema.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.document.DocumentUnitOfWork;

public class SimpleCassandraNoSchemaRepository<T extends Identifiable>
extends AbstractCassandraNoSchemaRepository<T>
{
	public SimpleCassandraNoSchemaRepository(CqlSession session, PrimaryTable table)
	{
		super(session, table);
	}

	@Override
	public T create(T entity)
	{
		DocumentUnitOfWork uow = createUnitOfWork();
		T created = create(entity, uow);
		uow.commit();

		return created;
	}

	@Override
	public boolean delete(Identifier id)
	{
		DocumentUnitOfWork uow = createUnitOfWork();
		delete(id, uow);
		uow.commit();

		return true;
	}

	@Override
	public T update(T entity)
	{
		DocumentUnitOfWork uow = createUnitOfWork();
		T updated = update(entity, uow);
		uow.commit();

		return updated;
	}

	@Override
	public T upsert(T entity)
	{
		DocumentUnitOfWork uow = createUnitOfWork();
		T upserted = upsert(entity, uow);
		uow.commit();

		return upserted;
	}
}
