package com.strategicgains.noschema.cassandra;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.datastax.oss.driver.api.core.cql.Row;

public class PagedRows
{
	private String cursor;
	private List<Row> rows = new ArrayList<>();

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

	public void add(Row row)
	{
		rows.add(row);
	}

	public Stream<Row> stream()
	{
		return rows.stream();
	}
}
