package com.genexus.webpanels;
import com.genexus.servlet.IServletContext;

public class ContextPath
{
	public static IContextPath getIContextPath(IServletContext context)
	{
		try
		{
			if ((context.getMajorVersion() == 2 && context.getMinorVersion() > 1) || context.getMajorVersion() > 2)
			{
				return (IContextPath) Class.forName("com.genexus.webpanels.ContextPathNew").newInstance();
			}
			else 
			{
				return (IContextPath) Class.forName("com.genexus.webpanels.ContextPathOld").newInstance();
			}
		}
		catch (Throwable e)
		{
			try
			{	// El JSDK.jar viejo no tiene la getMajorVersion(), por lo que tira un Error, asi que intentamos con el ContextPathOld
				return (IContextPath) Class.forName("com.genexus.webpanels.ContextPathOld").newInstance();
			}catch(Exception e2)
			{
				return null;
			}
		}
	}
}
