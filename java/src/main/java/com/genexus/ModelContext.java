package com.genexus;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.TimeZone;

import com.genexus.common.classes.AbstractModelContext;
import com.genexus.db.driver.DataSource;
import com.genexus.internet.HttpContext;
import com.genexus.internet.HttpContextNull;
import com.genexus.util.GUIContextNull;
import com.genexus.util.GXThreadLocal;
import com.genexus.util.GXTimeZone;
import com.genexus.util.IGUIContext;
import com.genexus.util.IThreadLocal;
import com.genexus.common.interfaces.SpecificImplementation;

public final class ModelContext extends AbstractModelContext
{
	public Class packageClass;
	public static Class gxcfgPackageClass;
	private String nameSpace;
	private String nameHost;
	private boolean poolConnections;
	private HttpContext httpContext = new HttpContextNull();
	private IGUIContext guiContext  = new GUIContextNull();
	private String staticContentBase = "";
	private int afterConnectHandle = 0;


	public boolean inBeforeCommit = false;
	public boolean inAfterCommit = false;
	public boolean inBeforeRollback = false;
	public boolean inAfterRollback = false; 
	public boolean inErrorHandler = false;


	public static IThreadLocal threadModelContext = GXThreadLocal.newThreadLocal(); 
	
	public static ModelContext getModelContext()
	{
		ModelContext context = (ModelContext)threadModelContext.get();
		if(DebugFlag.DEBUG)
		{
			if(context == null)
			{
				System.err.println(new Date() + " - Cannot find ModelContext for thread " + Thread.currentThread() );
			}
		}
		return context;
	}
	
	public static void endModelContext()
	{
		threadModelContext = null;
	}
	
	public static void deleteThreadContext()
	{
		threadModelContext.set(null);
	}
	
    public boolean isTimezoneSet()
    {
    	return SpecificImplementation.ModelContext.isTimezonSet();
    }
	
	
	public static ModelContext getModelContext(Class packageClass)
	{
		ModelContext context = getModelContext();
		if(context != null)
		{
			return context;
		}
		else
		{
			return new ModelContext(packageClass);
		}
	}
	
	public static Class getModelContextPackageClass()
	{
		if (ModelContext.getModelContext() == null)
		{
			return Application.getContextClassName();
		}
		else
		{
			return ModelContext.getModelContext().packageClass;
		}
	}

	public ModelContext(Class packageClass)
	{
		SpecificImplementation.ModelContext.initPackageClass(this, packageClass);
		if(threadModelContext.get() != null)
		{
			httpContext = ((ModelContext)threadModelContext.get()).getHttpContext();
		}
		
		Application.setContextClassName(this.packageClass);
		try
		{
			this.staticContentBase = getClientPreferences().getWEB_IMAGE_DIR();
			staticContentBase = (staticContentBase.startsWith("/") || (staticContentBase.startsWith("http")) || (staticContentBase.length() > 2 && staticContentBase.charAt(1) == ':'))? staticContentBase: "/" + staticContentBase;
		}
		catch (InternalError e)
		{
			// Esto pasa si corro una app no GX (appserver, Serverconfig, etc)
			this.staticContentBase = "";
		}
		if (httpContext!=null)
			httpContext.setStaticContentBase(staticContentBase);		
		if (threadModelContext.get() == null)
			threadModelContext.set(this);
	}
	
	public ModelContext(ModelContext modelContext)
	{
		this.packageClass = modelContext.packageClass;
		this.nameSpace = modelContext.nameSpace;
		this.nameHost = modelContext.nameHost;
		this.poolConnections = modelContext.poolConnections;
		this.httpContext = modelContext.httpContext;
		this.guiContext  = modelContext.guiContext;
		this.sessionInstances = modelContext.sessionInstances;
		this.staticContentBase = modelContext.staticContentBase;
		this.ctx = modelContext.ctx;
		this.afterConnectHandle = modelContext.afterConnectHandle;
	}
	
	public ModelContext submitCopy()
	{
		ModelContext newContext = new ModelContext(this);
		newContext.httpContext = new HttpContextNull();
		ModelContext.initializeSubmitSession(this, newContext);
		HttpContext ctx = this.getHttpContext();
		if (ctx != null)
		{
			newContext.httpContext.setDefaultPath(ctx.getDefaultPath());
			newContext.httpContext.setStaticContentBase(ctx.getStaticContentBase());
			newContext.httpContext.setClientId(ctx.getClientId());
			newContext.httpContext.setLanguage(ctx.getLanguage());
		}
		return newContext;
	}
		
	public ModelContext copy()
	{
		ModelContext ret = new ModelContext(packageClass);
		ret.httpContext = this.httpContext;
		ret.guiContext = this.guiContext;
		ret.poolConnections = this.poolConnections;
		ret.sessionInstances = this.sessionInstances;
		ret.globals = this.globals;
		return ret;
	}
	

	
	public SessionInstances getSessionInstances()
	{
		if	(sessionInstances == null)
			sessionInstances = new SessionInstances();

		return sessionInstances;
	}

	public HttpContext getHttpContext()
	{
		return httpContext;
	}

	public void setHttpContext(HttpContext httpContext)
	{
		this.httpContext = httpContext;
		if (this.httpContext!=null)
			this.httpContext.setStaticContentBase(staticContentBase);
	}

	public boolean getPoolConnections()
	{
		return poolConnections;
	}

	public void setPoolConnections(boolean poolConnections)
	{
		this.poolConnections = poolConnections;
	}

	public String getPackageName()
	{
		return CommonUtil.getPackageName(packageClass);
	}

	public Class getPackageClass()
	{
		return packageClass;
	}

	protected Preferences prefs;
		
	public Preferences getPreferences()
	{
		if	(prefs == null)
		{
			if	(ApplicationContext.getInstance().isApplicationServer())
				prefs = ServerPreferences.getInstance(packageClass);
			else
				prefs = ClientPreferences.getInstance(packageClass);
		}

		return prefs;
	}

	public boolean isLocalGXDB()
	{
		return getPreferences().getREMOTE_CALLS() == ClientPreferences.ORB_NEVER;
	}

	public int getREMOTE_CALLS()
	{
		return getPreferences().getREMOTE_CALLS();
	}

	public String getGXDB_LOCATION()
	{
		return getPreferences().getIniFile().getProperty(getNAME_SPACE(), "GXDB_LOCATION", "");
	}

	public String getNAME_SPACE()
	{
		if	(nameSpace == null)
			nameSpace = getPreferences().getNAME_SPACE();

		return nameSpace;
	}

	public String getNAME_HOST()
	{
		if	(nameHost == null)
			nameHost = getPreferences().getNAME_HOST();

		return nameHost;
	}

	public ServerPreferences getServerPreferences()
	{
		return ServerPreferences.getInstance(packageClass);
	}

	public ClientPreferences getClientPreferences()
	{
		// EJB: Aqui podriamos hacer un new ClientPreferences si no podemos
		//      usar un metodo est�tico. Implica que leo la configuraci�n
		//		cada vez (se podria hacer en el init).

		return ClientPreferences.getInstance(packageClass);
	}

	public String getServerKey()
	{
		return getPreferences().getServerKey();
	}

	public String getSiteKey()
	{
		return getPreferences().getSiteKey();
	}

    public short SetUserId(String user, int handle, String dataSource)
    {
		if (httpContext!=null)
			return httpContext.setUserId(handle, user, dataSource);
		else
			return 0;
    }

	/** 
	* @deprecated use getUserId(String key, int handle, com.genexus.db.IDataStoreProvider dataStore);
	* */
	public String getUserId(String key, int handle, String dataSource)
	{
		if (httpContext!=null)
			return httpContext.getUserId(key, this, handle, dataSource);
		else
			return "";
	}

	public String getUserId(String key, int handle, com.genexus.db.IDataStoreProvider dataStore)
	{
		if (httpContext!=null)
			return httpContext.getUserId(key, this, handle, dataStore);
		else
			return "";
	}

    public short SetWrkSt(String wrkSt, int handle)
    {
		if (httpContext!=null)
			return httpContext.setWrkSt(handle, wrkSt);
		else
			return 0;
    }

	public String getDeviceId(int handle) {
		return com.genexus.ClientInformation.getId();
	}
	
    public String getWorkstationId(int handle)
	{
		if (httpContext!=null)
			return httpContext.getWorkstationId(handle);
		else
			return "";
	}

	public String getApplicationId(int handle)
	{
		if (httpContext!=null)
			return httpContext.getApplicationId(handle);
		else
			return "";
	}

	public void msgStatus(String msg)
	{
		guiContext.msgStatus(msg);
	}

	public void setGUIContext(IGUIContext context)
	{
		this.guiContext = context;
	}

	public IGUIContext getGUIContext()
	{
	  return guiContext;
	}

	//Implementacion del before y after connect
	public DataSource beforeGetConnection(int handle, DataSource dataSource)	
	{
		DataSource returnDataSource = null;
		String proc = getPreferences().getEvent("before_connect");
		if (!proc.equals("")) 
		{
			try 
			{
				int remoteHandle = -2;
                if (ApplicationContext.getInstance().isApplicationServer())
                {
					String pkgName = getPackageName();
					if (!pkgName.equals(""))
					{
						proc = pkgName + "." + proc;
					}
					remoteHandle = handle;
                }
                else
                {
					proc = com.genexus.GXutil.getClassName(proc);
                }
				Class c = Class.forName(proc);
				Class[] parTypes = new Class[] {int.class, ModelContext.class};
				Constructor ct = c.getConstructor(parTypes);
				Object[] arglist = new Object[] { new Integer(remoteHandle), this };
				Object obj = ct.newInstance(arglist);
				Class[] parameterTypes = new Class[] {com.genexus.db.DBConnection[].class};
				com.genexus.db.DBConnection[] aP1 = new com.genexus.db.DBConnection[1];
				aP1[0] = com.genexus.db.DBConnection.getDataStore( dataSource.name, handle) ;
				Object[] arguments = new Object[] {aP1};
				Method m = c.getMethod("execute", parameterTypes);
				m.invoke(obj, arguments);
				aP1 = (com.genexus.db.DBConnection[]) arguments[0];
				//short err = aP1[0].disconnect(); //Siempre tengo que desconectar por si se conecto en el proc.
				return returnDataSource = aP1[0].getDataSource();
			}
			catch (ClassNotFoundException e) 
			{
				System.out.println(e);
				return null;
			}
			catch (InstantiationException e) 
			{
				System.out.println(e);
				return null;
			}
			catch (NoSuchMethodException e) 
			{
				System.out.println(e);
				return null;
			}
			catch (IllegalAccessException e) 
			{
				System.out.println(e);
				return null;
			}
			catch (InvocationTargetException e) 
			{
				e.printStackTrace();
				return null;
			}
		}
		else
		{
			return null;
		}
	}
				
	public void afterGetConnection(int handle, DataSource dataSource)
	{
			String proc = getPreferences().getEvent("after_connect");
			if (!proc.equals("")) 
			{
				afterConnectHandle = handle;
				try
				{					   
					if (ApplicationContext.getInstance().isApplicationServer())
                    {
						String pkgName = getPackageName();
                        if (!pkgName.equals(""))
                        {
							proc = pkgName + "." + proc;
                        }
                    }
                    else
                    {
						proc = com.genexus.GXutil.getClassName(proc);
                    }
					Class c = Class.forName(proc);
					Class[] parTypes = new Class[] {int.class, ModelContext.class};
					Constructor ct = c.getConstructor(parTypes);
					Object[] arglist = new Object[] { new Integer(handle), this};
					Object obj = ct.newInstance(arglist);
					Class[] parameterTypes = new Class[] {String.class};
					String aP0 = dataSource.name;
					Object[] arguments = new Object[] {aP0};
					Method m = c.getMethod("execute",parameterTypes);
					m.invoke(obj,arguments);
				} catch (ClassNotFoundException e) {
	                System.out.println(e);
		          } catch (InstantiationException e) {
			        System.out.println(e);
				  } catch (NoSuchMethodException e) {
					System.out.println(e);
				} catch (IllegalAccessException e) {
					System.out.println(e);
				} catch (InvocationTargetException e) {
		            e.printStackTrace();			
				}
			}
	}	

	
        public GxEjbContext getSessionContext()
        {
          return ctx;
        }

        public void setSessionContext(GxEjbContext ctx)
        {
          this.ctx = ctx;
        }
		
	private java.util.Hashtable properties = new java.util.Hashtable();		
	
	public void setContextProperty(String key, Object value)
	{
		properties.put(key, value);
	}
	
	public Object getContextProperty(String key)
	{
		return properties.get(key);
	}
     	
    int GX_NULL_TIMEZONEOFFSET = 9999;
    
	public java.util.Date toContextTz( java.util.Date dt)
	{
		return getClientPreferences().useTimezoneFix() ? CommonUtil.ConvertDateTime( dt, GXTimeZone.getDefault(), getClientTimeZone()) : dt;
	}

    public java.util.Date local2DBserver(java.util.Date dt)
    {
        int storagePty = getClientPreferences().getStorageTimezonePty();
        if (CommonUtil.nullDate().equals(dt) || storagePty == ClientPreferences.StorageTimeZonePty_Undefined)
			return dt;

        TimeZone ToTimezone = (storagePty == ClientPreferences.StorageTimeZonePty_Utc) ? TimeZone.getTimeZone("GMT") : CommonUtil.defaultTimeZone;
        return CommonUtil.ConvertDateTime(dt, getClientTimeZone(), ToTimezone);
    }

    public java.util.Date DBserver2local(java.util.Date dt)
    {
        int storagePty = getClientPreferences().getStorageTimezonePty();
        if (CommonUtil.nullDate().equals(dt) || storagePty == ClientPreferences.StorageTimeZonePty_Undefined)
			return dt;
        TimeZone FromTimezone = (storagePty == ClientPreferences.StorageTimeZonePty_Utc) ? TimeZone.getTimeZone("GMT") : CommonUtil.defaultTimeZone;
        return CommonUtil.ConvertDateTime(dt, FromTimezone, getClientTimeZone());
    }
    
    static String GX_REQUEST_TIMEZONE = "GxTZOffset";

    private TimeZone _currentTimeZone;
    
  	public TimeZone getClientTimeZone()
	{
		return TimeZone.getTimeZone(getTimeZone());
	}
    

    
    private TimeZone _getClientTimeZone()
    {
		if (_currentTimeZone != null)
			return _currentTimeZone;
		String sTZ = null;
		if (httpContext!=null)
		{
			sTZ = (String)httpContext.getHeader(GX_REQUEST_TIMEZONE);
			if (sTZ == null || sTZ.equals(""))
				sTZ = (String)httpContext.getCookie(GX_REQUEST_TIMEZONE);
		}
        try
        {
			_currentTimeZone = sTZ.equals("") || sTZ == null ? GXTimeZone.getDefaultOriginal() : TimeZone.getTimeZone(sTZ);
        }
        catch (Exception e)
        {
			_currentTimeZone = GXTimeZone.getDefaultOriginal();
        }
        return _currentTimeZone;
    }

    public String getTimeZone()
    {
		String TZ = null;
		if (httpContext != null)
			TZ = (String)httpContext.getSessionValue("GXTimezone");
        if (TZ != null && !TZ.equals(""))
			setTimeZone((String)TZ);
        if (_currentTimeZone == null)
            _currentTimeZone = _getClientTimeZone();
        return _currentTimeZone.getID();
    }

    public Boolean setTimeZone(String sTz)
    {
		sTz = CommonUtil.rtrim( sTz) ;
        TimeZone tz = TimeZone.getTimeZone( sTz);
        Boolean ret = tz.getID().equals(sTz);
        if (ret)
        {
			_currentTimeZone = tz;
            httpContext.webPutSessionValue("GXTimezone", _currentTimeZone.getID());
        }
        return ret;
    }

	@Override
	public String cgiGet(String varName) {
		return getHttpContext().cgiGet(varName);
	}

	@Override
	public String cgiGetFileName(String varName) {
		return getHttpContext().cgiGetFileName(varName);
	}

	@Override
	public String cgiGetFileType(String varName) {
		return getHttpContext().cgiGetFileType(varName);
	}

	@Override
	public String getSOAPErrMsg() {
		return this.globals.sSOAPErrMsg;
	}

	@Override
	public void setSOAPErrMsg(String msg) {
		this.globals.sSOAPErrMsg = msg;
	}

	@Override
	public String getLanguage() {
		return getHttpContext().getLanguage();
	}

	@Override
	public String getLanguageProperty(String property) {
		return getHttpContext().getLanguageProperty(property);
	}

	@Override
	public Object getThreadModelContext() {
		return threadModelContext.get();
	}

	@Override
	public void setThreadModelContext(Object ctx) {
		threadModelContext.set(ctx);
	}
 
}
