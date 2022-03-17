package com.genexus.db.dynamodb;

import com.genexus.CommonUtil;
import com.genexus.db.ServiceCursorBase;
import com.genexus.db.driver.GXConnection;
import com.genexus.db.driver.GXPreparedStatement;
import com.genexus.db.service.GXType;
import com.genexus.db.service.IQuery;
import com.genexus.db.service.ServiceDataStoreHelper;

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

	public Object empty(GXType gxtype)
	{
		switch(gxtype)
		{
			case Number:
			case Int16:
			case Int32:
			case Int64: return 0;
			case Date:
			case DateTime:
			case DateTime2:	return CommonUtil.nullDate();
			case Byte:
			case NChar:
			case NClob:
			case NVarChar:
			case Char:
			case LongVarChar:
			case Clob:
			case VarChar:
			case Raw:
			case Blob:
			case NText:
			case Text:
			case Image:
			case UniqueIdentifier:
			case Xml:
			case DateAsChar: return "";
			case Boolean: return false;
			case Decimal: return 0f;

			case Geography:
			case Geopoint:
			case Geoline:
			case Geopolygon:

			case Undefined:
			default: return null;
		}
	}

    @Override
    public GXPreparedStatement getPreparedStatement(GXConnection con, IQuery query, ServiceCursorBase cursor, int cursorNum, boolean currentOf, Object[] parms)
    {
        return new GXPreparedStatement(new DynamoDBPreparedStatement(con.getJDBCConnection(), (DynamoQuery)query, cursor, parms, con), con, con.getHandle(), "", cursor.getCursorId(), currentOf);
    }
}
