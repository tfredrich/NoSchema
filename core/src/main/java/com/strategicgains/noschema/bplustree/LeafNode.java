package com.strategicgains.noschema.bplustree;

import java.util.List;

/**
 * Represents a leaf node in a B+Tree that contains keys and values.
 * The values are the data stored in the tree. Leaf nodes are the only nodes
 * that contain data. They are linked together to form linked list at the leaf layer
 * that facilitates ordered access.
 * 
 * @author Todd Fredrich
 * @param <K> the type of the keys in the node. Must implement Comparable.
 * @param <V> the type of the values stored in the node.
 */
class LeafNode<K extends Comparable<K>, V>
extends Node<K, V>
{
	private LeafNode<K, V> previousSibling;
	private LeafNode<K, V> nextSibling;

	public LeafNode()
	{
		super();
	}

	public LeafNode(List<Entry<K,V>> entries)
	{
		super(entries);
	}

	public V search(K key)
	{
		int index = getKeyIndex(key);

		if (index < 0) {
			return null;
		}

		return getEntry(index).getValue();
	}

	@Override
	public boolean isLeaf()
	{
		return true;
	}

	public LeafNode<K, V> getPreviousSibling()
	{
		return previousSibling;
	}

	public LeafNode<K, V> getNextSibling()
	{
		return nextSibling;
	}

	void insert(K key, V value)
	{
		insertEntry(new LeafNodeEntry<>(key, value));
	}

	@Override
	protected LeafNode<K, V> createSibling(List<Entry<K,V>> list)
	{
		return new LeafNode<>(list);
	}
}
