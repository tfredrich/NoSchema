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
	private static final String UNIQUE_MODIFIER = "unique";

	private KeyDefinitionParser()
	{
		// prevents instantiation.
	}

	private enum ParseState
	{
		PARTITION_KEY,
		CLUSTER_KEY;

		boolean isPartitionKey()
		{
			return PARTITION_KEY.equals(this);
		}

		boolean isClusteringKey()
		{
			return CLUSTER_KEY.equals(this);
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
		String trimmed = keys.trim();

		if (keys.toLowerCase().endsWith(UNIQUE_MODIFIER))
		{
			definition.setUnique(true);
			trimmed = trimmed.substring(0, trimmed.length() - UNIQUE_MODIFIER.length() - 1);
		}
	
		char[] chars = trimmed.toCharArray();
		ParseState state = ParseState.PARTITION_KEY;
		StringBuilder phrase = new StringBuilder();
		int depth = 0;
		int maxDepth = 0;

		for (char c : chars)
		{
			switch(c)
			{
				case '(':
					++depth;
					maxDepth = Math.max(maxDepth, depth);
					if (depth > 2) throw new KeyDefinitionException("Too many parentheses: " + keys);
					state = transition(state, depth, maxDepth);
					break;
				case ')':
					processPhrase(phrase.toString(), definition, state);
					phrase.setLength(0);
					--depth;
					if (depth < 0) throw new KeyDefinitionException("Misplaced ')': " + keys);
					state = transition(state, depth, maxDepth);
					break;
				case ',':
					processPhrase(phrase.toString(), definition, state);
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

	private static ParseState transition(ParseState state, int depth, int maxDepth)
	{
		if (state.isPartitionKey() && depth < maxDepth)
		{
			return ParseState.CLUSTER_KEY;
		}

		return state;
	}

	private static void processPhrase(String phrase, KeyDefinition definition, ParseState state)
	throws KeyDefinitionException
	{
		String trimmed = phrase.trim();
		if (trimmed.length() == 0) return;

		if (state.isPartitionKey())
		{
			definition.addPartitionKey(KeyComponent.parse(trimmed));
		}
		else if (state.isClusteringKey())
		{
			definition.addClusteringKey(ClusteringKeyComponent.parse(trimmed));
		}
	}
}
