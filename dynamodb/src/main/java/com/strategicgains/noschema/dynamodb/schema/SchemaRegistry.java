package com.strategicgains.noschema.dynamodb.schema;

import java.util.ArrayList;
import java.util.List;

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

	public static SchemaRegistry instance()
	{
		return INSTANCE;
	}

	public static SchemaRegistry keyspace(String keyspace)
	{
		instance().setKeyspace(keyspace);
		return instance().schema(new KeyspaceProvider(keyspace));
	}

	public static String keyspace()
	{
		return instance().getKeyspace();
	}

	public String getKeyspace()
	{
		return keyspace;
	}

	public SchemaRegistry setKeyspace(String keyspace)
	{
		this.keyspace = keyspace;
		return this;
	}

	/**
	 * Registration order matters!
	 * 
	 * @param provider
	 * @return this schema registry
	 */
	public SchemaRegistry schema(SchemaProvider provider)
	{
		if (provider != null)
		{
			schemas.add(provider);
		}

		return this;
	}

	public SchemaRegistry exportCreateAll()
	{
		schemas.forEach(p -> System.out.println(p.asCreateScript()));
		return this;
	}

	public SchemaRegistry exportDropAll()
	{
		schemas.forEach(p -> System.out.println(p.asDropScript()));
		return this;
	}

	public SchemaRegistry exportInitializeAll()
	{
		schemas.forEach(p -> {
			System.out.println(p.asDropScript());	
			System.out.println(p.asCreateScript());	
		});

		return this;
	}

	/**
	 * Drops all schemas, then creates all.
	 * 
	 * @param session
	 * @param keyspace
	 */
	public SchemaRegistry initializeAll(CqlSession session)
	{
		schemas.forEach(p -> {
			p.drop(session);
			p.create(session);
		});

		return this;
	}

	public void createAll(CqlSession session)
	{
		schemas.forEach(p -> p.create(session));
	}

	public void dropAll(CqlSession session)
	{
		schemas.forEach(p -> p.drop(session));
	}
}
