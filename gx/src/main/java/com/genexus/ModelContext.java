// $Log: ModelContext.java,v $
// Revision 1.24  2008-05-26 15:03:53  alevin
// - Arreglo memory leak con los FileItems cuando se tenian inputs de tipo file en la pantalla.
// - Arreglo para que no se hagan redirects luego de que el response fue comiteado.
//
// Revision 1.21  2006/04/18 18:20:46  alevin
// - En el beforeGetConnection y en el afterGetConnection cambio la forma en que
//   se obtiene el nombre de la clase del proc (incluyendo en package), dependiendo
//   de si estoy en el AppServer o no.
//
// Revision 1.20  2006/04/05 20:19:31  alevin
// - Arreglo en el beforeGetConnection para que si estoy con AppServer mande al
//   constructor el handle dado.
//
// Revision 1.19  2006/03/01 18:02:14  iroqueta
// Hago que los metodos aftergetconnection y beforegetconnection tomen en cuenta el package del modelo si esta seteado.
//
// Revision 1.18  2005/12/02 16:59:59  gusbro
// - Al crear un ModelContext seteo el httpContext asociado al ModelContext del thread
//
// Revision 1.17  2005/11/28 17:29:25  iroqueta
// Hago que no se cierre el DBconn cuando se retorna del proc llamado en el before connect.
// Cuando se cerraba traia el problema de que se cerraba el cursor cuando se llama a un proc dentro de un for each que tiene un for each usando el pool del servidor.
//
// Revision 1.16  2005/10/18 16:33:37  gusbro
// Cambio compilacion condicioanl para que se definan after y before connect "dummies" para
// que no falle la compilacion en .Net.
//
// Revision 1.15  2005/10/18 12:23:31  iroqueta
// Implementacion del before connect para las aplics que usan el pool de GeneXus.
// Para que se pueda usar se implemento un array de pools que se indexan por el string de conexion + el usuario de la conexion.
//
// Revision 1.14  2005/09/28 20:27:09  aaguiar
// - Pongo protected la variable prefs
//
// Revision 1.13  2005/05/25 15:57:51  gusbro
// - Agrego metodos para obtener ModelContext asociados al thread actual
//
// Revision 1.12  2005/03/07 18:52:55  iroqueta
// Al crear el ModelContext seteo en la Application la className para luego poder leerla en la clase Message para saber de donde leer el recurso del lenguaje
//
// Revision 1.11  2004/09/17 15:18:17  iroqueta
// Arreglo para que la clase SessionBean solo se use en el caso de usar EJBs
//
// Revision 1.10  2004/09/16 17:58:26  iroqueta
// Agrego compilacion condicional para c# para que no moleste la implementacion de EJBs
//
// Revision 1.9  2004/09/09 18:44:02  iroqueta
// Se implement� el soporte para que las TRNs de los EJBs puedan ser masnejadas por el contenedor.
//
// Revision 1.8  2004/08/20 20:04:35  iroqueta
// Soporte para las funciones SetUserId y SetWrkSt
//
// Revision 1.7  2004/05/26 20:54:16  dmendez
// Se maneja correctamente las utls si no hay jta presente.
//
// Revision 1.6  2004/05/24 21:06:21  dmendez
// Soporte de JTA
//
// Revision 1.5  2003/04/21 18:09:28  aaguiar
// - Optimizacion en la getPreferences
//
// Revision 1.4  2002/09/10 13:16:55  aaguiar
// - Se agrego un metodo copy()
//
// Revision 1.3  2002/08/01 21:31:17  gusbro
// - Se agrega la getWorkstationId
//
// Revision 1.2  2002/07/29 18:40:33  gusbro
// Se pasa la convertURL al HttpContext
//
// Revision 1.1.1.1  2002/04/19 17:39:44  gusbro
// GeneXus Java Olimar
//
package com.genexus;

import com.genexus.internet.HttpContext;
import com.genexus.util.*;
import java.util.*;
import com.genexus.internet.*;
import com.genexus.common.classes.AbstractModelContext;
import com.genexus.db.driver.DataSource;
import java.lang.reflect.*;

public final class ModelContext extends AbstractModelContext
{
	public Class packageClass;
	public static Class gxcfgPackageClass;
	private String nameSpace;
	private String nameHost;
	private boolean poolConnections;
	private HttpContext httpContext = new HttpContextNull();
	private IGUIContext guiContext  = new GUIContextNull();
	private SessionInstances sessionInstances;
	public Globals globals = new Globals();
	private String staticContentBase = "";
    private GxEjbContext ctx = null;
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
		if (gxcfgPackageClass != null)
		{
			this.packageClass = gxcfgPackageClass;
		}
		else
		{
			if (threadModelContext.get() == null)
			{
				this.packageClass = packageClass;
			}
			else
			{
				this.packageClass = ModelContext.getModelContext().packageClass;
			}
		}
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
	
	private static String[] copyKeys = { "GAMConCli", "GAMSession", "GAMError", "GAMErrorURL", "GAMRemote" };
	private static void initializeSubmitSession(ModelContext oldContext, ModelContext newContext) {
		HttpContext httpCtx = oldContext.getHttpContext();
		HttpContext newHttpCtx = newContext.getHttpContext();		
		if (httpCtx != null && newHttpCtx != null) {		
			com.genexus.webpanels.WebSession ws = newHttpCtx.getWebSession();
			for (int i = 0; i < copyKeys.length && ws != null; i++){
				Object value = httpCtx.getSessionValue(copyKeys[i]);
				if (value != null) {
					ws.setValue(copyKeys[i], value.toString());			
				}
			}
		}	
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
		return PrivateUtilities.getPackageName(packageClass);
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
					proc = GXutil.getClassName(proc);
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
						proc = GXutil.getClassName(proc);
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
    
    public boolean isTimezoneSet()
    {
    	return !(httpContext instanceof HttpContextNull);
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
