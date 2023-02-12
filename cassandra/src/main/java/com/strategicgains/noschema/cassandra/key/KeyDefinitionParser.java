package com.strategicgains.noschema.cassandra.key;

import com.strategicgains.noschema.exception.KeyDefinitionException;

/**
 * Used to parse the View.keys property into something that can pull values from a BSONObject.
 * 
 * @author tfredrich
 * @since 1 Sept 2016
 */
public final class KeyDefinitionParser
{
	private KeyDefinitionParser()
	{
		// prevents instantiation.
	}

	private enum ParseState
	{
		PARTITION_KEY,
		CLUSTER_KEY;

		public boolean isPartitionKey()
		{
			return PARTITION_KEY.equals(this);
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
		char[] chars = keys.toCharArray();
		ParseState state = ParseState.PARTITION_KEY;
		StringBuilder phrase = new StringBuilder();
		int i = 0;
		int depth = 0;

		do
		{
			switch(chars[i])
			{
				case '(':
					if (state.isPartitionKey()) ++depth;
					else throw new KeyDefinitionException("Misplaced '('");
					break;
				case ')':
					processPhrase(phrase.toString(), definition, state);
					phrase.setLength(0);
					--depth;
					if (depth < 0) throw new KeyDefinitionException("Misplaced ')'");
					if (state.isPartitionKey())
					{
						state = ParseState.CLUSTER_KEY;
					}
					break;
				case ',':
					processPhrase(phrase.toString(), definition, state);
					phrase.setLength(0);
					break;
				default:
					phrase.append(chars[i]);
					break;
			}
		}
		while (++i < chars.length);

		if (depth != 0) throw new KeyDefinitionException("Unbalanced parenthises: " + keys);

		processPhrase(phrase.toString(), definition, state);
		return definition;
	}

	private static void processPhrase(String phrase, KeyDefinition definition, ParseState state)
	throws KeyDefinitionException
	{
		if (phrase.trim().length() == 0) return;

		if (state.isPartitionKey()) definition.addPartitionKey(KeyComponent.parse(phrase.trim()));
		else definition.addClusteringKey(ClusteringKeyComponent.parse(phrase.trim()));
	}
}
