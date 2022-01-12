package com.genexus;


import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import com.genexus.db.DBConnectionManager;
import com.genexus.db.DynamicExecute;
import com.genexus.db.Namespace;
import com.genexus.db.UserInformation;
import com.genexus.platform.NativeFunctions;
import com.genexus.util.ReorgSubmitThreadPool;
import com.genexus.util.SubmitThreadPool;

public class Application 
{
	public static boolean usingQueue = false;
	private static final boolean DEBUG = DebugFlag.DEBUG;
	//public static java.applet.Applet mainApplet;
	public static java.lang.Object realMainProgram;
	public static java.lang.Object nullObject = new Object();
	private static final String UNKNOWN_CALLER = "<unknown>";

	static Properties ORBproperties;

	public static Class<? extends ApplicationContext> gxCfg = ApplicationContext.getInstance().getClass();
	//public static ModelContext clientContext;
	private static Vector<ICleanedup> toCleanup = new Vector<ICleanedup>();
	static LocalUtil localUtil;
    static Class ClassName = null;
	private static Thread shutdownHookThread = null;
	
	static
	{
		try
		{
			if(!NativeFunctions.isMicrosoft() &&
			   !NativeFunctions.is12() &&
			   !PrivateUtilities.isClassPresent("org.eclipse.swt.widgets.Shell"))
			{
				shutdownHookThread = new Thread(new Runnable()
				{
					public void run()
					{
						shutdownHookThread = null;
						onExitCleanup();
					}
				}, "ShutdownThread");
				Runtime.getRuntime().addShutdownHook(shutdownHookThread);
			}
		}catch(Throwable e){;}
	}

	public static void removeShutdownHook()
	{
		if(shutdownHookThread != null)
		{
			try
			{
				Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
				shutdownHookThread = null;
			}catch(Throwable e){ ; }
		}
	}
	
	public static void onExitCleanup()
	{
		removeShutdownHook();
		if (toCleanup != null)
		{
			for (int i = 0; i < toCleanup.size(); i++)
			{
				try
				{
					ICleanedup cleanedUp = (ICleanedup)toCleanup.elementAt(i);
					cleanedUp.cleanup();
				}catch(Throwable e)
				{
					System.err.println("[Shutdown] " + e.toString());
				}
			}
			toCleanup.removeAllElements();
		}

		DBConnectionManager.getInstance().disconnectAll();
	}

	public static void cleanupConnection(ModelContext context, int handle)
	{
		try
		{
			getConnectionManager(context).disconnect(handle);

		}
		catch (SQLException e)
		{
			System.err.println("cleanupConnection/Can't disconnect " + handle + " text: " + e.getMessage());
		}
	}

	private static void setORBProperties(String host)
	{
		ORBproperties = new Properties();
		ORBproperties.put("SVCnameroot", "NameService");

		if	(!ApplicationContext.getInstance().isApplicationServer() && host.length() != 0)
		{
			ORBproperties.put("ORBagentAddr", host);
			ORBproperties.put("org.omg.CORBA.ORBInitialHost", host);
		}

	}

	public static Properties getORBProperties()
	{
		if	(ORBproperties == null)
			setORBProperties(ServerPreferences.getInstance(gxCfg).getNAME_HOST());

		return ORBproperties;
	}

        public static boolean getUserIdServerAsUserId(int handle)
        {
          if	(ApplicationContext.getInstance().isApplicationServer())
            return getConnectionManager().getUserInformation(handle).getProperty(
                "USERIDSERVER_AS_USERID").equals(String.valueOf(true));
          else
            return Application.getClientPreferences().getLOGIN_AS_USERID();
        }

	public static void init(Class gxCfg)
	{
		init(gxCfg, true);
	}

	private volatile static boolean initialized = false;
	
	public static void init(Class gxCfg, boolean doLogin)
	{
		if	(!initialized)
		{	
			com.genexus.specific.android.Connect.init();
			initialized = true;
			Application.gxCfg = gxCfg;
			ClientContext.setModelContext(new ModelContext(gxCfg));
			DebugFlag.DEBUG = ClientContext.getModelContext().getClientPreferences().getJDBC_LOGEnabled();
			Namespace.createNamespaces(((ModelContext)ClientContext.getModelContext()).getPreferences().getIniFile());
			startDateTime = new Date();
		}
	}
	
	static Date startDateTime;
	public static Date getStartDateTime()
	{
		return startDateTime;
	}

	public static ModelContext getClientContext()
	{
		return (ModelContext) ClientContext.getModelContext();
	}


	public static void exitApplet()
	{
		if (ApplicationContext.getInstance().getReorganization())
		{
			ReorgSubmitThreadPool.waitForEnd();			
		}
		else
		{
			SubmitThreadPool.waitForEnd();
		}
		//if	(mainApplet != null)
		//{
		//	NativeFunctions.getInstance().exitApplet();
		//}
		//else
		//{
			try
			{
				if (ApplicationContext.getInstance().isMsgsToUI())
				{
					if	(DEBUG)
						System.err.println("Exiting VM (1)...");

					onExitCleanup();
					
					if (ApplicationContext.getInstance().getReorganization() && ReorgSubmitThreadPool.hasAnyError())
					{
						System.exit(1);
					}
					else
					{
						System.exit(0);
					}
				}
			}
			catch (SecurityException e)
			{
			}
		//}
	}

	public static void exitAppletOnError()
	{
		if (ApplicationContext.getInstance().getReorganization())
		{
			ReorgSubmitThreadPool.waitForEnd();			
		}
		else
		{		
			SubmitThreadPool.waitForEnd();
		}
		//if	(mainApplet != null)
		//{
		//	NativeFunctions.getInstance().exitAppletOnError();
		//}
		//else
		//{
			try
			{
				if	(DEBUG)
					System.err.println("Exiting VM (2)...");

				System.exit(0);
			}
			catch (SecurityException e)
			{
			}
		//}
	}

	public static void exit()
	{
		exitApplet();
	}

	public static boolean hasClientPreferences()
	{
		// An Android online application does not have a client.cfg embedded resource.
		return (ClientContext.getModelContext() != null);
	}
	
	/**
	* @deprecated
	*/
	public static ClientPreferences getClientPreferences()
	{
		return (ClientPreferences) ((ModelContext)ClientContext.getModelContext()).getPreferences();
	}

	public static void addCleanup(ICleanedup o)
	{
		toCleanup.addElement(o);
	}

	public static void cleanupElements()
	{
		com.genexus.util.PropertiesManager.getInstance().flushProperties();

		for (int i = 0; i < toCleanup.size(); i++)
			((ICleanedup) toCleanup.elementAt(i)).cleanup();
		toCleanup.removeAllElements();
	}

	public static void cleanupOnError()
	{
		cleanupElements();
		exitAppletOnError();
	}

	public static void cleanup()
	{
		cleanupElements();
		// Esto es para que se desconecte del appserver

		exit();
	}

	public static void cleanup(ModelContext context, java.lang.Object o, int remoteHandle)
	{
	  if (Application.realMainProgram == o)
		{
		  if (!Application.getClientContext().getClientPreferences().getMDI_FORMS() && Application.getClientContext().getClientPreferences().getSDI_CLOSING_FIX())
		  {
				  cleanupConnection(context, remoteHandle);
				  cleanup();
				  Application.realMainProgram = null;
		  }
		  else 
		  {
			  Object tmp = Application.realMainProgram;
			  Application.realMainProgram = nullObject;
			  Application.realMainProgram = tmp;
			  cleanupConnection(context, remoteHandle);
			  cleanup();
			  Application.realMainProgram = null;
		  }
	  }
	  
	  if (o instanceof GXProcedure)
		  ((GXProcedure) o).endExecute(o.getClass().getName());

	}

	 public static DBConnectionManager getConnectionManager ()
	{
		return DBConnectionManager.getInstance();
	}

	 public static DBConnectionManager getConnectionManager(ModelContext context)
	{
		return DBConnectionManager.getInstance(context);
	}

	public static void setCurrentLocation(String location)
	{
		ApplicationContext.getInstance().setCurrentLocation(location);
	}

	public static int getNewRemoteHandle(ModelContext context)
	{
		return  Application.getConnectionManager().createUserInformation(Namespace.getNamespace(context.getNAME_SPACE())).getHandle();
	}

	public static void printWarning(String text, Exception e)
	{
		System.err.println(text);
	}

	public static void GXLocalException(ModelContext context, int handle, String text, SQLException ex)
	{
		GXLocalException(context, handle, text, (Exception) ex);
	}

	public static void GXLocalException(ModelContext context, int handle, String text, Exception ex)
	{
		GXLocalException(handle, text, ex);
	}

	public static void GXLocalException(int handle, String text, Exception ex)
	{
		ApplicationContext.getInstance().getErrorManager().runtimeError(handle, ex, "runtimedb", text, 0);
	}

	/**
	* Indica si hay que hacer un call remoto. La condicion es :
	*
	*	autoRemote = ""
	*		location  = currentLocation		Local
	*		location != currentLocation
	*			currentLocation = ""  && location != ""	Remoto
	*			currentLocation != "" && location =  ""	Local
	*	autoRemote != ""
	*		location  = currentLocation
	*		location != currentLocation
	*
	*/

	public static boolean isRemoteProcedure(ModelContext context, int handle, String location)
	{
		return !ApplicationContext.getInstance().getCurrentLocation().equals(location) && location.length() > 0;
	}

	public static LocalUtil getClientLocalUtil()
	{
		return ClientContext.getLocalUtil();
	}

	public static Messages getClientMessages()
	{
		return ClientContext.getMessages();
	}

	/**
	* Si estoy en el cliente:
	*
	* 	- GXDB++ del Namespace Remotas -> User del server
	* 	- GXDB++ del Namespace Locales -> User del cliente
	*
	*/
	public static String getDBMSUser(ModelContext context, int handle, String dataSource)
	{
		getConnectionManager().getUserInformation(handle);

			String dbmsUser = null;

			try
			{
				dbmsUser = getConnectionManager(context).getUserName(context, handle, dataSource);
			}
			catch (SQLException ex)
			{
				PrivateUtilities.errorHandler("getDBMSUser()" + ex.getErrorCode() + " SQLState " + ex.getSQLState() + " Statement", ex);
			}

			return dbmsUser;
	}

	public static boolean canConnect(ModelContext context, int remoteHandle, String location)
	{
		try
		{
			getConnectionManager().getUserInformation(remoteHandle);

				getConnectionManager(context).getServerDateTime(context, remoteHandle, "DEFAULT");
		}
		catch (Exception e)
		{
			System.out.println("Hola");
			return false;
		}

		return true;
	}


	public static Date getServerDateTime(ModelContext context, int handle, String dataSource)
	{
		getConnectionManager().getUserInformation(handle);

			Date dateTime = null;

			try
			{
				dateTime = getConnectionManager(context).getServerDateTime(context, handle, dataSource);
			}
			catch (SQLException ex)
			{
				PrivateUtilities.errorHandler("getDBMSUser()" + ex.getErrorCode() + " SQLState " + ex.getSQLState() + " Statement", ex);
				dateTime = CommonUtil.resetTime(CommonUtil.nullDate());
			}

			return dateTime ;
	}
	
	public static String getDBMSVersion(ModelContext context, int handle, String dataSource)
	{
		String dbmsVersion = null;
		
		try
		{
			dbmsVersion = getConnectionManager(context).getDBMSVersion(context, handle, dataSource);
		}
		catch (SQLException ex)
		{
			PrivateUtilities.errorHandler("getDBMSVersion()" + ex.getErrorCode() + " SQLState " + ex.getSQLState() + " Statement", ex);
		}
		
		return dbmsVersion ;
	}
	
	private static void executeEvent(ModelContext context, UserInformation info, String objName, String eventName, String dataSourceName)
	{
          if (dataSourceName.equals("DEFAULT"))
          {
            String proc = context.getPreferences().getEvent(eventName);
            if (info != null && !proc.equals("")) { // Veo si hay que llamar al proc del evento
              // El proc del evento es un proc que toma como parametro un String (el nombre del objeto que lo invoco)
              try {
                if (!DynamicExecute.dynamicExecute(context, info.getHandle(),
                                                   gxCfg, com.genexus.GXutil.getClassName(proc.toLowerCase()),
                                                   new Object[] {
                  objName
                }))
                    {
                  Application.GXLocalException(context, info.getHandle(),
                                               "EventHandling(" + eventName + ")",
                                               new
                                               RuntimeException(" Can't execute dynamic call " +
                                            		   com.genexus.GXutil.getClassName(proc.toLowerCase())));
                }
              }
              catch (Exception e) {
                System.err.println("Event" + eventName + " -> " + e.toString());
                Application.GXLocalException(context, info.getHandle(),
                                             "EventHandling(" + eventName + ")", e);
              }
            }
          }
	}

	/** 
	* @deprecated use commit(ModelContext context, int remoteHandle, String dataSourceName, com.genexus.db.IDataStoreProvider dataStore)
	* */
	public static void commit(ModelContext context, int remoteHandle, String dataSourceName)
	{
		commit(context, remoteHandle, dataSourceName, null, UNKNOWN_CALLER);
	}
	public static void commit(ModelContext context, int remoteHandle, String dataSourceName, com.genexus.db.IDataStoreProvider dataStore)
	{
		commit(context, remoteHandle, dataSourceName, dataStore, UNKNOWN_CALLER);
	}
	/** 
	* @deprecated use commit(ModelContext context, int remoteHandle, String dataSourceName, com.genexus.db.IDataStoreProvider dataStore, String objName)
	* */
	public static void commit(ModelContext context, int remoteHandle, String dataSourceName, String objName)
	{
		commit(context, remoteHandle, dataSourceName, null, UNKNOWN_CALLER);
	}
	//Commit de todos los datastores usando la error_handler del datastore pasado por parametro.
	public static void commitDataStores(ModelContext context, int remoteHandle, com.genexus.db.IDataStoreProvider dataStore, String objName)
	{
		UserInformation info = getConnectionManager().getUserInformation(remoteHandle);      
		for	(Enumeration<com.genexus.db.driver.DataSource> en = info.getNamespace().getDataSources(); en.hasMoreElements(); )
        {
			String dataSourceName = en.nextElement().getName();
			commit(context, remoteHandle, dataSourceName, dataStore, objName);
		}
	}
	public static void commit(ModelContext context, int remoteHandle, String dataSourceName, com.genexus.db.IDataStoreProvider dataStore, String objName)
	{
          boolean doCommit = true;
          if (doCommit)
          {
            UserInformation info = getConnectionManager().getUserInformation(
                remoteHandle);
            executeEvent(context, info, objName, "before_commit", dataSourceName);


              try {
                getConnectionManager(context).commit(context, remoteHandle, dataSourceName);
                executeEvent(context, info, objName, "after_commit", dataSourceName);
              }
              catch (SQLException e) {
                Application.GXLocalException(context, remoteHandle,
                                             "Application.commit", e);
              }
          }
		  		  
		  if (smartCacheProvider != null)
			  smartCacheProvider.recordUpdates();		  	
	}

	public static void rollback(ModelContext context, int remoteHandle, String dataSourceName, com.genexus.db.IDataStoreProvider dataStore)
	{
		rollback(context, remoteHandle, dataSourceName, dataStore, UNKNOWN_CALLER);
	}
	//Rollback de todos los datastores usando la error_handler del datastore pasado por parametro.
	public static void rollbackDataStores(ModelContext context, int remoteHandle, com.genexus.db.IDataStoreProvider dataStore, String objName)
	{
		UserInformation info = getConnectionManager().getUserInformation(remoteHandle);      
		for	(Enumeration<com.genexus.db.driver.DataSource> en = info.getNamespace().getDataSources(); en.hasMoreElements(); )
        {
			String dataSourceName = en.nextElement().getName();
			rollback(context, remoteHandle, dataSourceName, dataStore, objName);
		}
	}

	public static void rollback(ModelContext context, int remoteHandle, String dataSourceName, com.genexus.db.IDataStoreProvider dataStore, String objName)
	{
          boolean doRollback = true;
          if (doRollback)
          {
            UserInformation info = getConnectionManager().getUserInformation(
                remoteHandle);
            executeEvent(context, info, objName, "before_rollback", dataSourceName);

              try {

                getConnectionManager(context).rollback(context, remoteHandle, dataSourceName);
                executeEvent(context, info, objName, "after_rollback", dataSourceName);
              }
              catch (SQLException e) {
                Application.GXLocalException(context, remoteHandle,
                                             "Application.rollback", e);
              }
          }

          else
          {
              UserInformation info = getConnectionManager().getUserInformation(remoteHandle);
              executeEvent(context, info, objName, "before_rollback", dataSourceName);
              executeEvent(context, info, objName, "after_rollback", dataSourceName);
          }

		  if (smartCacheProvider != null)
			  smartCacheProvider.discardUpdates();		  
	}

      public static void setContextClassName(Class Name)
      {
          ClassName = Name;
      }

      public static Class getContextClassName()
      {
        return ClassName;
      }
	  
	public static boolean isJMXEnabled()
	{
		return false;
	}
	  
	  private static boolean showConnectError = true;
	  
	  public static void setShowConnectError(boolean showConnError)
	  {
		  showConnectError = showConnError;
	  }
	  
	  public static boolean getShowConnectError()
	  {
		  return showConnectError;
	  }

	  public static boolean executingGeneratorTool = false;

	  static com.genexus.GXSmartCacheProvider smartCacheProvider;
	  public static com.genexus.GXSmartCacheProvider getSmartCacheProvider()
	  {
		  if (smartCacheProvider == null)
			  smartCacheProvider = new com.genexus.GXSmartCacheProvider((int)0);
		  return smartCacheProvider;
	  }	 
}
