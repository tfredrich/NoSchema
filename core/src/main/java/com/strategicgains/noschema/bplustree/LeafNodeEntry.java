package com.strategicgains.noschema.bplustree;

public class LeafNodeEntry<T extends Comparable<T>, V>
implements Comparable<T>
{
	private T key;
	private V value;

	public LeafNodeEntry(T key, V value)
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
		if (!(obj instanceof LeafNodeEntry)) return false;
		LeafNodeEntry<?, ?> that = (LeafNodeEntry<?, ?>) obj;
		return key.equals(that.key);
	}

	@Override
	public int hashCode()
	{
		return key.hashCode();
	}
}
