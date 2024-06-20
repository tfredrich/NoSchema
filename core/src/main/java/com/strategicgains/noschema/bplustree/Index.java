package com.strategicgains.noschema.bplustree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Index<K extends Comparable<K>, V>
{
	private int order;
	private int mid;
	private Node<K, V> root;

	public Index(int order)
	{
		this.order = order;
		this.mid = (order + 1) / 2;
		this.root = new LeafNode<>();
	}

	public void insert(K key, V value)
	{
		Node<K, V> currentNode = root;

		while (true)
		{
			if (currentNode.isLeaf())
			{
				LeafNode<K, V> leaf = (LeafNode<K, V>) currentNode;
				leaf.insert(key, value);

				if (leaf.size() > order - 1)
				{
					LeafNode<K, V> parent = leaf.split();
				}
				break;
			}
			else
			{
				InternalNode<K, V> internal = (InternalNode<K, V>) currentNode;
				Node<K, V> child = internal.getChildFor(key);

				if (child.isLeaf())
				{
					LeafNode<K, V> leaf = (LeafNode<K, V>) child;
					leaf.insert(key, value);

					if (leaf.size() > order - 1)
					{
						LeafNode<K, V> parent = leaf.split();
					}
				}
				else
				{
					currentNode = child;
				}
			}
		}
	}

	public V search(K key) {
		Node<K, V> currentNode = root;
		while (true)
		{
			if (currentNode.isLeaf())
			{
				LeafNode<K, V> leaf = (LeafNode<K, V>) currentNode;
				int idx = leaf.getKeyIndex(key);
				return idx >= 0;
			}
			else
			{
				InternalNode<K, V> internal = (InternalNode<K, V>) currentNode;
				currentNode = internal.getChildFor(key);
			}
		}
	}

	public void delete(K key)
	{
		delete(root, key);
	}

	private Node<K, V> delete(Node<K, V> node, K key)
	{
		if (node.isLeaf())
		{
			LeafNode<K, V> leaf = (LeafNode<K, V>) node;
			int idx = Collections.binarySearch(leaf, key);
			if (idx >= 0) {
				leaf.remove(idx);
				if (leaf.size() < (order - 1) / 2) {
					balance(leaf);
				}
			}
			return node;
		} else {
			InternalNode<K, V> internal = (InternalNode<K, V>) node;
			int idx = Collections.binarySearch(internal, key);
			if (idx < 0) {
				idx = -(idx + 1);
			}
			Node<K, V> child = internal.children.get(idx);
			Node<K, V> newChild = delete(child, key);
			if (newChild != child) {
				internal.children.set(idx, newChild);
				if (newChild.size() < (order - 1) / 2) {
					balance(newChild);
				}
			}
			return node;
		}
	}

	private void balance(Node<K, V> node)
	{
		if (node.isLeaf())
		{
			LeafNode<K, V> leaf = (LeafNode<K, V>) node;
			if (leaf.size() < (order - 1) / 2)
			{
				LeafNode<K, V> sibling = getSibling(leaf);

				if (sibling != null)
				{
					leaf.merge(sibling);
				}
				else
				{
					// Node is root, make it smaller
					if (leaf == root) {
						List<K> newKeys = new ArrayList<>();
						for (K key : leaf.keys) {
							newKeys.add(key);
						}
						root = new LeafNode<>();
						((LeafNode<K, V>) root).keys = newKeys;
					} else {
						// Remove node
						InternalNode<K, V> parent = getParent(leaf);
						parent.children.remove(leaf);
						parent.keys.remove(0);
						balance(parent);
					}
				}
			}
		} else {
			InternalNode<K, V> internal = (InternalNode<K, V>) node;
			if (internal.size() < (order - 1) / 2) {
				InternalNode<K, V> sibling = getSibling(internal);
				if (sibling != null) {
					merge(internal, sibling);
				} else {
					// Node is root, make it smaller
					if (internal == root) {
						List<K> newKeys = new ArrayList<>();
						for (K key : internal.keys) {
							newKeys.add(key);
						}
						root = new InternalNode<>();
						((InternalNode<K, V>) root).keys = newKeys;
					} else {
						// Remove node
						InternalNode<K, V> parent = getParent(internal);
						parent.children.remove(internal);
						parent.keys.remove(0);
						balance(parent);
					}
				}
			}
		}
	}

	private LeafNode<K, V> getSibling(LeafNode<K, V> node) {
		InternalNode<K, V> parent = getParent(node);
		int idx = parent.children.indexOf(node);
		if (idx > 0) {
			return (LeafNode<K, V>) parent.children.get(idx - 1);
		} else {
			return null;
		}
	}

	private InternalNode<K, V> getSibling(InternalNode<K, V> node) {
		InternalNode<K, V> parent = getParent(node);
		int idx = parent.children.indexOf(node);
		if (idx > 0) {
			return (InternalNode<K, V>) parent.children.get(idx - 1);
		} else {
			return null;
		}
	}

	private InternalNode<K, V> getParent(Node<K, V> node) {
		if (node == root) {
			return null;
		}
		InternalNode<K, V> parent = (InternalNode<K, V>) root;
		Node<K, V> child = node;
		while (true) {
			int idx = parent.children.indexOf(child);
			if (idx < 0) {
				return null;
			}
			if (idx < parent.size()) {
				return parent;
			}
			child = parent;
			parent = (InternalNode<K, V>) parent.children.get(idx);
		}
	}
}
