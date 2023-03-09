package com.genexus.db.cosmosdb;

import com.genexus.CommonUtil;
import com.genexus.db.ServiceCursorBase;
import com.genexus.db.driver.GXConnection;
import com.genexus.db.driver.GXPreparedStatement;
import com.genexus.db.service.GXType;
import com.genexus.db.service.IQuery;
import com.genexus.db.service.ServiceDataStoreHelper;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DataStoreHelperCosmosDB extends ServiceDataStoreHelper
{
	public CosmosDBQuery newQuery()
	{
		return new CosmosDBQuery(this);
	}
	@Override
	public GXPreparedStatement getPreparedStatement(GXConnection con, IQuery query, ServiceCursorBase cursor, int cursorNum, boolean currentOf, Object[] parms)
	{
		try {
			return new GXPreparedStatement(new CosmosDBPreparedStatement(con.getJDBCConnection(), (CosmosDBQuery) query, cursor, parms, con), con, con.getHandle(), "", cursor.getCursorId(), currentOf);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	public Object empty(GXType gxtype)
	{
		switch(gxtype)
		{
			case Number:
			case Int16:
			case Int32:
			case Int64: return 0;
			case Date: return new Date(CommonUtil.nullDate().getTime());
			case DateTime:
			case DateTime2:	return new Timestamp(CommonUtil.nullDate().getTime());
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

}
