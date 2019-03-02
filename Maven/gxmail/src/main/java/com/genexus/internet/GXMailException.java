// $Log: GXMailException.java,v $
// Revision 1.1  2000/10/19 21:17:58  gusbro
// Initial revision
//
// Revision 1.1.1.1  2000/10/19 21:17:58  gusbro
// GeneXus Java Olimar
//
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
