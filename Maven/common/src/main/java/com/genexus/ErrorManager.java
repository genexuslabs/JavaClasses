// $Log: ErrorManager.java,v $
// Revision 1.1  1999/10/11 19:54:44  gusbro
// Initial revision
//
// Revision 1.1.1.1  1999/10/11 19:54:44  gusbro
// GeneXus Java Olimar
//
package com.genexus;

public interface ErrorManager
{
	int NO_RETRY = 0;
	
	int RETRY = 1;
	int QUIT  = 2;

	public int runtimeError(int handle, Throwable e, String description, String detail, int retryCount);
	void reset();
}
