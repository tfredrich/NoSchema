package com.strategicgains.noschema.cassandra.key;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.key.ClusteringKeyComponent.Ordering;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

/**
 * The KeyDefinition class is responsible for defining the keys used in Cassandra tables for Primary Tables and Views.
 * It provides methods to add partition keys and clustering keys, which are essential components of Cassandra's and NoSchema's key model.
 * 
 * The class maintains a list of KeyComponent objects for partition keys and ClusteringKeyComponent objects for clustering keys.
 * It also maintains a map of key fields, which are cached at runtime for performance.
 * 
 * The class provides methods to add keys, check the size and presence of keys, and generate identifiers from an entity.
 * It also provides methods to generate string representations of the keys for use in Cassandra queries.
 * 
 * The class also provides methods to check if the key definition is unique and valid, and to set the uniqueness of the key definition.
 * 
 * The class uses Java reflection to find fields in an entity that correspond to the keys and to extract their values.
 * It also uses Java streams to process the keys and generate string representations of them.
 * 
 * @author Todd Fredrich
 * @throws KeyDefinitionException if the entity is missing any fields required by the key definition.
 * @throws InvalidIdentifierException if the entity is missing any property values required by the key definition.
 * @see KeyDefinitionParser
 */
public class KeyDefinition
{
	private static final String ASSIGNMENT_PLACEHOLDER = " = ?";
	private static final String SPACE = " ";
	private static final String COMMA_DELIMITER = ",";
	private static final String AND = " and ";

	private List<KeyComponent> partitionKey = new ArrayList<>();
	private List<ClusteringKeyComponent> clusteringKey = new ArrayList<>();

	/**
	 * If true, causes uniqueness checks before writes. If false (default), 
	 * upsert is used with no uniqueness enforcement.
	 */
	private boolean isUnique;

	// Cached at runtime: Access Fields by property name.
	private Map<String, List<Field>> keyFields;

	public KeyDefinition addPartitionKey(String column, DataTypes type)
	throws KeyDefinitionException
	{
		return addPartitionKey(new KeyComponent(column, type));
	}

	public KeyDefinition addPartitionKey(String column, String property, DataTypes type)
	throws KeyDefinitionException
	{
		return addPartitionKey(new KeyComponent(column, property, type));
	}

	public KeyDefinition addPartitionKey(KeyComponent component)
	{
		partitionKey.add(component);
		return this;
	}

	public KeyDefinition addClusteringKey(String column, DataTypes type, Ordering order)
	throws KeyDefinitionException
	{
		return addClusteringKey(new ClusteringKeyComponent(column, type, order));
	}

	public KeyDefinition addClusteringKey(String column, String property, DataTypes type, Ordering order)
	throws KeyDefinitionException
	{
		return addClusteringKey(new ClusteringKeyComponent(column, property, type, order));
	}

	public KeyDefinition addClusteringKey(ClusteringKeyComponent component)
	{
		clusteringKey.add(component);
		return this;
	}

	public int size()
	{
		return ((hasPartitionKey() ? partitionKey.size() : 0) + (hasClusteringKey() ? clusteringKey.size() : 0));
	}

	public boolean hasPartitionKey()
	{
		return !partitionKey.isEmpty();
	}

	public boolean hasClusteringKey()
	{
		return !clusteringKey.isEmpty();
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

	public boolean isUnique()
	{
		return isUnique;
	}

	public void setUnique(boolean value)
	{
		this.isUnique = value;
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
			sb.append(COMMA_DELIMITER);
			appendAsColumns(clusteringKey, sb);
		}

		return sb.toString();
	}

	public String asPrimaryKey()
	{
		return String.format("%s %s", "primary key", toString());
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("(");

		if (hasClusteringKey())
		{
			sb.append("(");
		}

		appendAsProperties(partitionKey, sb, COMMA_DELIMITER);

		if (hasClusteringKey())
		{
			sb.append("),");
			appendAsProperties(clusteringKey, sb, COMMA_DELIMITER);
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
		appendAsProperties(partitionKey, sb, COMMA_DELIMITER);

		if (hasClusteringKey())
		{
			sb.append(COMMA_DELIMITER);
			appendAsProperties(clusteringKey, sb, COMMA_DELIMITER);
		}

		return sb.toString();
	}

	public String asQuestionMarks(int extras)
	{
		String[] qms = new String[size() + extras];
		Arrays.fill(qms, "?");
		return String.join(COMMA_DELIMITER, qms);
	}

	public String asIdentityClause()
	{
		return asIdentityClause(partitionKey.size() + clusteringKey.size());
	}

	public String asIdentityClause(int keyCount)
	{
		StringBuilder sb = new StringBuilder();
		appendAsAssignments(partitionKey, sb, AND, Math.min(keyCount, partitionKey.size()));

		if (hasClusteringKey() && partitionKey.size() < keyCount)
		{
			sb.append(AND);
			appendAsAssignments(clusteringKey, sb, AND, Math.max(clusteringKey.size(), keyCount - partitionKey.size()));
		}

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

		String result = components.stream()
			.map(component -> component.column() + SPACE + component.type().cassandraType())
			.collect(Collectors.joining(COMMA_DELIMITER));
		builder.append(result);
	}

	private void appendAsProperties(List<? extends KeyComponent> components, StringBuilder builder, String delimiter)
	{
		if (components == null || components.isEmpty()) return;

		String result = components.stream()
			.map(KeyComponent::column)
			.collect(Collectors.joining(delimiter));
		builder.append(result);
	}

	private void appendAsAssignments(List<? extends KeyComponent> components, StringBuilder builder, String delimiter, int keyCount)
	{
		if (components == null || components.isEmpty()) return;

		builder.append(
			components.stream()
			.limit(keyCount)
			.map(component -> component.column() + ASSIGNMENT_PLACEHOLDER)
			.collect(Collectors.joining(delimiter)));
	}


	private boolean hasDescendingSort(List<ClusteringKeyComponent> components)
	{
		return components.stream().anyMatch(t -> t.order().isDescending());
	}

	private void appendClusteringOrderPhrase(List<ClusteringKeyComponent> components, StringBuilder builder)
	{
		String clusteringOrder = Optional.ofNullable(components)
			.map(c -> c.stream()
				.map(t -> t.column() + SPACE + t.order())
				.collect(Collectors.joining(COMMA_DELIMITER)))
			.orElse("");

		builder.append("with clustering order by (")
			.append(clusteringOrder)
			.append(")");
	}

	public List<KeyComponent> components()
	{
		return Stream.concat(partitionKey.stream(), clusteringKey.stream())
			.collect(Collectors.toList());
	}
}
