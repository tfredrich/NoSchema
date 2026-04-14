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
package com.strategicgains.noschema.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.strategicgains.noschema.Identifiable;
import com.strategicgains.noschema.annotation.EntityAnnotationMapper;
import com.strategicgains.noschema.document.ObjectCodec;
import com.strategicgains.noschema.exception.KeyDefinitionException;

/**
 * A CassandraRepository implementation configured entirely by entity annotations.
 * 
 * @param <T> the type of entity to store.
 * @author Todd Fredrich
 * @since 14 Apr 2026
 */
public class AnnotationRepository<T extends Identifiable>
extends CassandraRepository<T>
{
	public AnnotationRepository(CqlSession session, String keyspace, Class<T> entityType, ObjectCodec<T> codec)
	throws KeyDefinitionException
	{
		super(
			session,
			EntityAnnotationMapper.toPrimaryTable(entityType, keyspace),
			EntityAnnotationMapper.commitType(entityType),
			codec
		);
	}
}
