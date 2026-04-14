package com.strategicgains.noschema.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.strategicgains.noschema.cassandra.unitofwork.CommitType;

/**
 * Identifies a class as an entity. The value is the table name. If not
 * specified, the simple class name is used to determine the table name.
 * 
 * @author Todd Fredrich
 * @since 14 Apr 2026
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity
{
	String value() default ""; // Table name. Default is to use the simple class name as the table name.
	CommitType commitType() default CommitType.ASYNC; // Default is to commit asynchronously.
}
