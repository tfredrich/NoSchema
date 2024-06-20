package com.strategicgains.noschema.bplustree;

public abstract class AbstractNodeEntry<K extends Comparable<K>, V>
implements Entry<K, V>
{
	private K key;

	protected AbstractNodeEntry(K key)
	{
		super();
		this.key = key;
	}

	public K getKey()
	{
		return key;
	}

	@Override
	public int compareTo(K o)
	{
		return key.compareTo(o);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof LeafNodeEntry)) return false;
		AbstractNodeEntry<?, ?> that = (AbstractNodeEntry<?, ?>) obj;
		return key.equals(that.key);
	}

	@Override
	public int hashCode()
	{
		return key.hashCode();
	}

}
