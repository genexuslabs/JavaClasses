// $Log: ServletEventListener.java,v $
// Revision 1.3  2006/10/06 21:00:22  alevin
// - Implementacion del borrado de blobs temporales cuando se destruye el contexto o cuando
//   se destruyen las sessiones.
//
// Revision 1.2  2005/11/16 14:42:00  gusbro
// - Agrego un try-catch al ApplicationServer.shutdown()
//
//

package com.genexus.webpanels;
import com.genexus.*;
import com.genexus.platform.*;
import com.genexus.util.*;
import com.genexus.db.*;
import com.genexus.webpanels.*;
import java.util.Enumeration;
import java.sql.*;
import javax.servlet.*;

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
		String gxcfg = context.getInitParameter("gxcfg");
		if (gxcfg != null)
		{
			try
			{
				Class gxcfgClass = Class.forName(gxcfg);
				ApplicationContext appContext = ApplicationContext.getInstance();
				appContext.setServletEngine(true);
				String basePath = context.getRealPath("/");
				com.genexus.diagnostics.core.LogManager.initialize(basePath);
				appContext.setServletEngineDefaultPath(basePath);
				Application.init(gxcfgClass);
			}
			catch (Exception e) {
			}
		}
	}
}
