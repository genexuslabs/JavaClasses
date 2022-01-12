package com.genexus.security;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

public class GXSecurityProvider
{
	public static final ILogger logger = LogManager.getLogger(GXSecurityProvider.class);

	private static SecurityProvider instance = null;

	public static SecurityProvider getInstance()
	{
		if(instance == null)
		{
			try
			{
				Class<?> c = Class.forName("genexus.security.GAMSecurityProvider");
				instance = (SecurityProvider)c.getDeclaredConstructor().newInstance();
			}
			catch(Exception e)
			{
				instance = new NoSecurityProvider();
				logger.error("GAMSecurityProvider class not found.");
			}
		}
		return instance;
	}
}