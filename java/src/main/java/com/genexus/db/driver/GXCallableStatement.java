package com.genexus.db.driver;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;
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
* No tiene sincronizaci�n, dado que los ResultSets nunca son usados simult�neamente desde
* dos threads.
* <p>
* Las funciones que no deber�an ser llamadas nunca, generan una l�nea con "Warning" en el Log.
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
		//JMX Counters
		con.incNumberRequest();
		con.setSentenceLastRequest(getSqlStatement());
		
		boolean ret;
		if(!con.isReadOnly())
		{ // Si la conexion es RO no pongo uncommited changes porque no se deberian realizar cambios y ademas
		  // no me van a hacer commit/rollback
			con.setUncommitedChanges();
		}

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MIN, "Executing callable statement");
			try
			{
				ret = con.getDBMS().execute(stmt);
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
			ret = con.getDBMS().execute(stmt);
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
		if ((con.getDBMS().getId() == GXDBMS.DBMS_ORACLE || con.getDBMS().getId() == GXDBMS.DBMS_DAMENG) && sqlType == Types.BIT)
		{
			sqlType = PLSQL_BOOLEAN;
		}
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
		return GXDbFile.resolveUri(getVarchar(columnIndex), absPath);
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
		return this.getGXDateTime( columnIndex, false);
	}

	public java.util.Date getGXDateTime(int columnIndex, boolean hasMilliSeconds) throws SQLException
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

	public BigDecimal getBigDecimal(int columnIndex) throws SQLException
	{
		BigDecimal value;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "getBigDecimal - index : " + columnIndex);

			try
			{
				value = stmt.getBigDecimal(columnIndex);
				if (stmt.wasNull() || value == null) value = BigDecimal.ZERO;
				log(GXDBDebug.LOG_MAX, "getBigDecimal - value : " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			value = stmt.getBigDecimal(columnIndex);
			if (stmt.wasNull() || value == null) value = BigDecimal.ZERO;
		}

		return value;
	}

	@Deprecated
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException
	{
		BigDecimal value;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "getBigDecimal - index : " + columnIndex);

			try
			{
				value = stmt.getBigDecimal(columnIndex, scale);
				if (stmt.wasNull() || value == null) value = BigDecimal.ZERO;
				log(GXDBDebug.LOG_MAX, "getBigDecimal - value : " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			value = stmt.getBigDecimal(columnIndex, scale);
			if (stmt.wasNull() || value == null) value = BigDecimal.ZERO;
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

	public Object  getObject (int i, java.util.Map<java.lang.String,java.lang.Class<?>> map) throws SQLException
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
	public Object getObject(String parameterName, Map<java.lang.String,java.lang.Class<?>> map) throws SQLException
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

	public Reader getCharacterStream(int arg0) throws SQLException {
		return stmt.getCharacterStream(arg0);
	}

	public Reader getCharacterStream(String arg0) throws SQLException {
		return stmt.getCharacterStream(arg0);
	}

	public Reader getNCharacterStream(int arg0) throws SQLException {
		return stmt.getNCharacterStream(arg0);
	}

	public Reader getNCharacterStream(String arg0) throws SQLException {
		return stmt.getNCharacterStream(arg0);
	}

	public NClob getNClob(int arg0) throws SQLException {
		return stmt.getNClob(arg0);
	}

	public NClob getNClob(String arg0) throws SQLException {
		return stmt.getNClob(arg0);
	}

	public String getNString(int arg0) throws SQLException {
		return stmt.getNString(arg0);
	}

	public String getNString(String arg0) throws SQLException {
		return stmt.getNString(arg0);
	}

	public RowId getRowId(int arg0) throws SQLException {
		return stmt.getRowId(arg0);
	}

	public RowId getRowId(String arg0) throws SQLException {
		return stmt.getRowId(arg0);
	}

	public SQLXML getSQLXML(int arg0) throws SQLException {
		return stmt.getSQLXML(arg0);
	}

	public SQLXML getSQLXML(String arg0) throws SQLException {
		return stmt.getSQLXML(arg0);
	}

	public void setAsciiStream(String arg0, InputStream arg1)
			throws SQLException {
		stmt.setAsciiStream(arg0, arg1);

	}

	public void setAsciiStream(String arg0, InputStream arg1, long arg2)
			throws SQLException {
		stmt.setAsciiStream(arg0, arg1, arg2);

	}

	public void setBinaryStream(String arg0, InputStream arg1)
			throws SQLException {
		stmt.setBinaryStream(arg0, arg1);

	}

	public void setBinaryStream(String arg0, InputStream arg1, long arg2)
			throws SQLException {
		stmt.setBinaryStream(arg0, arg1, arg2);

	}

	public void setBlob(String arg0, Blob arg1) throws SQLException {
		stmt.setBlob(arg0, arg1);

	}

	public void setBlob(String arg0, InputStream arg1) throws SQLException {
		stmt.setBlob(arg0, arg1);

	}

	public void setBlob(String arg0, InputStream arg1, long arg2)
			throws SQLException {
		stmt.setBlob(arg0, arg1, arg2);

	}

	public void setCharacterStream(String arg0, Reader arg1)
			throws SQLException {
		stmt.setCharacterStream(arg0, arg1);

	}

	public void setCharacterStream(String arg0, Reader arg1, long arg2)
			throws SQLException {
		stmt.setCharacterStream(arg0, arg1, arg2);

	}

	public void setClob(String arg0, Clob arg1) throws SQLException {
		stmt.setClob(arg0, arg1);

	}

	public void setClob(String arg0, Reader arg1) throws SQLException {
		stmt.setClob(arg0, arg1);

	}

	public void setClob(String arg0, Reader arg1, long arg2)
			throws SQLException {
		stmt.setClob(arg0, arg1, arg2);

	}

	public void setNCharacterStream(String arg0, Reader arg1)
			throws SQLException {
		stmt.setNCharacterStream(arg0, arg1);

	}

	public void setNCharacterStream(String arg0, Reader arg1, long arg2)
			throws SQLException {
		stmt.setNCharacterStream(arg0, arg1, arg2);

	}

	public void setNClob(String arg0, NClob arg1) throws SQLException {
		stmt.setNClob(arg0, arg1);

	}

	public void setNClob(String arg0, Reader arg1) throws SQLException {
		stmt.setNClob(arg0, arg1);

	}

	public void setNClob(String arg0, Reader arg1, long arg2)
			throws SQLException {
		stmt.setNClob(arg0, arg1, arg2);

	}

	public void setNString(String arg0, String arg1) throws SQLException {
		stmt.setNString(arg0, arg1);  
	}

	public void setRowId(String arg0, RowId arg1) throws SQLException {
		stmt.setRowId(arg0, arg1);

	}

	public void setSQLXML(String arg0, SQLXML arg1) throws SQLException {
		stmt.setSQLXML(arg0, arg1);
		
	}

	public <T> T getObject(String parameterName,
              Class<T> type)
            throws SQLException {
		return null;	
	}

	public <T> T getObject(int parameterIndex,
              Class<T> type)
            throws SQLException {
		return null;	
	}



}
