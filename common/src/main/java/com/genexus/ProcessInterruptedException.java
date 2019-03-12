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
