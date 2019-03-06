// $Log: BatchErrorManager.java,v $
// Revision 1.2  2002/08/27 23:10:32  gusbro
// - emprolijamiento
//
// Revision 1.1.1.1  1999/11/30 14:18:42  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  1999/11/30 14:18:42  gusbro
// GeneXus Java Olimar
//
package com.genexus;

public class BatchErrorManager implements ErrorManager
{
	private int retries = 0;
	
	public void reset()
	{
		retries = 0;
	}

	public int runtimeError(int handle, Throwable e, String description, String detail, int retryCount)
	{
		System.err.println("Runtime Error Handle: " + handle);
		System.err.println("Description: " + description);
		System.err.println("Detail: " + detail);
		if(e.getMessage() != null)System.err.println(e);
		if	(retries++ < retryCount)
		{
			System.err.println("Retrying...");
			return RETRY;
		}

		return QUIT;
	}
}