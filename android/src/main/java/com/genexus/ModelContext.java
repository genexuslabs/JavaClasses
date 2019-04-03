package com.genexus;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
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

public final class ModelContext extends AbstractModelContext
{
	public Class packageClass;
	private String nameSpace;
	private String nameHost;
	private boolean poolConnections;
	private HttpContext httpContext = new HttpContextNull();
	private IGUIContext guiContext  = new GUIContextNull();
	private String staticContentBase = "";
	private int afterConnectHandle = 0;
		
	public static IThreadLocal threadModelContext = GXThreadLocal.newThreadLocal(); 
	
	
	public static ModelContext getModelContext()
	{
		ModelContext context = (ModelContext)threadModelContext.get();
		if(DebugFlag.DEBUG)
		{
			if(context == null)
			{
				System.err.println("Cannot find ModelContext for thread " + Thread.currentThread());
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
    	return true;
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
		if (Application.getContextClassName() == null)
			this.packageClass = packageClass;
		else
			this.packageClass = Application.getContextClassName();
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
		this.staticContentBase = modelContext.staticContentBase;
		this.afterConnectHandle = modelContext.afterConnectHandle;
	}

	public ModelContext submitCopy()
	{
		return new ModelContext(this);
	}
		
	public ModelContext copy()
	{
		ModelContext ret = new ModelContext(packageClass);
		ret.httpContext = this.httpContext;
		ret.guiContext = this.guiContext;
		ret.poolConnections = this.poolConnections;
		ret.globals = this.globals;
		return ret;
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

        public short SetWrkSt(String wrkSt, int handle)
        {
          return httpContext.setWrkSt(handle, wrkSt);
        }

        public String getWorkstationId(int handle)
	{
		if (httpContext!=null)
			return httpContext.getWorkstationId(handle);
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
				returnDataSource = aP1[0].getDataSource();
				return returnDataSource;
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
		
	private java.util.Hashtable properties = new java.util.Hashtable();		
	
	public void setContextProperty(String key, Object value)
	{
		properties.put(key, value);
	}
	
	public Object getContextProperty(String key)
	{
		return properties.get(key);
	}
	
	public java.util.TimeZone getClientTimeZone()
    {
		return GXTimeZone.getDefault();
    }
    	
    public java.util.Date local2DBserver(java.util.Date dt, boolean hasMilliSeconds)
    {
		//Convert to UTC if needed
		if (com.artech.base.services.AndroidContext.ApplicationContext.getUseUtcConversion())
		{
			Calendar cal = CommonUtil.getCalendar();
			cal.setTime(dt);
			if (!hasMilliSeconds)
				cal.set(Calendar.MILLISECOND, 0);
			
			// if null/empty date , is only time, not convert
			//if (cal.get(Calendar.DAY_OF_MONTH)==1 && cal.get(Calendar.MONTH)==0  && cal.get(Calendar.YEAR)==1 )
			if (cal.get(Calendar.YEAR)==1 || cal.get(Calendar.YEAR)==0)
				return cal.getTime();
			
			// Convert From Local Time to UTC
			long offset = TimeZone.getDefault().getOffset(cal.getTime().getTime());
			cal.setTime(new Date( cal.getTime().getTime() - offset ) );
			return cal.getTime();
		}
		return dt;
    }

    public java.util.Date DBserver2local(java.util.Date dt, boolean hasMilliSeconds)
    {
    	//Convert to UTC if needed
		if (com.artech.base.services.AndroidContext.ApplicationContext.getUseUtcConversion())
		{
			Calendar cal = CommonUtil.getCalendar();
			cal.setTime(dt);
			if (!hasMilliSeconds)
				cal.set(Calendar.MILLISECOND, 0);
			
			// if null/empty date , is only time, not convert
			//if (cal.get(Calendar.DAY_OF_MONTH)==1 && cal.get(Calendar.MONTH)==0  && cal.get(Calendar.YEAR)==1 )
			if (cal.get(Calendar.YEAR)==1 || cal.get(Calendar.YEAR)==0)
				return cal.getTime();
			// Convert From UTC To Local Time
			long offset = TimeZone.getDefault().getOffset(cal.getTime().getTime());
			cal.setTime(new Date( cal.getTime().getTime() + offset ) );
			return cal.getTime();
		}
		return dt;
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
