
package com.genexus;
import java.io.*;

public class GXRuntimeException extends RuntimeException
{
	private Throwable e;

	public GXRuntimeException(Throwable e)
	{
		this.e = e;
	}

	public void printStackTrace()
	{
		e.printStackTrace();
	}

 	public void printStackTrace(PrintStream s)
	{
		e.printStackTrace(s);
	}

 	public void printStackTrace(PrintWriter s)
	{
		e.printStackTrace(s);
	}

	public String getStackTraceGX()
	{
		return CommonUtil.getStackTraceAsString(e);
	}

	public String getMessage()
	{
		return e.getMessage();
	}

	public Throwable getTargetException()
	{
		return e;
	}
}