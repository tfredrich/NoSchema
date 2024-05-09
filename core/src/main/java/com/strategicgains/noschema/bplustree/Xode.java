package com.strategicgains.noschema.bplustree;

import java.util.ArrayList;
import java.util.List;

public class Xode<T extends Comparable<T>>
{
	private List<LeafNodeEntry<T, ?>> entries = new ArrayList<>();

	public Xode()
	{
		super();
	}

	public int size()
	{
		return entries.size();
	}

	
}
