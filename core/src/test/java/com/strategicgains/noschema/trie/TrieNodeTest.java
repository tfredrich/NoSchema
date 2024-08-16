package com.strategicgains.noschema.trie;

import static org.junit.Assert.*;

import org.junit.Test;

public class TrieNodeTest
{
	@Test
	public void shouldCreateEmpty()
	{
		TrieNode<String> node = new TrieNode<>();
		assertNotNull(node);
		assertTrue(node.isEmpty());

		node.addValue("a string value");
		assertTrue(node.isEmpty());
	}

	@Test
	public void shouldStayEmpty()
	{
		TrieNode<String> node = new TrieNode<>();
		node.addValue("a string value");
		assertTrue(node.isEmpty());
	}

	@Test
	public void shouldAddValue()
	{
		TrieNode<String> node = new TrieNode<>();
		assertFalse(node.hasValues());
		node.addValue("a string value");
		assertTrue(node.hasValues());
		assertEquals(1, node.getValues().size());
		assertEquals("a string value", node.getValues().get(0));

		node.addValue("another string value");
		assertEquals(2, node.getValues().size());
		assertEquals("a string value", node.getValues().get(0));
		assertEquals("another string value", node.getValues().get(1));
	}

	@Test
	public void shouldAddChild()
	{
		TrieNode<String> node = new TrieNode<>();
		node.addChildIfAbsent('a');
		assertFalse(node.isEmpty());
		assertNotNull(node.getChild('a'));
	}

	@Test
	public void shouldRemoveChild()
	{
		TrieNode<String> node = new TrieNode<>();
		node.addChildIfAbsent('a');
		assertFalse(node.isEmpty());
		assertNotNull(node.getChild('a'));
		node.setChild('a', null);
		assertTrue(node.isEmpty());
		assertNull(node.getChild('a'));
	}

	@Test
	public void shouldAddGrandchild()
	{
		TrieNode<String> node = new TrieNode<>();
		node.addChildIfAbsent('a').addChildIfAbsent('b');
		assertNotNull(node.getChild('a'));
		assertNotNull(node.getChild('a').getChild('b'));
	}

	@Test
	public void shouldRemoveGrandchild()
	{
		TrieNode<String> node = new TrieNode<>();
		node.addChildIfAbsent('a').addChildIfAbsent('b');
		assertNotNull(node.getChild('a'));
		assertNotNull(node.getChild('a').getChild('b'));
		node.getChild('a').setChild('b', null);
		assertNotNull(node.getChild('a'));
		assertNull(node.getChild('a').getChild('b'));
	}

	@Test
	public void shouldAddValueToChild()
	{
		TrieNode<String> node = new TrieNode<>();
		node.addChildIfAbsent('a').addValue("a string value");
		assertTrue(node.getChild('a').hasValues());
		assertEquals(1, node.getChild('a').getValues().size());
		assertEquals("a string value", node.getChild('a').getValues().get(0));
	}

	@Test
	public void shouldAddValueToGrandchild()
	{
		TrieNode<String> node = new TrieNode<>();
		node.addChildIfAbsent('a').addChildIfAbsent('b').addValue("a string value");
		assertTrue(node.getChild('a').getChild('b').hasValues());
		assertEquals(1, node.getChild('a').getChild('b').getValues().size());
		assertEquals("a string value", node.getChild('a').getChild('b').getValues().get(0));
	}

	@Test
	public void shouldBeEndOfWord()
	{
		TrieNode<String> node = new TrieNode<>();
		node.addChildIfAbsent('a').addChildIfAbsent('b').setEndOfWord(true);
		assertTrue(node.getChild('a').getChild('b').isEndOfWord());
		assertFalse(node.getChild('a').isEndOfWord());
		assertFalse(node.isEndOfWord());
	}
}
