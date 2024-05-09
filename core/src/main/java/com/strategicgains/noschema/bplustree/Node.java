package com.strategicgains.noschema.bplustree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a node in a B+Tree. It contains a list of keys in natural order.
 * The keys are used to navigate the tree and to locate the values in the leaf.
 * Keys must implement the Comparable interface.
 *
 * @author Todd Fredrich
 * @param <T> the type of the keys in the node. Must implement Comparable.
 * @see InternalNode
 * @see LeafNode
 */
public abstract class Node<T extends Comparable<T>>
{
	private List<T> keys = new ArrayList<>();
	private List<LeafNodeEntry<T, ?>> entries = new ArrayList<>();

	protected Node()
	{
		super();
	}

	protected Node(List<T> keys)
	{
		this.keys = new ArrayList<>(keys);
	}

	/**
	 * Get the number of keys in this node.
	 * 
	 * @return the number of keys in this node.
	 */
	public int size()
	{
		return keys.size();
	}

	/**
	 * Get the key at the specified index.
	 * 
	 * @param idx the index of the key to get.
	 * @return the key at the specified index.
	 */
	public T getKey(int idx)
	{
		return keys.get(idx);
	}

	/**
	 * Get the keys in this node.
	 * 
	 * @return an unmodifiable list of the keys in this node.
	 */
	public List<T> getKeys()
	{
		return Collections.unmodifiableList(keys);
	}

	/**
	 * Determine if this node is a leaf node.
	 * 
	 * @return true if this node is a leaf node; false otherwise.
	 */
	public boolean isLeaf()
	{
		return false;
	}

	/**
	 * Insert a key into this node at the correct position.
	 * 
	 * @param key the key to insert.
	 * @return the index of the inserted key.
	 */
	int insertKey(T key)
	{
		int idx = getInsertionPoint(key);
		keys.add(idx, key);
		return idx;
	}

	/**
	 * Perform a binary search for the key in this node, returning the index of the key if found.
	 * If the key is not found, return the index where the key should be inserted.
	 * 
	 * @param key
	 * @return the index of the key if found; otherwise, the index where the key should be inserted.
	 */
	int getKeyIndex(T key)
	{
		return Collections.binarySearch(keys, key);
	}

	/**
	 * Split the keys in this node into two nodes. The keys in this node are split in half and the right half is
	 * returned in a new sibling node.
	 * 
	 * @return a new sibling node containing the right half of the keys.
	 */
	Node<T> split()
	{
		int mid = (size() + 1) / 2;
		Node<T> sibling = createSibling(getRightKeys(mid));
		truncateKeys(mid);
		return sibling;
	}

	/**
	 * Merge the keys from the sibling node into this node.
	 * The keys in the sibling node are added to the end of the keys in this node.
	 * 
	 * @param sibling the sibling node to merge into this node.
	 */
	void merge(Node<T> sibling)
	{
		keys.addAll(sibling.getKeys());
	}

	/**
	 * Create a sibling node of the same type as this node.
	 * 
	 * @param list the list of keys to add to the sibling node.
	 * @return a new sibling node.
	 */
	abstract Node<T> createSibling(List<T> list);

	/**
	 * Get the first keys (index 0 through mid) of this node.
	 * 
	 * @param mid the index of the middle key.
	 * @return the first n keys of this node.
	 */
	private List<T> getLeftKeys(int mid)
	{
		return new ArrayList<>(keys.subList(0, mid));
	}

	/**
	 * Get the last keys (index mid through size) of this node.
	 * 
	 * @param mid the index of the middle key.
	 * @return the last n keys of this node.
	 */
	private List<T> getRightKeys(int mid)
	{
		return new ArrayList<>(keys.subList(mid, size()));
	}

	/**
	 * Truncate the keys in this node to the first n keys.
	 * 
	 * @param mid the index of the middle key.
	 */
	private void truncateKeys(int mid)
	{
		this.keys = getLeftKeys(mid);
	}

	/**
	 * Get the index where the key should be inserted.
	 * 
	 * @param key the key to insert.
	 * @return the index where the key should be inserted.
	 */
	private int getInsertionPoint(T key)
	{
		int idx = getKeyIndex(key);

		if (idx < 0)
		{
			idx = -(idx + 1);
		}

		return idx;
	}
}
