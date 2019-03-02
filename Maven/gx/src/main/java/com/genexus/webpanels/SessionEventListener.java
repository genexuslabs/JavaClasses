// $Log:
//

package com.genexus.webpanels;

import javax.servlet.http.*;

public class SessionEventListener 
	implements HttpSessionListener, HttpSessionActivationListener, HttpSessionBindingListener
	
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
