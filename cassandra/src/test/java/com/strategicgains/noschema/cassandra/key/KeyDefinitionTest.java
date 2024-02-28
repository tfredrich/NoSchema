package com.strategicgains.noschema.cassandra.key;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.Flower;
import com.strategicgains.noschema.cassandra.key.ClusteringKeyComponent.Ordering;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public class KeyDefinitionTest
{
	@Test
	public void shouldHandleSimple()
	throws KeyDefinitionException
	{
		KeyDefinition kd = new KeyDefinition();
		kd.addPartitionKey(new KeyComponent("alpha", DataTypes.UUID));
		assertTrue(kd.isValid());
		assertEquals("alpha uuid", kd.asColumns());
		assertEquals("primary key (alpha)", kd.asPrimaryKey());
	}

	@Test
	public void shouldHandlePartitionKey()
	throws KeyDefinitionException
	{
		KeyDefinition kd = new KeyDefinition();
		kd.addPartitionKey(new KeyComponent("alpha", DataTypes.UUID))
			.addPartitionKey(new KeyComponent("beta", DataTypes.TEXT));
		assertTrue(kd.isValid());
		assertEquals("alpha uuid,beta text", kd.asColumns());
		assertEquals("primary key (alpha,beta)", kd.asPrimaryKey());
		assertEquals("", kd.asClusteringKey());
		assertTrue(kd.isValid());
	}

	@Test
	public void shouldHandleComplex()
	throws KeyDefinitionException
	{
		KeyDefinition kd = new KeyDefinition();
		kd.addPartitionKey(new KeyComponent("alpha", DataTypes.UUID))
			.addPartitionKey(new KeyComponent("beta", DataTypes.TEXT))
			.addClusteringKey(new ClusteringKeyComponent("chi", DataTypes.TIMESTAMP, Ordering.DESC))
			.addClusteringKey(new ClusteringKeyComponent("delta", DataTypes.INTEGER, Ordering.ASC));
		assertEquals("alpha uuid,beta text,chi timestamp,delta int", kd.asColumns());
		assertEquals("primary key ((alpha,beta),chi,delta)", kd.asPrimaryKey());
		assertEquals("with clustering order by (chi DESC,delta ASC)", kd.asClusteringKey());
		assertTrue(kd.isValid());
	}

	@Test
	public void shouldHandleComplexWithDefaultSort()
	throws KeyDefinitionException
	{
		KeyDefinition kd = new KeyDefinition();
		kd.addPartitionKey(new KeyComponent("alpha", DataTypes.UUID))
			.addPartitionKey(new KeyComponent("beta", DataTypes.TEXT))
			.addClusteringKey(new ClusteringKeyComponent("chi", DataTypes.TIMESTAMP, Ordering.ASC))
			.addClusteringKey(new ClusteringKeyComponent("delta", DataTypes.INTEGER, Ordering.ASC));
		assertEquals("alpha uuid,beta text,chi timestamp,delta int", kd.asColumns());
		assertEquals("primary key ((alpha,beta),chi,delta)", kd.asPrimaryKey());
		assertTrue(kd.isValid());
	}

	@Test
	public void shouldThrowOnMissingIdentifierProperties()
	throws KeyDefinitionException, InvalidIdentifierException
	{
		KeyDefinition kd = new KeyDefinition();
		kd.addPartitionKey(new KeyComponent("account_id", "account.id", DataTypes.TEXT))
			.addPartitionKey(new KeyComponent("name", DataTypes.INTEGER))
			.addClusteringKey(new ClusteringKeyComponent("height", DataTypes.TEXT, Ordering.ASC))
			.addClusteringKey(new ClusteringKeyComponent("createdAt", DataTypes.DECIMAL, Ordering.ASC));


		thrown.expect(InvalidIdentifierException.class);
	    thrown.expectMessage("Missing properties: account.id, name, height");
	    kd.identifier(new Flower());
	}

	@Test
	public void shouldReturnIdentifier()
	throws KeyDefinitionException, InvalidIdentifierException
	{
		KeyDefinition kd = new KeyDefinition();
		kd.addPartitionKey(new KeyComponent("account_id", "account.id", DataTypes.TEXT))
			.addPartitionKey(new KeyComponent("name", DataTypes.TEXT))
			.addClusteringKey(new ClusteringKeyComponent("height", DataTypes.FLOAT, Ordering.ASC))
			.addClusteringKey(new ClusteringKeyComponent("createdAt", DataTypes.TIMESTAMP, Ordering.ASC));

		UUID oid = UUID.fromString("8dbac965-a1c8-4ad6-a043-5f5a9a5ee8c0");
		UUID accountId = UUID.fromString("a87d3bff-6997-4739-ab4e-ded0cc85700f");
		Date createdAt = new Date(1648598130248L);
		Date updatedAt = new Date(1648598130233L);
		List<String> colors = Arrays.asList("red", "white", "pink", "yellow");
		Flower flower = new Flower(oid, "rose", true, 3.25f, colors);
		flower.setAccountId(accountId);
		flower.setCreatedAt(createdAt);
		flower.setUpdatedAt(updatedAt);		

		Identifier id = kd.identifier(flower);
		List<Object> components = id.components();
		assertEquals(4, components.size());
		assertEquals(accountId, components.get(0));
		assertEquals("rose", components.get(1));
		assertEquals(3.25f, components.get(2));
		assertEquals(createdAt, components.get(3));
	}

	@Test
	public void shouldReturnAccountIdIdentifier()
	throws KeyDefinitionException, InvalidIdentifierException
	{
		KeyDefinition kd = new KeyDefinition();
		kd.addPartitionKey(new KeyComponent("account_id", "account.id", DataTypes.UUID));
		UUID accountId = UUID.fromString("a87d3bff-6997-4739-ab4e-ded0cc85700f");
		Flower flower = new Flower();
		flower.setAccountId(accountId);
		Identifier id = kd.identifier(flower);
		List<Object> components = id.components();
		assertEquals(1, components.size());
		assertEquals(accountId, components.get(0));
	}

	@Test
	public void shouldReturnSmallIdentifier()
	throws KeyDefinitionException, InvalidIdentifierException
	{
		KeyDefinition kd = new KeyDefinition();
		kd.addPartitionKey(new KeyComponent("name", DataTypes.TEXT))
			.addClusteringKey(new ClusteringKeyComponent("height", DataTypes.DECIMAL, Ordering.ASC));
		Identifier id = kd.identifier(new Flower(null, "some", false, 3.14159f, null));
		List<Object> components = id.components();
		assertEquals(2, components.size());
		assertEquals("some", components.get(0));
		assertEquals(3.14159f, components.get(1));
	}

	@Test(expected=KeyDefinitionException.class)
	public void shouldThrowWithMissingClusteringProperty()
	throws KeyDefinitionException, InvalidIdentifierException
	{
		KeyDefinition kd = new KeyDefinition();
		kd.addPartitionKey(new KeyComponent("name", DataTypes.TEXT))
			.addPartitionKey(new KeyComponent("height", DataTypes.FLOAT))
			.addClusteringKey(new ClusteringKeyComponent("updatedAt", DataTypes.TIMESTAMP, Ordering.ASC))
			.addClusteringKey(new ClusteringKeyComponent("not_there", DataTypes.DECIMAL, Ordering.ASC));
		kd.identifier(new Flower());
	}

	@Test
	public void shouldThrowWithMissingPartitionField()
	throws KeyDefinitionException, InvalidIdentifierException
	{
		KeyDefinition kd = new KeyDefinition();
		kd.addPartitionKey(new KeyComponent("not_there", DataTypes.TEXT))
			.addPartitionKey(new KeyComponent("beta", DataTypes.INTEGER))
			.addClusteringKey(new ClusteringKeyComponent("chi", DataTypes.TEXT, Ordering.ASC))
			.addClusteringKey(new ClusteringKeyComponent("delta", DataTypes.DECIMAL, Ordering.ASC));

		thrown.expect(KeyDefinitionException.class);
	    thrown.expectMessage("Missing fields: not_there, beta, chi, delta");
	    kd.identifier(new Flower());
	}

	@Test
	public void shouldThrowWithMissingPartitionProperty()
	throws KeyDefinitionException, InvalidIdentifierException
	{
		KeyDefinition kd = new KeyDefinition();
		kd.addPartitionKey(new KeyComponent("id", DataTypes.UUID));

		thrown.expect(InvalidIdentifierException.class);
	    thrown.expectMessage("Missing properties: id");
	    Flower flower = new Flower();
	    flower.setId(null);
	    kd.identifier(flower);
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldContainMissingPropertiesInMessage()
	throws KeyDefinitionException, InvalidIdentifierException
	{
		KeyDefinition kd = new KeyDefinition();
		kd.addPartitionKey(new KeyComponent("not_there", DataTypes.TEXT))
			.addPartitionKey(new KeyComponent("id", DataTypes.UUID))
			.addPartitionKey(new KeyComponent("not_there_either", DataTypes.INTEGER))
			.addClusteringKey(new ClusteringKeyComponent("name", DataTypes.TEXT, Ordering.ASC))
			.addClusteringKey(new ClusteringKeyComponent("height", DataTypes.DECIMAL, Ordering.ASC));

		thrown.expect(KeyDefinitionException.class);
	    thrown.expectMessage("Missing fields: not_there, not_there_either");
	    kd.identifier(new Flower());
	}

	@Test
	public void shouldCreateIdentifier()
	throws KeyDefinitionException, InvalidIdentifierException
	{
		KeyDefinition defn = KeyDefinitionParser.parse("(account.id as account_id:uuid), name:text, id:uuid, createdAt as year:int");
		defn.component(3).extractor(k -> {
			if (k instanceof Date)
			{
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTime((Date) k);
				return cal.get(GregorianCalendar.YEAR);
			}
            return k;
        });

		UUID id = UUID.fromString("8dbac965-a1c8-4ad6-a043-5f5a9a5ee8c0");
		UUID accountId = UUID.fromString("a87d3bff-6997-4739-ab4e-ded0cc85700f");
		Date createdAt = new Date(1648598130248L);
		Date updatedAt = new Date(1648598130233L);
		List<String> colors = Arrays.asList("red", "white", "pink", "yellow");
		Flower flower = new Flower(id, "rose", true, 3.25f, colors);
		flower.setAccountId(accountId);
		flower.setCreatedAt(createdAt);
		flower.setUpdatedAt(updatedAt);		

		Identifier identifier = defn.identifier(flower);
		assertEquals(4, identifier.size());
		List<Object> components = identifier.components();
		assertEquals(accountId, components.get(0));
		assertEquals("rose", components.get(1));
		assertEquals(id, components.get(2));
		assertEquals(2022, components.get(3));
	}
}
