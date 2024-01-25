package com.strategicgains.noschema.cassandra.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.unitofwork.AbstractUnitOfWork;
import com.strategicgains.noschema.unitofwork.Change;
import com.strategicgains.noschema.unitofwork.DirtyChange;
import com.strategicgains.noschema.unitofwork.UnitOfWorkCommitException;
import com.strategicgains.noschema.unitofwork.UnitOfWorkRollbackException;

public class DocumentUnitOfWork
extends AbstractUnitOfWork<Document>
{
    private final CqlSession session;
    private final Map<String, DocumentStatementFactory<Document>> factories;

    public DocumentUnitOfWork(CqlSession session, Map<String, DocumentStatementFactory<Document>> statementsByView) {
        this.session = Objects.requireNonNull(session);
        this.factories = Objects.requireNonNull(statementsByView);
    }

    @Override
    public void commit()
    throws UnitOfWorkCommitException
    {
        BatchStatementBuilder batch = new BatchStatementBuilder(BatchType.LOGGED);
        changes().forEach(change -> createStatementsFor(change).forEach(batch::addStatements));
        ResultSet resultSet = session.execute(batch.build());

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

	private List<BoundStatement> createStatementsFor(Change<Document> change)
	{
		DocumentStatementFactory<Document> generator = factories.get(change.getEntity().getView());

		switch(change.getState())
		{
			case DELETED:
				return Collections.singletonList(generator.delete(change.getId()));
			case DIRTY:
				DirtyChange<Document> update = (DirtyChange<Document>) change;

				if (update.identityChanged())
				{
					ArrayList<BoundStatement> statements = new ArrayList<>(2);
					statements.add(generator.delete(update.getOriginal().getIdentifier()));
					statements.add(generator.create(update.getEntity()));
					return statements;
				}
				else
				{
					return Collections.singletonList(generator.update(change.getEntity()));
				}
			case NEW:
				return Collections.singletonList(generator.create(change.getEntity()));
			default:
				break;
		}

		return Collections.emptyList();
	}
}
