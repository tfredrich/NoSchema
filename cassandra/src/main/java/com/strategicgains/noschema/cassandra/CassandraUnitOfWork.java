package com.strategicgains.noschema.cassandra;

import java.util.Objects;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.unitofwork.AbstractUnitOfWork;
import com.strategicgains.noschema.unitofwork.UnitOfWorkCommitException;
import com.strategicgains.noschema.unitofwork.UnitOfWorkRollbackException;

public class CassandraUnitOfWork<T extends Identifiable>
extends AbstractUnitOfWork<T>
{
    private final CqlSession session;
    private final StatementFactory<T> statements;

    public CassandraUnitOfWork(CqlSession session, StatementFactory<T> statementGenerator) {
        this.session = Objects.requireNonNull(session);
        this.statements = Objects.requireNonNull(statementGenerator);
    }

    @Override
    public void commit()
    throws UnitOfWorkCommitException
    {
        BatchStatement batch = new BatchStatementBuilder(BatchType.LOGGED).build();

        newObjects().forEach(entity -> batch.add(statements.create(entity)));
        dirtyObjects().forEach(entity -> batch.add(statements.update(entity)));
        deletedObjects().forEach(entity -> batch.add(statements.delete(entity.getIdentifier())));

        ResultSet resultSet = session.execute(batch);

        if (resultSet.wasApplied())
        {
            reset();
        }
        else
        {
            throw new UnitOfWorkCommitException("Commit failed", resultSet.getExecutionInfo().getErrors().get(0).getValue());
        }
    }

    @Override
    public void rollback()
    throws UnitOfWorkRollbackException
    {
        // No-op for CassandraUnitOfWork since we're using a logged batch
    }
}
