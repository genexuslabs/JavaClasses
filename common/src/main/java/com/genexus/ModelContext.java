package com.genexus;

import java.util.Date;
import java.util.TimeZone;

import com.genexus.common.classes.AbstractDataSource;

import com.genexus.common.classes.AbstractModelContext;
import com.genexus.common.interfaces.IClientPreferences;
import com.genexus.common.interfaces.IPreferences;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.util.GUIContextNull;
import com.genexus.util.GXThreadLocal;
import com.genexus.util.IGUIContext;
import com.genexus.util.IThreadLocal;

public final class ModelContext extends AbstractModelContext
{
    private String nameSpace;
    private String nameHost;
    private IHttpContext httpContext = SpecificImplementation.ModelContext.getNullHttpContext();
    private IGUIContext guiContext  = new GUIContextNull();
    private String staticContentBase = "";
	public Class packageClass;
	public static Class gxcfgPackageClass;
    private ISessionInstances sessionInstances;
    private Object ctx;

    public boolean poolConnections;
	public boolean inBeforeCommit = false;
	public boolean inAfterCommit = false;
	public boolean inBeforeRollback = false;
	public boolean inAfterRollback = false; 
	public boolean inErrorHandler = false;

    public static IThreadLocal threadModelContext = GXThreadLocal.newThreadLocal();
	private static final ILogger logger = LogManager.getLogger(ModelContext.class);
 
    public static ModelContext getModelContext()
    {
        ModelContext context = (ModelContext)threadModelContext.get();
		if(context == null)
		{
			logger.error(new Date() + " - Cannot find ModelContext for thread " + Thread.currentThread() );
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
            return SpecificImplementation.Application.getContextClassName();
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

        SpecificImplementation.Application.setContextClassName(this.packageClass);
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
        if (httpContext != null)
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
        this.setAfterConnectHandle(modelContext.getAfterConnectHandle());
    }

    public ModelContext submitCopy()
    {
        return SpecificImplementation.ModelContext.submitCopy(this);
    }

    public ModelContext copy()
    {
        return SpecificImplementation.ModelContext.copy(this);
    }

    public ISessionInstances getSessionInstances()
    {
        if	(sessionInstances == null)
            sessionInstances = SpecificImplementation.ModelContext.createSessionInstances();

        return sessionInstances;
    }

    public void setSessionInstances(ISessionInstances sessionInstances) {
        this.sessionInstances = sessionInstances;
    }

    public IHttpContext getHttpContext()
    {
        return httpContext;
    }

    public void setHttpContext(IHttpContext httpContext)
    {
        this.httpContext = httpContext;
        if (this.httpContext!=null)
            this.httpContext.setStaticContentBase(staticContentBase);

		if (httpContext.isHttpContextWeb())
		{
			threadModelContext.set(this);
		}
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

    protected com.genexus.common.interfaces.IPreferences prefs;

    public com.genexus.common.interfaces.IPreferences getPreferences()
    {
        if	(prefs == null)
        {
            prefs = SpecificImplementation.ModelContext.createPreferences(packageClass);
        }

        return prefs;
    }

    public boolean isLocalGXDB()
    {
        return SpecificImplementation.ModelContext.isLocalGXDB(this);
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

    public IPreferences getServerPreferences()
    {
        return SpecificImplementation.ModelContext.getServerPreferences(packageClass);
    }

    public IClientPreferences getClientPreferences()
    {
        return SpecificImplementation.ModelContext.getClientPreferences(packageClass);
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
    public AbstractDataSource beforeGetConnection(int handle, AbstractDataSource dataSource)
    {
        return SpecificImplementation.ModelContext.beforeGetConnection(this, handle, dataSource);
    }

    public void afterGetConnection(int handle, AbstractDataSource dataSource)
    {
        SpecificImplementation.ModelContext.afterGetConnection(this, handle, dataSource);
    }


    public Object getSessionContext()
    {
        return ctx;
    }

    public void setSessionContext(Object ctx)
    {
        this.ctx = ctx;
    }

    private java.util.Hashtable<String, Object> properties = new java.util.Hashtable<>();

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
        return SpecificImplementation.ModelContext.toContextTz(this, dt);
    }

    public java.util.Date local2DBserver(java.util.Date dt)
    {
        return SpecificImplementation.ModelContext.local2DBserver(this, dt);
    }

    public java.util.Date local2DBserver(java.util.Date dt, boolean hasMilliSeconds)
    {
        return SpecificImplementation.ModelContext.local2DBserver(this, dt, hasMilliSeconds);
    }

    public java.util.Date DBserver2local(java.util.Date dt)
    {
        return SpecificImplementation.ModelContext.DBserver2local(this, dt);
    }

    public java.util.Date DBserver2local(java.util.Date dt, boolean hasMilliSeconds)
    {
        return SpecificImplementation.ModelContext.DBserver2local(this, dt, hasMilliSeconds);
    }

    private TimeZone _currentTimeZone;

    public TimeZone getClientTimeZone()
    {
        return SpecificImplementation.ModelContext.getClientTimeZone(this);
    }

    public String getTimeZone()
    {
       return SpecificImplementation.ModelContext.getTimeZone(this);
    }

    public Boolean setTimeZone(String sTz)
    {
        return SpecificImplementation.ModelContext.setTimeZone(this, sTz);
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

    public boolean isTimezoneSet() {
        return SpecificImplementation.ModelContext.isTimezoneSet(this);
    }
}
