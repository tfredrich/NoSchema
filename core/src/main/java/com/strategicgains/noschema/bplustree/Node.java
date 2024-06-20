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
public abstract class Node<K extends Comparable<K>, V>
{
	private List<Entry<K, V>> entries;

	protected Node()
	{
		super();
		entries = new ArrayList<>();
	}

	protected Node(List<Entry<K, V>> entries)
	{
		this.entries = new ArrayList<>(entries);
	}

	/**
	 * Get the number of entries in this node.
	 * 
	 * @return the number of entries in this node.
	 */
	public int size()
	{
		return entries.size();
	}

	/**
	 * Get the entry at the specified index.
	 * 
	 * @param idx the index of the entry to get.
	 * @return the entry at the specified index.
	 */
	public Entry<K, V> getEntry(int idx)
	{
		return entries.get(idx);
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
	 * @param entry the entry (with key and value) to insert.
	 * @return the index of the inserted key.
	 */
	int insertEntry(Entry<K, V> entry)
	{
		int idx = getInsertionPoint(entry.getKey());
		entries.add(idx, entry);
		return idx;
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
		return Collections.binarySearch(entries, key);
	}

	/**
	 * Split the keys in this node into two nodes. The keys in this node are split in half and the right half is
	 * returned in a new sibling node.
	 * 
	 * @return a new sibling node containing the right half of the keys.
	 */
	Node<K, V> split()
	{
		int mid = (size() + 1) / 2;
		Node<K, V> sibling = createSibling(getRightEntries(mid));
		truncateKeys(mid);
		return sibling;
	}

	/**
	 * Merge the keys from the sibling node into this node.
	 * The keys in the sibling node are added to the end of the keys in this node.
	 * 
	 * @param sibling the sibling node to merge into this node.
	 */
	void merge(Node<K, V> sibling)
	{
		entries.addAll(sibling.entries);
	}

	/**
	 * Create a sibling node of the same type as this node.
	 * 
	 * @param list the list of keys to add to the sibling node.
	 * @return a new sibling node.
	 */
	abstract Node<K, V> createSibling(List<Entry<K,V>> list);

	/**
	 * Get the first entries (index 0 through mid) of this node.
	 * 
	 * @param mid the index of the middle key.
	 * @return the first n entries of this node.
	 */
	private List<Entry<K,V>> getLeftEntries(int mid)
	{
		return new ArrayList<>(entries.subList(0, mid));
	}

	/**
	 * Get the last entries (index mid through size) of this node.
	 * 
	 * @param mid the index of the middle key.
	 * @return the last n entries of this node.
	 */
	private List<Entry<K,V>> getRightEntries(int mid)
	{
		return new ArrayList<>(entries.subList(mid, size()));
	}

	/**
	 * Truncate the keys in this node to the first n keys.
	 * 
	 * @param mid the index of the middle key.
	 */
	private void truncateKeys(int mid)
	{
		this.entries = getLeftEntries(mid);
	}

	/**
	 * Get the index where the key should be inserted.
	 * 
	 * @param key the key to insert.
	 * @return the index where the key should be inserted.
	 */
	private int getInsertionPoint(K key)
	{
		int idx = getKeyIndex(key);

		if (idx < 0)
		{
			idx = -(idx + 1);
		}

		return idx;
	}
}
