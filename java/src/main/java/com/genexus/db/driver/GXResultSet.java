package com.genexus.db.driver;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

import com.genexus.*;
import com.genexus.db.IGXResultSet;
import com.genexus.internet.HttpContext;
import com.genexus.util.GXFile;
import com.genexus.util.GXServices;

public final class GXResultSet implements ResultSet, com.genexus.db.IFieldGetter, IGXResultSet
{
	private static final boolean DEBUG = DebugFlag.DEBUG;

	public static boolean longVarCharAsOracleLong = false;

	private ResultSet result;
	private Statement stmt;
	GXConnection con;
	private AtomicBoolean closed = new AtomicBoolean(false);
	private int handle;
	private ConnectionPool pool;
	private long resultRegBytes;

	public GXResultSet(ResultSet result, Statement stmt, GXConnection con, int handle)
	{
		this.result = result;
		this.stmt   = stmt;
		this.con 	= con;
		this.handle = handle;

		pool = con.getPool();
		if	(pool != null)
		{
			pool.getDBConnectionState().addResultSet(handle, con, this);
		}
	}

	public boolean wasNull() throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "wasNull : " + result.wasNull());
			return result.wasNull();
		}
		else
		{
			return result.wasNull();
		}
	}

	public void resetWasNullHits()
	{
	}

	// -- Funciones que deberian ser llamadas desde los programas generados
	public boolean next() throws SQLException
	{
		resultRegBytes = 0;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "next");
			try
			{
				return result.next();
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{
			return result.next();
		}
	}

	public void close() throws SQLException {
                    SQLException disconnectException = null;
		if (isClosed())
			return;

		closed.set(true);
		if (DEBUG) {
			log(GXDBDebug.LOG_MAX, "close");
			}
		if (pool != null) {
			pool.getDBConnectionState().removeResultSet(handle, con, this);
			}
		try {
			con.setNotInUse((GXPreparedStatement) stmt);
		} catch (Throwable e) {
			System.err.println("e " + e.getMessage());
		}
		try {
                          result.close();
		} catch (SQLException sqlException) {
                    disconnectException = sqlException;
		}


		result = null;

                if (disconnectException != null)
                   throw disconnectException;
	}

	public GXConnection getConnection()
	{
		return con;
	}

	public long getResultRegBytes()
	{
		return resultRegBytes;
	}

	public String getLongVarchar(int columnIndex) throws SQLException
	{
		String value;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "getLongVarchar - index : " + columnIndex);

			try
			{
				if((con.getDBMS() instanceof GXDBMSoracle7) && !longVarCharAsOracleLong)
				{
					//Ahora los LongVarChar en Oracle se crean como CLOB
					//Intento leerlo como CLOB y si falla lo leo como un LONG
					try
					{
						value = getClobString(getClob(columnIndex));
					}
					catch(SQLException exc)
					{
						value = result.getString(columnIndex);
					}
				}
				else
				{
					value = result.getString(columnIndex);
				}

				// En algunos drivers viene en null
				if	(result.wasNull() || value == null) value = "";

				// Para los longVarchar no despliego el valor sino el largo.
				log(GXDBDebug.LOG_MAX, "getLongVarchar - real length: " + value.length() + " V " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{
			if((con.getDBMS() instanceof GXDBMSoracle7) && !longVarCharAsOracleLong)
			{
				//Ahora los LongVarChar en Oracle se crean como CLOB
				//Intento leerlo como CLOB y si falla lo leo como un LONG
				try
				{
					value = getClobString(getClob(columnIndex));
				}
				catch(SQLException exc)
				{
					value = result.getString(columnIndex);
				}
			}
			else
			{
				value = result.getString(columnIndex);
			}
			// En algunos drivers viene en null
			if	(result.wasNull() || value == null) value = "";
		}

		resultRegBytes += value.length();
		return value;
	}

	private String getClobString(Clob clob) throws SQLException
	{
		if(clob == null) return "";

		char[] cbuf = new char[(int)clob.length()];
		try
		{
			clob.getCharacterStream().read(cbuf);
		}
		catch(IOException ioException)
		{
			System.err.println("Error reading CLOB column");
		}
		return new String(cbuf);
	}
	public String getVarchar(int columnIndex) throws SQLException
	{
		String value;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "getVarchar - index : " + columnIndex );

			try
			{
				value = result.getString(columnIndex);
				if	(result.wasNull())
					value = "";

				log(GXDBDebug.LOG_MAX, "getString - value : " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{
			value = result.getString(columnIndex);
			if	(result.wasNull())
				value = "";
		}

		resultRegBytes += value.length();
		return value;
	}

	public String getString(int columnIndex, int length) throws SQLException
	{
		switch(con.getDBMS().getId())
		{
			case GXDBMS.DBMS_HANA:
			case GXDBMS.DBMS_SERVICE:
				return getVarchar(columnIndex);
			default: break;
		}
		
		String value;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "getString - index : " + columnIndex + " length : " + length);

			try
			{
				value = result.getString(columnIndex);
				if (result.wasNull() || value == null)
					value = CommonUtil.replicate(" ", length);
				else
					value = String.format(String.format("%%-%ds", length), value);

				log(GXDBDebug.LOG_MAX, "getString - value : " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(handle, sqlException);
				throw sqlException;
			}
		} else {
			value = result.getString(columnIndex);
			if (result.wasNull() || value == null)
				value = "";
			else
				value = String.format(String.format("%%-%ds", length), value);
		}

		resultRegBytes += value.length();
		return value;
	}

	public byte getByte(int columnIndex) throws SQLException
	{
		byte value;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "getByte - index : " + columnIndex);

			try
			{
				value = (byte) result.getInt(columnIndex);

				//value = result.getByte(columnIndex);
				if	(result.wasNull()) value = 0;
				log(GXDBDebug.LOG_MAX, "getByte - value : " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{
			value = (byte) result.getInt(columnIndex);
			//value = result.getByte(columnIndex);
			if	(result.wasNull()) value = 0;
		}

		resultRegBytes += 1;
		return value;
	}

	public short getShort(int columnIndex) throws SQLException
	{
		short value;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "getShort - index : " + columnIndex);

			try
			{
				value = result.getShort(columnIndex);
				if	(result.wasNull()) value = 0;
				log(GXDBDebug.LOG_MAX, "getShort - value : " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{
			value = result.getShort(columnIndex);
			if	(result.wasNull()) value = 0;
		}

		resultRegBytes += 2;
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
				value = result.getInt(columnIndex);
				if (result.wasNull()) value = 0;
				log(GXDBDebug.LOG_MAX, "getInt - value : " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{
		 	value = result.getInt(columnIndex);
		 	if (result.wasNull()) value = 0;
		}

		resultRegBytes += 4;
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
				value = result.getLong(columnIndex);
				if (result.wasNull()) value = 0;
				log(GXDBDebug.LOG_MAX, "getLong - value : " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{
		 	value = result.getLong(columnIndex);
		 	if (result.wasNull()) value = 0;
		}

		resultRegBytes += 8;
		return value;
	}

	public float getFloat(int columnIndex) throws SQLException
	{
		float value;

		if	(DEBUG )
		{
			log(GXDBDebug.LOG_MAX, "getFloat - index : " + columnIndex);

			try
			{
				value = result.getFloat(columnIndex);
				if (result.wasNull()) value = 0;
				log(GXDBDebug.LOG_MAX, "getFloat - value : " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{
		 	value = result.getFloat(columnIndex);
		 	if (result.wasNull()) value = 0;
		}

		resultRegBytes += 4;
		return value;
	}

	public double getDouble(int columnIndex) throws SQLException
	{
		double value ;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "getDouble - index : " + columnIndex);

			try
			{
				value = result.getDouble(columnIndex);
				if (result.wasNull()) value = 0;
				log(GXDBDebug.LOG_MAX, "getDouble - value : " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{
		 	value = result.getDouble(columnIndex);
		 	if (result.wasNull()) value = 0;
		}

		resultRegBytes += 8;
		return value;
	}

	public java.util.Date getGXDateTime(int columnIndex) throws SQLException
	{
		return getGXDateTime(columnIndex,false);
	}

	public java.util.Date getGXDateTime(int columnIndex, boolean hasMilliSeconds) throws SQLException
	{
		java.util.Date value = null;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "getGXDateTime - index : " + columnIndex);

			try
			{
				value = result.getTimestamp(columnIndex);

				if	(result.wasNull() || con.isNullDateTime(value))
					value = CommonUtil.nullDate();
				else if	(con.isNullDate(value))
					value = CommonUtil.resetDate(value);
				else
				{
					if(!hasMilliSeconds)
						value = CommonUtil.resetMillis(new java.util.Date(value.getTime()));
					
					if (ModelContext.getModelContext() != null)
						value = ModelContext.getModelContext().DBserver2local(value);
				}
 				log(GXDBDebug.LOG_MAX, "getGXDateTime - value2 : " + value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{

		 	value = result.getTimestamp(columnIndex);

			if	(result.wasNull() || con.isNullDateTime(value))
				value = CommonUtil.nullDate();
			else if	(con.isNullDate(value))
				value = CommonUtil.resetDate(value);
			else
			{
				if(!hasMilliSeconds)
				{
					value = CommonUtil.resetMillis(new java.util.Date(value.getTime()));
				}
				if (ModelContext.getModelContext() != null)
					value = ModelContext.getModelContext().DBserver2local(value);
			}
		}
		resultRegBytes += 8;		
		return value;
	}

	public java.util.Date getGXDate(int columnIndex) throws SQLException
	{
		resultRegBytes += 8;
		java.util.Date value = null;

		boolean retryReadDate = false;
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "getGXDate - index : " + columnIndex);

			try
			{
				if (con.getDBMS().useCharInDate())
				{
					try
					{					
						String valueString = getString(columnIndex);
						log(GXDBDebug.LOG_MAX, "getGXDate - stringRead : " + valueString);
	
						if	(result.wasNull())
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
					catch(Exception e)
					{
						retryReadDate = true;
					}					
				}
				
				if (!con.getDBMS().useCharInDate() || retryReadDate)
				{
					if	(con.getDBMS().useDateTimeInDate())
					{
						value = result.getTimestamp(columnIndex);
						log(GXDBDebug.LOG_MAX, "getGXDate/DateTime - value1 : " + value);
					}
					else
					{
						value = result.getDate(columnIndex);
						log(GXDBDebug.LOG_MAX, "getGXDate/Date - value1 : " + value);
					}

					if	(result.wasNull())
						value = CommonUtil.nullDate();
					else
					{
						value = CommonUtil.resetTime(value);
						if (con.isNullCleanDate(value))
						{
							value = CommonUtil.nullDate();
						}
					}
					log(GXDBDebug.LOG_MAX, "getGXDate - value2 : " + value);
				}
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{

			if (con.getDBMS().useCharInDate())
			{
				try
				{				
					String valueString = getString(columnIndex);
	
					if	(result.wasNull())
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
				catch(Exception e)
				{
					retryReadDate = true;
				}					
			}
			
			if (!con.getDBMS().useCharInDate() || retryReadDate)
			{
				if	(con.getDBMS().useDateTimeInDate())
				{
					value = result.getTimestamp(columnIndex);
				}
				else
				{
					value = result.getDate(columnIndex);
				}

				if	(result.wasNull())
					value = CommonUtil.nullDate();
				else
				{
					value = CommonUtil.resetTime(value);
					if (con.isNullCleanDate(value))
					{
						value = CommonUtil.nullDate();
					}
				}
					
			}
		}

		return value;
	}

	// -- Funciones que NO deberian ser llamadas desde los programas generados

	public String getString(int columnIndex) throws SQLException
	{
		if	(DEBUG )
			log(GXDBDebug.LOG_MAX, "Warning: getString");

		return result.getString(columnIndex);
	}

	public boolean getBoolean(int columnIndex) throws SQLException
	{
		if	(DEBUG)
			log(GXDBDebug.LOG_MAX, "Warning: getBoolean");

		return result.getBoolean(columnIndex);
	}

	@Deprecated
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException
	{
		BigDecimal ret;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "getBigDecimal - index : " + columnIndex);

			try
			{
				try
				{
					ret = result.getBigDecimal(columnIndex);
					if (ret != null)
					{
						ret = ret.setScale(scale, BigDecimal.ROUND_HALF_UP);
					}
				}
				catch(java.lang.AbstractMethodError e)
				{
					ret = result.getBigDecimal(columnIndex, scale);
				}
			}
			catch (java.lang.ArithmeticException aException)
			{
				if	(con.isLogEnabled()) con.logSQLException(handle, aException);
				throw aException;
			}			
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{
			try
			{			
				ret = result.getBigDecimal(columnIndex);
				if (ret != null)
				{
					ret = ret.setScale(scale, BigDecimal.ROUND_HALF_UP);
				}
			}
			catch(java.lang.AbstractMethodError e)
			{
				ret = result.getBigDecimal(columnIndex, scale);
			}			
		}

		if	(ret == null)
			return new BigDecimal(0);

		return ret;
	}

	public byte[] getBytes(int columnIndex) throws SQLException
	{
		if	(DEBUG )
			log(GXDBDebug.LOG_MAX, "Warning: getBytes");

		return result.getBytes(columnIndex);
	}

	public java.util.UUID getGUID(int columnIndex) throws SQLException
	{
		java.util.UUID value;
		String valueString;
		
		if (con.getDBMS().getId() == GXDBMS.DBMS_HANA)
		{		
			valueString = getVarchar(columnIndex);
		}
		else
		{
			valueString = result.getString(columnIndex);
		}

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "getGUID - index : " + columnIndex);

			try
			{
				if	(valueString == null  || result.wasNull())
					value = java.util.UUID.fromString("00000000-0000-0000-0000-000000000000");
				else
					value = java.util.UUID.fromString(valueString);

				log(GXDBDebug.LOG_MAX, "getGUID - value : " + value.toString());
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{
			
			if	(valueString == null || result.wasNull())
				value = java.util.UUID.fromString("00000000-0000-0000-0000-000000000000");
			else
				value = java.util.UUID.fromString(valueString);
		}

		return value;
	}		
	
	public java.sql.Date getDate(int columnIndex) throws SQLException
	{
		if	(DEBUG)
			log(GXDBDebug.LOG_MAX, "Warning: getDate");

		return result.getDate(columnIndex);
	}

	public java.sql.Time getTime(int columnIndex) throws SQLException
	{
		if	(DEBUG)
			log(GXDBDebug.LOG_MAX, "Warning: getTime");

		return result.getTime(columnIndex);
	}

	public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException
	{
		if	(DEBUG)
			log(GXDBDebug.LOG_MAX, "Warning: getTimestamp");

		return result.getTimestamp(columnIndex);
	}

	public java.io.InputStream getAsciiStream(int columnIndex) throws SQLException
	{
		if	(DEBUG)
			log(GXDBDebug.LOG_MAX, "Warning: getAsciiStream");

		return result.getAsciiStream(columnIndex);
	}

	@Deprecated
	public java.io.InputStream getUnicodeStream(int columnIndex) throws SQLException
	{
		if	(DEBUG )
			log(GXDBDebug.LOG_MAX, "Warning: getUnicodeStream");

		return result.getUnicodeStream(columnIndex);
	}

	public String getBLOBFile(int columnIndex) throws SQLException
	{
		return getBLOBFile(columnIndex, "tmp");
	}
	
	public String getBLOBFile(int columnIndex, String extension) throws SQLException
	{
		return getBLOBFile(columnIndex, extension, "");
	}

	public String getBLOBFile(int columnIndex, String extension, String name) throws SQLException
	{
		String fileName = getBlobFileName(name, extension);
		return getBLOBFile(columnIndex, extension, name, fileName, true);
	}

	private String getBLOBFile(int columnIndex, String extension, String name, String fileName, boolean temporary) throws SQLException
	{
		try
		{
			InputStream source = getBinaryStream(columnIndex);

            byte[] xbuffer = new byte[1];
			int firstByte = 0;
			int secondByte = 0;
			if (source != null)
			{
				firstByte = source.read(xbuffer);
				if (con.getDBMS() instanceof GXDBMSoracle7)
				{
					secondByte = source.read();
				}
			}

            boolean isEmptyBuffer = false;
            if((source != null) && (source.available() == 0) && (firstByte != -1) && (xbuffer[0] == 0))
            {//Si en el InputStream no queda mas por leer y el unico byte leido es un 0
             //se retorna el string vacio.
              isEmptyBuffer = true;
            }

			if	(result.wasNull() || source == null || firstByte == -1 || (con.getDBMS() instanceof GXDBMSoracle7 && secondByte == -1) || isEmptyBuffer)
			{
				if(source != null)
				{
					source.close();
				}
				return "";
			}

			if (Application.getGXServices().get(GXServices.STORAGE_SERVICE) == null)
			{
				File path = new File( fileName).getParentFile();
				if (!path.exists())
					path.mkdirs();
			}
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			OutputStream destination = new BufferedOutputStream(os);
			byte[] buffer = new byte[4096];
			int bytes_read;

			destination.write(xbuffer, 0, 1);
			if (con.getDBMS() instanceof GXDBMSoracle7)
			{
				destination.write(secondByte);
			}

			while (true)
			{
		    	bytes_read = source.read(buffer);
			    if (bytes_read == -1)
			    	break;
				destination.write(buffer, 0, bytes_read);
			}
			source.close();
			destination.close();

			InputStream is = new ByteArrayInputStream(os.toByteArray());
			GXFile gxFile = new GXFile(fileName);
		  	gxFile.create(is);
			
			fileName = gxFile.getFilePath();
			
			if (temporary)
			{
					com.genexus.webpanels.BlobsCleaner.getInstance().addBlobFile(fileName);
			}
		}
		catch (IOException e)
		{
			throw new SQLException("Can't read BLOB field into " + fileName);
		}

		return fileName;
	}
	
	public String getMultimediaFile(int columnIndex, String gxdbFileUri) throws SQLException
	{
		if (!GXDbFile.isFileExternal(gxdbFileUri))
		{
			String fileName = GXDbFile.getFileNameFromUri(gxdbFileUri);
			if (fileName.trim().length() != 0)
			{
				String filePath = "";
				if (Application.getGXServices().get(GXServices.STORAGE_SERVICE) == null)
				{ 
					String multimediaDir = com.genexus.Preferences.getDefaultPreferences().getMultimediaPath();
					filePath = multimediaDir + File.separator + fileName;
				}
				else
				{
					filePath = com.genexus.Preferences.getDefaultPreferences().getProperty("CS_BLOB_PATH", "").trim() + "/" + GXDbFile.getMultimediaDirectory() + "/" + fileName;
				}
				GXFile file = new GXFile(filePath);
				if (file.exists())
				{
					return file.getAbsolutePath();
				}
				else
				{
					return getBLOBFile(columnIndex, CommonUtil.getFileType(gxdbFileUri), CommonUtil.getFileName(gxdbFileUri), filePath, false);
				}
			}
		}

		return "";
	}

	public String getMultimediaUri(int columnIndex) throws SQLException
	{
		return GXDbFile.resolveUri(getVarchar(columnIndex));
	}

	private static String lastBlobsDir = "";
	private String getBlobFileName(String name, String extension)
	{
		String blobPath = com.genexus.Preferences.getDefaultPreferences().getBLOB_PATH();
		String fileName = com.genexus.PrivateUtilities.getTempFileName(blobPath, name, extension, true);
		if (Application.getGXServices().get(GXServices.STORAGE_SERVICE) == null)
		{
			File file = new File(fileName);
			
			if(!file.isAbsolute())
			{
				if(!lastBlobsDir.equals(""))
				{
					return lastBlobsDir + fileName;
				}
				if(con.getContext() != null)
				{
					com.genexus.internet.HttpContext webContext = (HttpContext) con.getContext().getHttpContext();
					if((webContext != null) && (webContext instanceof com.genexus.webpanels.HttpContextWeb))
					{
						try
						{
							String origFileName = fileName;
							fileName = ((com.genexus.webpanels.HttpContextWeb)webContext).getRealPath(fileName);
							lastBlobsDir = fileName.substring(0, fileName.indexOf(origFileName));
						}
						catch(NullPointerException e)
						{//Hay casos en que al dar refresh de una pagina el Tomcat
						 //retorna una session nula y da NullPointerException.
					     //Teoricamente el directorio es siempre el mismo, entonces
						 //lo que hago es guardar el ultimo path "satisfactorio" y usarlo
						 //en el caso de error.
							if(!lastBlobsDir.equals(""))
							{
								fileName = lastBlobsDir + fileName;
							}
						}
					}
				}
			}
		}
		else
		{
			fileName = fileName.replace(java.io.File.separator, "/");
		}
			
		
		return fileName;
	}

	public java.io.InputStream getBinaryStream(int columnIndex) throws SQLException
	{
		if	(DEBUG )
			log(GXDBDebug.LOG_MAX, "Warning: getBinaryStream");

		return result.getBinaryStream(columnIndex);
	}

	public Object getObject(int columnIndex) throws SQLException
	{
		if	(DEBUG )
			log(GXDBDebug.LOG_MAX, "Warning: getObject");

		return result.getObject(columnIndex);
	}

	public String getString(String columnName) throws SQLException
	{
		if	(DEBUG )
			log(GXDBDebug.LOG_MAX, "Warning: getString/2");

		return result.getString(columnName);
	}

	public boolean getBoolean(String columnName) throws SQLException
	{
		if	(DEBUG )
			log(GXDBDebug.LOG_MAX, "Warning: getBoolean/2");

		return result.getBoolean(columnName);
	}

	public byte getByte(String columnName) throws SQLException
	{
		if	(DEBUG )
			log(GXDBDebug.LOG_MAX, "Warning: getByte/2");

		return result.getByte(columnName);
	}

	public short getShort(String columnName) throws SQLException
	{
		if	(DEBUG)
			log(GXDBDebug.LOG_MAX , "Warning: getShort/2");

		return result.getShort(columnName);
	}

	public int getInt(String columnName) throws SQLException
	{
		if	(DEBUG)
			log(GXDBDebug.LOG_MAX, "Warning: getInt/2");

		return result.getInt(columnName);
	}

	public long getLong(String columnName) throws SQLException
	{
		if	(DEBUG)
			log(GXDBDebug.LOG_MAX, "Warning: getLong/2");

		return result.getLong(columnName);
	}

	public float getFloat(String columnName) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: getFloat/2");

		return result.getFloat(columnName);
	}

	public double getDouble(String columnName) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: getDouble/2");

		return result.getDouble(columnName);
	}

	@Deprecated
	public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: getBigDecimal/2");

		return result.getBigDecimal(columnName, scale);
	}

	public byte[] getBytes(String columnName) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: getBytes/2");

		return result.getBytes(columnName);
	}

	public java.sql.Date getDate(String columnName) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: getDate/2");

		return result.getDate(columnName);
	}

	public java.sql.Timestamp getTimestamp(String columnName) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: getTimestamp");

		return result.getTimestamp(columnName);
	}

	public java.sql.Time getTime(String columnName) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: getTime/2");

		return result.getTime(columnName);
	}

	public java.io.InputStream getAsciiStream(String columnName) throws SQLException
	{
		if	(DEBUG ) log(GXDBDebug.LOG_MAX, "Warning: getAsciiStream/2");

		return result.getAsciiStream(columnName);
	}

	@Deprecated
	public java.io.InputStream getUnicodeStream(String columnName) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: getUnicodeStream/2");

		return result.getUnicodeStream(columnName);
	}

	public java.io.InputStream getBinaryStream(String columnName) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: getBinaryStream/2");

		return result.getBinaryStream(columnName);
	}

	public Object getObject(String columnName) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: getObject/2");

		return result.getObject(columnName);
	}

	public SQLWarning getWarnings() throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: getWarnings");

		return result.getWarnings();
	}

	public void clearWarnings() throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: clearWarnings");

		result.clearWarnings();
	}

	public String getCursorName() throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: getCursorName");

		return result.getCursorName();
	}

	public ResultSetMetaData getMetaData() throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: getMetaData");

		return result.getMetaData();
	}

	public int findColumn(String columnName) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: findColumn");

		return result.findColumn(columnName);
	}

	private void log(int level, String text)
	{
		if	(DEBUG)
		{
			con.log(level, this, text);
		}
	}

    public java.io.Reader getCharacterStream(int columnIndex) throws SQLException
	{
		return result.getCharacterStream(columnIndex);
	}
    public java.io.Reader getCharacterStream(String columnName) throws SQLException
	{
		return result.getCharacterStream(columnName);
	}
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException
	{
		return result.getBigDecimal(columnIndex);
	}
    public BigDecimal getBigDecimal(String columnName) throws SQLException
	{
		return result.getBigDecimal(columnName);
	}

    public boolean isBeforeFirst() throws SQLException
	{
		return result.isBeforeFirst();
	}
    public boolean isFirst() throws SQLException
	{
		return result.isFirst();
	}
    public boolean isAfterLast() throws SQLException
	{
		return result.isAfterLast();
	}
    public boolean isLast() throws SQLException
	{
		return result.isLast();
	}
    public void beforeFirst() throws SQLException
	{
		result.beforeFirst();
	}
    public void afterLast() throws SQLException
	{
		result.afterLast();
	}
    public boolean first() throws SQLException
	{
		return result.first();
	}
    public boolean last() throws SQLException
	{
		return result.last();
	}
    public boolean previous() throws SQLException
	{
		return result.previous();
	}
    public boolean relative( int rows ) throws SQLException
	{
		return result.relative(rows);
	}
    public boolean absolute( int row ) throws SQLException
	{
		return result.absolute(row);
	}
    public int getRow() throws SQLException
	{
		return result.getRow();
	}
    public void setFetchSize(int rows) throws SQLException
	{
		result.setFetchSize(rows);
	}

    public int getFetchDirection() throws SQLException
	{
		return result.getFetchDirection();
	}

    public int getType() throws SQLException
	{
		return result.getType();
	}


    public void setFetchDirection(int direction) throws SQLException
	{
		result.setFetchDirection(direction);
	}

    public int getFetchSize() throws SQLException
	{
		return result.getFetchSize();
	}
    public int getConcurrency() throws SQLException
	{
		return result.getConcurrency();
	}

    public void updateShort(int columnIndex, short x) throws SQLException
	{
		result.updateShort(columnIndex, x);
	}

    public void updateShort(String columnIndex, short x) throws SQLException
	{
		result.updateShort(columnIndex, x);
	}


    public boolean rowInserted() throws SQLException
	{
		return result.rowInserted();
	}

    public void updateByte(int columnIndex, byte x) throws SQLException
	{
		result.updateByte(columnIndex, x);
	}

    public void updateByte(String columnIndex, byte x) throws SQLException
	{
		result.updateByte(columnIndex, x);
	}


    public boolean rowDeleted() throws SQLException
	{
		return result.rowDeleted();
	}

    public void updateNull(int columnIndex) throws SQLException
	{
		result.updateNull(columnIndex);
	}

    public void updateNull(String columnIndex) throws SQLException
	{
		result.updateNull(columnIndex);
	}


    public void updateBoolean(int columnIndex, boolean x) throws SQLException
	{
		result.updateBoolean(columnIndex, x);
	}

    public void updateBoolean(String columnIndex, boolean x) throws SQLException
	{
		result.updateBoolean(columnIndex, x);
	}


    public void updateBytes(int columnIndex, byte x[]) throws SQLException
	{
		result.updateBytes(columnIndex, x);
	}

    public void updateBytes(String columnIndex, byte x[]) throws SQLException
	{
		result.updateBytes(columnIndex, x);
	}

    public boolean rowUpdated() throws SQLException
	{
		return result.rowUpdated();
	}

    public void updateInt(int columnIndex, int x) throws SQLException
	{
		result.updateInt(columnIndex, x);
	}

    public void updateInt(String columnIndex, int x) throws SQLException
	{
		result.updateInt(columnIndex, x);
	}


    public void updateFloat(int columnIndex, float x) throws SQLException
	{
		result.updateFloat(columnIndex, x);
	}

    public void updateFloat(String columnIndex, float x) throws SQLException
	{
		result.updateFloat(columnIndex, x);
	}

    public void updateDate(int columnIndex, java.sql.Date x) throws SQLException
	{
		result.updateDate(columnIndex, x);
	}

    public void updateDate(String columnIndex, java.sql.Date x) throws SQLException
	{
		result.updateDate(columnIndex, x);
	}

    public void updateDouble(int columnIndex, double x) throws SQLException
	{
		result.updateDouble(columnIndex, x);
	}

    public void updateDouble(String columnIndex, double x) throws SQLException
	{
		result.updateDouble(columnIndex, x);
	}

    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException
	{
		result.updateBigDecimal(columnIndex, x);
	}

    public void updateBigDecimal(String columnIndex, BigDecimal x) throws SQLException
	{
		result.updateBigDecimal(columnIndex, x);
	}

    public void updateString(int columnIndex, String x) throws SQLException
	{
		result.updateString(columnIndex, x);
	}

    public void updateString(String columnIndex, String x) throws SQLException
	{
		result.updateString(columnIndex, x);
	}

    public void updateLong(int columnIndex, long x) throws SQLException
	{
		result.updateLong(columnIndex, x);
	}

    public void updateLong(String columnIndex, long x) throws SQLException
	{
		result.updateLong(columnIndex, x);
	}


    public void updateTime(String columnName, java.sql.Time x) throws SQLException
	{
		result.updateTime(columnName, x);
	}

    public void updateTime(int columnName, java.sql.Time x) throws SQLException
	{
		result.updateTime(columnName, x);
	}

    public void updateTimestamp(String columnName, java.sql.Timestamp x) throws SQLException
	{
		result.updateTimestamp(columnName, x);
	}

    public void updateTimestamp(int columnName, java.sql.Timestamp x) throws SQLException
	{
		result.updateTimestamp(columnName, x);
	}

    public void updateAsciiStream(String columnName,  java.io.InputStream x, int length) throws SQLException
	{
		result.updateAsciiStream(columnName, x, length);
	}

    public void updateAsciiStream(int columnName,  java.io.InputStream x, int length) throws SQLException
	{
		result.updateAsciiStream(columnName, x, length);
	}

    public void updateBinaryStream(int columnName, java.io.InputStream x, int length) throws SQLException
	{
		result.updateBinaryStream(columnName, x, length);
	}

    public void updateBinaryStream(String columnName, java.io.InputStream x, int length) throws SQLException
	{
		result.updateBinaryStream(columnName, x, length);
	}

    public void updateCharacterStream(String columnName, java.io.Reader reader,int length) throws SQLException
	{
		result.updateCharacterStream(columnName, reader, length);
	}

    public void updateCharacterStream(int columnName, java.io.Reader reader,int length) throws SQLException
	{
		result.updateCharacterStream(columnName, reader, length);
	}



    public void updateObject(String columnName, Object x, int scale) throws SQLException
	{
		result.updateObject(columnName, x, scale);
	}

    public void updateObject(String columnName, Object x) throws SQLException
	{
		result.updateObject(columnName, x);
	}

    public void updateObject(int columnName, Object x, int scale) throws SQLException
	{
		result.updateObject(columnName, x, scale);
	}

    public void updateObject(int columnName, Object x) throws SQLException
	{
		result.updateObject(columnName, x);
	}


    public void insertRow() throws SQLException
	{
		result.insertRow();

	}
    public void updateRow() throws SQLException
	{
		result.updateRow();
	}
    public void deleteRow() throws SQLException
	{
		result.deleteRow();
	}
    public void refreshRow() throws SQLException
	{
		result.refreshRow();
	}
    public void cancelRowUpdates() throws SQLException
	{
		result.cancelRowUpdates();
	}
    public void moveToInsertRow() throws SQLException
	{
		result.moveToInsertRow();
	}
    public void moveToCurrentRow() throws SQLException
	{
		result.moveToCurrentRow();
	}
    public Statement getStatement() throws SQLException
	{
		return result.getStatement();
	}


    public Object getObject(int i, java.util.Map<java.lang.String,java.lang.Class<?>> map) throws SQLException
	{
		return result.getObject(i, map);
	}

    public Ref getRef(int i) throws SQLException
	{
		return result.getRef(i);
	}
    public Blob getBlob(int i) throws SQLException
	{
		return result.getBlob(i);
	}
    public Clob getClob(int i) throws SQLException
	{
		return result.getClob(i);
	}
    public Array getArray(int i) throws SQLException
	{
		return result.getArray(i);
	}

    public Object getObject(String colName, java.util.Map<java.lang.String,java.lang.Class<?>> map) throws SQLException
	{
		return result.getObject(colName, map);
	}

    public Ref getRef(String colName) throws SQLException
	{
		return result.getRef(colName);
	}
    public Blob getBlob(String colName) throws SQLException
	{
		return result.getBlob(colName);
	}
    public Clob getClob(String colName) throws SQLException
	{
		return result.getClob(colName);
	}
    public Array getArray(String colName) throws SQLException
	{
		return result.getArray(colName);
	}

    public java.sql.Date getDate(int columnIndex, Calendar cal) throws SQLException
	{
		return result.getDate(columnIndex, cal);
	}

    public java.sql.Date getDate(String columnIndex, Calendar cal) throws SQLException
	{
		return result.getDate(columnIndex, cal);
	}


    public java.sql.Time getTime(int columnIndex, Calendar cal) throws SQLException
	{
		return result.getTime(columnIndex, cal);
	}
    public java.sql.Time getTime(String columnName, Calendar cal) throws SQLException
	{
		return result.getTime(columnName, cal);
	}

    public java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException
	{
		return result.getTimestamp(columnIndex, cal);
	}

    public java.sql.Timestamp getTimestamp(String columnIndex, Calendar cal) throws SQLException
	{
		return result.getTimestamp(columnIndex, cal);
	}


	// Metodos Agregados en el jdk1.4

	public java.net.URL getURL(int c) throws SQLException
	{
		return result.getURL(c);
	}
	
	public java.net.URL getURL(String c) throws SQLException
	{
		return result.getURL(c);
	}
	
	public void updateRef(int c, Ref x)throws SQLException
	{
		result.updateRef(c, x);
	}
		
	public void updateRef(String c, Ref x)throws SQLException
	{
		result.updateRef(c, x);
	}
	
	public void updateBlob(int c, Blob x)throws SQLException
	{
		result.updateBlob(c, x);
	}

	public void updateBlob(String c, Blob x)throws SQLException
	{
		result.updateBlob(c, x);
	}

	public void updateClob(int c, Clob x)throws SQLException
	{
		result.updateClob(c, x);
	}

	public void updateClob(String c, Clob x)throws SQLException
	{
		result.updateClob(c, x);
	}

	public void updateArray(int c, Array x)throws SQLException
	{
		result.updateArray(c, x);
	}

	public void updateArray(String c, Array x)throws SQLException
	{
		result.updateArray(c, x);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getHoldability() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public Reader getNCharacterStream(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Reader getNCharacterStream(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public NClob getNClob(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public NClob getNClob(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNString(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNString(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public RowId getRowId(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public RowId getRowId(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public SQLXML getSQLXML(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public SQLXML getSQLXML(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isClosed() throws SQLException {
		return closed.get();
	}

	public void updateAsciiStream(int arg0, InputStream arg1)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateAsciiStream(String arg0, InputStream arg1)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateAsciiStream(int arg0, InputStream arg1, long arg2)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateAsciiStream(String arg0, InputStream arg1, long arg2)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateBinaryStream(int arg0, InputStream arg1)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateBinaryStream(String arg0, InputStream arg1)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateBinaryStream(int arg0, InputStream arg1, long arg2)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateBinaryStream(String arg0, InputStream arg1, long arg2)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateBlob(int arg0, InputStream arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateBlob(String arg0, InputStream arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateBlob(int arg0, InputStream arg1, long arg2)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateBlob(String arg0, InputStream arg1, long arg2)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateCharacterStream(int arg0, Reader arg1)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateCharacterStream(String arg0, Reader arg1)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateCharacterStream(int arg0, Reader arg1, long arg2)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateCharacterStream(String arg0, Reader arg1, long arg2)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateClob(int arg0, Reader arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateClob(String arg0, Reader arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateClob(int arg0, Reader arg1, long arg2)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateClob(String arg0, Reader arg1, long arg2)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNCharacterStream(int arg0, Reader arg1)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNCharacterStream(String arg0, Reader arg1)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNCharacterStream(int arg0, Reader arg1, long arg2)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNCharacterStream(String arg0, Reader arg1, long arg2)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNClob(int arg0, NClob arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNClob(String arg0, NClob arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNClob(int arg0, Reader arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNClob(String arg0, Reader arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNClob(int arg0, Reader arg1, long arg2)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNClob(String arg0, Reader arg1, long arg2)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNString(int arg0, String arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateNString(String arg0, String arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateRowId(int arg0, RowId arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateRowId(String arg0, RowId arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateSQLXML(int arg0, SQLXML arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateSQLXML(String arg0, SQLXML arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public <T> T getObject(String columnLabel,
              Class<T> type)
            throws SQLException {
				return null;
			}

	public <T> T getObject(int columnIndex,
              Class<T> type)
            throws SQLException {
				return null;
			}
}
