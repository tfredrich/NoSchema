package com.strategicgains.noschema.unitofwork;

public class UnitOfWorkRollbackException
extends Exception
{
	private static final long serialVersionUID = -2683447409756870271L;

	public UnitOfWorkRollbackException() {
		super();
	}

	public UnitOfWorkRollbackException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnitOfWorkRollbackException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnitOfWorkRollbackException(String message) {
		super(message);
	}

	public UnitOfWorkRollbackException(Throwable cause) {
		super(cause);
	}
}
