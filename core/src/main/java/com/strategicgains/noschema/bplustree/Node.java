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

	protected Node()
	{
		super();
	}

	protected Node(List<T> keys)
	{
		this.keys = new ArrayList<>(keys);
	}

	public int size()
	{
		return keys.size();
	}

	public T getKey(int idx)
	{
		return keys.get(idx);
	}

	public List<T> getKeys()
	{
		return Collections.unmodifiableList(keys);
	}

	public boolean isLeaf()
	{
		return false;
	}

	int insertKey(T key)
	{
		int idx = getInsertionPoint(key);
		keys.add(idx, key);
		return idx;
	}

	int getKeyIndex(T key)
	{
		return Collections.binarySearch(keys, key);
	}

	Node<T> split()
	{
		int mid = size() / 2;
		Node<T> sibling = createSibling(getRightKeys(mid));
		truncateKeys(mid);
		return sibling;
	}

	void merge(Node<T> sibling)
	{
		keys.addAll(sibling.getKeys());
	}

	protected abstract Node<T> createSibling(List<T> list);

	private List<T> getLeftKeys(int mid)
	{
		return new ArrayList<>(keys.subList(0, mid));
	}

	private List<T> getRightKeys(int mid)
	{
		return new ArrayList<>(keys.subList(mid, size()));
	}

	private void truncateKeys(int mid)
	{
		this.keys = getLeftKeys(mid);
	}

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
