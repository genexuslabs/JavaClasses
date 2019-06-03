package com.genexus.webpanels;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.genexus.*;
import com.genexus.db.DBConnectionManager;
import com.genexus.db.Namespace;
import com.genexus.platform.NativeFunctions;
import com.genexus.specific.java.LogManager;
import com.genexus.util.GXServices;
import com.genexus.util.PropertiesManager;
import com.genexus.util.SubmitThreadPool;

public class ServletEventListener implements ServletContextListener
{
	/** Se llama cuando se tira abajo la aplicacion (o se actualiza)
	 */
	public void contextDestroyed(ServletContextEvent event)
	{
		BlobsCleaner.getInstance().contextDestroyed();


		//Singletons
		Application.gxCfg = null;
		ModelContext.endModelContext();
		LocalUtil.endLocalUtil();
		Messages.endMessages();
		ApplicationContext.endApplicationContext();
		GXServices.endGXServices();
		ClientPreferences.endClientPreferences();
		PropertiesManager.endPropertiesManager();
		Namespace.endNamespace();
		DBConnectionManager.endDBConnectionManager();
		BlobsCleaner.endBlobCleaner();
		NativeFunctions.endNativeFunctions();
		uk.org.retep.pdf.TrueTypeFontCache.cleanup();
		CommonUtil.threadCalendar = null;
		GXutil.threadTimeZone = null;
		ClientContext.setLocalUtil(null);
		ClientContext.setModelContext(null);
		SubmitThreadPool.waitForEnd();

		if (Application.isJMXEnabled())
		{
			com.genexus.management.MBeanUtils.unregisterObjects();
			com.genexus.performance.MBeanUtils.unregisterObjects();
		}

		//Desregistro los JDBC drivers para que no tire Tomcat el mensaje que los va a desregistrar para evitar un memory leak
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements())
		{
			Driver driver = drivers.nextElement();
			if (driver.getClass().getClassLoader() == cl)
			{
				try
				{
					DriverManager.deregisterDriver(driver);
				}
				catch (SQLException e)
				{
					System.out.println(String.format("Error deregistering driver %s", driver));
				}
			}
		}

		//Para que libere la memoria de las clases cargadas en el classloader y no se sature el PermGen.
		System.gc();
	}

	/** Se llama al iniciar la aplicacion
	 */
	public void contextInitialized(ServletContextEvent event)
	{
		ServletContext context = event.getServletContext();
		String basePath = context.getRealPath("/");
		LogManager.initialize(basePath);
		String gxcfg = context.getInitParameter("gxcfg");
		if (gxcfg != null)
		{
			try
			{
				Class gxcfgClass = Class.forName(gxcfg);
				ApplicationContext appContext = ApplicationContext.getInstance();
				appContext.setServletEngine(true);
				appContext.setServletEngineDefaultPath(basePath);
				Application.init(gxcfgClass);
			}
			catch (Exception e) {
			}
		}
	}
}
