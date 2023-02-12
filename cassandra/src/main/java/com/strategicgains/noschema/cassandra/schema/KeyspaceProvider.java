package com.strategicgains.noschema.cassandra.schema;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class KeyspaceProvider
extends AbstractSchemaProvider
{
	private static String DEFAULT_KEYSPACE = "noschema";

	private class Schema
	{
		static final String DROP = "drop keyspace if exists %s";
		static final String CREATE = "create keyspace if not exists %s";
		static final String LOCAL_REPLICATION = " with replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }";		
		static final String NETWORK_REPLICATION = " with replication = { 'class' : 'NetworkTopologyStrategy', %s}";
	}

	private Map<String, Integer> dataCenters = null;
	private String keyspace;

	public KeyspaceProvider()
	{
		this(DEFAULT_KEYSPACE);
	}

	public KeyspaceProvider(String keyspace)
    {
		super();
		this.keyspace = keyspace;
    }

	public boolean isNetworkReplication()
	{
		return (dataCenters != null);
	}

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
	String replicationFactors(Map<String, Integer> replFactors)
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
