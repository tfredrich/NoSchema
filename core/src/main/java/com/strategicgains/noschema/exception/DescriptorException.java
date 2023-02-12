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
