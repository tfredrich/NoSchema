package com.strategicgains.noschema.bplustree;

public class LeafNodeEntry<K extends Comparable<K>, V>
extends AbstractNodeEntry<K, V>
{
	private V value;

	public LeafNodeEntry(K key, V value)
	{
		super(key);
		this.value = value;
	}

	public V getValue()
	{
		return value;
	}
}
