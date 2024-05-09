package com.strategicgains.noschema.bplustree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Index<T extends Comparable<T>, V>
{
	private int order;
	private int mid;
	private Node<T> root;

	public Index(int order)
	{
		this.order = order;
		this.mid = (order + 1) / 2;
		this.root = new LeafNode<>();
	}

	public void insert(T key, V value)
	{
		Node<T> currentNode = root;

		while (true)
		{
			if (currentNode.isLeaf())
			{
				LeafNode<T, V> leaf = (LeafNode<T, V>) currentNode;
				leaf.insert(key, value);

				if (leaf.size() > order - 1)
				{
					LeafNode<T, V> parent = leaf.split(order);
				}
				break;
			}
			else
			{
				InternalNode<T> internal = (InternalNode<T>) currentNode;
				Node<T> child = internal.getChildFor(key);

				if (child.isLeaf())
				{
					LeafNode<T, V> leaf = (LeafNode<T, V>) child;
					leaf.insert(key, value);

					if (leaf.size() > order - 1)
					{
						LeafNode<T, V> parent = leaf.split(order);
					}
				}
				else
				{
					currentNode = child;
				}
			}
		}
	}

	public V search(T key) {
		Node<T> currentNode = root;
		while (true)
		{
			if (currentNode.isLeaf())
			{
				LeafNode<T> leaf = (LeafNode<T>) currentNode;
				int idx = leaf.getKeyIndex(key);
				return idx >= 0;
			}
			else
			{
				InternalNode<T> internal = (InternalNode<T>) currentNode;
				currentNode = internal.getChildFor(key);
			}
		}
	}

	public void delete(T key)
	{
		delete(root, key);
	}

	private Node<T> delete(Node<T> node, T key)
	{
		if (node.isLeaf())
		{
			LeafNode<T> leaf = (LeafNode<T>) node;
			int idx = Collections.binarySearch(leaf.keys, key);
			if (idx >= 0) {
				leaf.keys.remove(idx);
				if (leaf.keys.size() < (order - 1) / 2) {
					balance(leaf);
				}
			}
			return node;
		} else {
			InternalNode<T> internal = (InternalNode<T>) node;
			int idx = Collections.binarySearch(internal.keys, key);
			if (idx < 0) {
				idx = -(idx + 1);
			}
			Node<T> child = internal.children.get(idx);
			Node<T> newChild = delete(child, key);
			if (newChild != child) {
				internal.children.set(idx, newChild);
				if (newChild.keys.size() < (order - 1) / 2) {
					balance(newChild);
				}
			}
			return node;
		}
	}

	private void balance(Node<T> node)
	{
		if (node.isLeaf())
		{
			LeafNode<T> leaf = (LeafNode<T>) node;
			if (leaf.size() < (order - 1) / 2)
			{
				LeafNode<T> sibling = getSibling(leaf);

				if (sibling != null)
				{
					leaf.merge(sibling);
				}
				else
				{
					// Node is root, make it smaller
					if (leaf == root) {
						List<T> newKeys = new ArrayList<>();
						for (T key : leaf.keys) {
							newKeys.add(key);
						}
						root = new LeafNode<>();
						((LeafNode<T>) root).keys = newKeys;
					} else {
						// Remove node
						InternalNode<T> parent = getParent(leaf);
						parent.children.remove(leaf);
						parent.keys.remove(0);
						balance(parent);
					}
				}
			}
		} else {
			InternalNode<T> internal = (InternalNode<T>) node;
			if (internal.keys.size() < (order - 1) / 2) {
				InternalNode<T> sibling = getSibling(internal);
				if (sibling != null) {
					merge(internal, sibling);
				} else {
					// Node is root, make it smaller
					if (internal == root) {
						List<T> newKeys = new ArrayList<>();
						for (T key : internal.keys) {
							newKeys.add(key);
						}
						root = new InternalNode<>();
						((InternalNode<T>) root).keys = newKeys;
					} else {
						// Remove node
						InternalNode<T> parent = getParent(internal);
						parent.children.remove(internal);
						parent.keys.remove(0);
						balance(parent);
					}
				}
			}
		}
	}

	private LeafNode<T> getSibling(LeafNode<T> node) {
		InternalNode<T> parent = getParent(node);
		int idx = parent.children.indexOf(node);
		if (idx > 0) {
			return (LeafNode<T>) parent.children.get(idx - 1);
		} else {
			return null;
		}
	}

	private InternalNode<T> getSibling(InternalNode<T> node) {
		InternalNode<T> parent = getParent(node);
		int idx = parent.children.indexOf(node);
		if (idx > 0) {
			return (InternalNode<T>) parent.children.get(idx - 1);
		} else {
			return null;
		}
	}

	private InternalNode<T> getParent(Node<T> node) {
		if (node == root) {
			return null;
		}
		InternalNode<T> parent = (InternalNode<T>) root;
		Node<T> child = node;
		while (true) {
			int idx = parent.children.indexOf(child);
			if (idx < 0) {
				return null;
			}
			if (idx < parent.size()) {
				return parent;
			}
			child = parent;
			parent = (InternalNode<T>) parent.children.get(idx);
		}
	}
}
