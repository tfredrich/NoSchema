package com.strategicgains.noschema.bplustree;

import static org.junit.Assert.*;

import org.junit.Test;

public class LeafNodeTest
{
	@Test
	public void shouldCreateEmpty()
	{
		LeafNode<Integer, String> node = new LeafNode<>();
		assertNotNull(node);
		assertEquals(0, node.size());
		assertTrue(node.isLeaf());
		assertNull(node.getPreviousSibling());
		assertNull(node.getNextSibling());
		assertNull(node.split(3));
	}

	@Test
	public void shouldInsert()
	{
		LeafNode<Integer, String> node = new LeafNode<>();
		node.insert(2, "two");
		node.insert(2, "two-also");
		node.insert(3, "three");
		node.insert(1, "one");
		assertEquals(3, node.size());
		assertEquals("one", node.search(1));
		assertEquals("two-also", node.search(2));
		assertEquals("three", node.search(3));
	}

	@Test
	public void shouldSplitOrderThree()
	{
		LeafNode<Integer, String> node = new LeafNode<>();
		node.insert(1, "one");
		node.insert(2, "two");
		node.insert(3, "three");
		assertEquals(3, node.getMiddleKey(3).intValue());
		LeafNode<Integer, String> sibling = node.split(3);
		assertNotNull(sibling);
		assertEquals(2, node.size());
		assertEquals(1, sibling.size());
		assertEquals("one", node.search(1));
		assertEquals("two", node.search(2));
		assertEquals("three", sibling.search(3));
		assertNull(node.getPreviousSibling());
		assertEquals(sibling, node.getNextSibling());
		assertEquals(node, sibling.getPreviousSibling());
		assertNull(sibling.getNextSibling());
	}

	@Test
	public void shouldSplitOrderFour()
	{
		LeafNode<Integer, String> node = new LeafNode<>();
		node.insert(1, "one");
		node.insert(2, "two");
		node.insert(3, "three");
		node.insert(4, "four");
		assertEquals(3, node.getMiddleKey(4).intValue());
		LeafNode<Integer, String> sibling = node.split(4);
		assertNotNull(sibling);
		assertEquals(2, node.size());
		assertEquals(2, sibling.size());
		assertEquals("one", node.search(1));
		assertEquals("two", node.search(2));
		assertEquals("three", sibling.search(3));
		assertEquals("four", sibling.search(4));
		assertNull(node.getPreviousSibling());
		assertEquals(sibling, node.getNextSibling());
		assertEquals(node, sibling.getPreviousSibling());
		assertNull(sibling.getNextSibling());
	}

	@Test
	public void shouldMerge()
	{
		LeafNode<Integer, String> node = new LeafNode<>();
		node.insert(1, "one");
		node.insert(2, "two");
		LeafNode<Integer, String> sibling = new LeafNode<>();
		sibling.insert(3, "three");
		sibling.insert(4, "four");
		node.merge(sibling);
		assertEquals(4, node.size());
		assertEquals("one", node.search(1));
		assertEquals("two", node.search(2));
		assertEquals("three", node.search(3));
		assertEquals("four", node.search(4));
		assertNull(node.getPreviousSibling());
		assertNull(node.getNextSibling());
	}

	@Test
	public void shouldNotSplit()
	{
		LeafNode<Integer, String> node = new LeafNode<>();
		node.insert(1, "one");
		node.insert(2, "two");
		node.insert(3, "three");
		assertNull(node.split(4));
	}

	@Test
	public void shouldGetMiddleKeyOrderThree()
	{
		LeafNode<Integer, String> node = new LeafNode<>();
		node.insert(1, "one");
		node.insert(2, "two");
		node.insert(3, "three");
		assertEquals(3, node.getMiddleKey(3).intValue());
	}

	@Test
	public void shouldGetMiddleKeyOrderFour()
	{
		LeafNode<Integer, String> node = new LeafNode<>();
		node.insert(1, "one");
		node.insert(2, "two");
		node.insert(3, "three");
		node.insert(4, "four");
		assertEquals(3, node.getMiddleKey(4).intValue());
	}
}
