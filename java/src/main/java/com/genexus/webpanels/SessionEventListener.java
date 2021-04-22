package com.genexus.webpanels;

import com.genexus.servlet.http.SessionEventListenerWrapper;
import com.genexus.servlet.http.HttpSessionBindingEvent;
import com.genexus.servlet.http.HttpSessionEvent;

public class SessionEventListener
	extends SessionEventListenerWrapper
	
{		
	public void sessionCreated(HttpSessionEvent se)
	{
	}
	
	public void sessionDestroyed(HttpSessionEvent se)
	{
		cleanBlobSession(se);
	}
	
	public void sessionDidActivate(HttpSessionEvent se)
	{
	}
	
	public void sessionWillPassivate(HttpSessionEvent se)
	{
		cleanBlobSession(se);
	}
	
	public void valueBound(HttpSessionBindingEvent se)
	{
	}
	
	public void valueUnbound(HttpSessionBindingEvent se)
	{
		if(se.getName().equals("GX_SESSION_DESTROY_FLAG"))
		{
			BlobsCleaner.getInstance().sessionDestroyed(se.getSession().getId());
		}
	}
	
	private void cleanBlobSession(HttpSessionEvent se) 
	{
		BlobsCleaner.getInstance().sessionDestroyed(se.getSession().getId());
	}
}
