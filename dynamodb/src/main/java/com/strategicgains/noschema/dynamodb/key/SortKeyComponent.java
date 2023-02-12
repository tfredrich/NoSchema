/**
 * 
 */
package com.strategicgains.noschema.dynamodb.key;

import com.strategicgains.noschema.exception.KeyDefinitionException;

/**
 * @author tfredrich
 * @since 1 Sept 2016
 */
public class SortKeyComponent
extends KeyComponent
{
	public enum Ordering
	{
		ASC,
		DESC;

		public boolean isDescending()
		{
			return DESC.equals(this);
		}
	}

	private Ordering order;

	public SortKeyComponent(String column, DataTypes type, Ordering order)
	{
		this(column, column, type, order);
	}

	public SortKeyComponent(String column, String property, DataTypes type, Ordering order)
	{
		super(column, property, type);
		this.order = order;
	}

	public Ordering order()
	{
		return order;
	}

	public static SortKeyComponent parse(String phrase)
	throws KeyDefinitionException
	{
		String[] keyPhrase = splitColumnDefinition(phrase.trim());

		if (keyPhrase.length != 2) throw new KeyDefinitionException("Invalid clustering key phrase: " + phrase);

		Ordering order = Ordering.ASC;
		String column = keyPhrase[0];

		if (column.startsWith("-"))
		{
			column = column.substring(1);
			order = Ordering.DESC;
		}
		else if (column.startsWith("+"))
		{
			column = column.substring(1);
		}
		else if (!Character.isAlphabetic(column.charAt(0)))
		{
			throw new KeyDefinitionException("Invalid clustering key property name: " + phrase);
		}

		try
		{
			String[] names = splitNameDefinition(column);
			if (names.length == 1) return new SortKeyComponent(column.trim(), DataTypes.from(keyPhrase[1].trim()), order);
			return new SortKeyComponent(names[1].trim(), names[0].trim(), DataTypes.from(keyPhrase[1].trim()), order);
		}
		catch (IllegalStateException e)
		{
			throw new KeyDefinitionException(e.getMessage());
		}
	}
}
