package com.strategicgains.noschema.cassandra.schema;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Enables NoSchema to create a keyspace in Cassandra if it doesn't already exist.
 * By default, the keyspace is created with a replication factor of 1 using the SimpleStrategy.
 * If you want to use NetworkTopologyStrategy, call useNetworkReplication() and provide a map
 * of data center names and replication factors.
 */
public class KeyspaceProvider
extends AbstractSchemaProvider
{
	private static final String DEFAULT_KEYSPACE = "noschema";

	private class Schema
	{
		static final String DROP = "drop keyspace if exists %s";
		static final String CREATE = "create keyspace if not exists %s";
		static final String LOCAL_REPLICATION = " with replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }";		
		static final String NETWORK_REPLICATION = " with replication = { 'class' : 'NetworkTopologyStrategy', %s}";
	}

	private Map<String, Integer> dataCenters = null;
	private String keyspace;

	/**
	 * Creates a KeyspaceProvider with the default keyspace name of "noschema".
	 */
	public KeyspaceProvider()
	{
		this(DEFAULT_KEYSPACE);
	}

	/**
	 * Creates a KeyspaceProvider with the given keyspace name.
	 * 
	 * @param keyspace the name of the keyspace to create if it doesn't already exist.
	 */
	public KeyspaceProvider(String keyspace)
    {
		super();
		this.keyspace = keyspace;
    }

	/**
	 * Determines whether this KeyspaceProvider is configured to use NetworkTopologyStrategy for replication.
	 * 
	 * @return true if this KeyspaceProvider is configured to use NetworkTopologyStrategy for replication; false otherwise.
	 */
	public boolean isNetworkReplication()
	{
		return (dataCenters != null);
	}

	/**
	 * Configures this KeyspaceProvider to use NetworkTopologyStrategy for replication with the given data
	 * center names and replication factors.
	 * 
	 * @param dataCenters a map of data center names with corresponding integer replication factors.
	 */
	public void useNetworkReplication(Map<String, Integer> dataCenters)
	{
		this.dataCenters = new HashMap<>(dataCenters);
	}

	@Override
	public String asCreateScript()
	{
		String create = String.format(Schema.CREATE, keyspace);

		if (isNetworkReplication())
		{
			return (create + String.format(Schema.NETWORK_REPLICATION, replicationFactors(dataCenters)));
		}
		else
		{
			return (create + Schema.LOCAL_REPLICATION);
		}
	}

	@Override
	public String asDropScript()
	{
		return String.format(Schema.DROP, keyspace);
	}

	/**
	 * Creates a string of the form "'use1' : 2, 'usw2' : 2" that is used to set
	 * datacenter/replication factors on the NetworkTopologyStrategy.
	 * 
	 * @param replFactors a map of datacenter names with corresponding integer replication factors.
	 * @return a formatted string of the form, "'use1' : 2, 'usw2' : 2"
	 */
	private String replicationFactors(Map<String, Integer> replFactors)
	{
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;

		for (Entry<String, Integer> entry : replFactors.entrySet())
		{
			if (isFirst)
			{
				sb.append("'");
			}
			else
			{
				sb.append(", '");
			}

			sb.append(entry.getKey());
			sb.append("' : ");
			sb.append(entry.getValue());
			isFirst = false;
		}

		return sb.toString();
	}
}
