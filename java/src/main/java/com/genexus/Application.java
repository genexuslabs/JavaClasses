package com.genexus;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import com.genexus.db.DBConnectionManager;
import com.genexus.db.DynamicExecute;
import com.genexus.db.Namespace;
import com.genexus.db.UserInformation;
import com.genexus.db.driver.ExternalProvider;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.internet.websocket.IGXWebSocketService;
import com.genexus.specific.java.Connect;
import com.genexus.util.GXQueue;
import com.genexus.util.GXService;
import com.genexus.util.GXServices;
import com.genexus.util.ReorgSubmitThreadPool;
import com.genexus.util.SubmitThreadPool;
import com.genexus.xml.XMLWriter;


public class Application
{
	static {
		Connect.init();
	}

	public static ILogger logger = LogManager.getLogger(Application.class);

	public static boolean usingQueue = false;
	public static java.lang.Object realMainProgram;
	public static java.lang.Object nullObject = new Object();
	private static final String UNKNOWN_CALLER = "<unknown>";

	static Properties ORBproperties;

	private static Boolean isJMXEnabled;

	public static Class gxCfg = ApplicationContext.getInstance().getClass();
	//public static ModelContext clientContext;
	private static Vector<ICleanedup> toCleanup = new Vector<>();
	static LocalUtil localUtil;
	static Class ClassName = null;

	private static volatile ExternalProvider externalProvider = null;
	private static volatile ExternalProvider externalProviderAPI = null;
	private static volatile HashMap<String, Object> services = new HashMap<>();
	private static Object objectLock = new Object();
	private static volatile boolean initialized = false;

	public static void onExitCleanup()
	{
		if (toCleanup != null)
		{
			for (int i = 0; i < toCleanup.size(); i++)
			{
				try
				{
					ICleanedup cleanedUp = toCleanup.elementAt(i);
					if(!(cleanedUp instanceof com.genexus.reports.GXReportViewerThreaded))
					{ //@HACK: el cleanup del ReportViewer queda esperando a que el usuario
						// salga lo cual es inadmisible
						cleanedUp.cleanup();
					}
				}catch(Throwable e)
				{
					logger.error("onExitCleanup", e);
				}
			}
			toCleanup.removeAllElements();
		}
		DBConnectionManager.getInstance().disconnectAll();

	}

	public static void cleanupConnection(int handle)
	{
		try
		{
			getConnectionManager().disconnect(handle);

		}
		catch (Throwable e)
		{
			logger.error("cleanupConnection/Can't disconnect " + handle, e);
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

	public static void init(Class gxCfg, boolean doLogin)
	{
		if	(!initialized)
		{
			synchronized (objectLock) {
				if (!initialized) {
					Application.gxCfg = gxCfg;
					ClientContext.setModelContext(new ModelContext(gxCfg));
					DebugFlag.DEBUG = ClientContext.getModelContext().getClientPreferences().getJDBC_LOGEnabled();
					Namespace.createNamespaces(((ModelContext) ClientContext.getModelContext()).getPreferences().getIniFile());
					XMLWriter.setUseTagToCloseElement(ClientContext.getModelContext().getClientPreferences().getProperty("UseTagToCloseElement", "0").equals("1"));
					startDateTime = new Date();
					initialized = true;
				}
			}
		}
	}

	public static GXServices getGXServices()
	{
		return GXServices.getInstance();
	}

	public static ExternalProvider getExternalProviderAPI()
	{
		if (externalProviderAPI == null)
		{
			externalProviderAPI = getExternalProviderImpl(GXServices.STORAGE_APISERVICE);
			if (externalProviderAPI == null)
			{
				externalProviderAPI = getExternalProvider();
			}
		}
		return externalProviderAPI;
	}

	public static ExternalProvider getExternalProvider()
	{
		if (externalProvider == null)
		{
			externalProvider = getExternalProviderImpl(GXServices.STORAGE_SERVICE);
		}
		return externalProvider;
	}

	private static ExternalProvider getExternalProviderImpl(String service)
	{
		ExternalProvider externalProviderImpl = null;
		GXService providerService = getGXServices().get(service);
		if (providerService != null)
		{
			Class providerClass;
			try
			{
				providerClass = Class.forName(providerService.getClassName());
			}
			catch (ClassNotFoundException e)
			{
				logger.fatal("Unrecognized External Provider class (ClassNotFound) : " + providerService.getName() + " / " + providerService.getClassName(), e);
				throw new InternalError("Unrecognized External Provider class (ClassNotFound) : " + providerService.getName() + " / " + providerService.getClassName());
			}
			try {
				externalProviderImpl = (ExternalProvider) providerClass.getConstructor(String.class).newInstance(service);
			}
			catch (Exception e)
			{
				logger.fatal("Unable to Initialize External Provider Class: " + providerService.getClassName(), e);
				throw new InternalError("Unable to Initialize External Provider Class: " + providerService.getClassName(), e);
			}
		}
		return externalProviderImpl;
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
			try
			{
				if (ApplicationContext.getInstance().isMsgsToUI())
				{
					logger.debug("Exiting VM (1)...");

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
			try
			{
				logger.debug("Exiting VM (2)...");

				System.exit(0);
			}
			catch (SecurityException e)
			{
			}
	}

	public static void exit()
	{
		exitApplet();
	}


	public static ClientPreferences getClientPreferences()
	{
		return (ClientPreferences) (ClientContext.getModelContext()).getClientPreferences();
	}

	public static void addCleanup(ICleanedup o)
	{
		toCleanup.addElement(o);
	}

	public static void cleanupElements()
	{
		com.genexus.util.PropertiesManager.getInstance().flushProperties();

		for (int i = 0; i < toCleanup.size(); i++)
			toCleanup.elementAt(i).cleanup();
		toCleanup.removeAllElements();

		if (Preferences.getDefaultPreferences().getCACHING()){
			ICacheService cacheService = CacheFactory.getInstance();
			if (cacheService instanceof Closeable){
				try {
					((Closeable) cacheService).close();
				}catch (IOException ioex){
					logger.error("Can´t close cache service", ioex);
				}
			}
		}
	}

	public static void cleanupOnError()
	{
		cleanupElements();
		exitAppletOnError();
	}

	public static void cleanup()
	{
		cleanupElements();

		exit();
	}

	public static void cleanup(ModelContext context, java.lang.Object o, int remoteHandle)
	{
	  if (Application.realMainProgram == o)
		{
		  if (!Application.getClientContext().getClientPreferences().getMDI_FORMS() && Application.getClientContext().getClientPreferences().getSDI_CLOSING_FIX())
		  {
				  cleanupConnection(remoteHandle);

				  cleanup();
				  Application.realMainProgram = null;
		  }
		  else
		  {
			  Object tmp = Application.realMainProgram;
			  Application.realMainProgram = nullObject;
			  Application.realMainProgram = tmp;
			  cleanupConnection(remoteHandle);
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
		logger.warn(text, e);
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
	/**
	* @deprecated use com.genexus.db.IDataStoreProvider.getDBMSUser();
	* */
	public static String getDBMSUser(ModelContext context, int handle, String dataSource)
	{
		UserInformation info = getConnectionManager().getUserInformation(handle);

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
			UserInformation info = getConnectionManager().getUserInformation(remoteHandle);

			getConnectionManager(context).getServerDateTime(context, remoteHandle, "DEFAULT");
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}


	public static Date getServerDateTime(ModelContext context, int handle, String dataSource)
	{
		UserInformation info = getConnectionManager().getUserInformation(handle);

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


	private static String dbmsVersion;
	public static String getDBMSVersion(ModelContext context, int handle, String dataSource)
	{
		if (dbmsVersion == null)
		{
			try
			{
				dbmsVersion = getConnectionManager(context).getDBMSVersion(context, handle, dataSource);
			}
			catch (SQLException ex)
			{
				PrivateUtilities.errorHandler("getDBMSVersion()" + ex.getErrorCode() + " SQLState " + ex.getSQLState() + " Statement", ex);
			}
		}
		
		return dbmsVersion ;
	}

	public static String getDatabaseName(ModelContext context, int handle, String dataSource)
	{
		String databaseName = null;
		try
		{
			databaseName = getConnectionManager(context).getDatabaseName(context, handle, dataSource);
		}
		catch (SQLException ex)
		{
			ReorgSubmitThreadPool.setAnError();
			PrivateUtilities.errorHandler("getDatabaseName()" + ex.getErrorCode() + " SQLState " + ex.getSQLState() + " Statement", ex);
		}


		return databaseName ;
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
                                                   gxCfg, GXutil.getClassName(proc.toLowerCase()),
                                                   new Object[] {
                  objName
                }))
                    {
                  Application.GXLocalException(context, info.getHandle(),
                                               "EventHandling(" + eventName + ")",
                                               new
                                               RuntimeException(" Can't execute dynamic call " +
                      GXutil.getClassName(proc.toLowerCase())));
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


	public static void commit(ModelContext context, int remoteHandle, String dataSourceName, com.genexus.db.IDataStoreProvider dataStore)
	{
		commit(context, remoteHandle, dataSourceName, dataStore, UNKNOWN_CALLER);
	}
	//Commit de todos los datastores, no utiliza mencaismo de error_handler. Utilizado en OfflineEventReplicator
	public static void commit(ModelContext context, int remoteHandle, String objName)
	{
		commitDataStores(context, remoteHandle, null, objName);
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

          if (context.getSessionContext() != null) //Si estoy en el contexto de un EJB
          {
            try
            {
              javax.naming.Context initCtx = new javax.naming.InitialContext();
              String trnType = (String) initCtx.lookup("java:comp/env/GX/TrnType");
              if (trnType.equals("CONTAINER")) // Si la TRN en el EJB es manejada por el contenedor
                  doCommit = false;
            }
            catch (javax.naming.NamingException e)
            {
                    throw new RuntimeException(e.getMessage());
            }
          }

		  if (usingQueue)
			GXQueue.commitAll();

          if (doCommit)
          {
          	UserInformation info = getConnectionManager().getUserInformation(
              	 remoteHandle);
          	if (!context.inBeforeCommit)
          	{
          		context.inBeforeCommit = true;
            	executeEvent(context, info, objName, "before_commit", dataSourceName);
            	context.inBeforeCommit = false;
            }

			try {
				if (dataStore != null && !context.inErrorHandler){
					dataStore.commit(dataSourceName); //Commit con mecanismo de error_handler
				}else{
					getConnectionManager(context).commit(context, remoteHandle, dataSourceName);//commit desde reorg por ejemplo, en donde no debe entrar al mencaismo de error_handler.
				}
          			if (!context.inAfterCommit)
          			{
          				context.inAfterCommit = true;
                	executeEvent(context, info, objName, "after_commit", dataSourceName);
                	context.inAfterCommit = false;
                }
              }
              catch (SQLException e) {
                Application.GXLocalException(context, remoteHandle,
                                             "Application.commit", e);
              }
          }

		if (useSmartCache && !ApplicationContext.getInstance().getReorganization())
		  getSmartCacheProvider(remoteHandle).recordUpdates();

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
          if (context.getSessionContext() != null) //Si estoy en el contexto de un EJB
          {
            try
            {
              javax.naming.Context initCtx = new javax.naming.InitialContext();
              String trnType = (String) initCtx.lookup("java:comp/env/GX/TrnType");
              if (trnType.equals("CONTAINER")) // Si la TRN en el EJB es manejada por el contenedor
                doRollback = false;
            }
            catch (javax.naming.NamingException e)
            {
                    throw new RuntimeException(e.getMessage());
            }
          }
		  if (usingQueue)
			GXQueue.rollbackAll();

          if (doRollback)
          {
            UserInformation info = getConnectionManager().getUserInformation(
                remoteHandle);
						if (!context.inBeforeRollback)
						{
							context.inBeforeRollback = true;
            	executeEvent(context, info, objName, "before_rollback", dataSourceName);
            	context.inBeforeRollback = false;
            }

              try {
					if (dataStore!=null && !context.inErrorHandler)
					{
						dataStore.rollback(dataSourceName); //Rollback con mecanismo de error_handler
					}else{
						getConnectionManager(context).rollback(context, remoteHandle, dataSourceName); //rollback desde error_handler por ejemplo, en donde no debe entrar al mencaismo de error_handler.
					}
          			if (!context.inAfterRollback)
          			{
          				context.inAfterRollback = true;
                	executeEvent(context, info, objName, "after_rollback", dataSourceName);
                	context.inAfterRollback = false;
                }
              }
              catch (SQLException e) {
                Application.GXLocalException(context, remoteHandle,
                                             "Application.commit", e);
              }
          }
          else
          {
              UserInformation info = getConnectionManager().getUserInformation(remoteHandle);
							if (!context.inBeforeRollback)
							{
								context.inBeforeRollback = true;
              	executeEvent(context, info, objName, "before_rollback", dataSourceName);
              	context.inBeforeRollback = false;
              }
			  ((GxEjbContext) context.getSessionContext()).setRollback();
							if (!context.inAfterRollback)
							{
								context.inAfterRollback = true;
              	executeEvent(context, info, objName, "after_rollback", dataSourceName);
              	context.inAfterRollback = false;
              }
          }

		if (useSmartCache)
		  getSmartCacheProvider(remoteHandle).discardUpdates();		  
		  
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
		  if (isJMXEnabled == null)
		  {
			  if (!initialized)
			  {
				  return false;
			  }
			  Preferences preferences;
				preferences = (Preferences) (ClientContext.getModelContext()).getPreferences();

			  if (!preferences.getProperty("ENABLE_MANAGEMENT", "0").equals("0"))
			  {
			  	isJMXEnabled = true;
				}
				else
				{
					isJMXEnabled = false;
				}
		  }

		  return isJMXEnabled.booleanValue();
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

 
	  static boolean useSmartCache = false;

	  public static com.genexus.GXSmartCacheProvider getSmartCacheProvider(int handle)
	  {
	  	useSmartCache = true;
			return getConnectionManager().getUserInformation(handle).getSmartCacheProvider();	  	
	  }	  

	private static String WEBSOCKET_SERVICE_NAME = "WS_SERVER";

	public static void registerSocketService(IGXWebSocketService wsService) {		
		services.put(WEBSOCKET_SERVICE_NAME, wsService);
		logger.info("WebSocket Service has been initialized successfully");		
	}

	public static IGXWebSocketService getSocketService() {
		return (IGXWebSocketService) services.get(WEBSOCKET_SERVICE_NAME);
	}
}
