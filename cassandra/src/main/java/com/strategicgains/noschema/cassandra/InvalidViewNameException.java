package com.strategicgains.noschema.cassandra;

import com.strategicgains.noschema.exception.ConfigurationException;

public class InvalidViewNameException extends ConfigurationException
{
	private static final long serialVersionUID = -7883606740014460827L;

	public InvalidViewNameException()
	{
		super();
	}

	public InvalidViewNameException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidViewNameException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public InvalidViewNameException(String message)
	{
		super(message);
	}

	public InvalidViewNameException(Throwable cause)
	{
		super(cause);
	}
}
