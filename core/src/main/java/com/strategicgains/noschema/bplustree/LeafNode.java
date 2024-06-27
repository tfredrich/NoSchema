package com.strategicgains.noschema.bplustree;

import java.util.ArrayList;
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
extends AbstractNode<K, V>
{
	private List<V> values;
	private LeafNode<K, V> previousSibling;
	private LeafNode<K, V> nextSibling;

	public LeafNode()
	{
		super();
	}

	public LeafNode(List<K> keys, List<V> values)
	{
		super(keys);
		this.values = new ArrayList<>(values);
	}

	public V search(K key)
	{
		int index = getKeyIndex(key);

		if (index < 0)
		{
			return null;
		}

		return values.get(index);
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

	private void setPreviousSibling(LeafNode<K, V> previousSibling)
	{
		this.previousSibling = previousSibling;
	}

	public LeafNode<K, V> getNextSibling()
	{
		return nextSibling;
	}

	private void setNextSibling(LeafNode<K, V> nextSibling)
	{
		this.nextSibling = nextSibling;
	}

	void insert(K key, V value)
	{
		int idx = insertKey(key);
		values.add(idx, value);
	}

	@Override
	public LeafNode<K, V> split(int order)
	{
		if (size() < order) return null;

		int mid = (order + 1) / 2;
		LeafNode<K, V> sibling = new LeafNode<>(getRightKeys(mid), getRightValues(mid));
		truncateKeys(mid);
		truncateValues(mid);
		sibling.setPreviousSibling(this);
        this.setNextSibling(sibling);
		return sibling;
	}

	private List<V> getRightValues(int mid)
	{
		return values.subList(mid, values.size());
	}

	private void truncateValues(int mid)
	{
		values = values.subList(0, mid);
	}

	@Override
	public void merge(Node<K, V> node)
	{
		LeafNode<K, V> sibling = (LeafNode<K, V>) node;
		super.merge(sibling);
		values.addAll(sibling.values);
	}
}
