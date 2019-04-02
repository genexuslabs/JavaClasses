package com.genexus.db.odata;

import com.genexus.db.ServiceCursorBase;
import com.genexus.db.driver.GXConnection;
import com.genexus.db.driver.GXPreparedStatement;
import com.genexus.db.service.*;
import java.util.function.*;

public class DataStoreHelperOData extends ServiceDataStoreHelper
{
    private final CurrentOfManager currentOfManager = new CurrentOfManager();
    public CurrentOfManager getCurrentOfManager()
    {
        return currentOfManager;
    }
    
    public ODataQuery getQuery(BiFunction<ODataClientHelper, Object [], ODataClientHelper> query, IODataMap[] selectList)
    {
        return new ODataQuery(query, selectList);
    }

    public ODataQuery getQuery(BiFunction<ODataClientHelper, Object [], ODataClientHelper> query, String queryType)
    {
        return new ODataQuery(query, queryType);
    }
    
    public ODataQuery getQuery(BiFunction<ODataClientHelper, Object [], ODataClientHelper> query, ODataQuery continuation)
    {
        return new ODataQuery(query, continuation);
    }

    public ODataQuery getQuery(BiFunction<ODataClientHelper, Object [], ODataClientHelper> query, String queryType, ODataQuery continuation)
    {
        return new ODataQuery(query, queryType, continuation);
    }
    
    public ComplexHashMap complex(String entity)
    {
        return new ComplexHashMap(entity);
    }

    public CurrentOf currentOf(String cursorName, String entity)
    {
        return new CurrentOf(currentOfManager, cursorName, entity);
    }
    
    protected ODataMapDomain MapDomain(String name)
    {
        return new ODataMapDomain(name);
    }
    
    @Override
    public GXPreparedStatement getPreparedStatement(GXConnection con, IQuery query, ServiceCursorBase cursor, int cursorNum, boolean currentOf, Object[] parms)
    {
        return new GXPreparedStatement(new ODataPreparedStatement(con.getJDBCConnection(), (ODataQuery)query, cursor, parms, con), con, con.getHandle(), "", cursor.getCursorId(), currentOf);
    }
    
    @Override
    public Object GetParmDateTime(Object parm)
    {
        java.util.Date date = GetParmDate(parm);
        return date != null ? new java.sql.Timestamp(date.getTime()) : date;
    }
}
