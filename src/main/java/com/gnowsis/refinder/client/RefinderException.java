package com.gnowsis.refinder.client;

public class RefinderException
extends RuntimeException
{
	private static final long	serialVersionUID	= -6166712059810219711L;

	public enum Status { Unknown, Offline, NotAuthenticated, NotFound, EnvironmentProblem };

    public final Status status;

    public RefinderException(Status status)
    {
    	this(status, null);
    }

    public RefinderException(Status status, Exception cause)
    {
    	super(status.toString(), cause);
        this.status = status;
    }
}
