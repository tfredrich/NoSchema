package com.strategicgains.noschema.trie;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A TrieNode represents a node in a trie and stores a character and a map of child nodes.
 * 
 * It can also carry values to be used after retrieval of a word from the trie for filtering or sorting.
 * 
 * A TrieNode has the following operations:
 * - addChildIfAbsent(char ch): Add a child node to this node if it does not already exist.
 * - isEndOfWord(): Return true if this node is the end of a word.
 * - setEndOfWord(boolean endOfWord): Set this node as the end of a word.
 * - getChild(char ch): Get the child node associated with the given character.
 * - forEachChild(BiConsumer<Character, TrieNode> action): Iterate over each child node in this node performing the given action.
 * - setChild(char ch, TrieNode child): Set the child node associated with the given character.
 * - addMetadata(Object values): Add values to this node.
 * - hasMetadata(): Return true if this node has values.
 * - getMetadata(): Get the values associated with this node.
 * - isEmpty(): Return true if this node has no children.
 */
public class TrieNode<V>
{
	private boolean endOfWord;
	private Map<Character, TrieNode<V>> children;
	private V value;

	public TrieNode()
	{
		this.endOfWord = false;
		this.children = new HashMap<>();
	}

	/**
     * Add a child node to this node if it does not already exist.
     * 
     * @param ch the character to add as a child node.
     * @return the child whether it was added or already existed. Never null.
     */
	public TrieNode<V> addChildIfAbsent(char ch)
	{
		return children.computeIfAbsent(ch, k -> new TrieNode<>());
	}

	/**
	 * Return true if this node is the end of a word.
	 * 
	 * @return true if this node is the end of a word.
	 */
	public boolean isEndOfWord()
	{
		return endOfWord;
	}

	/**
	 * Set this node as the end of a word.
	 * 
	 * @param endOfWord true if this node is the end of a word.
	 */
	public void setEndOfWord(boolean endOfWord)
	{
		this.endOfWord = endOfWord;
	}

	/**
	 * Get the child node associated with the given character.
	 * 
	 * @param ch the character to find in the children.
	 * @return the child node associated with the given character or null if not found.
	 */
	public TrieNode<V> getChild(char ch)
	{
		return children.get(ch);
	}

	/**
	 * Iterate over each child node in this node performing the given action.
	 * 
	 * @param action the action to perform on each child node.
	 */
	public void forEachChild(BiConsumer<Character, TrieNode<V>> action)
	{
		children.forEach(action);
	}

	/**
	 * Set the child node associated with the given character.
	 * 
	 * @param ch    the character to associate with the child node.
	 * @param child the child node to associate with the character.
	 */
	public void setChild(char ch, TrieNode<V> child)
	{
		if (child == null)
		{
			children.remove(ch);
			return;
		}

		children.put(ch, child);
	}

	/**
	 * Add a value to this node.
	 * 
	 * @param value the value to add to this node.
	 * @return this node.
	 */
	public TrieNode<V> setValue(V value)
	{
		if (value == null)
		{
			return this;
		}

		this.value = value;
		return this;
	}

	public boolean hasValue()
	{
		return value != null;
	}

	/**
	 * Get the value associated with this node.
	 * 
	 * @return the value associated with this node.
	 */
	public V getValue()
	{
		return value;
	}

	/**
	 * Return true if this node has no children.
	 * 
	 * @return true if this node has no children.
	 */
	public boolean isEmpty()
	{
		return children.isEmpty();
	}
}
