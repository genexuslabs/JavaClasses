// $Log: ProcessInterruptedException.java,v $
// Revision 1.1  2001/06/05 13:53:36  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/06/05 13:53:36  gusbro
// GeneXus Java Olimar
//
package com.genexus;

public class ProcessInterruptedException extends RuntimeException
{
  	public ProcessInterruptedException(String exc) 
  	{
    	super(exc);
  	}

  	public ProcessInterruptedException() 
  	{
    	super();
  	}
}
