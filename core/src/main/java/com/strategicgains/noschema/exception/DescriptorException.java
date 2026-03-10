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

public class DescriptorException
extends ConfigurationException
{
	private static final long serialVersionUID = -6176939498695981994L;

	public DescriptorException() {
		super();
	}

	public DescriptorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DescriptorException(String message, Throwable cause) {
		super(message, cause);
	}

	public DescriptorException(String message) {
		super(message);
	}

	public DescriptorException(Throwable cause) {
		super(cause);
	}
}
