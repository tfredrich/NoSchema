package com.strategicgains.noschema.trie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TrieTest
{
	@Test
	public void shouldInsertWord()
	{
		Trie<Integer> trie = new Trie<>();
		trie.insert("hello");
		trie.insert("bye");
		assertTrue(trie.contains("hello"));
		assertTrue(trie.contains("bye"));
	}

	@Test
	public void shouldInsertWordWithValue()
	{
		Trie<Integer> trie = new Trie<>();
		trie.insert("hello", 42);
		trie.insert("bye", 55);
		assertTrue(trie.contains("hello"));
		assertTrue(trie.contains("bye"));
		assertEquals(Integer.valueOf(42), trie.getValues("hello").get(0));
		assertEquals(Integer.valueOf(55), trie.getValues("bye").get(0));
	}

	@Test
	public void shouldNotContainWord()
	{
		Trie<Integer> trie = new Trie<>();
		assertFalse(trie.contains("hello"));
	}

	@Test
	public void shouldBeEndOfWord()
	{
		Trie<Integer> trie = new Trie<>();
		trie.insert("hello");
		trie.insert("hellos");
		trie.insert("bye");
		trie.insert("byebye");
		assertTrue(trie.findNode("hello").get().isEndOfWord());
		assertTrue(trie.findNode("hellos").get().isEndOfWord());
		assertTrue(trie.findNode("bye").get().isEndOfWord());
		assertTrue(trie.findNode("byebye").get().isEndOfWord());
	}

	@Test
	public void shouldNotBeFound()
	{
		Trie<Integer> trie = new Trie<>();
		trie.insert("hello");
		trie.insert("hellos");
		trie.insert("bye");
		trie.insert("byebye");
		assertTrue(trie.findNode("hell").isEmpty());
		assertTrue(trie.findNode("by").isEmpty());
		assertTrue(trie.findNode("byeby").isEmpty());
	}

	@Test
	public void shouldStartWith()
	{
		Trie<Integer> trie = new Trie<>();
		trie.insert("hello");
		trie.insert("hellos");
		trie.insert("bye");
		trie.insert("byebye");
		assertEquals("hello", trie.startsWith("hell").get(0));
		assertEquals("hellos", trie.startsWith("hell").get(1));
		assertEquals("bye", trie.startsWith("by").get(0));
		assertEquals("byebye", trie.startsWith("by").get(1));
	}

	@Test
	public void shouldSearchWithFilter()
	{
		Trie<Integer> trie = new Trie<>();
		trie.insert("hello");
		trie.insert("hellos");
		trie.insert("bye");
		trie.insert("byebye");
		trie.insert("yyyyyyyyyyyy");
		List<String> filteredWords = trie.searchWithFilter(Arrays.asList(word -> word.length() > 5, word -> word.matches(".*[aeiou].*")));
		assertEquals(2, filteredWords.size());
		assertEquals("byebye", filteredWords.get(0));
		assertEquals("hellos", filteredWords.get(1));
	}
}
