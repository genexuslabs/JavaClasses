package com.genexus.webpanels;

import com.genexus.Application;
import com.genexus.ApplicationContext;
import com.genexus.ModelContext;
import com.genexus.internet.HttpContext;
import com.genexus.specific.java.LogManager;

public class WebApplicationStartup
{
	private static boolean initialized = false;

	public static void init(Class baseClass, HttpContext httpContext)
	{
		if (!initialized)
		{
			initImpl(baseClass, httpContext);
		}
	}
	
	
	private static synchronized void initImpl(Class baseClass, HttpContext httpContext)
	{
		if	(!initialized)
		{
			String basePath = httpContext.getDefaultPath();
			ApplicationContext appContext = ApplicationContext.getInstance();

			appContext.setMsgsToUI(false);
			appContext.setServletEngine(true);
			appContext.setServletEngineDefaultPath(basePath);
			LogManager.initialize(basePath);
      		Application.init(baseClass, false);
			ModelContext.getModelContext(baseClass).setHttpContext(httpContext);
			initialized = true;
		}
	}	
}


