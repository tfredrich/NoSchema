package com.strategicgains.noschema.cassandra.key;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.strategicgains.noschema.exception.KeyDefinitionException;

public class KeyDefinitionParserTest
{
	@Test
	public void shouldParseSimple()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("alpha:uuid");
		makeAssertions(kd,
			"alpha uuid",
			"primary key (alpha)",
			"",
			false);
	}

	@Test
	public void shouldParseSimpleWithUnique()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("alpha:uuid unique");
		makeAssertions(kd,
			"alpha uuid",
			"primary key (alpha)",
			"",
			true);
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
		makeAssertions(kd,
			"alpha uuid,beta text",
			"primary key (alpha,beta)",
			"",
			false);
	}

	@Test
	public void shouldParsePartitionKeyWithUnique()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("alpha:uuid, beta:text unique");
		makeAssertions(kd,
			"alpha uuid,beta text",
			"primary key (alpha,beta)",
			"",
			true);
	}

	@Test
	public void shouldParseComplex()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("((alpha:uuid, beta:text), -chi:timestamp, +delta:int)");
		makeAssertions(kd,
			"alpha uuid,beta text,chi timestamp,delta int",
			"primary key ((alpha,beta),chi,delta)",
			"with clustering order by (chi DESC,delta ASC)",
			false);
	}

	@Test
	public void shouldParseComplexWithOptionalParens()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("(alpha:uuid, beta:text), -chi:timestamp, +delta:int");
		makeAssertions(kd,
			"alpha uuid,beta text,chi timestamp,delta int",
			"primary key ((alpha,beta),chi,delta)",
			"with clustering order by (chi DESC,delta ASC)",
			false);
	}

	@Test
	public void shouldParseComplexWithOptionalParensWithUnique()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("(alpha:uuid, beta:text), -chi:timestamp, +delta:int unique");
		makeAssertions(kd,
			"alpha uuid,beta text,chi timestamp,delta int",
			"primary key ((alpha,beta),chi,delta)",
			"with clustering order by (chi DESC,delta ASC)",
			true);
	}

	@Test
	public void shouldParseComplexWithoutOptionalSort()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("(alpha:uuid, beta:text), chi:timestamp, delta:int");
		makeAssertions(kd,
			"alpha uuid,beta text,chi timestamp,delta int",
			"primary key ((alpha,beta),chi,delta)",
			"",
			false);
	}

	@Test
	public void shouldParseComplexWithoutOptionalSortWithUnique()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("(alpha:uuid, beta:text), chi:timestamp, delta:int unique");
		makeAssertions(kd,
			"alpha uuid,beta text,chi timestamp,delta int",
			"primary key ((alpha,beta),chi,delta)",
			"",
			true);
	}

	@Test
	public void shouldParseComplexWithoutOptionalComma()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("((alpha:uuid, beta:text) -chi:timestamp, +delta:int)");
		makeAssertions(kd,
			"alpha uuid,beta text,chi timestamp,delta int",
			"primary key ((alpha,beta),chi,delta)",
			"with clustering order by (chi DESC,delta ASC)",
			false);
	}

	@Test
	public void shouldParseComplexWithoutOptionalCommaWithUnique()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("((alpha:uuid, beta:text) -chi:timestamp, +delta:int) unique");
		makeAssertions(kd,
			"alpha uuid,beta text,chi timestamp,delta int",
			"primary key ((alpha,beta),chi,delta)",
			"with clustering order by (chi DESC,delta ASC)",
			true);
	}

	@Test
	public void shouldParseComplexWithoutOptionalCommaAndParens()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("(alpha:uuid, beta:text) -chi:timestamp, +delta:int");
		makeAssertions(kd,
			"alpha uuid,beta text,chi timestamp,delta int",
			"primary key ((alpha,beta),chi,delta)",
			"with clustering order by (chi DESC,delta ASC)",
			false);
	}

	@Test
	public void shouldParseComplexWithoutOptionalCommaAndParensWithUnique()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("(alpha:uuid, beta:text) -chi:timestamp, +delta:int unique");
		makeAssertions(kd,
			"alpha uuid,beta text,chi timestamp,delta int",
			"primary key ((alpha,beta),chi,delta)",
			"with clustering order by (chi DESC,delta ASC)",
			false);
	}

	@Test
	public void shouldParseComplexWithPropertyMapping()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("(account.id as alpha:uuid, beta:text) -chi:timestamp, +foo.bar as delta:int");
		makeAssertions(kd,
			"alpha uuid,beta text,chi timestamp,delta int",
			"primary key ((alpha,beta),chi,delta)",
			"with clustering order by (chi DESC,delta ASC)",
			false);
	}

	@Test
	public void shouldParseComplexWithPropertyMappingWithUnique()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("(account.id as alpha:uuid, beta:text) -chi:timestamp, +foo.bar as delta:int unique");
		makeAssertions(kd,
			"alpha uuid,beta text,chi timestamp,delta int",
			"primary key ((alpha,beta),chi,delta)",
			"with clustering order by (chi DESC,delta ASC)",
			true);
	}

	@Test
	public void shouldTollerateParenDepth()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse("(((alpha:uuid, beta:text)), -chi:timestamp, +delta:int)");
		makeAssertions(kd,
			"alpha uuid,beta text,chi timestamp,delta int",
			"primary key ((alpha,beta),chi,delta)",
			"with clustering order by (chi DESC,delta ASC)",
			false);
	}

	@Test
	public void shouldTollerateSpaces()
	throws KeyDefinitionException
	{
		KeyDefinition kd = KeyDefinitionParser.parse(" ( ( ( alpha:uuid, beta:text ) ) , -chi:timestamp , +delta:int ) ");
		makeAssertions(kd,
			"alpha uuid,beta text,chi timestamp,delta int",
			"primary key ((alpha,beta),chi,delta)",
			"with clustering order by (chi DESC,delta ASC)",
			false);
	}

	@Test(expected=KeyDefinitionException.class)
	public void shouldThrowOnUnbalancedParens()
	throws KeyDefinitionException
	{
		KeyDefinitionParser.parse("((alpha:uuid, beta:text), -chi:timestamp, +delta:int");
	}

	@Test(expected=KeyDefinitionException.class)
	public void shouldThrowOnMisplacedUniqueWithoutParens()
	throws KeyDefinitionException
	{
		KeyDefinitionParser.parse("(alpha:uuid, beta:text) unique -chi:timestamp, +delta:int");
	}

	@Test(expected=KeyDefinitionException.class)
	public void shouldThrowOnMisplacedUnique()
	throws KeyDefinitionException
	{
		KeyDefinitionParser.parse("unique alpha:uuid");
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
		KeyDefinitionParser.parse("((alpha:uuid, beta:invalid), -chi:timestamp, +delta:int)");
	}

	@Test(expected=KeyDefinitionException.class)
	public void shouldThrowOnInvalidClusterKeyType()
	throws KeyDefinitionException
	{
		KeyDefinitionParser.parse("((alpha:uuid, beta:text), -chi:invalid, +delta:int)");
	}

	private void makeAssertions(KeyDefinition kd, String columns, String primaryKey, String clusterKey, boolean unique)
	{
		assertTrue(kd.isValid());
		assertEquals(columns, kd.asColumns());
		assertEquals(primaryKey, kd.asPrimaryKey());
		assertEquals(clusterKey, kd.asClusteringKey());
		assertEquals(unique, kd.isUnique());
	}
}
