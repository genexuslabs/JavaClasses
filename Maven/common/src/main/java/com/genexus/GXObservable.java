// $Log: GXObservable.java,v $
// Revision 1.1  2001/10/30 14:42:52  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/10/30 14:42:52  gusbro
// GeneXus Java Olimar
//
package com.genexus;

public class GXObservable extends java.util.Observable
{
	public void forceNotify()
	{
		setChanged();
		notifyObservers();
	}
}
