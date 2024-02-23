/**
 * 
 */
package com.strategicgains.noschema.cassandra.key;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

/**
 * @author tfredrich
 * @since 1 Sept 2016
 */
public class KeyComponent
{
	// the name of the DB column.
	private String column;

	// the type of the DB column.
	private DataTypes type;

	// the name of the object property to map to the DB column (using the 'as' clause).
	private String property;

	// the function to map the extracted property value from the object into the column value.
	private UnaryOperator<Object> extractor;

	// Cached during runtime extraction: Access Fields by property name. Used to extract values from objects.
	private List<Field> keyFields;

	public KeyComponent(String column, DataTypes type)
	throws KeyDefinitionException
	{
		this(column, column, type);
	}

	public KeyComponent(String column, String property, DataTypes type)
	throws KeyDefinitionException
	{
		super();
		column(column);
		property(property);
		type(type);
	}

	public String column()
	{
		return column;
	}

	private KeyComponent column(String column)
	throws KeyDefinitionException
	{
		if (column == null || column.isEmpty() || column.contains(" "))
		{
			throw new KeyDefinitionException("Invalid column value: " + column);
		}

		this.column = column;
		return this;
	}

	public KeyComponent extractor(UnaryOperator<Object> extractor)
	{
		this.extractor = extractor;
		return this;
	}

	private boolean hasExtractor()
	{
		return (extractor != null);
	}

	public Object extract(Object object)
	{
		Object value = findValue(object);
		if (value == null) return null;
		return (hasExtractor() ? extractor.apply(value) : value);
	}

	public String property()
	{
		return property;
	}

	private KeyComponent property(String property)
	throws KeyDefinitionException
	{
		if (property == null || column.isEmpty() || column.contains(" "))
		{
			throw new KeyDefinitionException("Invalid property value: " + property);
		}

		this.property = property;
		return this;
	}

	public DataTypes type()
	{
		return type;
	}

	private KeyComponent type(DataTypes type)
	{
		this.type = type;
		return this;
	}

	public static KeyComponent parse(String phrase)
	throws KeyDefinitionException
	{
		String[] colDefn = splitColumnDefinition(phrase.trim());

		if (colDefn.length != 2) throw new KeyDefinitionException("Invalid key phrase: " + phrase);
		else if (!Character.isAlphabetic(colDefn[0].charAt(0)))
		{
			throw new KeyDefinitionException("Invalid key property name: " + phrase);
		}

		try
		{
			String[] names = splitNameDefinition(colDefn[0]);
			if (names.length == 1) return new KeyComponent(colDefn[0].trim(), DataTypes.from(colDefn[1].trim()));
			return new KeyComponent(names[1].trim(), names[0].trim(), DataTypes.from(colDefn[1].trim()));
		}
		catch (IllegalStateException e)
		{
			throw new KeyDefinitionException(e);
		}
	}

	protected static String[] splitNameDefinition(String typeDefn)
	{
		return typeDefn.split("\\s*as\\s*");
	}

	protected static String[] splitColumnDefinition(String phrase)
	{
		return phrase.split("\\s*:\\s*");
	}

	private Object findValue(Object entity)
	{
		List<Field> fields = getKeyFields(entity);

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
			throw new InvalidIdentifierException("Unable to access field: " + property, e);
		}
	}

	private List<Field> getKeyFields(Object entity)
	throws KeyDefinitionException
	{
		// Return the cached value, if available.
		if (keyFields != null) return keyFields;

		// Otherwise, find the fields and cache them.
		keyFields = findFields(property, entity);
		return keyFields;
	}

	private List<Field> findFields(String property, Object entity)
	throws KeyDefinitionException
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
				throw new KeyDefinitionException("Missing field: " + property, e);
			}
		}
		while (i < path.length);

		return fields;
	}

	private Field findFieldInHierarchy(String property, Object entity)
	throws KeyDefinitionException
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

		throw new KeyDefinitionException("Missing field: " + property);
	}
}
