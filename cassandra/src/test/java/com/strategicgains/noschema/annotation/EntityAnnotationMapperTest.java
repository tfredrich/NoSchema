/*
    Copyright 2026, Strategic Gains, Inc.

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
package com.strategicgains.noschema.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.Identifier;
import com.strategicgains.noschema.cassandra.PrimaryTable;
import com.strategicgains.noschema.cassandra.unitofwork.CommitType;
import com.strategicgains.noschema.exception.ConfigurationException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

public class EntityAnnotationMapperTest
{
	@Test
	public void shouldMapAnnotatedEntityToPrimaryTable()
	throws KeyDefinitionException
	{
		PrimaryTable table = EntityAnnotationMapper.toPrimaryTable(AnnotatedEntity.class, "noschema_test");

		assertEquals("noschema_test", table.keyspace());
		assertEquals("entities", table.name());
		assertEquals(2, table.getViewCount());

		List<com.strategicgains.noschema.cassandra.View> views = table.views().toList();
		assertEquals("by_name", views.get(0).name());
		assertEquals("by_created_at", views.get(1).name());

		AnnotatedEntity entity = new AnnotatedEntity(UUID.randomUUID(), "orchid", 12345L);
		Identifier tableId = table.getIdentifier(entity);
		assertEquals(new Identifier(entity.id), tableId);

		Identifier byNameId = views.get(0).getIdentifier(entity);
		assertEquals(new Identifier(entity.name), byNameId);

		Identifier byCreatedAtId = views.get(1).getIdentifier(entity);
		assertEquals(new Identifier(entity.createdAt), byCreatedAtId);
	}

	@Test
	public void shouldUseDefaultTableNameAndCommitType()
	throws KeyDefinitionException
	{
		PrimaryTable table = EntityAnnotationMapper.toPrimaryTable(DefaultEntity.class, "noschema_test");
		assertEquals("DefaultEntity", table.name());
		assertEquals(CommitType.ASYNC, EntityAnnotationMapper.commitType(DefaultEntity.class));
	}

	@Test
	public void shouldReadConfiguredCommitType()
	{
		assertEquals(CommitType.LOGGED, EntityAnnotationMapper.commitType(AnnotatedEntity.class));
	}

	@Test
	public void shouldUseInheritedIdField()
	throws KeyDefinitionException
	{
		PrimaryTable table = EntityAnnotationMapper.toPrimaryTable(InheritedIdEntity.class, "noschema_test");
		InheritedIdEntity entity = new InheritedIdEntity(UUID.randomUUID());
		assertEquals(new Identifier(entity.id), table.getIdentifier(entity));
	}

	@Test
	public void shouldThrowWhenMissingEntityAnnotation()
	throws KeyDefinitionException
	{
		try
		{
			EntityAnnotationMapper.toPrimaryTable(NoEntityAnnotation.class, "noschema_test");
			fail("Expected ConfigurationException");
		}
		catch (ConfigurationException e)
		{
			assertNotNull(e.getMessage());
		}
	}

	@Test
	public void shouldThrowWhenMissingIdAnnotation()
	throws KeyDefinitionException
	{
		try
		{
			EntityAnnotationMapper.toPrimaryTable(MissingIdEntity.class, "noschema_test");
			fail("Expected ConfigurationException");
		}
		catch (ConfigurationException e)
		{
			assertNotNull(e.getMessage());
		}
	}

	@Test
	public void shouldThrowWhenMultipleIdAnnotations()
	throws KeyDefinitionException
	{
		try
		{
			EntityAnnotationMapper.toPrimaryTable(MultipleIdsEntity.class, "noschema_test");
			fail("Expected ConfigurationException");
		}
		catch (ConfigurationException e)
		{
			assertNotNull(e.getMessage());
		}
	}

	@Test
	public void shouldThrowWhenIdTypeUnsupported()
	throws KeyDefinitionException
	{
		try
		{
			EntityAnnotationMapper.toPrimaryTable(UnsupportedIdTypeEntity.class, "noschema_test");
			fail("Expected ConfigurationException");
		}
		catch (ConfigurationException e)
		{
			assertNotNull(e.getMessage());
		}
	}

	@Entity(commitType = CommitType.LOGGED, value = "entities")
	@Views({
		@View(name = "by_name", keyDefinition = "name:text"),
		@View(name = "by_created_at", keyDefinition = "createdAt:bigint")
	})
	private static class AnnotatedEntity
	implements Identifiable
	{
		@Id
		private UUID id;
		private String name;
		private Long createdAt;

		private AnnotatedEntity(UUID id, String name, Long createdAt)
		{
			this.id = id;
			this.name = name;
			this.createdAt = createdAt;
		}

		@Override
		public Identifier getIdentifier()
		{
			return new Identifier(id);
		}
	}

	@Entity
	private static class DefaultEntity
	implements Identifiable
	{
		@Id
		private UUID id;

		@Override
		public Identifier getIdentifier()
		{
			return new Identifier(id);
		}
	}

	private abstract static class BaseEntity
	implements Identifiable
	{
		@Id
		protected UUID id;

		@Override
		public Identifier getIdentifier()
		{
			return new Identifier(id);
		}
	}

	@Entity
	private static class InheritedIdEntity
	extends BaseEntity
	{
		private InheritedIdEntity(UUID id)
		{
			this.id = id;
		}
	}

	private static class NoEntityAnnotation
	implements Identifiable
	{
		@Id
		private UUID id;

		@Override
		public Identifier getIdentifier()
		{
			return new Identifier(id);
		}
	}

	@Entity
	private static class MissingIdEntity
	implements Identifiable
	{
		private UUID id;

		@Override
		public Identifier getIdentifier()
		{
			return new Identifier(id);
		}
	}

	@Entity
	private static class MultipleIdsEntity
	implements Identifiable
	{
		@Id
		private UUID id;
		@Id
		private String alternateId;

		@Override
		public Identifier getIdentifier()
		{
			return new Identifier(id, alternateId);
		}
	}

	@Entity
	private static class UnsupportedIdTypeEntity
	implements Identifiable
	{
		@Id
		private Boolean enabled;

		@Override
		public Identifier getIdentifier()
		{
			return new Identifier(enabled);
		}
	}
}
