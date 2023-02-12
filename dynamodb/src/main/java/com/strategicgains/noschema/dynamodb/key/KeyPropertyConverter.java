/*
    Copyright 2016, Strategic Gains, Inc.

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
package com.strategicgains.noschema.dynamodb.key;

import com.datastax.oss.driver.api.core.cql.Row;

/**
 * @author toddf
 * @since Oct 7, 2016
 */
public class KeyPropertyConverter
{
	public static final Object marshal(String property, DataTypes type, Row row)
    {
		switch(type)
		{
			case BIGINT: return row.getLong(property);
			case DECIMAL: return row.getBigDecimal(property);
			case DOUBLE: return row.getDouble(property);
			case FLOAT: return row.getFloat(property);
			case INTEGER: return row.getInt(property);
			case TEXT: return row.getString(property);
			case TIMESTAMP: return row.getInstant(property);
			case TIMEUUID:
			case UUID:  return row.getUuid(property);
			default: throw new UnsupportedOperationException("Conversion of property type: " + type.toString());
		}
    }
}
