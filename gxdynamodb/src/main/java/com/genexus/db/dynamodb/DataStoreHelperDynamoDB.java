package com.genexus.db.dynamodb;

import com.genexus.db.ServiceCursorBase;
import com.genexus.db.driver.GXConnection;
import com.genexus.db.driver.GXPreparedStatement;
import com.genexus.db.service.*;
import java.util.function.*;

public class DataStoreHelperDynamoDB extends ServiceDataStoreHelper
{
	public DynamoQuery newQuery()
	{
		return new DynamoQuery(this);
	}
	public DynamoQuery newScan()
	{
		return new DynamoScan(this);
	}

	public DynamoDBMap Map(String name)
	{
		return new DynamoDBMap(name);
	}

/*	public object empty(GXType gxtype)
	{
		switch(gxtype)
		{
			case GXType.Number:
			case GXType.Int16:
			case GXType.Int32:
			case GXType.Int64: return 0;
			case GXType.Date:
			case GXType.DateTime:
			case GXType.DateTime2:	return DateTimeUtil.NullDate();
			case GXType.Byte:
			case GXType.NChar:
			case GXType.NClob:
			case GXType.NVarChar:
			case GXType.Char:
			case GXType.LongVarChar:
			case GXType.Clob:
			case GXType.VarChar:
			case GXType.Raw:
			case GXType.Blob: return string.Empty;
			case GXType.Boolean: return false;
			case GXType.Decimal: return 0f;
			case GXType.NText:
			case GXType.Text:
			case GXType.Image:
			case GXType.UniqueIdentifier:
			case GXType.Xml: return string.Empty;
			case GXType.Geography:
			case GXType.Geopoint:
			case GXType.Geoline:
			case GXType.Geopolygon: return new Geospatial();
			case GXType.DateAsChar: return string.Empty;
			case GXType.Undefined:
			default: return null;
		}
	}
*/

	@Override
	public GXPreparedStatement getPreparedStatement(GXConnection con, IQuery query, ServiceCursorBase cursor, int cursorNum, boolean currentOf, Object[] parms) {
		return null;
	}
	/**
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
        return date != null ? new java.sql.Timestamp(date.getTime()) : null;
    }
    */
}
