package com.strategicgains.noschema.cassandra.key;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.key.ClusteringKeyComponent.Ordering;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public class KeyDefinition
{
	private List<KeyComponent> partitionKey;
	private List<ClusteringKeyComponent> clusteringKey;

	// Cached at runtime: Access Fields by property name.
	private transient Map<String, List<Field>> keyFields;

	public KeyDefinition addPartitionKey(String column, DataTypes type)
	{
		return addPartitionKey(new KeyComponent(column, type));
	}

	public KeyDefinition addPartitionKey(String column, String property, DataTypes type)
	{
		return addPartitionKey(new KeyComponent(column, property, type));
	}

	public KeyDefinition addPartitionKey(KeyComponent component)
	{
		if (partitionKey == null)
		{
			partitionKey = new ArrayList<>();
		}

		partitionKey.add(component);
		return this;
	}

	public KeyDefinition addClusteringKey(String column, DataTypes type, Ordering order)
	{
		return addClusteringKey(new ClusteringKeyComponent(column, type, order));
	}

	public KeyDefinition addClusteringKey(String column, String property, DataTypes type, Ordering order)
	{
		return addClusteringKey(new ClusteringKeyComponent(column, property, type, order));
	}

	public KeyDefinition addClusteringKey(ClusteringKeyComponent component)
	{
		if (clusteringKey == null)
		{
			clusteringKey = new ArrayList<>();
		}

		clusteringKey.add(component);
		return this;
	}

	public int size()
	{
		return ((hasPartitionKey() ? partitionKey.size() : 0) + (hasClusteringKey() ? clusteringKey.size() : 0));
	}

	public boolean hasPartitionKey()
	{
		return (partitionKey != null && !partitionKey.isEmpty());
	}

	public boolean hasClusteringKey()
	{
		return (clusteringKey != null && !clusteringKey.isEmpty());
	}

	/**
	 * Pulls the properties from an entity that correspond to this KeyDefinition
	 * into an Identifier instance.
	 * 
	 * If one or more of the properties are missing, returns null.
	 * 
	 * @param entity a POJO from which to extract an identifier.
	 * @return an Identifier instance or null.
	 * @throws KeyDefinitionException if the entity is missing any fields required by the key definition. 
	 * @throws InvalidIdentifierException if the entity is missing any property values required by the key definition.
	 */
	public Identifier identifier(Object entity)
	throws KeyDefinitionException, InvalidIdentifierException
	{
		Identifier identifier = new Identifier();
		List<String> missingProperties = new ArrayList<>(0);
		Map<String, List<Field>> fields = buildKeyFields(entity);

		fields.entrySet().stream().forEach(e -> {
			Object value = findValue(e.getKey(), e.getValue(), entity);

			if (value != null) identifier.add(value);
			else missingProperties.add(e.getKey());
		});

		if (identifier.size() != size())
		{
			throw new InvalidIdentifierException("Missing properties: " + String.join(", ", missingProperties));
		}

		return identifier;
	}

	private Map<String, List<Field>> buildKeyFields(Object entity)
	throws KeyDefinitionException
	{
		if (keyFields != null) return keyFields;

		keyFields = new LinkedHashMap<>();
		List<String> missingFields = new ArrayList<>();
		partitionKey.stream().forEach(k -> {
			try
			{
				keyFields.put(k.property(), findFields(k.property(), entity));
			}
			catch (KeyDefinitionException | InvalidIdentifierException e)
			{
				missingFields.add(k.property());
			}
		});
		if (hasClusteringKey())
		{
			clusteringKey.stream().forEach(k -> {
				try
				{
					keyFields.put(k.property(), findFields(k.property(), entity));
				}
				catch (KeyDefinitionException | InvalidIdentifierException e)
				{
					missingFields.add(k.property());
				}
			});
		}

		if (!missingFields.isEmpty())
		{
			throw new KeyDefinitionException("Missing fields: " + String.join(", ", missingFields));
		}

		return keyFields;
	}

	public boolean isValid()
	{
		return hasPartitionKey();
	}

	public String asColumns()
	{
		StringBuilder sb = new StringBuilder();

		if (hasPartitionKey())
		{
			appendAsColumns(partitionKey, sb);
		}

		if (hasClusteringKey())
		{
			sb.append(",");
			appendAsColumns(clusteringKey, sb);
		}

		return sb.toString();
	}

	public String asPrimaryKey()
	{
		StringBuilder sb = new StringBuilder("primary key (");

		if (hasClusteringKey())
		{
			sb.append("(");
		}

		appendAsProperties(partitionKey, sb, ",");

		if (hasClusteringKey())
		{
			sb.append("),");
			appendAsProperties(clusteringKey, sb, ",");
		}

		sb.append(")");
		return sb.toString();
	}

	public String asClusteringKey()
	{
		StringBuilder sb = new StringBuilder();

		if (hasDescendingSort(clusteringKey))
		{
			appendClusteringOrderPhrase(clusteringKey, sb);
		}

		return sb.toString();
	}

	public String asSelectProperties()
	{
		StringBuilder sb = new StringBuilder();
		appendAsProperties(partitionKey, sb, ",");

		if (hasClusteringKey())
		{
			sb.append(",");
			appendAsProperties(clusteringKey, sb, ",");
		}

		return sb.toString();
	}

	public String asQuestionMarks(int extras)
	{
		String[] qms = new String[size() + extras];
		Arrays.fill(qms, "?");
		return String.join(",", qms);
	}

	public String asIdentityClause()
	{
		StringBuilder sb = new StringBuilder();
		appendAsAssignments(partitionKey, sb, " and ");

		if (hasClusteringKey())
		{
			sb.append(" and ");
			appendAsAssignments(clusteringKey, sb, " and ");
		}

		return sb.toString();
	}

	public Object asPartitionIdentityClause()
	{
		StringBuilder sb = new StringBuilder();
		appendAsAssignments(partitionKey, sb, " and ");
		return sb.toString();
	}

	private List<Field> findFields(String property, Object entity)
	throws KeyDefinitionException, InvalidIdentifierException
	{
		String[] path = property.split("\\.");
		int i = 0;
		List<Field> fields = new ArrayList<>(path.length);
		Object current = entity;
		do
		{
			Field field = findFieldInHierarchy(path[i++], current);
			field.setAccessible(true);
			fields.add(field);
			try
			{
				current = field.get(current);
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				throw new KeyDefinitionException(property, e);
			}
		}
		while (i < path.length);

		return fields;
	}

	private Field findFieldInHierarchy(String property, Object entity)
	throws InvalidIdentifierException
	{
		Class<?> currentClass = entity.getClass();

		do
		{
			try
			{
				return currentClass.getDeclaredField(property);
			}
			catch (NoSuchFieldException e)
			{
				currentClass = currentClass.getSuperclass();
			}
		}
		while (currentClass != null);

		throw new InvalidIdentifierException(property);
	}

	private Object findValue(String property, List<Field> fields, Object entity)
	{
		try
		{
			if (fields.size() == 1) return fields.get(0).get(entity);
	
			int i = 0;
			Object obj = entity;
			do
			{
				obj = fields.get(i++).get(obj);
				if (obj == null) return null;
			}
			while (i < fields.size());
	
			return obj;
		}
		catch(IllegalAccessException e)
		{
			return null;
//			throw new InvalidIdentifierException(property, e);
		}
	}

	private void appendAsColumns(List<? extends KeyComponent> components, StringBuilder builder)
	{
		if (components == null || components.isEmpty()) return;

		Iterator<? extends KeyComponent> iterator = components.iterator();
		KeyComponent component = iterator.next();
		builder
			.append(component.column())
			.append(" ")
			.append(component.type().cassandraType());

		while(iterator.hasNext())
		{
			component = iterator.next();
			builder
				.append(",")
				.append(component.column())
				.append(" ")
				.append(component.type().cassandraType());
		}
	}

	private void appendAsProperties(List<? extends KeyComponent> components, StringBuilder builder, String delimiter)
	{
		if (components == null || components.isEmpty()) return;

		Iterator<? extends KeyComponent> iterator = components.iterator();
		builder.append(iterator.next().column());

		while(iterator.hasNext())
		{
			builder
				.append(delimiter)
				.append(iterator.next().column());
		}
	}

	private void appendAsAssignments(List<? extends KeyComponent> components, StringBuilder builder, String delimiter)
	{
		if (components == null || components.isEmpty()) return;

		Iterator<? extends KeyComponent> iterator = components.iterator();
		KeyComponent component = iterator.next();
		builder
			.append(component.column())
			.append(" = ?");

		while(iterator.hasNext())
		{
			component = iterator.next();
			builder
				.append(" and ")
				.append(component.column())
				.append(" = ?");
		}
	}

	private boolean hasDescendingSort(List<ClusteringKeyComponent> components)
	{
		if (components == null) return false;

		return components.stream().anyMatch(new Predicate<ClusteringKeyComponent>()
		{
			@Override
			public boolean test(ClusteringKeyComponent t)
			{
				return ((t.order().isDescending()));
			}
		});
	}

	private void appendClusteringOrderPhrase(List<ClusteringKeyComponent> components, StringBuilder builder)
	{
		builder.append("with clustering order by (");
		components.forEach(new Consumer<ClusteringKeyComponent>()
		{
			private boolean isFirst = true;

			@Override
			public void accept(ClusteringKeyComponent t)
			{
				if (!isFirst)
				{
					builder.append(",");
				}

				builder.append(t.column())
					.append(" ")
					.append(t.order());
				isFirst = false;
			}
		});
		builder.append(")");
	}

	public List<KeyComponent> components()
	{
		ArrayList<KeyComponent> c = new ArrayList<>(size());
		c.addAll(partitionKey);

		if (hasClusteringKey())
		{
			c.addAll(clusteringKey);
		}

		return c;
	}
}
