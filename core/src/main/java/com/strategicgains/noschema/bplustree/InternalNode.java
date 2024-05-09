package com.strategicgains.noschema.bplustree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an internal node in a B+Tree that contains keys and child nodes.
 * The child nodes are either InternalNodes or LeafNodes.
 * 
 * @author Todd Fredrich
 * @param <T> the type of the keys in the node. Must implement Comparable.
 * @see Node
 * @see LeafNode
 */
class InternalNode<T extends Comparable<T>>
extends Node<T>
{
	private List<Node<T>> children = new ArrayList<>();

	public InternalNode(List<T> list)
	{
		super(list);
	}

	public Node<T> getChildFor(T key)
	{
		return children.get(getKeyIndex(key));
	}

	public void addChild(Node<T> child)
	{
		children.add(child);
	}

	public List<Node<T>> getChildren()
	{
		return Collections.unmodifiableList(children);
	}

	public Node<T> getChild(int index)
	{
		return children.get(index);
	}

	@Override
	InternalNode<T> split()
	{
		InternalNode<T> sibling = (InternalNode<T>) super.split();
		sibling.children.addAll(children.subList(sibling.size() + 1, children.size()));
		children = children.subList(0, sibling.size() + 1);
		return sibling;
	}

	void merge(InternalNode<T> sibling)
	{
		super.merge(sibling);
		children.addAll(sibling.getChildren());
	}

	@Override
	protected Node<T> createSibling(List<T> list)
	{
		return new InternalNode<>(list);
	}
}
