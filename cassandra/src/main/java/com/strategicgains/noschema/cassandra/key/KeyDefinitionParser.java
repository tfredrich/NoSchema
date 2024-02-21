package com.strategicgains.noschema.cassandra.key;

import com.strategicgains.noschema.exception.KeyDefinitionException;

/**
 * The KeyDefinitionParser class is a utility class used to parse key definition strings into a KeyDefinition instance that can extract values from an entity to create Identifiers.
 * 
 * The static method, KeyDefinitionParser.parse(String) parses the key definition string into a KeyDefinition instance.
 * The string is expected to follow a specific format, which is defined as follows:
 * <pre>
 * key-definition ::= "(" partition-key ")" clustering-key modifier
 * partition-key ::= column-definition | column-definition "," column-definition
 * clustering-key ::= clustering-key-component | clustering-key "," clustering-key-component
 * modifier ::= "unique" | ε
 * clustering-key-component ::= "+" column-definition | "-" column-definition | column-definition
 * column-definition ::= name-type-pair | property-name " as " name-type-pair
 * name-type-pair ::= name ":" type
 * property-name ::= [a-zA-Z0-9_]+
 * </pre>
 * 
 * In the above BNF-style diagram:
 * - The partition-key is a comma-separated list of column-definitions enclosed in parentheses.
 * - The clustering-key is a comma-separated list of clustering-key-components.
 * - The modifier is an optional "unique" keyword which causes the UnitOfWork to enforce uniqueness on create/update and requires presence on delete. Note this causes read-before-write on create/update/delete.
 * - The clustering-key-component is a name-value-pair, optionally prefixed with a "+" or "-" to indicate ascending or descending order, respectively.
 * - The column-definition is a name-type-pair, optionally prefixed with a property-name and "as" to indicate a different name in the entity.
 * - The name-type-pair is a property name and a type separated by a colon.
 * - The property-name is a string of alphanumeric characters and underscores; a PoJo property name.
 * 
 * Examples:
 * <pre>
 * (id:uuid)	// partition key only
 * ((id:uuid) name:text unique)	// partition key of id, clustering key of name, with unique modifier.
 * ((id:uuid, name:text), -created:timestamp, +age:int)	// partition key of id and name + clustering key of created and age, with sort order on each.
 * </pre>
 * 
 * The class throws a KeyDefinitionException if the string is invalid. This can occur if the string is null or empty, if it contains too many parentheses, if a parenthesis is misplaced, or if the parentheses are unmatched.
 * 
 * @author tfredrich
 * @since 1 Sept 2016
 * @see KeyDefinition
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
