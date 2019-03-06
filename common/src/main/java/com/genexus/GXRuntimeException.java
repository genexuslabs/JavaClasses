// $Log: GXRuntimeException.java,v $
// Revision 1.2  2002/09/04 20:40:09  aaguiar
// - El message es el stack trace, la idea es que llegue al cliente.
//
// Revision 1.1.1.1  2002/04/08 15:56:48  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2002/04/08 15:56:48  gusbro
// GeneXus Java Olimar
//
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
		return getStackTraceGX();
	}

	public Throwable getTargetException()
	{
		return e;
	}
}