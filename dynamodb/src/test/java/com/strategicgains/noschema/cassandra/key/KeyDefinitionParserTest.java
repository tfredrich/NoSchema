package com.strategicgains.noschema.cassandra.key;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.strategicgains.noschema.dynamodb.key.KeyDefinition;
import com.strategicgains.noschema.dynamodb.key.KeyDefinitionParser;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public class KeyDefinitionParserTest
{
	@Test
	public void shouldParseSimple()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("alpha:uuid");
		assertTrue(kd.isValid());
		assertEquals("alpha uuid", kd.asColumns());
		assertEquals("primary key (alpha)", kd.asPrimaryKey());
	}

	@Test(expected=KeyDefinitionException.class)
	public void shouldThrowWithSortOnPartitionKey()
	throws KeyDefinitionException
	{
		KeyDefinitionParser.parse("-alpha:uuid");
	}

	@Test(expected=KeyDefinitionException.class)
	public void shouldThrowWithInvalidPartitionKey()
	throws KeyDefinitionException
	{
		KeyDefinitionParser.parse("9alpha:uuid");
	}

	@Test
	public void shouldParsePartitionKey()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("alpha:uuid, beta:text");
		assertTrue(kd.isValid());
		assertEquals("alpha uuid,beta text", kd.asColumns());
		assertEquals("primary key (alpha,beta)", kd.asPrimaryKey());
		assertEquals("", kd.asClusteringKey());
	}

	@Test
	public void shouldParseComplex()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("((alpha:uuid, beta:text), -chi:timestamp, +delta:int)");
		assertTrue(kd.isValid());
		assertEquals("alpha uuid,beta text,chi timestamp,delta int", kd.asColumns());
		assertEquals("primary key ((alpha,beta),chi,delta)", kd.asPrimaryKey());
		assertEquals("with clustering order by (chi DESC,delta ASC)", kd.asClusteringKey());
	}

	@Test
	public void shouldParseComplexWithOptionalParens()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("(alpha:uuid, beta:text), -chi:timestamp, +delta:int");
		assertTrue(kd.isValid());
		assertEquals("alpha uuid,beta text,chi timestamp,delta int", kd.asColumns());
		assertEquals("primary key ((alpha,beta),chi,delta)", kd.asPrimaryKey());
		assertEquals("with clustering order by (chi DESC,delta ASC)", kd.asClusteringKey());
	}

	@Test
	public void shouldParseComplexWithoutOptionalSort()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("(alpha:uuid, beta:text), chi:timestamp, delta:int");
		assertTrue(kd.isValid());
		assertEquals("alpha uuid,beta text,chi timestamp,delta int", kd.asColumns());
		assertEquals("primary key ((alpha,beta),chi,delta)", kd.asPrimaryKey());
		assertEquals("", kd.asClusteringKey());
	}

	@Test
	public void shouldParseComplexWithoutOptionalComma()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("((alpha:uuid, beta:text) -chi:timestamp, +delta:int)");
		assertTrue(kd.isValid());
		assertEquals("alpha uuid,beta text,chi timestamp,delta int", kd.asColumns());
		assertEquals("primary key ((alpha,beta),chi,delta)", kd.asPrimaryKey());
		assertEquals("with clustering order by (chi DESC,delta ASC)", kd.asClusteringKey());
	}

	@Test
	public void shouldParseComplexWithoutOptionalCommaAndParens()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("(alpha:uuid, beta:text) -chi:timestamp, +delta:int");
		assertTrue(kd.isValid());
		assertEquals("alpha uuid,beta text,chi timestamp,delta int", kd.asColumns());
		assertEquals("primary key ((alpha,beta),chi,delta)", kd.asPrimaryKey());
		assertEquals("with clustering order by (chi DESC,delta ASC)", kd.asClusteringKey());
	}

	@Test
	public void shouldParseComplexWithPropertyMapping()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("(account.id as alpha:uuid, beta:text) -chi:timestamp, +foo.bar as delta:int");
		assertTrue(kd.isValid());
		assertEquals("alpha uuid,beta text,chi timestamp,delta int", kd.asColumns());
		assertEquals("primary key ((alpha,beta),chi,delta)", kd.asPrimaryKey());
		assertEquals("with clustering order by (chi DESC,delta ASC)", kd.asClusteringKey());
	}

	@Test
	public void shouldTollerateParenDepth()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("(((alpha:uuid, beta:text)), -chi:timestamp, +delta:int)");
		assertTrue(kd.isValid());
		assertEquals("alpha uuid,beta text,chi timestamp,delta int", kd.asColumns());
		assertEquals("primary key ((alpha,beta),chi,delta)", kd.asPrimaryKey());
		assertEquals("with clustering order by (chi DESC,delta ASC)", kd.asClusteringKey());
	}

	@Test(expected=KeyDefinitionException.class)
	public void shouldThrowOnUnbalancedParens()
	throws KeyDefinitionException
	{
		KeyDefinitionParser.parse("((alpha:uuid, beta:text), -chi:timestamp, +delta:int");
	}

	@Test(expected=KeyDefinitionException.class)
	public void shouldThrowOnInvalidChar()
	throws KeyDefinitionException
	{
		KeyDefinitionParser.parse("((alpha:uuid, beta:text), %chi:timestamp, +delta:int)");
	}

	@Test(expected=KeyDefinitionException.class)
	public void shouldThrowOnInvalidSort()
	throws KeyDefinitionException
	{
		KeyDefinitionParser.parse("((alpha:uuid, beta:text), 9chi:timestamp, +delta:int)");
	}

	@Test(expected=KeyDefinitionException.class)
	public void shouldThrowOnInvalidPartitionKeyType()
	throws KeyDefinitionException
	{
		KeyDefinitionParser.parse("((alpha:uuid, beta:data), -chi:timestamp, +delta:int)");
	}

	@Test(expected=KeyDefinitionException.class)
	public void shouldThrowOnInvalidClusterKeyType()
	throws KeyDefinitionException
	{
		KeyDefinitionParser.parse("((alpha:uuid, beta:text), -chi:data, +delta:int)");
	}
}
