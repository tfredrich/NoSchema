package com.strategicgains.noschema.cassandra;

import com.strategicgains.noschema.exception.ConfigurationException;

public class InvalidTableNameException extends ConfigurationException
{
	private static final long serialVersionUID = -7883606740014460827L;

	public InvalidTableNameException()
	{
		super();
	}

	public InvalidTableNameException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidTableNameException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public InvalidTableNameException(String message)
	{
		super(message);
	}

	public InvalidTableNameException(Throwable cause)
	{
		super(cause);
	}
}
