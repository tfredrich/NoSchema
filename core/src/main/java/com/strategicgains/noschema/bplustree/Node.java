package com.strategicgains.noschema.bplustree;

public interface Node<K extends Comparable<K>, V>
{
	/**
	 * Get the middle key of this node given the order of the B+Tree.
	 * 
	 * @param order the order of the B+Tree.
	 * @return the middle key of this node.
	 */
	K getMiddleKey(int order);

	/**
	 * Determine if this node is a leaf node.
	 * 
	 * @return true if this node is a leaf node; false otherwise.
	 */
	boolean isLeaf();

	/**
	 * Determine if this node is full. If so, split it and return the new sibling node.
	 * Otherwise, return null.
	 * 
	 * @param order the order of the B+Tree.
	 * @return the new sibling node if this node is full; null otherwise.
	 */
	Node<K, V> split(int order);

	/**
	 * Merge this node with the sibling node.
	 * 
	 * @param sibling the sibling node to merge with this node.
	 */
	void merge(Node<K, V> sibling);
}
