package com.strategicgains.noschema.bson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.strategicgains.noschema.entity.Flower;
import com.strategicgains.noschema.entity.PrimitiveEntity;

public class EntityDescriptorTest
{
	@Test
	public void shouldCreateFlowerEntityDescriptor()
	{
		Flower flower = new Flower();
		flower.setUpdatedAt(null);
		flower.setCreatedAt(null);
		EntityDescriptor descriptor = EntityDescriptor.from(flower, BsonObjectCodec.NOSCHEMA_CODEC_REGISTRY);
		assertNotNull(descriptor);
		assertEquals(flower.getClass(), descriptor.getDescribedClass());
		assertEquals(8, descriptor.fields().count());
		assertEquals("StringCodec", descriptor.getField("name").getCodec().getClass().getSimpleName());
		assertEquals("CollectionCodec", descriptor.getField("colors").getCodec().getClass().getSimpleName());
		assertEquals("BooleanCodec", descriptor.getField("isBlooming").getCodec().getClass().getSimpleName());
		assertEquals("FloatCodec", descriptor.getField("height").getCodec().getClass().getSimpleName());
		assertEquals("DateCodec", descriptor.getField("createdAt").getCodec().getClass().getSimpleName());
		assertEquals("DateCodec", descriptor.getField("updatedAt").getCodec().getClass().getSimpleName());
		assertEquals("OverridableUuidRepresentationUuidCodec", descriptor.getField("id").getCodec().getClass().getSimpleName());

		EntityDescriptor reference = descriptor.getField("account").getReference();
		assertEquals(1, reference.fields().count());
		assertEquals("OverridableUuidRepresentationUuidCodec", reference.getField("id").getCodec().getClass().getSimpleName());
	}

	@Test
	public void shouldCreatePrimitiveEntityDescriptor()
    {
		PrimitiveEntity entity = new PrimitiveEntity();
        EntityDescriptor descriptor = EntityDescriptor.from(entity, BsonObjectCodec.NOSCHEMA_CODEC_REGISTRY);
		assertNotNull(descriptor);
		assertEquals(entity.getClass(), descriptor.getDescribedClass());
		assertEquals(13, descriptor.fields().count());
		System.out.println(String.format("%s: {", descriptor.getDescribedClass().getSimpleName()));

		descriptor.fields().forEach(d -> {
			if (d.isProperty())
			{
				System.out.println(String.format("\t%s: %s", d.getName(), d.getCodec().getClass().getSimpleName()));
			}
			else if (d.isGeneric())
			{
				System.out.println(String.format("\t%s: %s", d.getName(), "Generic"));
			}
			else
			{
				EntityDescriptor ref = d.getReference();
				System.out.println(String.format("\t%s: {", d.getName()));
				ref.fields().forEach(f -> {
                    System.out.println(String.format("\t\t%s: %s", f.getName(), f.getCodec().getClass().getSimpleName()));
				});
				System.out.println("\t}");
			}
		});
		System.out.println("}");
    }
}
