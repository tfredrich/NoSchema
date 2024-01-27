package com.strategicgains.noschema.cassandra.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.exception.DuplicateItemException;
import com.strategicgains.noschema.exception.ItemNotFoundException;
import com.strategicgains.noschema.unitofwork.AbstractUnitOfWork;
import com.strategicgains.noschema.unitofwork.Change;
import com.strategicgains.noschema.unitofwork.DirtyChange;
import com.strategicgains.noschema.unitofwork.UnitOfWorkCommitException;
import com.strategicgains.noschema.unitofwork.UnitOfWorkRollbackException;

public class DocumentUnitOfWork
extends AbstractUnitOfWork<Document>
{
    private final CqlSession session;
    private final DocumentStatementGenerator generator;

    public DocumentUnitOfWork(CqlSession session, DocumentStatementGenerator statementsByView)
    {
        this.session = Objects.requireNonNull(session);
        this.generator = Objects.requireNonNull(statementsByView);
    }

    @Override
    public void commit()
    throws UnitOfWorkCommitException
    {
    	List<BoundStatement> uniqueness = new ArrayList<>();
    	List<BoundStatement> existence = new ArrayList<>();
    	List<BoundStatement> statements = new ArrayList<>();

        changes().forEach(change -> {
        	uniqueness.add(createUniquenessCheckFor(change));
        	existence.add(createExistenceCheckFor(change));
        	statements.addAll(createStatementsFor(change));
        });

        BatchStatementBuilder batch = new BatchStatementBuilder(BatchType.LOGGED);
        statements.stream().forEach(batch::addStatement);
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

	private BoundStatement createExistenceCheckFor(Change<Document> change)
	{
		return generator.exists(change.getEntity().getView(), change.getId());
	}

	private BoundStatement createUniquenessCheckFor(Change<Document> change)
	{
		return generator.exists(change.getEntity().getView(), change.getId());
	}

	private List<BoundStatement> createStatementsFor(Change<Document> change)
	{
		String viewName = change.getEntity().getView();

		switch(change.getState())
		{
			case DELETED:
				return Collections.singletonList(generator.delete(viewName, change.getId()));
			case DIRTY:
				DirtyChange<Document> update = (DirtyChange<Document>) change;

				if (update.identityChanged())
				{
					ArrayList<BoundStatement> statements = new ArrayList<>(2);
					statements.add(generator.delete(viewName, update.getOriginal().getIdentifier()));
					statements.add(generator.create(viewName, update.getEntity()));
					return statements;
				}
				else
				{
					return Collections.singletonList(generator.update(viewName, change.getEntity()));
				}
			case NEW:
				return Collections.singletonList(generator.create(viewName, change.getEntity()));
			default:
				break;
		}

		return Collections.emptyList();
	}

	private void ensureUniqueness()
	throws DuplicateItemException
	{
		// TODO Auto-generated method stub
		
	}

    private void ensureExistence()
    throws ItemNotFoundException
    {
		// TODO Auto-generated method stub
		
	}
}
