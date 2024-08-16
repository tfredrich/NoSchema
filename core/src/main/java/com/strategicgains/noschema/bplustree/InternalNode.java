package com.strategicgains.noschema.bplustree;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an internal node in a B+Tree that contains keys and child nodes.
 * The child nodes are either InternalNodes or LeafNodes.
 * 
 * @author Todd Fredrich
 * @param <K> the type of the keys in the node. Must implement Comparable.
 * @param <V> the type of the values in the node.
 * @see Node
 * @see LeafNode
 */
class InternalNode<K extends Comparable<K>, V>
extends AbstractNode<K, V>
{
	private List<Node<K, V>> children;

	public InternalNode(List<K> keys, List<Node<K, V>> children)
	{
		super(keys);
		this.children = new ArrayList<>(children);
	}

	public Node<K, V> getChildFor(K key)
	{
		return children.get(getKeyIndex(key));
	}

	void insert(K key, Node<K, V> left, Node<K, V> right)
	{
		int index = insertKey(key);
		children.set(index, left);
		children.add(index + 1, right);
	}

	@Override
	public InternalNode<K, V> split(int order)
	{
		int mid = (order + 1) / 2;
		InternalNode<K, V> sibling = new InternalNode<>(getRightKeys(mid), children.subList(mid, children.size()));
		children = children.subList(0, mid);
		return sibling;
	}

	@Override
	public void merge(Node<K, V> node)
	{
		InternalNode<K, V> sibling = (InternalNode<K, V>) node;
		super.merge(sibling);
		children.addAll(sibling.children);
	}
}
