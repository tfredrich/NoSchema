package com.strategicgains.noschema.bplustree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class InternalNodeTest
{
/*	@Test
	public void testInsert()
	{
		InternalNode<Integer> node = new InternalNode<>(Arrays.asList(2, 6));
		node.insert(1, new LeafNode(), node);
	}

	@Test
	public void testGetForChild()
	{
		InternalNode<Integer> node = new InternalNode<>(Arrays.asList(2, 4, 6, 8, 10));
		assertFalse(node.isLeaf());
        assertEquals(5, node.size());
        assertEquals(2, (int) node.getKey(0));
        assertEquals(4, (int) node.getKey(1));
        assertEquals(6, (int) node.getKey(2));
        assertEquals(8, (int) node.getKey(3));
        assertEquals(10, (int) node.getKey(4));
        assertEquals(3, node.getKeyIndex(8));
        assertEquals(2, node.insertKey(5));
        assertEquals(6, node.size());

        node.addChild(new LeafNode<Integer, String>(Arrays.asList(1, 2), Arrays.asList("one", "two")));
        node.addChild(new LeafNode<Integer, String>(Arrays.asList(3, 4), Arrays.asList("three", "four")));
        node.addChild(new LeafNode<Integer, String>(Arrays.asList(5, 6), Arrays.asList("five", "six")));
        node.addChild(new LeafNode<Integer, String>(Arrays.asList(7, 8), Arrays.asList("seven", "eight")));
        node.addChild(new LeafNode<Integer, String>(Arrays.asList(9, 10), Arrays.asList("nine", "ten")));
        node.addChild(new LeafNode<Integer, String>(Arrays.asList(11, 12), Arrays.asList("eleven", "twelve")));
        assertEquals(6, node.getChildren().size());

        assertEquals(0, node.getChildFor(1).getKeyIndex(1));
	}

	@Test
	public void testSplit()
	{
		InternalNode<Integer> node = new InternalNode<>(Arrays.asList(2, 4, 6, 8, 10));
		InternalNode<Integer> sibling = node.split();
		assertEquals(3, node.size());
		assertEquals(2, sibling.size());

		assertEquals(0, node.getKeyIndex(2));
		assertEquals(1, node.getKeyIndex(4));
		assertEquals(2, node.getKeyIndex(6));

		assertEquals(0, sibling.getKeyIndex(8));
		assertEquals(1, sibling.getKeyIndex(10));
	}
*/
}
