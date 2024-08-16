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
 * @param <K> the type of the keys in the node. Must implement Comparable.
 * @param <V> the type of the values stored in the leaf nodes.
 * @see InternalNode
 * @see LeafNode
 */
public abstract class AbstractNode<K extends Comparable<K>, V>
implements Node<K, V>
{
	private List<K> keys;

	protected AbstractNode()
	{
		super();
		keys = new ArrayList<>();
	}

	protected AbstractNode(List<K> keys)
	{
		this.keys = new ArrayList<>(keys);
		Collections.sort(this.keys);
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
	 * Determine if this node is a leaf node.
	 * 
	 * @return true if this node is a leaf node; false otherwise.
	 */
	@Override
	public boolean isLeaf()
	{
		return false;
	}

	/**
	 * Insert a key into this node at the correct position.
	 * 
	 * The return value is that of Collections.binarySearch(), with the index negated and decremented by 1.
	 * This is the index where the values corresponding to the key should be inserted.
	 * 
	 * If the key already exists in the node, the key is not inserted but the index of the existing key is returned.
	 * 
	 * @param entry the entry (with key and value) to insert.
	 * @return the index of the inserted key or the existing key.
	 */
	int insertKey(K key)
	{
		int idx = getKeyIndex(key);

		if (idx < 0)
		{
			keys.add(-(idx + 1), key);
		}

		return idx;
	}

	/**
	 * Merge the keys from the sibling node into this node.
	 * The keys in the sibling node are added to the end of the keys in this node.
	 * 
	 * @param sibling the sibling node to merge into this node.
	 */
	@Override
	public void merge(Node<K, V> sibling)
	{
		AbstractNode<K, V> node = (AbstractNode<K, V>) sibling;
		keys.addAll(node.keys);
	}

	/**
	 * Get the middle key of this node given the order of the B+Tree.
	 * 
	 * @param order the order of the B+Tree.
	 * @return the middle key of this node.
	 */
	@Override
	public K getMiddleKey(int order)
	{
		return keys.get(getMiddleKeyIndex(order));
	}

	/**
	 * Get the first keys (index 0 through mid) of this node.
	 * 
	 * @param mid the index of the middle key.
	 * @return the first n keys of this node.
	 */
	List<K> getLeftKeys(int mid)
	{
		return keys.subList(0, mid);
	}

	/**
	 * Get the last keys (index mid through size) of this node.
	 * 
	 * @param mid the index of the middle key.
	 * @return the last n keys of this node.
	 */
	List<K> getRightKeys(int mid)
	{
		return keys.subList(mid, keys.size());
	}

	/**
	 * Truncate the keys in this node to the first n keys.
	 * 
	 * @param mid the index of the middle key.
	 */
	void truncateKeys(int mid)
	{
		this.keys = getLeftKeys(mid);
	}

	/**
	 * Perform a binary search for the key in this node, returning the index of the key if found.
	 * If the key is not found, return the index where the key should be inserted.
	 * 
	 * @param key
	 * @return the index of the key if found; otherwise, the index where the key should be inserted.
	 */
	int getKeyIndex(K key)
	{
		return Collections.binarySearch(keys, key);
	}

	int getMiddleKeyIndex(int order)
	{
		return (order + 1) / 2;
	}
}
