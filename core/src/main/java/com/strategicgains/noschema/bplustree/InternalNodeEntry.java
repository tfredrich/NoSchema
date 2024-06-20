package com.strategicgains.noschema.bplustree;

public class InternalNodeEntry<T extends Comparable<T>, V>
extends AbstractNodeEntry<T, InternalValue<V>>
{
	private InternalValue<V> value;

	public InternalNodeEntry(T key, V leftChild, V rightChild)
	{
		super(key);
		this.value = new InternalValue<V>(leftChild, rightChild);
	}

	public InternalValue<V> getValue()
	{
		return value;
	}
}
