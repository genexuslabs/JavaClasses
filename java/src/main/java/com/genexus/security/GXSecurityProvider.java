package com.genexus.security;

public class GXSecurityProvider
{
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
			}
		}
		return instance;
	}
}