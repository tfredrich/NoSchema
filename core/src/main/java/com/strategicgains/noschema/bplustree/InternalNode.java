package com.strategicgains.noschema.bplustree;

import java.util.ArrayList;
import java.util.Collections;
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
extends Node<K, V>
{
	private List<Node<K, V>> children = new ArrayList<>();

	public InternalNode(List<Entry<K,V>> list)
	{
		super(list);
	}

	public Node<K, V> getChildFor(K key)
	{
		return children.get(getKeyIndex(key));
	}

	public void addChild(Node<K, V> child)
	{
		children.add(child);
	}

	public List<Node<K, V>> getChildren()
	{
		return Collections.unmodifiableList(children);
	}

	public Node<K, V> getChild(int index)
	{
		return children.get(index);
	}

	void insert(K key, Node<K, V> left, Node<K, V> right)
	{
//		int index = insertKey(key);
//		children.set(index, left);
//		children.add(index + 1, right);
	}

	@Override
	InternalNode<K, V> split()
	{
		InternalNode<K, V> sibling = (InternalNode<K, V>) super.split();
		sibling.children.addAll(children.subList(sibling.size() + 1, children.size()));
		children = children.subList(0, sibling.size() + 1);
		return sibling;
	}

	void merge(InternalNode<K, V> sibling)
	{
		super.merge(sibling);
		children.addAll(sibling.getChildren());
	}

	@Override
	protected InternalNode<K, V> createSibling(List<Entry<K,V>> list)
	{
		return new InternalNode<>(list);
	}
}
