package com.strategicgains.noschema.cassandra.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.datastax.oss.driver.api.core.CqlSession;

/**
 * A Singleton object to drop and/or (re)create keyspaces and database schemas in Cassandra.
 * 
 * @author tfredrich
 * @since May 7, 2015
 */
public class SchemaRegistry
{
	private static final SchemaRegistry INSTANCE = new SchemaRegistry();
	private List<SchemaProvider> schemas = new ArrayList<>();
	private String keyspace;

	private SchemaRegistry()
	{
		// prevents instantiation.
	}

	/**
	 * Retrieve the singleton instance of the SchemaRegistry.
	 * 
	 * @return the singleton instance of the SchemaRegistry.
	 */
	private static SchemaRegistry instance()
	{
		return INSTANCE;
	}

	/**
	 * Clear all schema providers from the registry.
	 * 
	 * @return this schema registry to enable method chaining.
	 */
	public static SchemaRegistry clear()
	{
		return instance()._clearAll();
	}

	/**
	 * Initialize the database schema using the given CQL session.
	 * 
	 * @param session a Cassandra CQL session.
	 * @return this schema registry to enable method chaining.
	 */
	public static SchemaRegistry initialize(CqlSession session)
    {
        return instance()._initializeAll(session);
    }

	/**
	 * Register a keyspace with the SchemaRegistry. Causes the registration of a KeyspaceProvider
	 * which will create a very basic keyspace in Cassandra if it does not already exist when
	 * initializing the schema.
	 * 
	 * @param keyspace
	 * @return
	 */
	public static SchemaRegistry keyspace(String keyspace)
	{
		instance()._setKeyspace(keyspace);
		return instance()._addProvider(new KeyspaceProvider(keyspace));
	}

	/**
	 * Retrieve the keyspace name registered with the SchemaRegistry.
	 * 
	 * @return the keyspace name registered with the SchemaRegistry.
	 */
	public static String keyspace()
	{
		return instance()._getKeyspace();
	}

	/**
	 * Execute the CQL script to create all schemas on this session.
	 * 
	 * @param session a Cassandra CQL session.
	 */
	public static void createAll(CqlSession session)
	{
		instance()._createAll(session);
	}

	/**
	 * Execute the CQL script to drop all schemas on this session.
	 * 
	 * @param session a Cassandra CQL session.
	 */
	public static void dropAll(CqlSession session)
	{
		instance()._dropAll(session);
	}

	/**
	 * Retrieve the CQL script to create all schemas.
	 * 
	 * @return a CQL script to create all schemas.
	 */
	public static String getCreateAllSchema()
	{
		return instance()._exportCreateAll();
	}

	/**
	 * Retrieve the CQL script to drop all schemas.
	 * 
	 * @return a CQL script to drop all schemas.
	 */
	public static String getDropAllSchema()
	{
		return instance()._exportDropAll();
	}

	/**
	 * Retrieve the CQL script to drop all schemas, then create them.
	 * 
	 * @return a CQL script to drop all schemas, then create them.
	 */
	public static String getInitializeSchema()
	{
		return instance()._exportInitializeAll();
	}

	private String _getKeyspace()
	{
		return keyspace;
	}

	private SchemaRegistry _setKeyspace(String keyspace)
	{
		this.keyspace = keyspace;
		return this;
	}

	private SchemaRegistry _clearAll()
	{
		_setKeyspace(null);
		schemas.clear();
		return this;
	}

	/**
	 * Registration order matters!
	 * 
	 * @param provider
	 * @return this schema registry
	 */
	private SchemaRegistry _addProvider(SchemaProvider provider)
	{
		if (provider != null)
		{
			schemas.add(provider);
		}

		return this;
	}

	private String _exportCreateAll()
	{
		return schemas.stream()
			.map(p -> p.asCreateScript())
			.collect(Collectors.joining("\n"));
	}

	private String _exportDropAll() {
		return schemas.stream()
			.map(p -> p.asDropScript())
			.collect(Collectors.joining("\n"));
	}

	private String _exportInitializeAll()
	{
	    return schemas.stream()
	    	.flatMap(p -> Stream.of(p.asDropScript(), p.asCreateScript()))
	    	.collect(Collectors.joining("\n"));
	}

	/**
	 * Drops all schemas, then creates all.
	 * 
	 * @param session
	 */
	private SchemaRegistry _initializeAll(CqlSession session)
	{
		schemas.forEach(p -> {
			p.drop(session);
			p.create(session);
		});

		return this;
	}

	private void _createAll(CqlSession session)
	{
		schemas.forEach(p -> p.create(session));
	}

	private void _dropAll(CqlSession session)
	{
		schemas.forEach(p -> p.drop(session));
	}
}
