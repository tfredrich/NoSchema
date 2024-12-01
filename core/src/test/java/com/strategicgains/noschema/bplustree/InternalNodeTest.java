package com.strategicgains.noschema.bplustree;

import static org.junit.Assert.*;

import org.junit.Test;

public class InternalNodeTest
{
	@Test
	public void shouldCreateEmpty()
	{
		InternalNode<Integer, String> node = new InternalNode<>();
		assertNotNull(node);
		assertEquals(0, node.size());
		assertFalse(node.isLeaf());
		assertNull(node.split(3));
	}

//	@Test
//	public void shouldInsert()
//	{
//		InternalNode<Integer, String> node = new InternalNode<>();
//		node.insert(2, "two");
//		node.insert(2, "two-also");
//		node.insert(3, "three");
//		node.insert(1, "one");
//		assertEquals(3, node.size());
//		assertEquals("one", node.search(1));
//		assertEquals("two-also", node.search(2));
//		assertEquals("three", node.search(3));
//	}

	@Test
	public void shouldSplitOrderThree()
	{
		InternalNode<Integer, String> node = new InternalNode<>();
		LeafNode<Integer, String> one = new LeafNode<>();
		one.insert(1, "one");
		LeafNode<Integer, String> two = new LeafNode<>();
		two.insert(2, "two");
		LeafNode<Integer, String> three = new LeafNode<>();
		three.insert(3, "three");
		node.insert(1, one, two);
		node.insert(2, two, three);
		node.insert(3, three, null);
		assertEquals(3, node.getMiddleKey(3).intValue());
		InternalNode<Integer, String> sibling = node.split(3);
		assertNotNull(sibling);
		assertEquals(1, sibling.size());
		assertEquals(2, node.size());
		Node<Integer, String> search = node.search(1);
		assertEquals("one", node.search(1));
		assertEquals("two", node.search(2));
		assertEquals("three", sibling.search(3));
	}

//	@Test
//	public void shouldSplitOrderFour()
//	{
//		InternalNode<Integer, String> node = new InternalNode<>();
//		node.insert(1, "one");
//		node.insert(2, "two");
//		node.insert(3, "three");
//		node.insert(4, "four");
//		assertEquals(3, node.getMiddleKey(4).intValue());
//		InternalNode<Integer, String> sibling = node.split(4);
//		assertNotNull(sibling);
//		assertEquals(2, node.size());
//		assertEquals(2, sibling.size());
//		assertEquals("one", node.search(1));
//		assertEquals("two", node.search(2));
//		assertEquals("three", sibling.search(3));
//		assertEquals("four", sibling.search(4));
//	}

//	@Test
//	public void shouldMerge()
//	{
//		InternalNode<Integer, String> node = new InternalNode<>();
//		node.insert(1, "one");
//		node.insert(2, "two");
//		InternalNode<Integer, String> sibling = new InternalNode<>();
//		sibling.insert(3, "three");
//		sibling.insert(4, "four");
//		node.merge(sibling);
//		assertEquals(4, node.size());
//		assertEquals("one", node.search(1));
//		assertEquals("two", node.search(2));
//		assertEquals("three", node.search(3));
//		assertEquals("four", node.search(4));
//	}
//
//	@Test
//	public void shouldNotSplit()
//	{
//		InternalNode<Integer, String> node = new InternalNode<>();
//		node.insert(1, "one");
//		node.insert(2, "two");
//		node.insert(3, "three");
//		assertNull(node.split(4));
//	}
//
//	@Test
//	public void shouldGetMiddleKeyOrderThree()
//	{
//		InternalNode<Integer, String> node = new InternalNode<>();
//		node.insert(1, "one");
//		node.insert(2, "two");
//		node.insert(3, "three");
//		assertEquals(3, node.getMiddleKey(3).intValue());
//	}
//
//	@Test
//	public void shouldGetMiddleKeyOrderFour()
//	{
//		InternalNode<Integer, String> node = new InternalNode<>();
//		node.insert(1, "one");
//		node.insert(2, "two");
//		node.insert(3, "three");
//		node.insert(4, "four");
//		assertEquals(3, node.getMiddleKey(4).intValue());
//	}
}
