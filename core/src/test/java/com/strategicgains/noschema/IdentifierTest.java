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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IdentifierTest
{
	@Test
	public void shouldBeEqual()
	{
		assertTrue(new Identifier("key1", "key2", "key3").equals(new Identifier("key1", "key2", "key3")));
		assertTrue(new Identifier("key1").equals(new Identifier("key1")));
	}

	@Test
	public void shouldBeNotEqual()
	{
		assertFalse(new Identifier("key1", "key2", "key2").equals(new Identifier("key1", "key2", "key3")));
		assertFalse(new Identifier("key0").equals(new Identifier("key1")));
	}

	@Test
	public void shouldCompareEqual()
	{
		assertEquals(0, new Identifier("key1", "key2", "key3").compareTo(new Identifier("key1", "key2", "key3")));
		assertEquals(0, new Identifier("key1").compareTo(new Identifier("key1")));
	}

	@Test
	public void shouldCompareLessThan()
	{
		assertEquals(-1, new Identifier("key1", "key2", "key2").compareTo(new Identifier("key1", "key2", "key3")));
		assertEquals(-1, new Identifier("key0").compareTo(new Identifier("key1")));
	}

	@Test
	public void shouldCompareGreaterThan()
	{
		assertEquals(1, new Identifier("key1", "key2", "key4").compareTo(new Identifier("key1", "key2", "key3")));
		assertEquals(1, new Identifier("key2").compareTo(new Identifier("key1")));
	}
}
