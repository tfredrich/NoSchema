package com.strategicgains.noschema.cassandra.key;

import com.strategicgains.noschema.exception.KeyDefinitionException;

/**
 * Used to parse key definition strings into Cassandra keys and something that can pull values from a BSONObject.
 * 
 * The structure is "((partition-key) clustering-key) modifier" where:
 * partition-key = name-value-pair | [, name-value-pair]
 * clustering-key = clustering-key-component | [, clustering-key-component]
 * modifier = 'unique' #is optional.
 * clustering-key-component = [+|-]name-value-pair #where '+' implies ascending order, '-' implies descending. Default is ascending.
 * name-value-pair = name:type
 * 
 * @author tfredrich
 * @since 1 Sept 2016
 * @see KeyComponent
 */
public final class KeyDefinitionParser
{
	private static final String UNIQUE_SPECIFIER = "unique";

	private KeyDefinitionParser()
	{
		// prevents instantiation.
	}

	private enum ParseState
	{
		PARTITION_KEY,
		CLUSTER_KEY,
		MODIFIER;

		boolean isPartitionKey()
		{
			return PARTITION_KEY.equals(this);
		}

		boolean isClusteringKey()
		{
			return PARTITION_KEY.equals(this);
		}

		boolean isModifier()
		{
			return MODIFIER.equals(this);
		}
	}

	/**
	 * Used to parse the View.keys property into something that can retrieve values from a BSONObject.
	 * 
	 * Key string is of the form (property name : type), just like Cassandra's 'key is' phrase:
	 * a:uuid 									// partition key only
	 * ((a:uuid, b:text), -c:timestamp, +d:int)	// partition key + clustering key, with (or without) sort order
	 * 
	 * @param keys a string defining the key structure of a Table (or View). Cannot be null or empty.
	 * @return a new KeyDefinition instance.
	 * @throws KeyDefinitionException if the string is invalid.
	 */
	public static KeyDefinition parse(String keys)
	throws KeyDefinitionException
	{
		if (keys == null || keys.isEmpty()) throw new KeyDefinitionException("Key string null or empty");

		KeyDefinition definition = new KeyDefinition();
		char[] chars = keys.trim().toCharArray();
		ParseState state = ParseState.PARTITION_KEY;
		StringBuilder phrase = new StringBuilder();
		int depth = 0;

		for (char c : chars)
		{
			switch(c)
			{
				case '(':
					if (state.isPartitionKey()) ++depth;
					else throw new KeyDefinitionException("Misplaced '('");
					break;
				case ')':
					state = processPhrase(phrase.toString(), definition, state);
					phrase.setLength(0);
					--depth;

					if (depth < 0) throw new KeyDefinitionException("Misplaced ')'");
					break;
				case ',':
				case ' ':
					state = processPhrase(phrase.toString(), definition, state);
					phrase.setLength(0);
					break;
				default:
					phrase.append(c);
					break;
			}
		}

		if (depth != 0) throw new KeyDefinitionException("Unmatched parenthises: " + keys);

		processPhrase(phrase.toString(), definition, state);
		return definition;
	}

	private static ParseState processPhrase(String phrase, KeyDefinition definition, ParseState state)
	throws KeyDefinitionException
	{
		String trimmed = phrase.trim();
		if (trimmed.length() == 0) return state;

		if (state.isPartitionKey())
		{
			definition.addPartitionKey(KeyComponent.parse(trimmed));
			return ParseState.CLUSTER_KEY;
		}
		else
		{
			if (UNIQUE_SPECIFIER.equalsIgnoreCase(trimmed))
			{
				definition.setUnique(true);
				return state;
			}

			definition.addClusteringKey(ClusteringKeyComponent.parse(trimmed));
		}

		return state;
	}
}
