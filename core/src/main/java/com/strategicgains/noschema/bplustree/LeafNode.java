package com.strategicgains.noschema.bplustree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a leaf node in a B+Tree that contains keys and values.
 * The values are the data stored in the tree. Leaf nodes are the only nodes
 * that contain data. They are linked together to form linked list at the leaf layer
 * that facilitates ordered access.
 * 
 * @author Todd Fredrich
 * @param <T> the type of the keys in the node. Must implement Comparable.
 * @param <V> the type of the values in the node.
 */
class LeafNode<T extends Comparable<T>, V>
extends Node<T>
{
	private LeafNode<T, V> previousSibling;
	private LeafNode<T, V> nextSibling;
	private List<V> values = new ArrayList<>();

	public LeafNode()
	{
		super();
	}

	public LeafNode(List<T> keys, List<V> values)
	{
		super(keys);
		this.values = values;
	}

	public V search(T key)
	{
		int index = getKeyIndex(key);

		if (index < 0) {
			return null;
		}

		return values.get(index);
	}

	@Override
	public boolean isLeaf()
	{
		return true;
	}

	public LeafNode<T, V> getPreviousSibling()
	{
		return previousSibling;
	}

	public LeafNode<T, V> getNextSibling()
	{
		return nextSibling;
	}

	void insert(T key, V value)
	{
		int index = insertKey(key);
		this.values.add(index, value);
	}

	/**
	 * Splits the leaf node in half, creating a new sibling leaf node with the right-half values and
	 * returns it. The left-half values remain in this node.
	 * 
	 * @param mid the index at which to split the node.
	 * @return a new LeafNode that contains the right-half of the values.
	 */
	@Override
	LeafNode<T, V> split()
	{
		@SuppressWarnings("unchecked")
		LeafNode<T, V> sibling = (LeafNode<T, V>) super.split();
		sibling.values.addAll(values.subList(sibling.size() + 1, values.size()));
		truncateValues(sibling.size() + 1);
		return sibling;
	}

	void merge(LeafNode<T, V> sibling)
	{
		super.merge(sibling);
		values.addAll(sibling.getValues());
	}

	private List<? extends V> getValues()
	{
		return Collections.unmodifiableList(values);
	}

	private List<V> getValues(int qty)
	{
		return new ArrayList<>(values.subList(0, qty));
	}

	private void truncateValues(int qty)
	{
		values = getValues(qty);
	}

	@Override
	protected Node<T> createSibling(List<T> list)
	{
		return new LeafNode<>(list, getValues(list.size()));
	}
}
