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

		node.setValue("a string value");
		assertTrue(node.isEmpty());
	}

	@Test
	public void shouldStayEmpty()
	{
		TrieNode<String> node = new TrieNode<>();
		node.setValue("a string value");
		assertTrue(node.isEmpty());
	}

	@Test
	public void shouldAddValue()
	{
		TrieNode<String> node = new TrieNode<>();
		assertFalse(node.hasValue());
		node.setValue("a string value");
		assertTrue(node.hasValue());
		assertEquals("a string value", node.getValue());

		node.setValue("another string value");
		assertEquals("another string value", node.getValue());
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
		node.addChildIfAbsent('a').setValue("a string value");
		assertTrue(node.getChild('a').hasValue());
		assertEquals("a string value", node.getChild('a').getValue());
	}

	@Test
	public void shouldAddValueToGrandchild()
	{
		TrieNode<String> node = new TrieNode<>();
		node.addChildIfAbsent('a').addChildIfAbsent('b').setValue("a string value");
		assertTrue(node.getChild('a').getChild('b').hasValue());
		assertEquals("a string value", node.getChild('a').getChild('b').getValue());
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
