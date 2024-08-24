package com.strategicgains.noschema;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.document.GzipCompressionObserver;

public class GzipCompressionObserverTest
{
	private GzipCompressionObserver observer = new GzipCompressionObserver();
	private String JSON = "{\"name\":\"Joe Blow\",\"age\":42,\"address\":\"123 Main St.\",\"city\":\"Anytown\",\"state\":\"TX\",\"zip\":\"12345\"}";

	@Before
	public void setup()
	{
	}

	@Test
	public void shouldCompressAndDecompress()
	{
		Document document = new Document(JSON.getBytes(), Object.class);
		System.out.println("Before: " + document.getObject().length);
		observer.afterDecoding(document);
		System.out.println("Compressed: " + document.getObject().length);
		observer.afterEncoding(document);
		System.out.println("Decompressed: " + document.getObject().length);
		assertEquals(JSON, new String(document.getObject()));
	}
}
