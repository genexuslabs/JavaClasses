
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