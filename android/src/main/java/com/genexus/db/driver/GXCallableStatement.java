package com.genexus.db.driver;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import com.genexus.CommonUtil;
import com.genexus.DebugFlag;
import com.genexus.GXDbFile;
import com.genexus.db.IGXCallableStatement;

/**
* Esta clase es un wrapper de un CallableStatement real. Le agrega debugging y algunos procesamientos en
* las funciones set<Type> para ajustarlas a las necesidades de los programas generados.
* <p>
* No tiene sincronización, dado que los ResultSets nunca son usados simultáneamente desde
* dos threads.
* <p>
* Las funciones que no deberían ser llamadas nunca, generan una línea con "Warning" en el Log.
*/

public final class GXCallableStatement extends GXPreparedStatement implements CallableStatement, com.genexus.db.IFieldGetter, IGXCallableStatement
{
	private static final boolean DEBUG       = DebugFlag.DEBUG;

	private CallableStatement stmt;
	private GXConnection con;

	public GXCallableStatement(CallableStatement stmt, GXConnection con, int handle, String sqlSentence)
	{
		super(stmt, con, handle, sqlSentence, "");
		this.stmt = stmt;
		this.con  = con;
	}

    public boolean execute() throws SQLException
	{
		
		boolean ret;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MIN, "Executing callable statement");
			try
			{
				ret = stmt.execute();
				log(GXDBDebug.LOG_MIN, "succesfully executed");
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			ret = stmt.execute();
		}

		//con.setNotInUse(this);
		return ret;
	}

        public void setNotInUse()
        {
          con.setNotInUse(this);
        }

	public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "registerOutParameter - index : " + parameterIndex + " sqlType " + sqlType);

			try
			{
				stmt.registerOutParameter(parameterIndex, sqlType);
				log(GXDBDebug.LOG_MAX,"parameter Registered");
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled())
					con.logSQLException(con.getHandle(), sqlException);

				throw sqlException;
			}
		}
		else
		{
		 	stmt.registerOutParameter(parameterIndex, sqlType);
		}
	}

	public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX,"registerOutParameter - index : " + parameterIndex + " sqlType " + sqlType + " scale " + scale);

			try
			{
				stmt.registerOutParameter(parameterIndex, sqlType, scale);
				log(GXDBDebug.LOG_MAX,"parameter Registered");
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled())
					con.logSQLException(con.getHandle(), sqlException);

				throw sqlException;
			}
		}
		else
		{
			stmt.registerOutParameter(parameterIndex, sqlType, scale);
		}
	}

	public String getLongVarchar(int columnIndex) throws SQLException
	{
		String value;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX,"getLongVarchar - index : " + columnIndex);

			try
			{
				value = stmt.getString(columnIndex);
				// En algunos drivers viene en null
				if	(stmt.wasNull() || value == null) value = "";

				// Para los longVarchar no despliego el valor sino el largo.
				log(GXDBDebug.LOG_MAX,"getLongVarchar - real length: " + value.length());
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			value = stmt.getString(columnIndex);
			// En algunos drivers viene en null
			if	(stmt.wasNull() || value == null) value = "";
		}

		return value;
	}

	public String getVarchar(int columnIndex) throws SQLException
	{
		String value;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX,"getVarchar - index : " + columnIndex );

			try
			{
				value = stmt.getString(columnIndex);
				if	(stmt.wasNull())
					value = "";

				log(GXDBDebug.LOG_MAX,"getString - value : " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			value = stmt.getString(columnIndex);
			if	(stmt.wasNull())
				value = "";
		}

		return value;
	}
	
	public String getBLOBFile(int columnIndex, String extension, String name) throws SQLException
	{
		return "";
	}

	public String getBLOBFile(int columnIndex, String extension) throws SQLException
	{
		return "";
	}

	public String getBLOBFile(int columnIndex) throws SQLException
	{
		return getBLOBFile(columnIndex, "tmp");
	}

	public String getMultimediaFile(int columnIndex, String name) throws SQLException
	{
		return "";
	}

	public String getMultimediaUri(int columnIndex) throws SQLException
	{
		return getMultimediaUri(columnIndex, true);
	}
	
	public String getMultimediaUri(int columnIndex, boolean absPath) throws SQLException
	{
		return GXDbFile.resolveUri(getVarchar(columnIndex));
	}

	public String getString(int columnIndex, int length) throws SQLException
	{
		String value;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX,"getString - index : " + columnIndex + " length : " + length);

			try
			{
				value = stmt.getString(columnIndex);
				if	(stmt.wasNull()) value = CommonUtil.replicate(" ", length);

				log(GXDBDebug.LOG_MAX,"getString - value : " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			value = stmt.getString(columnIndex);
			if	(stmt.wasNull()) value = CommonUtil.replicate(" ", length);
		}

		return value;
	}

	public byte getByte(int columnIndex) throws SQLException
	{
		byte value;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX,"getByte - index : " + columnIndex);

			try
			{
				value = stmt.getByte(columnIndex);
				if	(stmt.wasNull()) value = 0;
				log(GXDBDebug.LOG_MAX,"getByte - value : " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			value = stmt.getByte(columnIndex);
			if	(stmt.wasNull()) value = 0;
		}

		return value;
	}

	public short getShort(int columnIndex) throws SQLException
	{
		short value;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX,"getShort - index : " + columnIndex);

			try
			{
				value = stmt.getShort(columnIndex);
				if	(stmt.wasNull()) value = 0;
				log(GXDBDebug.LOG_MAX,"getShort - value : " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			value = stmt.getShort(columnIndex);
			if	(stmt.wasNull()) value = 0;
		}

		return value;
	}

	public int getInt(int columnIndex) throws SQLException
	{
		int value;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "getInt - index : " + columnIndex);

			try
			{
				value = stmt.getInt(columnIndex);
				if (stmt.wasNull()) value = 0;
				log(GXDBDebug.LOG_MAX, "getInt - value : " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
		 	value = stmt.getInt(columnIndex);
		 	if (stmt.wasNull()) value = 0;
		}

		return value;
	}

	public long getLong(int columnIndex) throws SQLException
	{
		long value;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "getLong - index : " + columnIndex);

			try
			{
				value = stmt.getLong(columnIndex);
				if (stmt.wasNull()) value = 0;
				log(GXDBDebug.LOG_MAX, "getLong - value : " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
		 	value = stmt.getLong(columnIndex);
		 	if (stmt.wasNull()) value = 0;
		}

		return value;
	}

	public float getFloat(int columnIndex) throws SQLException
	{
		float value;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "getFloat - index : " + columnIndex);

			try
			{
				value = stmt.getFloat(columnIndex);
				if (stmt.wasNull()) value = 0;
				log(GXDBDebug.LOG_MAX, "getFloat - value : " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
		 	value = stmt.getFloat(columnIndex);
		 	if (stmt.wasNull()) value = 0;
		}

		return value;
	}

	public double getDouble(int columnIndex) throws SQLException
	{
		double value;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "getDouble - index : " + columnIndex);

			try
			{
				value = stmt.getDouble(columnIndex);
				if (stmt.wasNull()) value = 0;
				log(GXDBDebug.LOG_MAX, "getDouble - value : " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
		 	value = stmt.getDouble(columnIndex);
		 	if (stmt.wasNull()) value = 0;
		}

		return value;
	}

	public java.util.Date getGXDateTime(int columnIndex) throws SQLException
	{
		return this.getGXDateTime(columnIndex, false);
	}

	public java.util.Date getGXDateTime(int columnIndex,boolean hasMilliSeconds) throws SQLException
	{
		java.util.Date value = null;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "getGXDateTime - index : " + columnIndex);

			try
			{
				value = stmt.getTimestamp(columnIndex);

//				log("getGXDateTime - value1 : " + value.getTime());
				if	(stmt.wasNull() || con.isNullDate(value))
					value = CommonUtil.nullDate();

 				log(GXDBDebug.LOG_MAX, "getGXDateTime - value2 : " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
		 	value = stmt.getTimestamp(columnIndex);

			if	(stmt.wasNull() || con.isNullDate(value))
				value = CommonUtil.nullDate();
		}

		return value;
	}

	public java.util.Date getGXDate(int columnIndex) throws SQLException
	{
		java.util.Date value = null;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "getGXDate - index : " + columnIndex);

			try
			{
				if (con.getDBMS().useCharInDate())
				{
					String valueString = getString(columnIndex);
					log(GXDBDebug.LOG_MAX, "getGXDate - stringRead : " + valueString);

					if	(stmt.wasNull())
						return CommonUtil.nullDate();

					if	(valueString.length() > 8)
					{
						if	(valueString.equals("00000000000000"))
							value = CommonUtil.nullDate();
						else
						{
							StringBuffer newValueString  = new StringBuffer();
							for (int i = 0; i < valueString.length(); i++)
							{
								switch (valueString.charAt(i))
								{
									case ' ':
									case '-':
									case ':':
											break;
									default:
										newValueString.append(valueString.charAt(i));
								}
							}

							valueString = newValueString.toString();

							value = CommonUtil.ymdhmsToT_noYL(
													(int) CommonUtil.val(valueString.substring(0, 4)),
										    	 	(int) CommonUtil.val(valueString.substring(4, 6)),
											     	(int) CommonUtil.val(valueString.substring(6, 8)),
										     		(int) CommonUtil.val(valueString.substring(8, 10)),
										     		(int) CommonUtil.val(valueString.substring(10, 12)),
										     		(int) CommonUtil.val(valueString.substring(12, 14)));
						}
						// es un datetime
					}
					else
					{
						if	(valueString.equals("00000000"))
							value = CommonUtil.nullDate();
						else
							value = CommonUtil.ymdhmsToT_noYL(
													(int) CommonUtil.val(valueString.substring(0, 4)),
										    	 	(int) CommonUtil.val(valueString.substring(4, 6)),
											     	(int) CommonUtil.val(valueString.substring(6, 8)),
													0,
													0,
													0);
					}
					log(GXDBDebug.LOG_MAX, "getGXDate as Char - value1 : " + value + " string " + valueString);
				}
				else
				{
					value = stmt.getTimestamp(columnIndex);
					log(GXDBDebug.LOG_MAX, "getGXDate - value1 : " + value);
					if	(stmt.wasNull() || con.isNullDate(value))
						value = CommonUtil.nullDate();
					else
						value = CommonUtil.resetTime(value);
					log(GXDBDebug.LOG_MAX, "getGXDate - value2 : " + value);
				}
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{

			if (con.getDBMS().useCharInDate())
			{
				String valueString = getString(columnIndex);

				if	(stmt.wasNull())
					return CommonUtil.nullDate();

				if	(valueString.length() > 8)
				{
					if	(valueString.equals("00000000000000"))
						value = CommonUtil.nullDate();
					else
					{
						StringBuffer newValueString  = new StringBuffer();
						for (int i = 0; i < valueString.length(); i++)
						{
							switch (valueString.charAt(i))
							{
								case ' ':
								case '-':
								case ':':
										break;
								default:
									newValueString.append(valueString.charAt(i));
							}
						}

						valueString = newValueString.toString();

						value = CommonUtil.ymdhmsToT_noYL(
												(int) CommonUtil.val(valueString.substring(0, 4)),
									    	 	(int) CommonUtil.val(valueString.substring(4, 6)),
										     	(int) CommonUtil.val(valueString.substring(6, 8)),
									     		(int) CommonUtil.val(valueString.substring(8, 10)),
									     		(int) CommonUtil.val(valueString.substring(10, 12)),
									     		(int) CommonUtil.val(valueString.substring(12, 14)));
					}
					// es un datetime
				}
				else
				{
					if	(valueString.equals("00000000"))
						value = CommonUtil.nullDate();
					else
						value = CommonUtil.ymdhmsToT_noYL(
											  (int) CommonUtil.val(valueString.substring(0, 4)),
											  (int) CommonUtil.val(valueString.substring(4, 6)),
											  (int) CommonUtil.val(valueString.substring(6, 8)),
											  0,
											  0,
											  0);
				}
			}
			else
			{
				value = stmt.getTimestamp(columnIndex);

				if	(stmt.wasNull() || value.equals(con.getNullDate()))
					value = CommonUtil.nullDate();
				else
					value = CommonUtil.resetTime(value);
			}
		}

		return value;
	}

	// -- Funciones que NO deberian ser llamadas desde los programas generados

	public String getString(int columnIndex) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: getString");

		return stmt.getString(columnIndex);
	}

	public boolean getBoolean(int columnIndex) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: getBoolean");

		return stmt.getBoolean(columnIndex);
	}

	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: getBigDecimal");

		return stmt.getBigDecimal(columnIndex, scale);
	}

	public byte[] getBytes(int columnIndex) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: getBytes");

		return stmt.getBytes(columnIndex);
	}

	public java.util.UUID getGUID(int columnIndex) throws SQLException
	{
		if	(DEBUG)
			log(GXDBDebug.LOG_MAX, "Warning: getGUID");
		
		return java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"); 
	}	

	public java.sql.Date getDate(int columnIndex) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: getDate");

		return stmt.getDate(columnIndex);
	}

	public java.sql.Time getTime(int columnIndex) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX,"Warning: getTime");

		return stmt.getTime(columnIndex);
	}

	public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX,"Warning: getTimestamp");

		return stmt.getTimestamp(columnIndex);
	}

	public Object getObject(int columnIndex) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX,"Warning: getObject");

		return stmt.getObject(columnIndex);
	}

	public boolean wasNull() throws SQLException
	{
		return stmt.wasNull();
	}

	public void resetWasNullHits()
	{
	}

	private void log(int level, String text)
	{
		if	(DEBUG)
			con.log(level, this, text);
	}

	public BigDecimal getBigDecimal(int parameterIndex) throws SQLException
	{
		return stmt.getBigDecimal(parameterIndex);
	}

	public Object  getObject (int i, java.util.Map map) throws SQLException
	{
		return stmt.getObject(i, map);
	}


	public Blob getBlob (int i) throws SQLException
	{
		return stmt.getBlob(i);
	}

	public Array getArray (int i) throws SQLException
	{
		return stmt.getArray(i);
	}

	public Clob getClob (int i) throws SQLException
	{
		return stmt.getClob(i);
	}

	public Ref getRef (int i) throws SQLException
	{
		return stmt.getRef(i);
	}

	public java.sql.Date getDate(int parameterIndex, Calendar cal) throws SQLException
	{
		return stmt.getDate(parameterIndex, cal);
	}

	public java.sql.Time getTime(int parameterIndex, Calendar cal) throws SQLException
	{
		return stmt.getTime(parameterIndex, cal);
	}

	public java.sql.Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException
	{
		return stmt.getTimestamp(parameterIndex, cal);
	}

    public void registerOutParameter (int paramIndex, int sqlType, String typeName) throws SQLException
	{
		stmt.registerOutParameter(paramIndex, sqlType, typeName);
	}


	// Metodos agregados en JDK 1.4
	public void setURL(String parameterName, java.net.URL val)throws SQLException
	{
		stmt.setURL(parameterName, val);
	}
	public void setNull(String parameterName, int sqlType)throws SQLException
	{
		stmt.setNull(parameterName, sqlType);
	}
	public void setNull(String parameterName, int sqlType, String typeName)throws SQLException
	{
		stmt.setNull(parameterName, sqlType, typeName);
	}
	public void setBoolean(String a, boolean b)throws SQLException
	{
		stmt.setBoolean(a, b);
	}
	public void setByte(String a, byte b)throws SQLException
	{
		stmt.setByte(a, b);
	}
	public void setInt(String a, int b)throws SQLException
	{
		stmt.setInt(a, b);
	}
	public void setShort(String a, short b)throws SQLException
	{
		stmt.setShort(a, b);
	}
	public void setLong(String a, long b)throws SQLException
	{
		stmt.setLong(a, b);
	}
	public void setFloat(String a, float b)throws SQLException
	{
		stmt.setFloat(a, b);
	}
	public void setDouble(String a, double b)throws SQLException
	{
		stmt.setDouble(a, b);
	}
	public void setBigDecimal(String a, BigDecimal b)throws SQLException
	{
		stmt.setBigDecimal(a, b);
	}
	public void setBytes(String a, byte[] b)throws SQLException
	{
		stmt.setBytes(a, b);
	}
	public void setString(String a, String b)throws SQLException
	{
		stmt.setString(a, b);
	}
	public void setDate(String a, Date b)throws SQLException
	{
		stmt.setDate(a, b);
	}
	public void setDate(String a, Date b, Calendar c)throws SQLException
	{
		stmt.setDate(a, b, c);
	}
	public void setTime(String a, Time b)throws SQLException
	{
		stmt.setTime(a, b);
	}
	public void setTime(String a, Time b, Calendar c)throws SQLException
	{
		stmt.setTime(a, b, c);
	}
	public void setTimestamp(String a, Timestamp b)throws SQLException
	{
		stmt.setTimestamp(a, b);
	}
	public void setTimestamp(String a, Timestamp b, Calendar c)throws SQLException
	{
		stmt.setTimestamp(a, b, c);
	}
	public void setAsciiStream(String a, InputStream b, int c)throws SQLException
	{
		stmt.setAsciiStream(a, b, c);
	}
	public void setBinaryStream(String a, InputStream b, int c)throws SQLException
	{
		stmt.setBinaryStream(a, b, c);
	}
	public void setCharacterStream(String a, Reader b, int c)throws SQLException
	{
		stmt.setCharacterStream(a, b, c);
	}
	public void setObject(String a, Object b, int c, int d)throws SQLException
	{
		stmt.setObject(a, b, c, d);
	}
	public void setObject(String a, Object b, int c)throws SQLException
	{
		stmt.setObject(a, b, c);
	}
	public void setObject(String a, Object b)throws SQLException
	{
		stmt.setObject(a, b);
	}
	public Array getArray(String parameterName) throws SQLException
	{
		return stmt.getArray(parameterName);
	}
	public BigDecimal getBigDecimal(String parameterName) throws SQLException
	{
		return stmt.getBigDecimal(parameterName);
	}
	public Blob getBlob(String parameterName) throws SQLException
	{
		return stmt.getBlob(parameterName);
	}
	public boolean getBoolean(String parameterName)throws SQLException
	{
		return stmt.getBoolean(parameterName);
	}
	public byte getByte(String parameterName) throws SQLException
	{
		return stmt.getByte(parameterName);
	}
	public byte[] getBytes(String parameterName) throws SQLException
	{
		return stmt.getBytes(parameterName);
	}
	public Clob getClob(String parameterName) throws SQLException
	{
		return stmt.getClob(parameterName);
	}
	public Date getDate(String parameterName) throws SQLException
	{
		return stmt.getDate(parameterName);
	}
	public Date getDate(String parameterName, Calendar cal) throws SQLException
	{
		return stmt.getDate(parameterName, cal);
	}
	public double getDouble(String parameterName) throws SQLException
	{
		return stmt.getDouble(parameterName);
	}
	public float getFloat(String parameterName) throws SQLException
	{
		return stmt.getFloat(parameterName);
	}
	public int getInt(String parameterName) throws SQLException
	{
		return stmt.getInt(parameterName);
	}
	public long getLong(String parameterName) throws SQLException
	{
		return stmt.getLong(parameterName);
	}
	public Object getObject(String parameterName) throws SQLException
	{
		return stmt.getObject(parameterName);
	}
	public Object getObject(String parameterName, Map map) throws SQLException
	{
		return stmt.getObject(parameterName, map);
	}
	public Ref getRef(String parameterName) throws SQLException
	{
		return stmt.getRef(parameterName);
	}
	public short getShort(String parameterName) throws SQLException
	{
		return stmt.getShort(parameterName);
	}
	public String getString(String parameterName) throws SQLException
	{
		return stmt.getString(parameterName);
	}
	public Time getTime(String parameterName) throws SQLException
	{
		return stmt.getTime(parameterName);
	}
	public Time getTime(String parameterName, Calendar cal) throws SQLException
	{
		return stmt.getTime(parameterName, cal );
	}
	public Timestamp getTimestamp(String parameterName) throws SQLException
	{
		return stmt.getTimestamp(parameterName);
	}
	public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException
	{
		return stmt.getTimestamp(parameterName, cal );
	}
	public java.net.URL getURL(int parameterIndex) throws SQLException
	{
		return stmt.getURL(parameterIndex);
	}
	public java.net.URL getURL(String parameterName) throws SQLException
	{
		return stmt.getURL(parameterName);
	}
	public void registerOutParameter(String parameterName, int sqlType) throws SQLException
	{
		stmt.registerOutParameter(parameterName, sqlType);
	}
	public void registerOutParameter(String parameterName, int sqlType, int scale)throws SQLException
	{
		stmt.registerOutParameter(parameterName, sqlType, scale);
	}
	public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException
	{
		stmt.registerOutParameter(parameterName, sqlType, typeName);
	}

	/*** New methods in Java's 1.6 PreparedStatement interface. These are stubs. ***/
	public RowId getRowId(int parameterIndex) throws SQLException {return null;}
    public RowId getRowId(String parameterName) throws SQLException {return null;}
    public void setRowId(String parameterName, RowId x) throws SQLException {}
    public void setNString(String parameterName, String value) throws SQLException {}
    public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {}
    public void setNClob(String parameterName, NClob value) throws SQLException {}
    public void setClob(String parameterName, Reader reader, long length) throws SQLException {}
    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {}
    public void setNClob(String parameterName, Reader reader, long length) throws SQLException {}
    public NClob getNClob(int parameterIndex) throws SQLException {return null;}
    public NClob getNClob(String parameterName) throws SQLException {return null;}
   	public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {}
    public SQLXML getSQLXML(int parameterIndex) throws SQLException {return null;}
    public SQLXML getSQLXML(String parameterName) throws SQLException {return null;}
    public String getNString(int parameterIndex) throws SQLException {return null;}
    public String getNString(String parameterName) throws SQLException {return null;}
    public Reader getNCharacterStream(int parameterIndex) throws SQLException {return null;}
    public Reader getNCharacterStream(String parameterName) throws SQLException {return null;}
    public Reader getCharacterStream(int parameterIndex) throws SQLException {return null;}
    public Reader getCharacterStream(String parameterName) throws SQLException {return null;}
    public void setBlob(String parameterName, Blob x) throws SQLException {}
    public void setClob(String parameterName, Clob x) throws SQLException {}
    public void setAsciiStream(String parameterName, InputStream x, long length)throws SQLException {}
    public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {}
    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {}
    public void setAsciiStream(String parameterName, InputStream x) throws SQLException {}
    public void setBinaryStream(String parameterName, InputStream x) throws SQLException {}
    public void setCharacterStream(String parameterName, Reader reader) throws SQLException {}
    public void setNCharacterStream(String parameterName, Reader value) throws SQLException {}
    public void setClob(String parameterName, Reader reader) throws SQLException {}
    public void setBlob(String parameterName, InputStream inputStream) throws SQLException {}
    public void setNClob(String parameterName, Reader reader) throws SQLException {}
    /*** End of new methods. ***/

    /** New methods in Java 7 **/
	public <T> T getObject(int i, Class<T> aClass) throws SQLException {
		return null;
	}

	public <T> T getObject(String s, Class<T> aClass) throws SQLException {
		return null;
	}

	public void closeOnCompletion() throws SQLException { }

	public boolean isCloseOnCompletion() throws SQLException {
		return false;
	}
	/*** End of new methods. ***/
}
