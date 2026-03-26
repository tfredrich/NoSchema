/*
    Copyright 2024-2026, Strategic Gains, Inc.

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package com.strategicgains.noschema;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.document.GzipDocumentFilter;

public class GzipCompressionFilterTest
{
	private static final String JSON = "{\"name\":\"Joe Blow\",\"age\":42,\"address\":\"123 Main St.\",\"city\":\"Anytown\",\"state\":\"TX\",\"zip\":\"12345\"}";

	private GzipDocumentFilter filter = new GzipDocumentFilter();

	@Test
	public void shouldCompressAndDecompress()
	{
		Document document = new Document(JSON.getBytes(), Object.class);
		int originalLength = document.getObject().length;
		System.out.println("Original size: " + originalLength);
		filter.onWrite(document);
		System.out.println("Compressed size: " + document.getObject().length);
		filter.onRead(document);
		System.out.println("Decompressed size: " + document.getObject().length);
		assertEquals(JSON, new String(document.getObject()));
		assertEquals(originalLength, document.getObject().length);
	}
}
