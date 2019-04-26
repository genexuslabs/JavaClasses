package com.genexus.common.interfaces;

import com.genexus.IHttpContext;
import com.genexus.ISessionInstances;
import com.genexus.ModelContext;
import com.genexus.common.classes.AbstractDataSource;

import java.util.TimeZone;

public interface IExtensionModelContext {
    // ModelContext initialization
    void initPackageClass(ModelContext ctx, Class packageClass);

    ISessionInstances createSessionInstances();
    // Http
    IHttpContext getNullHttpContext();
    // DataSource Connection Events
    AbstractDataSource beforeGetConnection(ModelContext context, int handle, AbstractDataSource dataSource);
    void afterGetConnection(ModelContext modelContext, int handle, AbstractDataSource dataSource);
    // Cloning
    ModelContext submitCopy(ModelContext modelContext);
    ModelContext copy(ModelContext modelContext);
    // TimeZone functions
    boolean isTimezoneSet(ModelContext ctx);
    String getTimeZone(ModelContext model);
    Boolean setTimeZone(ModelContext model, String sTz);
    java.util.Date toContextTz(ModelContext context, java.util.Date dt);
    java.util.Date local2DBserver(ModelContext context, java.util.Date dt);
    java.util.Date local2DBserver(ModelContext context, java.util.Date dt, boolean hasMilliSeconds);

    java.util.Date DBserver2local(ModelContext context, java.util.Date dt);
    java.util.Date DBserver2local(ModelContext context, java.util.Date dt, boolean hasMilliSeconds);
    // Config Preferences handling
    IPreferences createPreferences(Class packageClass);
    IPreferences getServerPreferences(Class packageClass);
    IClientPreferences getClientPreferences(Class packageClass);

    boolean isLocalGXDB(ModelContext context);

    TimeZone getClientTimeZone(ModelContext modelContext);
}
