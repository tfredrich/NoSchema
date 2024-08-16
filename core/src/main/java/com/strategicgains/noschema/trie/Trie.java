package com.strategicgains.noschema.trie;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * This Trie (pronounced "try") implementation is a tree data structure used for efficient retrieval
 * of key-value pairs in which the keys are strings.
 * 
 * This trie implementation is case-sensitive and supports unicode characters.
 * 
 * The trie has the following operations:
 *  - insert(String word): Inserts a word into the trie.
 *  - contains(String word): Returns true if the word is in the trie, and false otherwise.
 *  - delete(String word): Deletes a word from the trie.
 *  - startsWith(String prefix): Returns a list of words in the trie that start with the given prefix.
 *  
 *  The trie also supports storing one or more values for each word.
 *  
 *  <V> is the type of the values indexed by the Trie.
 */
public class Trie<V>
{
	private TrieNode<V> root;

	public Trie()
	{
		this.root = new TrieNode<>();
	}

	public void insert(String word)
	{
		insert(word, null);
	}

	public void insert(String word, V value)
	{
		TrieNode<V> current = this.root;

		for (int i = 0; i < word.length(); i++)
		{
			char ch = word.charAt(i);
			current = current.addChildIfAbsent(ch);
		}

		current.setEndOfWord(true);

		if (value != null)
        {
			current.addValue(value);
        }
	}

	public boolean contains(String word)
	{
		Optional<TrieNode<V>> found = findNode(word);
		return found.isPresent() && found.get().isEndOfWord();
	}

	public List<V> getValues(String word)
	{
		return findNode(word).map(TrieNode::getValues).orElse(null);
	}

	Optional<TrieNode<V>> findNode(String word)
	{
		TrieNode<V> current = this.root;

		for (int i = 0; i < word.length(); i++)
		{
			char ch = word.charAt(i);
			current = current.getChild(ch);

			if (current == null)
			{
				return Optional.empty();
			}
		}

		return (current.isEndOfWord() ? Optional.of(current) : Optional.empty());
	}

	public void delete(String word)
	{
		deleteIfEmpty(this.root, word, 0);
	}

	private boolean deleteIfEmpty(TrieNode<V> node, String word, int index)
	{
		if (index == word.length())
		{
			if (!node.isEndOfWord())
			{
				return false;
			}
			node.setEndOfWord(false);
			return node.isEmpty();
		}

		char ch = word.charAt(index);
		TrieNode<V> child = node.getChild(ch);

		if (child == null)
		{
			return false;
		}

		boolean shouldDeleteCurrentNode = deleteIfEmpty(child, word, index + 1) && !child.isEndOfWord();

		if (shouldDeleteCurrentNode)
		{
			node.setChild(ch, null);
			return node.isEmpty();
		}

		return false;
	}

	public List<String> startsWith(String prefix)
	{
		List<String> words = new ArrayList<>();
		TrieNode<V> current = this.root;

		for (int i = 0; i < prefix.length(); i++)
		{
			char ch = prefix.charAt(i);
			current = current.getChild(ch);

			if (current == null)
			{
				return words;
			}
		}

		depthFirstSearch(current, new StringBuilder(prefix), words);
		return words;
	}

	private void depthFirstSearch(TrieNode<V> node, StringBuilder prefix, List<String> words)
	{
		if (node.isEndOfWord())
		{
			words.add(prefix.toString());
		}

		node.forEachChild((ch, child) ->
		{
			prefix.append(ch);
			depthFirstSearch(child, prefix, words);
			prefix.deleteCharAt(prefix.length() - 1);
		});
	}

	public List<String> searchWithFilter(List<BiFunction<String, TrieNode<V>, Boolean>> filters)
	{
		List<String> words = new ArrayList<>();
		depthFirstSearch(this.root, new StringBuilder(), words, filters);
		return words;
	}

	private void depthFirstSearch(TrieNode<V> node, StringBuilder prefix, List<String> words, List<BiFunction<String, TrieNode<V>, Boolean>> filters)
	{
		if (node.isEndOfWord() && filters.stream().allMatch(filter -> filter.apply(prefix.toString(), node)))
		{
			words.add(prefix.toString());
		}

		node.forEachChild((ch, child) ->
		{
			prefix.append(ch);
			depthFirstSearch(child, prefix, words, filters);
			prefix.deleteCharAt(prefix.length() - 1);
		});
	}
}
