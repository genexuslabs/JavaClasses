
package com.genexus;

public class ApplicationContext
{
	private static ApplicationContext instance;

	private String  currentLocation = "";

	private boolean poolConnections     = false;
	private boolean isReorganization    = false;
	private boolean isEJB			    = false;
	private boolean isApplicationServer = false;
	private boolean isGXUtility			= false;
	private boolean isMsgsToUI		    = true ;
	private boolean isServletEngine 	= false;
	private boolean isSpringBootApp 	= false;
	private boolean isEJBEngine 		= false;
	private boolean isDeveloperMenu 	= false;
	private static Object  syncObject = new Object();

	private ApplicationContext()
	{
	}

	private static java.util.HashMap<String, String> customCSSContent = new java.util.HashMap<String, String>();

	public static java.util.HashMap<String, String> getcustomCSSContent()
	{
		return customCSSContent;
	}

	public static ApplicationContext getInstance()
	{
		if	(instance == null) {
			synchronized (syncObject){
				if (instance == null)
					instance = new ApplicationContext();
			}
		}
		return instance;
	}

	public static void endApplicationContext()
	{
		instance = null;
	}

	public void setCurrentLocation(String location)
	{
		if	(currentLocation == null || currentLocation.trim().length() == 0)
		{
			currentLocation = location;
		}
	}

	public String getCurrentLocation()
	{
		return currentLocation;
	}

	public void setPoolConnections(boolean poolConnections)
	{
		this.poolConnections = poolConnections;
	}

	public boolean getPoolConnections()
	{
		return poolConnections;
	}

	public void setReorganization(boolean isReorganization)
	{
		this.isReorganization = isReorganization;
	}

	public boolean getReorganization()
	{
		return isReorganization;
	}

	public boolean getEJB()
	{
		return isEJB;
	}

	public void setEJB(boolean isEJB)
	{
		this.isEJB = isEJB;
	}


	public void setApplicationServer(boolean isApplicationServer)
	{
		this.isApplicationServer = isApplicationServer ;
		setMsgsToUI(false);
		setPoolConnections(true);
	}

	public boolean isApplicationServer()
	{
		return isApplicationServer ;
	}

	public void setServletEngine(boolean isServletEngine)
	{
		this.isServletEngine = isServletEngine;
		setMsgsToUI(false);
	}

	public boolean isServletEngine()
	{
		return isServletEngine;
	}

	public void setSpringBootApp(boolean isSpringBootApp)
	{
		this.isSpringBootApp = isSpringBootApp;
	}

	public boolean isSpringBootApp()
	{
		return isSpringBootApp;
	}

	public void setEJBEngine(boolean isEJBEngine)
	{
		this.isEJBEngine = isEJBEngine;
		setMsgsToUI(false);
	}

	public boolean isEJBEngine()
	{
		return isEJBEngine;
	}

	private String servletEngineDefaultPath = "";
	public void setServletEngineDefaultPath(String servletEngineDefaultPath)
	{
		if (servletEngineDefaultPath != null) {
			this.servletEngineDefaultPath = servletEngineDefaultPath.trim();
			if(this.servletEngineDefaultPath.endsWith(java.io.File.separator))
			{
				this.servletEngineDefaultPath = this.servletEngineDefaultPath.substring(0, this.servletEngineDefaultPath.length() - 1);
			}
		}
	}

	public String getServletEngineDefaultPath()
	{
		return servletEngineDefaultPath;
	}

	public void setGXUtility(boolean isGXUtility)
	{
		this.isGXUtility = isGXUtility;
	}

	public boolean isGXUtility()
	{
		return isGXUtility;
	}

	public void setMsgsToUI(boolean isMsgsToUI)
	{
		this.isMsgsToUI = isMsgsToUI;
	}

	public static final String SUBMIT_THREAD = "SubmitThread-";

	public boolean isMsgsToUI()
	{
		if(isMsgsToUI)
		{
			String currentThreadName = Thread.currentThread().getName();
			return currentThreadName == null || !currentThreadName.startsWith(SUBMIT_THREAD);
		}
		return false;
	}

	public boolean isDeveloperMenu()
	{
		return isDeveloperMenu;
	}

	public void setDeveloperMenu(boolean isDeveloperMenu)
	{
		this.isDeveloperMenu = isDeveloperMenu;
	}


	public ErrorManager getErrorManager()
	{
		return new BatchErrorManager();
	}
}
