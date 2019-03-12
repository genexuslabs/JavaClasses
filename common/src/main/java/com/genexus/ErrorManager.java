package com.genexus;

public interface ErrorManager
{
	int NO_RETRY = 0;
	
	int RETRY = 1;
	int QUIT  = 2;

	public int runtimeError(int handle, Throwable e, String description, String detail, int retryCount);
	void reset();
}
