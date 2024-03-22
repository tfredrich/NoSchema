package com.strategicgains.noschema.cassandra.key.builder;

import com.strategicgains.noschema.cassandra.key.ClusteringKeyComponent.Ordering;
import com.strategicgains.noschema.cassandra.key.DataTypes;
import com.strategicgains.noschema.cassandra.key.KeyDefinition;

public abstract class AbstractKeyComponentBuilder
{
	protected KeyDefinitionBuilder parent;

	protected AbstractKeyComponentBuilder(KeyDefinitionBuilder parent)
	{
		super();
		this.parent = parent;
	}

	public ClusteringKeyComponentBuilder withClusteringKey(String clusteringKey)
	{
		return parent.withClusteringKey(clusteringKey);
	}

	public ClusteringKeyComponentBuilder withClusteringKey(String columnName, DataTypes type, Ordering ordering)
	{
		return parent.withClusteringKey(columnName, type, ordering);
	}

	public ClusteringKeyComponentBuilder withClusteringKey(String columnName, String propertyName, DataTypes type, Ordering ordering)
	{
		return parent.withClusteringKey(columnName, propertyName, type, ordering);
	}

	public KeyDefinitionBuilder unique()
	{
		return parent.isUnique();
	}

	public KeyDefinition build()
	{
		return parent.build();
	}
}
