package com.genexus.specific.java;

import com.genexus.*;
import com.genexus.common.classes.AbstractDataSource;
import com.genexus.common.interfaces.IClientPreferences;
import com.genexus.common.interfaces.IExtensionModelContext;
import com.genexus.common.interfaces.IPreferences;
import com.genexus.internet.HttpContext;
import com.genexus.internet.HttpContextNull;
import com.genexus.util.GXTimeZone;
import com.lowagie.text.html.HtmlParser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.TimeZone;

public class JavaModelContextExtension implements IExtensionModelContext {

    public boolean isTimezoneSet(ModelContext ctx) {
        return !(ctx.isNullHttpContext());
    }

    public void initPackageClass(ModelContext ctx, Class packageClass) {
        if (ModelContext.gxcfgPackageClass != null)
        {
            ctx.packageClass = ModelContext.gxcfgPackageClass;
        }
        else
        {
            if (ModelContext.threadModelContext.get() == null)
            {
                ctx.packageClass = packageClass;
            }
            else
            {
                ctx.packageClass = ModelContext.getModelContext().packageClass;
            }
        }
    }

    @Override
    public IHttpContext getNullHttpContext() {
        return new HttpContextNull();
    }

    @Override
    public AbstractDataSource beforeGetConnection(ModelContext context, int handle, AbstractDataSource dataSource) {
        com.genexus.db.driver.DataSource returnDataSource = null;
        String proc = context.getPreferences().getEvent("before_connect");
        if (!proc.equals(""))
        {
            try
            {
                int remoteHandle = -2;
                if (ApplicationContext.getInstance().isApplicationServer())
                {
                    String pkgName = context.getPackageName();
                    if (!pkgName.equals(""))
                    {
                        proc = pkgName + "." + proc;
                    }
                    remoteHandle = handle;
                }
                else
                {
                    proc = CommonUtil.getClassName(proc);
                }
                Class<?> c = Class.forName(proc);
                Class<?>[] parTypes = new Class[] {int.class, ModelContext.class};
                Constructor ct = c.getConstructor(parTypes);
                Object[] arglist = new Object[] { new Integer(remoteHandle), context };
                Object obj = ct.newInstance(arglist);
                Class[] parameterTypes = new Class[] {com.genexus.db.DBConnection[].class};
                com.genexus.db.DBConnection[] aP1 = new com.genexus.db.DBConnection[1];
                aP1[0] = com.genexus.db.DBConnection.getDataStoreCopy( dataSource.getName(), handle) ;
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


    @Override
    public void afterGetConnection(ModelContext modelContext, int handle, AbstractDataSource dataSource) {
        String proc = modelContext.getPreferences().getEvent("after_connect");
        if (!proc.equals(""))
        {
            modelContext.setAfterConnectHandle(handle);
            try
            {
                if (ApplicationContext.getInstance().isApplicationServer())
                {
                    String pkgName = modelContext.getPackageName();
                    if (!pkgName.equals(""))
                    {
                        proc = pkgName + "." + proc;
                    }
                }
                else
                {
                    proc = CommonUtil.getClassName(proc);
                }
                Class<?> c = Class.forName(proc);
                Class[] parTypes = new Class[] {int.class, ModelContext.class};
                Constructor ct = c.getConstructor(parTypes);
                Object[] arglist = new Object[] { new Integer(handle), modelContext};
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

    @Override
    public ModelContext submitCopy(ModelContext modelContext) {
        ModelContext newContext = new ModelContext(modelContext);
        newContext.setHttpContext(new HttpContextNull());
        initializeSubmitSession(modelContext, newContext);
        HttpContext ctx = (HttpContext) modelContext.getHttpContext();
        if (ctx != null)
        {
            HttpContext newHttpContext = (HttpContext) newContext.getHttpContext();
            newHttpContext.setDefaultPath(ctx.getDefaultPath());
            newHttpContext.setContextPath(ctx.getContextPath());
            newHttpContext.setStaticContentBase(ctx.getStaticContentBase());
            newHttpContext.setClientId(ctx.getClientId());
            newHttpContext.setLanguage(ctx.getLanguage());
        }
        return newContext;   }

    @Override
    public ModelContext copy(ModelContext modelContext) {
        ModelContext ret = new ModelContext(ModelContext.getModelContextPackageClass());
        ret.setHttpContext(modelContext.getHttpContext());
        ret.setGUIContext(modelContext.getGUIContext());
        ret.poolConnections = modelContext.poolConnections;
        ret.setSessionInstances(modelContext.getSessionInstances());
        ret.globals = modelContext.globals;
        return ret;
    }

    @Override
    public ISessionInstances createSessionInstances() {
        return new SessionInstances();
    }

    private static String[] copyKeys = { "GAMConCli", "GAMSession", "GAMError", "GAMErrorURL", "GAMRemote" };
    private static void initializeSubmitSession(ModelContext oldContext, ModelContext newContext) {
        HttpContext httpCtx = (HttpContext) oldContext.getHttpContext();
        HttpContext newHttpCtx = (HttpContext) newContext.getHttpContext();
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

    int GX_NULL_TIMEZONEOFFSET = 9999;

    public java.util.Date toContextTz(ModelContext context, java.util.Date dt)
    {
        return ((ClientPreferences)context.getClientPreferences()).useTimezoneFix() ? CommonUtil.ConvertDateTime( dt, GXTimeZone.getDefault(), getClientTimeZone(context)) : dt;
    }

    public java.util.Date local2DBserver(ModelContext context, java.util.Date dt)
    {
        int storagePty = ((ClientPreferences)context.getClientPreferences()).getStorageTimezonePty();
        if (CommonUtil.nullDate().equals(dt) || storagePty == ClientPreferences.StorageTimeZonePty_Undefined)
            return dt;

        TimeZone ToTimezone = (storagePty == ClientPreferences.StorageTimeZonePty_Utc) ? TimeZone.getTimeZone("GMT") : CommonUtil.defaultTimeZone;
        return CommonUtil.ConvertDateTime(dt, getClientTimeZone(context), ToTimezone);
    }

    @Override
    public Date local2DBserver(ModelContext context, Date dt, boolean hasMilliSeconds) {
        return local2DBserver(context, dt); // not implemented for Java with milliseconds
    }

    public java.util.Date DBserver2local(ModelContext context, java.util.Date dt)
    {
        int storagePty = ((ClientPreferences)context.getClientPreferences()).getStorageTimezonePty();
        if (CommonUtil.nullDate().equals(dt) || storagePty == ClientPreferences.StorageTimeZonePty_Undefined)
            return dt;
        TimeZone FromTimezone = (storagePty == ClientPreferences.StorageTimeZonePty_Utc) ? TimeZone.getTimeZone("GMT") : CommonUtil.defaultTimeZone;
        return CommonUtil.ConvertDateTime(dt, FromTimezone, getClientTimeZone(context));
    }

    @Override
    public Date DBserver2local(ModelContext context, Date dt, boolean hasMilliSeconds) {
        return DBserver2local(context, dt); // not implemented for Java with milliseconds
    }

    @Override
    public IPreferences createPreferences(Class packageClass) {
        return (ApplicationContext.getInstance().isApplicationServer()) ? ServerPreferences.getInstance(packageClass) : ClientPreferences.getInstance(packageClass);
    }

    @Override
    public IPreferences getServerPreferences(Class packageClass) {
        return ServerPreferences.getInstance(packageClass);
    }

    @Override
    public IClientPreferences getClientPreferences(Class packageClass) {
        // EJB: Aqui podriamos hacer un new ClientPreferences si no podemos
        //      usar un metodo est�tico. Implica que leo la configuraci�n
        //		cada vez (se podria hacer en el init).

        return ClientPreferences.getInstance(packageClass);
    }

    @Override
    public boolean isLocalGXDB(ModelContext context) {
        return context.getPreferences().getREMOTE_CALLS() == ClientPreferences.ORB_NEVER;
    }

    static String GX_REQUEST_TIMEZONE = "GxTZOffset";

    public TimeZone getClientTimeZone(ModelContext context)
    {
        return TimeZone.getTimeZone(getTimeZone(context));
    }

    private TimeZone _getClientTimeZone(ModelContext model)
    {
        if (model != null && model.getCurrentTimeZone() != null)
            return model.getCurrentTimeZone();
        String sTZ = null;
        HttpContext httpContext = (HttpContext) model.getHttpContext();
        if (httpContext!=null)
        {
            sTZ = httpContext.getHeader(GX_REQUEST_TIMEZONE);
            if (sTZ == null || sTZ.equals(""))
                sTZ = httpContext.getCookie(GX_REQUEST_TIMEZONE);
        }
        try
        {
            model.setCurrentTimeZone(sTZ.equals("") || sTZ == null ? GXTimeZone.getDefaultOriginal() : TimeZone.getTimeZone(sTZ));
        }
        catch (Exception e)
        {
            model.setCurrentTimeZone(GXTimeZone.getDefaultOriginal());
        }
        return model.getCurrentTimeZone();
    }

    public String getTimeZone(ModelContext context)
    {
        String TZ = null;
        HttpContext httpContext = (HttpContext) context.getHttpContext();
        if (context != null)
            TZ = (String)httpContext.getSessionValue("GXTimezone");
        if (TZ != null && !TZ.equals(""))
            setTimeZone(context, (String)TZ);
        if (context.getCurrentTimeZone() == null)
            context.setCurrentTimeZone(_getClientTimeZone(context));
        return context.getCurrentTimeZone().getID();
    }

    public Boolean setTimeZone(ModelContext model, String sTz)
    {
        HttpContext httpContext = (HttpContext) model.getHttpContext();
        sTz = CommonUtil.rtrim( sTz) ;
        TimeZone tz = TimeZone.getTimeZone( sTz);
        Boolean ret = tz.getID().equals(sTz);
        if (ret)
        {
            model.setCurrentTimeZone(tz);
            httpContext.webPutSessionValue("GXTimezone", tz.getID());
        }
        return ret;
    }

}
