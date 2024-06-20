package com.strategicgains.noschema.bplustree;

public interface Entry<K extends Comparable<K>, V>
extends Comparable<K>
{
	K getKey();
	V getValue();
}
