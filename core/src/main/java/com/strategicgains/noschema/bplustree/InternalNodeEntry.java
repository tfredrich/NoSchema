package com.strategicgains.noschema.bplustree;

public class InternalNodeEntry<T extends Comparable<T>, V>
implements Comparable<T>
{
	private T key;
	private V leftChild;
	private V rightChild;

	public InternalNodeEntry(T key, V leftChild, V rightChild)
	{
		super();
		this.key = key;
		this.leftChild = leftChild;
		this.rightChild = rightChild;
	}
	{
		super();
		this.key = key;
		this.value = value;
	}

	public T getKey()
	{
		return key;
	}

	public V getValue()
	{
		return value;
	}

	@Override
	public int compareTo(T o)
	{
		return key.compareTo(o);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof InternalNodeEntry)) return false;
		InternalNodeEntry<?, ?> that = (InternalNodeEntry<?, ?>) obj;
		return key.equals(that.key);
	}

	@Override
	public int hashCode()
	{
		return key.hashCode();
	}
}
