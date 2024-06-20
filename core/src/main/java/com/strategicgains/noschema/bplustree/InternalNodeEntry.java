package com.strategicgains.noschema.bplustree;

public class InternalNodeEntry<K extends Comparable<K>, V>
extends AbstractNodeEntry<K, InternalValue<V>>
{
	private InternalValue<V> value;

	public InternalNodeEntry(K key, V leftChild, V rightChild)
	{
		super(key);
		this.value = new InternalValue<>(leftChild, rightChild);
	}

	public InternalValue<V> getValue()
	{
		return value;
	}
}
