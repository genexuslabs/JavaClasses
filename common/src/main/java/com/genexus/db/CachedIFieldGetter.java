package com.genexus.db;
import java.math.BigDecimal;
import java.util.*;
import java.io.Serializable;
import java.sql.*;
import com.genexus.CommonUtil;
import com.genexus.TypeConstants;
import com.genexus.common.interfaces.SpecificImplementation;


public class CachedIFieldGetter implements IFieldGetter, Serializable
{
	private static final long serialVersionUID = 1L;
	private Object [] value;
	private int wasNullHits;
	private TimeZone mTimeZone;	
	private Hashtable<Integer, Integer> realColIdx; //Introduced in order to enable getting an older column index, because wasNullHits always go forward.
	
	public CachedIFieldGetter()
	{
	}
	public CachedIFieldGetter(Object [] value)
	{
		this.value = value;
		this.wasNullHits = 0;		
		realColIdx = new Hashtable<Integer, Integer>(value.length);
	}
	public void setTimeZone(TimeZone cachedValueTimeZone) 
	{
		mTimeZone = cachedValueTimeZone;
	}
	public Object [] getValue()
	{
		return value;
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue(int index)
	{
		if (value[index] instanceof ArrayList)
			return (T)((ArrayList)value[index]).get(0);
		else 
			return ((T[])value[index])[0];
	}
		
	public void resetWasNullHits()
	{
		this.wasNullHits = 0;
	}

	private int getColumnIndex(int colIdx)
	{
		if (realColIdx.containsKey(colIdx))
			return (int) realColIdx.get(colIdx);
		else {
			int returnVal = colIdx + wasNullHits - 1;
			realColIdx.put(colIdx, returnVal);
			return returnVal;
		}
	}

	public boolean wasNull() throws SQLException
	{
		boolean result = (value == null);
		this.wasNullHits++;
		return result;
	}
		
	public String getLongVarchar(int columnIndex) throws SQLException
	{
		return getVarchar(columnIndex);
	}
		
	public String getVarchar(int columnIndex) throws SQLException
	{
		Object result = this.<String>getValue(getColumnIndex(columnIndex));
		if (result.getClass().getName().equals("com.genexus.GXGeospatial"))
			return result.toString();
		else
			return (String) result;
	}
		
	public String getString(int columnIndex, int length) throws SQLException
	{
		return getVarchar(columnIndex);
	}
		
	public byte getByte(int columnIndex) throws SQLException
	{
		int index = getColumnIndex(columnIndex);
		if (value[index] instanceof ArrayList) {
			return (Byte) CommonUtil.convertObjectTo(((ArrayList) value[index]).get(0), TypeConstants.BYTE);
		}
		else if (value[index] instanceof String){ //Some Cache providers encode64 bytes[]
			byte[] decodedBytes = Base64.getDecoder().decode((String)value[index]);
			return decodedBytes[0];
		}
		else 
			return ((byte[])value[index])[0];
	}
		
	public short getShort(int columnIndex) throws SQLException
	{
		int index = getColumnIndex(columnIndex);
		if (value[index] instanceof ArrayList)
			return (Short)CommonUtil.convertObjectTo(((ArrayList)value[index]).get(0), TypeConstants.SHORT);
		else 
			return ((short[])value[index])[0];
	}
		
	public int getInt(int columnIndex) throws SQLException
	{
		int index = getColumnIndex(columnIndex);
		if (value[index] instanceof ArrayList)
			return (Integer)CommonUtil.convertObjectTo(((ArrayList)value[index]).get(0), TypeConstants.INT);
		else 
			return ((int[])value[index])[0];
	}
		
	public long getLong(int columnIndex) throws SQLException
	{
		int index = getColumnIndex(columnIndex);
		if (value[index] instanceof ArrayList)
			return  (Long)CommonUtil.convertObjectTo(((ArrayList)value[index]).get(0), TypeConstants.LONG);
		else 
			return ((long[])value[index])[0];
	}
		
	public float getFloat(int columnIndex) throws SQLException
	{
		int index = getColumnIndex(columnIndex);
		if (value[index] instanceof ArrayList)
			return (Float)CommonUtil.convertObjectTo(((ArrayList)value[index]).get(0), TypeConstants.FLOAT);
		else 
			return ((float[])value[index])[0];

	}
		
	public double getDouble(int columnIndex) throws SQLException
	{
		int index = getColumnIndex(columnIndex);
		if (value[index] instanceof ArrayList)
			return (Double)CommonUtil.convertObjectTo(((ArrayList)value[index]).get(0), TypeConstants.DOUBLE);
		else 
			return ((double[])value[index])[0];
	}

 	public java.util.Date getGXDateTime(int columnIndex) throws SQLException {
        return getGXDateTime(columnIndex, false);
    }
		
	public java.util.Date getGXDateTime(int columnIndex, boolean hasMilliSeconds) throws SQLException
	{
		// The values of datetimes where cached with a specific client timezone, the client is expecting the new one, so convert from the cached to 
		// the new one
		java.util.Date val = getGXDate(columnIndex);
		if (CommonUtil.nullDate().equals(val))
		{
			return val;
		}
		else
		{
			if (SpecificImplementation.Application.getModelContext() != null && SpecificImplementation.Application.getModelContext().getClientTimeZone() != null && mTimeZone != null)
				val = CommonUtil.ConvertDateTime(val, mTimeZone, SpecificImplementation.Application.getModelContext().getClientTimeZone());
		}
		return val;
	}
		
	public java.util.Date getGXDate(int columnIndex) throws SQLException {

		int index = getColumnIndex(columnIndex);
		if (value[index] instanceof Long[])
			return new java.util.Date(((Long[]) value[index])[0]);
		if (value[index] instanceof ArrayList) {
			ArrayList valueArray = (ArrayList) value[index];
			if (valueArray.get(0) instanceof Long)
				return new java.util.Date((Long) valueArray.get(0));
			else
				return (java.util.Date) CommonUtil.convertObjectTo(valueArray.get(0), TypeConstants.DATE);
		} else
			return ((java.util.Date[]) value[index])[0];
	}
		
	public String getString(int columnIndex) throws SQLException
	{
		return getVarchar(columnIndex);
	}
		
	public boolean getBoolean(int columnIndex) throws SQLException
	{
		int index = getColumnIndex(columnIndex);
		if (value[index] instanceof ArrayList)
			return (Boolean)CommonUtil.convertObjectTo(((ArrayList)value[index]).get(0), TypeConstants.BOOLEAN);
		else 
			return ((boolean[])value[index])[0];
	}
		
	public java.math.BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException
	{
		if (this.getValue(getColumnIndex(columnIndex)) instanceof Double)
			return new BigDecimal((Double)this.getValue(getColumnIndex(columnIndex)));
		return this.<java.math.BigDecimal>getValue(getColumnIndex(columnIndex));
	}
		
	public byte[] getBytes(int columnIndex) throws SQLException
	{
		int index = getColumnIndex(columnIndex);
		if (value[index] instanceof ArrayList)
			return (byte[])((ArrayList)value[index]).get(0);
		else 
			return ((byte[][])value[index])[0];
	}
		
	public java.sql.Date getDate(int columnIndex) throws SQLException
	{
		return this.<java.sql.Date>getValue(getColumnIndex(columnIndex));
	}		
		
	public java.sql.Time getTime(int columnIndex) throws SQLException
	{
		return this.<java.sql.Time>getValue(getColumnIndex(columnIndex));
	}
		
	public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException
	{
		return this.<java.sql.Timestamp>getValue(getColumnIndex(columnIndex));
	}
		
	public String getBLOBFile(int columnIndex) throws SQLException
	{
		return getString(columnIndex);
	}
		
	public String getBLOBFile(int columnIndex, String extension) throws SQLException
	{
		return getString(columnIndex);
	}
		
	public String getBLOBFile(int columnIndex, String extension, String name) throws SQLException
	{
		return getString(columnIndex);
	}

	public String getMultimediaFile(int columnIndex, String name) throws SQLException
	{
		return getString(columnIndex);
	}

	public String getMultimediaUri(int columnIndex) throws SQLException
	{
		return getMultimediaUri(columnIndex, true);
	}

	public String getMultimediaUri(int columnIndex, boolean absPath) throws SQLException
	{
		return getString(columnIndex);
	}

	public java.util.UUID getGUID(int columnIndex) throws SQLException
	{
		Object value = this.getValue(getColumnIndex(columnIndex));
		if (value instanceof java.util.UUID)
			return this.<java.util.UUID>getValue(getColumnIndex(columnIndex));
		else
			return (java.util.UUID) CommonUtil.convertObjectTo(value, TypeConstants.UUID);
	}
}
