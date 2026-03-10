/*
    Copyright 2024-2026, Strategic Gains, Inc.

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
package com.strategicgains.noschema.exception;

/**
 * @author tfredrich
 * @since 2 Sept 2016
 */
public class KeyDefinitionException
extends RuntimeException
{
	private static final long serialVersionUID = 2488298881183539211L;

	public KeyDefinitionException()
	{
		super();
	}

	public KeyDefinitionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public KeyDefinitionException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public KeyDefinitionException(String message)
	{
		super(message);
	}

	public KeyDefinitionException(Throwable cause)
	{
		super(cause);
	}
}
