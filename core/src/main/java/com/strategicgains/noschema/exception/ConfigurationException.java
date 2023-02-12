package com.strategicgains.noschema.exception;

public class ConfigurationException
extends RuntimeException
{
	private static final long serialVersionUID = 2933571951701929089L;

	public ConfigurationException()
	{
	}

	public ConfigurationException(String message)
	{
		super(message);
	}

	public ConfigurationException(Throwable cause)
	{
		super(cause);
	}

	public ConfigurationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
