/**
 * 
 */
package com.strategicgains.noschema.cassandra.key;

import com.strategicgains.noschema.exception.KeyDefinitionException;

/**
 * @author tfredrich
 * @since 1 Sept 2016
 */
public class KeyComponent
{
	// the name of the DB column.
	private String column;

	// the name of the object property to map to the DB column (using the 'as' clause).
	private String property;

	// the type of the DB column.
	private DataTypes type;

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
}
