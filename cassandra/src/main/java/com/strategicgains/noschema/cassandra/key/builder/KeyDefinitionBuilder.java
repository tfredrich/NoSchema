package com.strategicgains.noschema.cassandra.key.builder;

import com.strategicgains.noschema.cassandra.key.ClusteringKeyComponent;
import com.strategicgains.noschema.cassandra.key.ClusteringKeyComponent.Ordering;
import com.strategicgains.noschema.cassandra.key.DataTypes;
import com.strategicgains.noschema.cassandra.key.KeyComponent;
import com.strategicgains.noschema.cassandra.key.KeyDefinition;
import com.strategicgains.noschema.exception.KeyDefinitionException;

/**
 * Entry point for building KeyDefinitions using a fluent API.
 */
public class KeyDefinitionBuilder
{
	private KeyDefinition keyDefinition = new KeyDefinition();

	public KeyDefinitionBuilder()
	{
		super();
    }

	public KeyComponentBuilder withPartitionKey(String partitionKey)
	throws KeyDefinitionException
	{
		KeyComponentBuilder builder = new KeyComponentBuilder(this, KeyComponent.parse(partitionKey));
		keyDefinition.addPartitionKey(builder.getKeyComponent());
		return builder;
	}

	public KeyComponentBuilder withPartitionKey(String columnName, DataTypes type)
	{
		KeyComponentBuilder builder = new KeyComponentBuilder(this, new KeyComponent(columnName, type));
		keyDefinition.addPartitionKey(builder.getKeyComponent());
		return builder;
	}

	public KeyComponentBuilder withPartitionKey(String columnName, String propertyName, DataTypes type)
	{
		KeyComponentBuilder builder = new KeyComponentBuilder(this, new KeyComponent(columnName, propertyName, type));
		keyDefinition.addPartitionKey(builder.getKeyComponent());
		return builder;
	}

	public ClusteringKeyComponentBuilder withClusteringKey(String clusteringKey)
	throws KeyDefinitionException
	{
		ClusteringKeyComponentBuilder builder = new ClusteringKeyComponentBuilder(this, ClusteringKeyComponent.parse(clusteringKey));
		keyDefinition.addClusteringKey(builder.getKeyComponent());
		return builder;
	}

	public ClusteringKeyComponentBuilder withClusteringKey(String columnName, DataTypes type, Ordering ordering)
	{
		ClusteringKeyComponentBuilder builder = new ClusteringKeyComponentBuilder(this, new ClusteringKeyComponent(columnName, type, ordering));
		keyDefinition.addClusteringKey(builder.getKeyComponent());
		return builder;
	}

	public ClusteringKeyComponentBuilder withClusteringKey(String columnName, String propertyName, DataTypes type, Ordering ordering)
	{
		ClusteringKeyComponentBuilder builder = new ClusteringKeyComponentBuilder(this, new ClusteringKeyComponent(columnName, propertyName, type, ordering));
		keyDefinition.addClusteringKey(builder.getKeyComponent());
		return builder;
	}

	public KeyDefinition build()
	{
		return keyDefinition;
	}
}
