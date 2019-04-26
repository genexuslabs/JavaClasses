package com.genexus.specific.android;

import com.genexus.*;
import com.genexus.common.classes.AbstractDataSource;
import com.genexus.common.interfaces.IClientPreferences;
import com.genexus.common.interfaces.IExtensionModelContext;
import com.genexus.common.interfaces.IPreferences;
import com.genexus.internet.HttpContextNull;
import com.genexus.util.GXTimeZone;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class AndroidModelContextExtension implements IExtensionModelContext {

    public boolean isTimezoneSet(ModelContext ctx) {
        return true;
    }

    @Override
    public String getTimeZone(ModelContext model) {
        return "";
    }

    @Override
    public Boolean setTimeZone(ModelContext model, String sTz) {
        return true;
    }

    @Override
    public Date toContextTz(ModelContext context, Date dt) {
        return null;
    }

    @Override
    public Date local2DBserver(ModelContext context, Date dt) {
        return local2DBserver(context, dt, false);
    }

    @Override
    public Date local2DBserver(ModelContext context, Date dt, boolean hasMilliSeconds) {
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

    @Override
    public Date DBserver2local(ModelContext context, Date dt) {
        return DBserver2local(context, dt, false);
    }

    @Override
    public Date DBserver2local(ModelContext context, Date dt, boolean hasMilliSeconds) {
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
    public IPreferences createPreferences(Class packageClass) {
        return (ApplicationContext.getInstance().isApplicationServer()) ? ServerPreferences.getInstance(packageClass) : ClientPreferences.getInstance(packageClass);
    }

    @Override
    public IPreferences getServerPreferences(Class packageClass) {
        return ServerPreferences.getInstance(packageClass);
    }

    @Override
    public IClientPreferences getClientPreferences(Class packageClass) {
        return ClientPreferences.getInstance(packageClass);
    }

    @Override
    public boolean isLocalGXDB(ModelContext context) {
        return context.getPreferences().getREMOTE_CALLS() == ClientPreferences.ORB_NEVER;
    }

    @Override
    public TimeZone getClientTimeZone(ModelContext modelContext) {
        return GXTimeZone.getDefault();
    }

    public void initPackageClass(ModelContext ctx, Class packageClass) {
        if (com.genexus.Application.getContextClassName() == null)
            ctx.packageClass = packageClass;
        else
            ctx.packageClass = com.genexus.Application.getContextClassName();
    }

    @Override
    public ISessionInstances createSessionInstances() {
        return null;
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
                Class c = Class.forName(proc);
                Class[] parTypes = new Class[] {int.class, ModelContext.class};
                Constructor ct = c.getConstructor(parTypes);
                Object[] arglist = new Object[] { new Integer(remoteHandle), this };
                Object obj = ct.newInstance(arglist);
                Class[] parameterTypes = new Class[] {com.genexus.db.DBConnection[].class};
                com.genexus.db.DBConnection[] aP1 = new com.genexus.db.DBConnection[1];
                aP1[0] = com.genexus.db.DBConnection.getDataStore( dataSource.getName(), handle) ;
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

    @Override
    public ModelContext submitCopy(ModelContext modelContext) {
        return new ModelContext(modelContext);
    }

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
}
