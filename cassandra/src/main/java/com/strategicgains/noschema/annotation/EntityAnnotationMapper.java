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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.cassandra.PrimaryTable;
import com.strategicgains.noschema.cassandra.key.DataTypes;
import com.strategicgains.noschema.cassandra.unitofwork.CommitType;
import com.strategicgains.noschema.exception.ConfigurationException;
import com.strategicgains.noschema.exception.KeyDefinitionException;

/**
 * Maps entity annotations into Cassandra table metadata used by NoSchema.
 * 
 * @author Todd Fredrich
 * @since 14 Apr 2026
 */
public final class EntityAnnotationMapper
{
	private EntityAnnotationMapper()
	{
		// Prevents instantiation.
	}

	public static <T extends Identifiable> PrimaryTable toPrimaryTable(Class<T> entityType, String keyspace)
	throws KeyDefinitionException
	{
		Objects.requireNonNull(entityType, "entityType");
		Objects.requireNonNull(keyspace, "keyspace");

		Entity entity = requireEntityAnnotation(entityType);
		String tableName = resolveTableName(entity, entityType);
		String keyDefinition = resolvePrimaryKeyDefinition(entityType);
		PrimaryTable table = new PrimaryTable(keyspace, tableName, keyDefinition);
		List<View> views = getViews(entityType);
		validateViewDefinitions(entityType, views);
		views.forEach(v -> addView(v, table));
		return table;
	}

	public static CommitType commitType(Class<?> entityType)
	{
		Objects.requireNonNull(entityType, "entityType");
		return requireEntityAnnotation(entityType).commitType();
	}

	private static void addView(View view, PrimaryTable table)
	{
		try
		{
			table.withView(view.name(), view.keyDefinition());
		}
		catch (KeyDefinitionException e)
		{
			throw new ConfigurationException(String.format("Invalid @View keyDefinition for view '%s': %s", view.name(), view.keyDefinition()), e);
		}
	}

	private static Entity requireEntityAnnotation(Class<?> entityType)
	{
		Entity entity = entityType.getAnnotation(Entity.class);
		if (entity != null) return entity;
		throw new ConfigurationException(String.format("Missing @Entity annotation on class: %s", entityType.getName()));
	}

	private static Field requireIdField(Class<?> entityType)
	{
		List<Field> idFields = findIdFields(entityType);
		if (idFields.isEmpty())
		{
			throw new ConfigurationException(String.format("Missing @Id field on @Entity class: %s", entityType.getName()));
		}

		if (idFields.size() > 1)
		{
			throw new ConfigurationException(String.format("Multiple @Id fields found on @Entity class: %s", entityType.getName()));
		}

		return idFields.get(0);
	}

	private static String resolvePrimaryKeyDefinition(Class<?> entityType)
	{
		PrimaryKey primaryKey = entityType.getAnnotation(PrimaryKey.class);
		if (primaryKey != null)
		{
			if (primaryKey.value() == null || primaryKey.value().isBlank())
			{
				throw new ConfigurationException(String.format("Missing @PrimaryKey.keyDefinition value on class: %s", entityType.getName()));
			}

			return primaryKey.value();
		}

		Field idField = requireIdField(entityType);
		return String.format("%s:%s unique", idField.getName(), toDataType(idField.getType()).cassandraType());
	}

	private static List<Field> findIdFields(Class<?> entityType)
	{
		List<Field> idFields = new ArrayList<>();
		Class<?> currentClass = entityType;

		while (currentClass != null)
		{
			for (Field field : currentClass.getDeclaredFields())
			{
				if (field.isAnnotationPresent(Id.class))
				{
					idFields.add(field);
				}
			}

			currentClass = currentClass.getSuperclass();
		}

		return idFields;
	}

	private static String resolveTableName(Entity entity, Class<?> entityType)
	{
		if (entity.value().isBlank()) return entityType.getSimpleName();
		return entity.value();
	}

	private static List<View> getViews(Class<?> entityType)
	{
		List<View> views = new ArrayList<>();
		View view = entityType.getAnnotation(View.class);
		if (view != null) views.add(view);

		Views groupedViews = entityType.getAnnotation(Views.class);
		if (groupedViews != null) views.addAll(Arrays.asList(groupedViews.value()));
		return views;
	}

	private static void validateViewDefinitions(Class<?> entityType, List<View> views)
	{
		Set<String> viewNames = new HashSet<>();
		for (View view : views)
		{
			if (view.name() == null || view.name().isBlank())
			{
				throw new ConfigurationException(String.format("Missing @View.name value on class: %s", entityType.getName()));
			}

			if (view.keyDefinition() == null || view.keyDefinition().isBlank())
			{
				throw new ConfigurationException(String.format("Missing @View.keyDefinition value for view '%s' on class: %s", view.name(), entityType.getName()));
			}

			if (!viewNames.add(view.name()))
			{
				throw new ConfigurationException(String.format("Duplicate @View name '%s' on class: %s", view.name(), entityType.getName()));
			}
		}
	}

	private static DataTypes toDataType(Class<?> fieldType)
	{
		try
		{
			return DataTypes.fromClass(fieldType);
		}
		catch (IllegalStateException e)
		{
			throw new ConfigurationException(String.format("Unsupported @Id field type '%s'.", fieldType.getName()));
		}
	}
}
