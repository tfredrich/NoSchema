package com.strategicgains.noschema.cassandra;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.strategicgains.noschema.cassandra.document.CassandraDocumentFactory;
import com.strategicgains.noschema.cassandra.key.KeyDefinitionParser;
import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.exception.InvalidIdentifierException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public class DocumentFactoryTimer
{
	private static final String FLOWER_NAME = "rose";
	private static final long UPDATED_AT = 1648598130233L;
	private static final long CREATED_AT = 1648598130248L;
	private static final String ACCOUNT_ID = "a87d3bff-6997-4739-ab4e-ded0cc85700f";
	private static final String FLOWER_ID = "8dbac965-a1c8-4ad6-a043-5f5a9a5ee8c0";
	private static final List<String> COLORS = Arrays.asList("red", "white", "pink", "yellow");
	private static final int SERIALIZATION_COUNT = 500000;

	private CassandraDocumentFactory<Flower> factory;
	private long startedAt;
	private long endedAt;
	private int iterations;

	public static void main(String[] args)
	throws KeyDefinitionException, InvalidIdentifierException, IOException
	{
		DocumentFactoryTimer timer = new DocumentFactoryTimer();
		Flower flower = timer.setup();
		System.out.println("Ready! (hit any key to start)");
		System.in.read();
		System.out.println("Running...");
		timer.start();
		timer.run(flower, SERIALIZATION_COUNT);
		timer.stop();
		System.out.println(timer.details());
	}

	private Flower setup()
	throws KeyDefinitionException
	{
		factory  = new CassandraDocumentFactory<>(KeyDefinitionParser.parse("account.id as account_id:UUID, name:text"));
		Flower flower = new Flower(UUID.fromString(FLOWER_ID), FLOWER_NAME, true, 3.25f, COLORS);
		flower.setAccountId(UUID.fromString(ACCOUNT_ID));
		flower.setCreatedAt(new Date(CREATED_AT));
		flower.setUpdatedAt(new Date(UPDATED_AT));
		return flower;
	}

	private void start()
	{
		this.startedAt = System.currentTimeMillis();
	}

	private void run(Flower flower, int iterations)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		this.iterations = iterations;

		for (int i = 0; i < iterations; i++)
		{
			Document document = factory.asDocument(flower);
			factory.asPojo(document);
		}
	}

	private void stop()
	{
		this.endedAt = System.currentTimeMillis();
	}

	private String details()
	{
		if (startedAt > 0 && endedAt > 0 && iterations > 0)
		{
			long totalTime = endedAt - startedAt;
			double avgTime = (double) totalTime / (double) iterations;
			return String.format("Total Time (ms): %d, Mean (ms): %.5f (%.2f micros)", totalTime, avgTime, avgTime * 1000.0);
		}

		return "ERROR: Invalid configuration";
	}
}
