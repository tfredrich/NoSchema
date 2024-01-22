package com.strategicgains.noschema.cassandra;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;

import org.bson.BSONDecoder;
import org.bson.BasicBSONDecoder;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.NoSchemaRepository;
import com.strategicgains.noschema.cassandra.document.CassandraDocumentFactory;
import com.strategicgains.noschema.cassandra.document.DocumentSchemaProvider;
import com.strategicgains.noschema.cassandra.document.DocumentStatementFactory;
import com.strategicgains.noschema.cassandra.document.DocumentSchemaProvider.Columns;
import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.cassandra.key.KeyPropertyConverter;
import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.document.ObjectCodec;
import com.strategicgains.noschema.exception.DuplicateItemException;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.ItemNotFoundException;
import com.strategicgains.noschema.exception.KeyDefinitionException;
import com.strategicgains.noschema.exception.StorageException;

/**
 * Repository implementation that converts POJOs into Documents and stores them in Cassandra.
 * Also retrieves Document instances and converts them to POJOs before returning them.
 * 
 * @author tfredrich
 * @param <T> The type of object to be stored in the repository (e.g. User).
 */
public class CassandraDocumentRepository<T>
extends AbstractCassandraRepository<T, DocumentStatementFactory<T>>
implements NoSchemaRepository<T>
{
	private static final BSONDecoder DECODER = new BasicBSONDecoder();

	private PrimaryTable table;
	private CassandraDocumentFactory<T> documentFactory;

	public CassandraDocumentRepository(CqlSession session, PrimaryTable table)
	{
		this(session, table, false);
	}

	public CassandraDocumentRepository(CqlSession session, PrimaryTable table, boolean ensureTable)
	{
		super(session, table.keyspace(), table.name(), new DocumentStatementFactory<>(session, table));
		this.table = table;
		this.documentFactory = new CassandraDocumentFactory<>(table.keys());

		if (ensureTable) ensureTables();
	}

	public void ensureTables()
	{
		new DocumentSchemaProvider(table).create(session());
	}

	public void dropTables()
	{
		new DocumentSchemaProvider(table).drop(session());
	}

	private KeyDefinition keys()
	{
		return table.keys();
	}

	public void setCodec(ObjectCodec<T> objectCodec)
	{
		this.documentFactory.setCodec(objectCodec);
	}

	public T create(T entity)
	throws InvalidIdentifierException, DuplicateItemException, KeyDefinitionException
	{
		T document = doCreate(entity);
		//TODO create views as needed.
		//TODO: pre-check views don't exist (if unique).
		return entity;
	}

	public T update(T entity)
	throws ItemNotFoundException, InvalidIdentifierException, KeyDefinitionException
	{
		T document = doUpdate(entity);
		//TODO: update views as needed.
		//TODO: pre-check unique views for existence.
		return entity;
	}

	public T upsert(T entity)
	throws InvalidIdentifierException, StorageException, KeyDefinitionException
	{
		T document = doUpsert(entity);
		//TODO: handle views
		return entity;
	}

	public boolean delete(Identifier id)
	throws ItemNotFoundException, InvalidIdentifierException
	{
		return doDelete(id);
		//TODO: delete views as well.
	}

	public T read(Identifier id)
	throws ItemNotFoundException, InvalidIdentifierException
	{
		return doRead(id);
	}

	public List<T> readAll(Object... parms)
	{
		return (List<T>) doReadAll(parms);
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
//	@Override
//	public List<T> readIn(Identifier... ids)
//	{
//		return (List<T>) doReadIn(ids);
//	}

	protected T marshalRow(Row row)
	{
		Document d = marshalDocument(row);

		if (d == null) return null;

		return documentFactory.asPojo(d);
	}

	// TODO: This is misplaced (depends on DocumentSchemaProvider.Columns)
	protected Document marshalDocument(Row row)
	{
		if (row == null)
		{
			return null;
		}

		Document d = new Document();
		d.setIdentifier(marshalId(keys(), row));
		ByteBuffer b = row.getByteBuffer(Columns.OBJECT);

		if (b != null && b.hasArray())
		{
			//Force the reading of all the bytes.
			d.setObject(DECODER.readObject(b.array()));
		}

		d.setType(row.getString(Columns.TYPE));
		d.setCreatedAt(new Date(row.getInstant(Columns.CREATED_AT).toEpochMilli()));
		d.setUpdatedAt(new Date(row.getInstant(Columns.UPDATED_AT).toEpochMilli()));
		return d;
	}

	private com.strategicgains.noschema.Identifier marshalId(KeyDefinition keyDefinition, Row row)
	{
		com.strategicgains.noschema.Identifier id = new com.strategicgains.noschema.Identifier();
		keyDefinition.components().forEach(t -> id.add(KeyPropertyConverter.marshal(t.column(), t.type(), row)));
		return id;
	}
}
