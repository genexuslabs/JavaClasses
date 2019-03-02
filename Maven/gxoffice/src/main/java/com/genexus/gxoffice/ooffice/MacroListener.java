/*
 * Created on 19/08/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.genexus.gxoffice.ooffice;


import com.sun.star.frame.DispatchResultEvent;
import com.sun.star.frame.XDispatchResultListener;
import com.sun.star.lang.EventObject;

/**
 * @author dvillagra
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MacroListener implements XDispatchResultListener {

	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatchResultListener#dispatchFinished(com.sun.star.frame.DispatchResultEvent)
	 */
	private short xResult;
	private boolean isFinished;
	private Object pCaller;
	
	public MacroListener(Object caller)
	{
		super();
		pCaller = caller;
		isFinished = false;
	}
	
	public void dispatchFinished(DispatchResultEvent arg0) {
		
		// TODO Auto-generated method stub
		xResult = arg0.State;
		checkFinished(true);
		pCaller.notify();
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XEventListener#disposing(com.sun.star.lang.EventObject)
	 */
	public void disposing(EventObject arg0) {
		// TODO Auto-generated method stub
	}

	public short getResult()
	{
		return xResult;
	}
	
	public synchronized boolean checkFinished(boolean set) { 
		if (set) { 
			isFinished = true; 
		} 
		return (isFinished); 
	}
}
