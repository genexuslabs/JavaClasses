
package com.genexus;

public class GXObservable extends java.util.Observable
{
	public void forceNotify()
	{
		setChanged();
		notifyObservers();
	}
}
