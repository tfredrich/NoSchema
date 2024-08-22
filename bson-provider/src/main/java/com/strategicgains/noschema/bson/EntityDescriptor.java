package com.strategicgains.noschema.bson;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import com.strategicgains.noschema.exception.DescriptorException;

/**
 * Describes the fields and their associated BSON codecs for a given entity.
 * 
 * @author tfredrich
 */
public class EntityDescriptor
{
	private static final int IGNORED_FIELD_MODIFIERS = Modifier.FINAL | Modifier.STATIC | Modifier.TRANSIENT | Modifier.VOLATILE;

	private Class<?> describedClass;
	private Map<String, FieldDescriptor> fields = new LinkedHashMap<>();

	public EntityDescriptor(Class<?> described)
	{
		super();
		this.describedClass = described;
	}

	public EntityDescriptor add(Field field)
	{
		return add(new FieldDescriptor(field, null));
	}

	public EntityDescriptor add(Field field, Codec<? super Object> codec)
	{
		return add(new FieldDescriptor(field, codec));
	}

	public EntityDescriptor add(FieldDescriptor field)
	{
		fields.put(field.getName(), field);
		return this;
	}

	public FieldDescriptor getField(String name)
	{
		return fields.get(name);
	}

	public Class<?> getDescribedClass()
	{
		return describedClass;
	}

	public Stream<FieldDescriptor> fields()
	{
		return fields.values().stream();
	}

	public String toString()
	{
		return String.format("class=%s, fields=%d", describedClass.getSimpleName(), fields.size());
	}

	public static EntityDescriptor from(Object entity, CodecRegistry registry)
	{
		return buildEntityDescriptor(entity, registry);
	}

	@SuppressWarnings("unchecked")
	private static EntityDescriptor buildEntityDescriptor(Object entity, CodecRegistry registry)
	{
		EntityDescriptor descriptor = new EntityDescriptor(entity.getClass());
		List<Field> fields = buildFieldHierarchy(entity);
		fields.forEach(f -> {
			try
			{
				if (shouldInclude(f))
				{
					Codec<? super Object> codec = null;
					Type type = f.getGenericType();

					if (type instanceof TypeVariable)
					{
						Object value = f.get(entity);
						codec = (Codec<? super Object>) registry.get(value.getClass());
					}
					else if (!isPrimitive(f))
					{
						codec = (Codec<? super Object>) registry.get(f.getType());
					}

					descriptor.add(f, codec);
				}
			}
			catch (CodecConfigurationException e)
			{
				FieldDescriptor fieldDescriptor = new FieldDescriptor(f);
				fieldDescriptor.setReference(buildReferencedDescriptor(entity, f, registry));
				descriptor.add(fieldDescriptor);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		return descriptor;
	}

	private static boolean isPrimitive(Field f)
	{
		return f.getType().isPrimitive();
	}

	private static boolean shouldInclude(Field field)
	{
		if ((field.getModifiers() & IGNORED_FIELD_MODIFIERS) == 0) return true;

		return (!field.isAnnotationPresent(BsonIgnore.class));
	}

	private static EntityDescriptor buildReferencedDescriptor(Object entity, Field field, CodecRegistry registry)
	{
		try
		{
			return buildEntityDescriptor(field.get(entity), registry);
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			throw new DescriptorException(e);
		}
	}

	private static List<Field> buildFieldHierarchy(Object object)
	{
		List<Field> fields = new ArrayList<>();
		Class<?> superClass = object.getClass();

		while(superClass != null)
		{
			Stream.of(superClass.getDeclaredFields()).filter(f -> {
                int mod = f.getModifiers();
                return (!Modifier.isAbstract(mod) && !Modifier.isStatic(mod) && !Modifier.isTransient(mod) && !Modifier.isFinal(mod));
            }).forEach(f -> {
                f.setAccessible(true);
                fields.add(f);
            });
			superClass = superClass.getSuperclass();
		}

		return fields;
	}
}
