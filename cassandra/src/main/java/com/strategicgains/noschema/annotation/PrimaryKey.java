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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the primary-table key definition for an entity class.
 * Overrides the @Id annotation on any property/field, and is used in these cases:
 *  - when the primary key is inherited from a parent class, and the parent class is not annotated with @Id.
 *  - the primary key is not a single property/field, but rather a composite key derived from multiple properties/fields.
 * 
 * @author Todd Fredrich
 * @since 14 Apr 2026
 * @see KeyDefinition
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PrimaryKey
{
	/**
	 * The complete NoSchema key-definition DSL string for the primary table.
	 */
	String value();
}
