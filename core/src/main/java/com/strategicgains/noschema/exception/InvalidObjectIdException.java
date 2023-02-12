package com.strategicgains.noschema.exception;

public class InvalidObjectIdException
extends ConfigurationException
{
	private static final long serialVersionUID = 5932212723849442808L;

	public InvalidObjectIdException()
	{
	}

	public InvalidObjectIdException(String message)
	{
		super(message);
	}

	public InvalidObjectIdException(Throwable cause)
	{
		super(cause);
	}

	public InvalidObjectIdException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public InvalidObjectIdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
