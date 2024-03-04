package com.strategicgains.noschema.cassandra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.strategicgains.noschema.Identifiable;

public class PagedResponse<T extends Identifiable>
{
	private String cursor;
	private List<T> items;

	public void cursor(String hexString)
	{
		this.cursor = hexString;
	}

	public boolean hasCursor()
	{
		return (cursor != null);
	}

	public String cursor()
	{
		return cursor;
	}

	public void add(T item)
	{
		if (items == null)
		{
			items = new ArrayList<>();
		}

		items.add(item);
	}

	public boolean hasItems()
	{
		return (items != null && !items.isEmpty());
	}

	public int size()
	{
		return (hasItems() ? items.size() : 0);
	}

	public T get(int i)
	{
		return (hasItems() ? items.get(i) : null);
	}

	public List<T> items()
	{
		return (hasItems() ? items : Collections.emptyList());
	}
}
