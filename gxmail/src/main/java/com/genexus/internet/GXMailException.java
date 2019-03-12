package com.genexus.internet;

public class GXMailException extends Exception
{
	private int errorCode;

	public GXMailException(String msg)
	{
		super(msg);
	}

	public GXMailException(String msg, int errorCode)
	{
		super(msg);
		this.errorCode = errorCode;
	}


	public int getErrorCode()
	{
		return errorCode;
	}
}
