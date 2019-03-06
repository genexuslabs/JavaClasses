package com.genexus.webpanels;

public class GXWebException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Exception e;
	String    msg;

	public GXWebException(Exception e)
	{
		this.e = e;
		this.msg = e.getMessage();
	}

	public GXWebException(String text)
	{
		this.msg = text;

	}

	public String toString()
	{
		if (e != null)
			return e.toString() + " / " + msg;
		return msg;
	}
}