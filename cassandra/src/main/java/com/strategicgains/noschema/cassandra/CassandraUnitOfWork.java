package com.strategicgains.noschema.cassandra;

import java.util.Objects;
import java.util.Optional;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.unitofwork.AbstractUnitOfWork;
import com.strategicgains.noschema.unitofwork.Change;
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
        changes().forEach(change -> createStatementFor(change).ifPresent(batch::add));
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

	private Optional<BoundStatement> createStatementFor(Change<T> change)
	{
		switch(change.getState())
		{
			case DELETED:
				return Optional.of(statements.delete(change.getId()));
			case DIRTY:
				return Optional.of(statements.update(change.getEntity()));
			case NEW:
				return Optional.of(statements.create(change.getEntity()));
			default:
				break;
		}

		return Optional.empty();
	}
}
