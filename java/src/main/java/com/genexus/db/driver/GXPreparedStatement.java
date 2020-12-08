package com.genexus.db.driver;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;

import com.genexus.*;
import com.genexus.common.classes.IGXPreparedStatement;
import com.genexus.internet.HttpContext;
import com.genexus.util.GXFile;
import com.genexus.util.GXServices;

/**
* Esta clase es un wrapper de un PreparedStatement real. Le agrega debugging y algunos procesamientos en
* las funciones set<Type> para ajustarlas a las necesidades de los programas generados.
* <p>
* No tiene sincronización, dado que los ResultSets nunca son usados simultáneamente desde
* dos threads.
* <p>
* Las funciones que no deberían ser llamadas nunca, generan una línea con "Warning" en el Log.
*/
public class GXPreparedStatement extends GXStatement implements PreparedStatement, com.genexus.db.IFieldSetter, IGXPreparedStatement
{
	private static final boolean DEBUG       = DebugFlag.DEBUG;

	public static boolean longVarCharAsOracleLong = false;
	public static com.genexus.LocalUtil localUtil = new com.genexus.LocalUtil('.', "MDY", "24", 40, "eng");

	private PreparedStatement stmt;
	private String sqlSentence;
	private String cursorId;
    private boolean currentOf;
	private long creationTime;
	private java.util.Vector<InputStream> streamsToClose;
    private int recordCount;
    private int batchSize;
    private Object[] batchRecords;
    private Object onCommitInstance;
    private String onCommitMethod;
    private boolean batchStmt;

	public GXPreparedStatement(PreparedStatement stmt, GXConnection con, int handle, String sqlSentence, String cursorId)
        {
          this(stmt, con, handle, sqlSentence, cursorId, false);
        }

	public GXPreparedStatement(PreparedStatement stmt, GXConnection con, int handle, String sqlSentence, String cursorId, boolean currentOf)
	{
		super(stmt, con, handle);
		this.stmt = stmt;
		this.con  = con;
		this.sqlSentence = sqlSentence;
		this.cursorId = cursorId;
                this.currentOf = currentOf;
		this.creationTime = System.currentTimeMillis();
		this.streamsToClose = new java.util.Vector<InputStream>();
	}

	public long getCreationTime()
	{
		return creationTime;
	}

	public String getSqlStatement()
	{
		return sqlSentence;
	}
	public String getCursorId()
	{
		return cursorId;
	}

    public boolean getCurrentOf()
    {
        return currentOf;
    }

    public String getCacheId(GXConnection conn) {
	   if (isBatch() || (getCurrentOf() && conn.getDBMS() instanceof GXDBMSas400))
	        return getCursorId();
	   else
	        return getSqlStatement();
    }
 
	public int getHandle()
	{
		return handle;
	}

	public void setHandle(int handle)
	{
		this.handle = handle;
	}

    public ResultSet executeQuery() throws SQLException
	{
		return executeQuery(false);
	}

	/**
	* Dos threads podrían querer ejecutar el siguiente método al mismo tiempo
	* Podria asumir que stmt.executeQuery está synchronized y no pasaría nada
	* pero prefiero marcarlo como synchronized.
	*
	*/
    public ResultSet executeQuery(boolean hold) throws SQLException
	{
		//JMX Counters
		con.incNumberRequest();
		con.setSentenceLastRequest(getSqlStatement());
		
		// Consideraciones especiales :
		// el .incOpenCursor hay que hacerlo despues de obtener el resultset, porque
		// sino, si hay una SQLException deberia dar marcha atrás.

		GXResultSet result = null;

		try
		{
			if	(con.getDBMS().getSupportsQueryTimeout() && stmt.getQueryTimeout() > 0)
			{
				setQueryTimeout(0);
			}
		}
		catch (SQLException e)
		{
			// Sometimes the drivers don't support QueryTimeout...
		}

		if	(DEBUG)
		{
			try
			{
				if	(con.getDBMS().getSupportsQueryTimeout())
					log(GXDBDebug.LOG_MAX, "executeQuery start timeout " + stmt.getQueryTimeout());
			}
			catch (SQLException e)
			{
				// Sometimes the drivers don't support QueryTimeout...
			}

			try
			{
				result = new GXResultSet(con.getDBMS().executeQuery(stmt, hold), this, con, handle);
				log(GXDBDebug.LOG_MIN, "executeQuery - id = " + this + " - " + GXDBDebug.getJDBCObjectId(result));
				return result;
			}
			catch (SQLException sqlException)
			{
				if (!con.getDBMS().ObjectLocked(sqlException))
				{
					notInUse();
				}
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
			finally
			{
				closeOpenedStreams();
			}
		}
		else
		{
			try
			{
				result = new GXResultSet(con.getDBMS().executeQuery(stmt, hold), this, con, handle);
			}
			catch (SQLException sqlException)
			{
				if (!con.getDBMS().ObjectLocked(sqlException))
				{				
					notInUse();
				}
				throw sqlException;
			}
			finally
			{
				closeOpenedStreams();
			}
		}

		creationTime = System.currentTimeMillis();
		return result;
	}

    public boolean execute() throws SQLException
	{
		//JMX Counters
		con.incNumberRequest();
		con.setSentenceLastRequest(getSqlStatement());		
		
		boolean ret;
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "Warning: execute");
			try
			{
				ret = stmt.execute();
				return ret;
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
			finally
			{
				closeOpenedStreams();
			}
		}
		else
		{
			try
			{
				ret = stmt.execute();
				return ret;
			}
			catch (SQLException sqlException)
			{
				throw sqlException;
			}
			finally
			{
				closeOpenedStreams();
			}
		}
	}

	private void closeOpenedStreams()
	{
		Enumeration<InputStream> e = streamsToClose.elements();
		while(e.hasMoreElements())
		{
			try
			{
				((java.io.InputStream)e.nextElement()).close();
			}
			catch(java.io.IOException ioex)
			{
				if	(DEBUG) System.err.println("Error closing stream: " + ioex.getMessage());
			}
		}
		streamsToClose.removeAllElements();
	}

	private void log(int level, String text)
	{
		if	(DEBUG)
			con.log(level, this, text);
	}

        public void notInUse()
        {
            con.setNotInUse(this);
        }
        public int[] executeBatch() throws SQLException {
            con.incNumberRequest();
            con.setSentenceLastRequest(getSqlStatement());

            int[] ret = null;
            SQLException sqlException = null;
            con.setUncommitedChanges();

            try
            {
                    if	(DEBUG)
                    {
                            log(GXDBDebug.LOG_MIN, "executeBatch");
                            ret = super.executeBatch();
                    }
                    else
                    {
                            ret = super.executeBatch();
                    }
            }
            catch (SQLException e)
            {
                    if	(DEBUG)
                            if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), e);

                    sqlException = e;
            }
            closeOpenedStreams();

            con.setNotInUse(this);

            if	(sqlException != null)
                    throw sqlException;

            return ret;


        }
	/**
	* Los PreparedStatement que no son read-only no son usados por mas de un thread
	* simultáneamente, por lo que este método no necesita ser synchronized.
	*/
    public int executeUpdate() throws SQLException
	{
		//JMX Counters
		con.incNumberRequest();
		con.setSentenceLastRequest(getSqlStatement());
		
		int ret = 0;
		SQLException sqlException = null;
		con.setUncommitedChanges();

		try
		{
			if	(DEBUG)
			{
				log(GXDBDebug.LOG_MIN, "executeUpdate");
				ret = stmt.executeUpdate();
			}
			else
			{
				ret = stmt.executeUpdate();
			}
		}
		catch (SQLException e)
		{
			if	(DEBUG)
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), e);

			sqlException = e;
		}
		closeOpenedStreams();

		con.setNotInUse(this);

		if	(sqlException != null)
			throw sqlException;

		return ret;
	}

    public void setNull(int index, int sqlType) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setNull - index : " + index + " sqlType : " + sqlType);
			try
			{
				stmt.setNull(index, sqlType);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setNull(index, sqlType);
		}
	}

    public void setBoolean(int index, boolean value) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setBoolean - index : " + index + " value : " + value);
			try
			{
		    	stmt.setBoolean(index, value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setBoolean(index, value);
		}
	}

    public void setByte(int index, byte value) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setByte - index : " + index + " value : " + value);
			try
			{
		    	stmt.setByte(index, value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setByte(index, value);
		}
	}

    public void setShort(int index, short value) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setShort - index : " + index + " value : " + value);
			try
			{
		    	stmt.setShort(index, value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setShort(index, value);
		}
	}

    public void setInt(int index, int value) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setInt - index : " + index + " value : " + value);
			try
			{
				stmt.setInt(index, value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setInt(index, value);
		}
	}

    public void setLong(int index, long value) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setLong - index : " + index + " value : " + value);
			try
			{
				stmt.setLong(index, value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setLong(index, value);
		}
	}

    public void setFloat(int index, float value) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setFloat - index : " + index + " value : " + value);
			try
			{
				stmt.setFloat(index, value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setFloat(index, value);
		}
	}

    public void setDouble(int index, double value) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setDouble - index : " + index + " value : " + value);
			try
			{
				stmt.setDouble(index, value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setDouble(index, value);
		}
	}

    public void setBigDecimal(int index, double value, int decimals) throws SQLException
	{
		BigDecimal decimalValue = com.genexus.DecimalUtil.unexponentString(Double.toString(value)).setScale(decimals, BigDecimal.ROUND_HALF_UP);

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setBigDecimal (double) - index : " + index + " value : " + value + " decimal : " + decimalValue);
			try
			{
				stmt.setBigDecimal(index, decimalValue);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setBigDecimal(index, decimalValue);
		}
	}

    public void setBigDecimal(int index, BigDecimal value, int scale) throws SQLException
	{
		setBigDecimal(index, value.setScale(scale, BigDecimal.ROUND_HALF_UP));
	}

    public void setBigDecimal(int index, BigDecimal value) throws SQLException
	{

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "Warning: setBigDecimal - index : " + index + " value : " + value);
			try
			{
				stmt.setBigDecimal(index, value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setBigDecimal(index, value);
		}
	}


	boolean fieldAcceptsNull = true;
	public void setVarchar(int index, String value, int length, boolean acceptsNull) throws SQLException
	{
		fieldAcceptsNull = acceptsNull;
		setVarchar(index, value, length);
		fieldAcceptsNull = false;
	}
														   
														   
	public void setVarchar(int index, String value) throws SQLException
	{
		setVarchar(index, value, value.length());
	}

	public static boolean addSpaceToEmptyVarChar = true;
	public void setVarchar(int index, String value, int length) throws SQLException
	{
		String realValue = (con.getDBMS().getId() == GXDBMS.DBMS_ORACLE) ? CommonUtil.rtrim(value) : value;
		realValue  = CommonUtil.left(realValue, length);

		if(realValue.equals("") &&
		   (addSpaceToEmptyVarChar||!fieldAcceptsNull) &&
		   con.getDBMS().getId() == GXDBMS.DBMS_ORACLE)
		{
			realValue = " ";
		}
		
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setVarchar - index : " + index + " length " + realValue.length());
			try
			{
				stmt.setString(index, realValue);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setString(index, realValue);
		}
	}

	public void setLongVarchar(int index, String value, int maxLength) throws SQLException
	{
		if	(value.length() > maxLength && con.getDBMS().getId() == GXDBMS.DBMS_AS400)
		{
			setLongVarchar(index, value.substring(0, maxLength));
		}
		else
		{
			setLongVarchar(index, value);
		}
	}

	public void setLongVarchar(int index, String value, int maxLength, boolean acceptsNull) throws SQLException
	{
		fieldAcceptsNull = acceptsNull;
		setLongVarchar(index, value, maxLength);
		fieldAcceptsNull = false;
	}
	
	public void setLongVarchar(int index, String value, boolean acceptsNull) throws SQLException
	{
		fieldAcceptsNull = acceptsNull;
		setLongVarchar(index, value);
		fieldAcceptsNull = false;		
	}

	public void setNLongVarchar(int index, String value, boolean acceptsNull) throws SQLException
	{
		fieldAcceptsNull = acceptsNull;
		setLongVarchar2(index, value, true);
		fieldAcceptsNull = false;		
	}
	public void setNLongVarchar(int index, String value) throws SQLException
	{
		setLongVarchar2(index, value, true);
	}	
	public void setLongVarchar(int index, String value) throws SQLException
	{
		setLongVarchar2(index, value, false);
	}	
	private void setLongVarchar2(int index, String value, boolean nls) throws SQLException
	{
		String 	realValue  = (con.getDBMS().getId() == GXDBMS.DBMS_ORACLE) ? CommonUtil.rtrim(value) : value;		
		if(realValue.equals("") &&
		   (addSpaceToEmptyVarChar||!fieldAcceptsNull) &&
		   con.getDBMS().getId() == GXDBMS.DBMS_ORACLE)
		{
			realValue = " ";
		}
		
		int 	realLength = realValue.length();
		
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setLongVarcharString - index : " + index + " length " + realLength);
			try
			{
				if	( (realLength > 0  && con.getDBMS().useStreamsInLongVarchar()    ) ||
				      (realLength == 0 && con.getDBMS().useStreamsInNullLongVarchar()))
				{
					if(con.getDBMS().getId() == GXDBMS.DBMS_ORACLE)
					{ // Con Oracle no funciona bien el setAsciiStream con caracteres con tildes o eñes, etc
					  // así que usamos un setUnicodeStream
						try
						{
							if(!nls && !longVarCharAsOracleLong)
							{
								try
								{
									setOracleClob(index, realValue);
								}
								catch(Exception exc)
								{
									setUnicodeStream(index, realValue);
								}
							}
							else
							{
								if (nls) oracleSetFormOfUse(index);
								setUnicodeStream(index, realValue);
							}
						}catch(UnsupportedEncodingException e)
						{
							stmt.setAsciiStream(index, new ByteArrayInputStream(realValue.getBytes()), realLength);
						}
					}
					else
					{
						stmt.setAsciiStream(index, new ByteArrayInputStream(realValue.getBytes()), realLength);
					}

				}
				else
				{
					setStringRealLength(index, realValue, realLength);
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

			if	( (realLength > 0  && con.getDBMS().useStreamsInLongVarchar()    ) ||
			      (realLength == 0 && con.getDBMS().useStreamsInNullLongVarchar()))
			{
				if(con.getDBMS().getId() == GXDBMS.DBMS_ORACLE)
				{ // Con Oracle no funciona bien el setAsciiStream con caracteres con tildes o eñes, etc
				  // así que usamos un setUnicodeStream
					try
					{
						if(!nls && !longVarCharAsOracleLong)
						{
							try
							{
								setOracleClob(index, realValue);
							}
							catch(Exception exc)
							{
								setUnicodeStream(index, realValue);
							}
						}
						else
						{
							if (nls) oracleSetFormOfUse(index);
							setUnicodeStream(index, realValue);
						}
					}catch(UnsupportedEncodingException e)
					{
						stmt.setAsciiStream(index, new ByteArrayInputStream(realValue.getBytes()), realLength);
					}
				}
				else
				{
					stmt.setAsciiStream(index, new ByteArrayInputStream(realValue.getBytes()), realLength);
				}
			}
			else
			{
				setStringRealLength(index, realValue, realLength);
			}
		}
		
	}
	public void oracleSetFormOfUse(int index)
	{
		try{
			// oracle NCHAR/NVARCHAR/NCLOB unicode columns require some
			// special handling to configure them correctly;
			Method setFormOfUseMth =  stmt.getClass().getMethod("setFormOfUse", new Class[]{ int.class, short.class });
			if (setFormOfUseMth!=null && setFormOfUseMth.isAccessible()) {
				setFormOfUseMth.invoke(stmt, new Object[]{new Integer(index), Class.forName("oracle.jdbc.OraclePreparedStatement").getField("FORM_NCHAR").get(null)});
			}
		}
		catch (Exception ex1) 
		{
			System.out.println(ex1.getMessage());
		}
	}

        private void setUnicodeStream(int index, String value) throws SQLException, UnsupportedEncodingException
        {
			byte [] unicodeBytes = value.getBytes("UnicodeBigUnmarked");
			stmt.setCharacterStream(index, new InputStreamReader(new ByteArrayInputStream(unicodeBytes), "UnicodeBigUnmarked"), unicodeBytes.length);
        }

	private void setOracleClob(int index, String value) throws
      UnsupportedEncodingException, ClassNotFoundException, SecurityException,
      NoSuchMethodException, NoSuchFieldException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException, IOException,
      SQLException {
          Class<?> clobClass = Class.forName("oracle.sql.CLOB");
          Class<?> oracleConn = Class.forName("oracle.jdbc.OracleConnection");
          Class<?> parmTypes[] = new Class[3];
          parmTypes[0] = java.sql.Connection.class;
          parmTypes[1] = Boolean.TYPE;
          parmTypes[2] = Integer.TYPE;
          Method createTemporaryMethod = clobClass.getDeclaredMethod("createTemporary", parmTypes);
          Field durationSessionField = clobClass.getField("DURATION_SESSION");

          Object argList[] = new Object[3];
          argList[0] = con.getJDBCConnection();
          argList[1] = Boolean.TRUE;
          argList[2] = durationSessionField.get(null);
          Object tempClob = createTemporaryMethod.invoke(null, argList);

          parmTypes = new Class[1];
          parmTypes[0] = Integer.TYPE;
          Method openMethod = clobClass.getDeclaredMethod("open", parmTypes);
          Field modeReadWriteField = clobClass.getField("MODE_READWRITE");
          argList = new Object[1];
          argList[0] = modeReadWriteField.get(null);
          openMethod.invoke(tempClob, argList);

          Method getCharacterOutputStreamMethod = clobClass.getDeclaredMethod("getCharacterOutputStream");
          Writer tempClobWriter = (Writer) getCharacterOutputStreamMethod.invoke(tempClob);
          tempClobWriter.write(value);
          tempClobWriter.flush();
          tempClobWriter.close();

          Method closeMethod = clobClass.getDeclaredMethod("close");
          closeMethod.invoke(tempClob);

          stmt.setClob(index, (Clob) tempClob);
	}
	
    public void setParameterRT(String name, String value)
    {
    }

    public void setStringRealLength(int index, String value, int length) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setString - index : " + index + " value : " + value + " length " + length);
			try
			{
				stmt.setString(index, CommonUtil.padr(value, length, " "));
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setString(index, CommonUtil.padr(value, length, " "));
		}
	}

	public static boolean avoidDataTruncationError = false;
    public void setString(int index, String value, int length) throws SQLException
	{
		if(avoidDataTruncationError)
	    {
		   value = CommonUtil.padr(value, length, " ");
		}
		else
		{
			if( con.getDBMS().getId() == GXDBMS.DBMS_HANA)
			{
				value = CommonUtil.left(value, length);
			}
		}


		if(value.equals("") && con.getDBMS().getId() == GXDBMS.DBMS_ORACLE)
		{
			value = " ";
		}
		
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setString - index : " + index + " value : " + value);
			try
			{
				if (con.getDBMS().getId() == GXDBMS.DBMS_ORACLE)
				{
					stmt.setObject(index, value);
				}
				else
				{
					stmt.setString(index, value);
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
			if (con.getDBMS().getId() == GXDBMS.DBMS_ORACLE)
			{
				stmt.setObject(index, value);
			}
			else
			{
				stmt.setString(index, value);
			}
		}
	}	

    public void setString(int index, String value) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: setString: " + value);

		stmt.setString(index, value);
	}

    public void setGXDbFileURI(int index, String fileName, String blobPath, int length) throws SQLException
    {
		setGXDbFileURI(index, fileName, blobPath, length, null, null);
	}

	public void setGXDbFileURI(int index, String fileName, String blobPath, int length, String tableName, String fieldName, boolean downloadContent) throws SQLException
	{
		setGXDbFileURI(index, fileName, blobPath, length, tableName, fieldName);
	}

    public void setGXDbFileURI(int index, String fileName, String blobPath, int length, String tableName, String fieldName) throws SQLException
    {
		 
    	if (blobPath==null || blobPath.trim().length() == 0)
    	{
    		setVarchar(index, fileName, length, false);
    	}
    	else
    	{
			String fileUri = "";
			if (fileName.trim().length() != 0 && !(blobPath.startsWith(com.genexus.Preferences.getDefaultPreferences().getMultimediaPath()) && ((fileName.toLowerCase().startsWith("http://") || fileName.toLowerCase().startsWith("https://")))))
				fileUri = GXDbFile.generateUri(fileName, !GXDbFile.hasToken(fileName), true);
			else
			{
				if (blobPath.trim().length() != 0)
				{
					blobPath = com.genexus.GXutil.cutUploadPrefix(blobPath);
					File file = new File(blobPath);
					fileUri = GXDbFile.generateUri(file.getName(), !GXDbFile.hasToken(blobPath), true);
				}
			}
			boolean attInExternalStorage = (tableName!=null && fieldName!=null);
			if (attInExternalStorage && Application.getGXServices().get(GXServices.STORAGE_SERVICE) != null)
			{				
					String sourceName = blobPath;
					String folder = Application.getGXServices().get(GXServices.STORAGE_SERVICE).getProperties().get("FOLDER_NAME");
					if (fileName == blobPath || fileName.toLowerCase().startsWith("http"))
					{
						try
						{
							URL fileURL = new URL(fileName);
							InputStream is = fileURL.openStream();
							int dDelimIdx2 = sourceName.lastIndexOf("/");
							if ( (dDelimIdx2 != -1) && (dDelimIdx2 < sourceName.length() - 1) )
							{
								sourceName = sourceName.substring(dDelimIdx2 + 1);
							}							
							sourceName = com.genexus.PrivateUtilities.getTempFileName("", CommonUtil.getFileName(sourceName), CommonUtil.getFileType(sourceName), true);
							sourceName = folder + "/" + tableName + "/" + fieldName + "/" + sourceName;
							fileUri = Application.getExternalProvider().upload(sourceName, is, false);
						}
						catch(IOException e)
						{
							throw new SQLException("An error occurred while downloading data from url: " + fileName + e.getMessage());
						}						
					}
					else
					{
						GXFile gxFile = new GXFile(sourceName, true);
						if (gxFile.exists())
						{
							fileName = gxFile.getName();
							int dDelimIdx = fileName.lastIndexOf("/");
							if ( (dDelimIdx != -1) && (dDelimIdx < fileName.length() - 1) )
							{
								fileName = fileName.substring(dDelimIdx + 1);
							}										
							fileUri = Application.getExternalProvider().copy(sourceName, fileName, tableName, fieldName, true);
						}
						else
						{
							//Cuando el blobPath no es una URL entonces tengo que hacer upload del archivo local (por ejemplo en el caso del FromImage)
							int dDelimIdx1 = blobPath.lastIndexOf("/");
							if ( (dDelimIdx1 != -1) && (dDelimIdx1 < blobPath.length() - 1) )
							{
								fileName = blobPath.substring(dDelimIdx1 + 1);
							}
							fileName = com.genexus.PrivateUtilities.getTempFileName("", CommonUtil.getFileName(fileName), CommonUtil.getFileType(fileName), true);
							fileName = folder + "/" + tableName + "/" + fieldName + "/" + fileName;
							if (con.getContext() != null)
							{
								com.genexus.internet.HttpContext webContext = (HttpContext) con.getContext().getHttpContext();
								if((webContext != null) && (webContext instanceof com.genexus.webpanels.HttpContextWeb) && (blobPath.startsWith(webContext.getContextPath()) || blobPath.startsWith(webContext.getDefaultPath())))
								{
									blobPath = ((com.genexus.webpanels.HttpContextWeb)webContext).getRealPath(blobPath);
								}
							}							
							fileUri = Application.getExternalProvider().upload(blobPath, fileName, false);
						}
					}
			}
    		setVarchar(index, fileUri, length, false);
    	}
    }

    public void setBytes(int index, byte value[]) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setBytes - index : " + index + " value : " + Arrays.toString(value));
			try
			{
				stmt.setBytes(index, value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setBytes(index, value);
		}
	}
	
	public void setGUID(int index, java.util.UUID value) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setGUID - index : " + index + " value : " + value.toString());
			try
			{
				stmt.setString(index, value.toString());
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setString(index, value.toString());
		}		
	}

	public void setDateTime(int index, java.util.Date value, boolean onlyTime) throws SQLException
	{
		setDateTime(index, value, onlyTime, false);
	}

	public void setDateTime(int index, java.util.Date value, boolean onlyTime, boolean onlyDate, boolean hasmilliseconds) throws SQLException
	{
		setDateTime(index, value, onlyTime, hasmilliseconds);
	}

	public void setDateTime(int index, java.util.Date value, boolean onlyTime, boolean hasmilliseconds) throws SQLException
	{
		if	(onlyTime && !value.equals(CommonUtil.nullDate()))
		{
			Calendar valueCalendar = GregorianCalendar.getInstance();
			valueCalendar.setTime(value);

			java.util.Date newValue = con.getNullDate();
			Calendar newCalendar = GregorianCalendar.getInstance();
			newCalendar.setTime(newValue);

			newCalendar.set(Calendar.HOUR_OF_DAY, valueCalendar.get(Calendar.HOUR_OF_DAY));
			newCalendar.set(Calendar.MINUTE, valueCalendar.get(Calendar.MINUTE));
			newCalendar.set(Calendar.SECOND, valueCalendar.get(Calendar.SECOND));

			if (hasmilliseconds)
			 	value = CommonUtil.dtaddms(newCalendar.getTime(), (double)(CommonUtil.millisecond(value)/1000.0));
			else	 			
				value = newCalendar.getTime();
		}

		if (!onlyTime && ModelContext.getModelContext() != null)
		{
			value = ModelContext.getModelContext().local2DBserver(value);
		}
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setDateTime - index : " + index + " value : " + value + " isnull " + (value.equals(CommonUtil.nullDate())));
		}
		try
		{		
			if (false && con.getDBMS().useCharInDate())
			{
				if	(value.equals(CommonUtil.nullDate()) )
					setString(index, "0001-01-01-00.00.00.000000");
				else
					if (hasmilliseconds)
						setString(index, CommonUtil.getYYYYMMDDHHMMSSmmm(value));
					else
						setString(index, CommonUtil.getYYYYMMDDHHMMSS(value));
			}			
			else
			{
				if	(DEBUG)
				{
					log(GXDBDebug.LOG_MAX, "setDateTime - index : " + index + " time : " + new Timestamp(value.equals(CommonUtil.nullDate())?con.getNullDate().getTime():value.getTime()));
				}
				if(con.getDBMS().getId() == GXDBMS.DBMS_POSTGRESQL && saveNullDate(value))
				{ 	// @HACK: 27/05/04
				  	// Si estoy con PostgreSQL, no marcha bien guardar un timestamp con 1/1/0001 00:00:00,
					// dependiendo del driver se guardan distintas horas e incluso fechas
					stmt.setDate(index, new java.sql.Date(con.getNullDate().getTime()));
				}
				else
				{
					if	(hasmilliseconds)
					{
						if (con.getDBMS().getId() == GXDBMS.DBMS_SQLSERVER || con.getDBMS().getId() == GXDBMS.DBMS_MYSQL)
						{
							// 7/3/2018
							// El Driver JDBC de SQL Server (jTDS) tiene problemas con el tipo DATETIME2 pierde precision al 
							// setear el valor en la base de datos porque lo convierte a DATETIME antes de mandarlo.
							// Por lo tanto se usa provisionalmente el setString() para el caso datetime con milisegundos. (APC)
							stmt.setString( index, (new Timestamp(saveNullDate(value)?con.getNullDate().getTime():value.getTime())).toString());						
						}
						else
						{
							stmt.setTimestamp( index, new Timestamp(saveNullDate(value)?con.getNullDate().getTime():value.getTime()));							
							//stmt.setDate( index, saveNullDate(value)?con.getNullDate():value);
						}
					}					
					else
					{
						stmt.setTimestamp( index, new Timestamp(saveNullDate(value)?con.getNullDate().getTime():CommonUtil.resetMillis(value).getTime()));
					}			
				}					
			}
		}
		catch (SQLException sqlException)
		{
			if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
					throw sqlException;
		}			
	}

	/**
 		El saveNullDate tiene que hacerse sobre un java.util.Date de verdad, no sobre el java.sql.Date
    */
	private boolean saveNullDate(java.util.Date value)
	{
		return (value.equals(CommonUtil.nullDate()) || value.before(con.getNullDate()));
	}

    public void setDate(int index, java.util.Date value) throws SQLException
	{

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setDate - index : " + index + " value : " + value + " time " + value.getTime());
		}

		if	(con.getDBMS().useDateTimeInDate())
		{
			setDateTime(index, value, false);
		}
		else if (con.getDBMS().useCharInDate())
		{
			if	(value.equals(CommonUtil.nullDate()))
				setString(index, "00000000");
			else
				setString(index, CommonUtil.getYYYYMMDD(value));
		}
		else
		{
			// El saveNullDate tiene que hacerse sobre un java.util.Date de verdad, no sobre el java.sql.Date
			java.sql.Date sDate = new java.sql.Date(con.getNullDate().getTime());
			if (!saveNullDate(value))
			{
				Calendar valueCalendar = GregorianCalendar.getInstance();
				valueCalendar.setTime(value);

				GregorianCalendar sGregorianCalendar = new GregorianCalendar();
				sGregorianCalendar.set(valueCalendar.get(Calendar.YEAR), valueCalendar.get(Calendar.MONTH), valueCalendar.get(Calendar.DATE), 0, 0, 0);
				sDate = new java.sql.Date(sGregorianCalendar.getTime().getTime());
			}
			setDate(index, sDate);
		}
	}

    public void setDate(int index, java.sql.Date value) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setDateSQL - index : " + index + " value : " + value);
			try
			{
				if	(con.getDBMS().useDateTimeInDate())
					setDateTime(index, value, false);
				else
				{
					stmt.setDate(index, value);
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
			if	(con.getDBMS().useDateTimeInDate())
				setDateTime(index, value, false);
			else
			{
				stmt.setDate(index, value);
			}
		}
	}

    public void setTime(int index, java.sql.Time value) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "Warning: setTime - index : " + index + " value : " + value);
			try
			{
				stmt.setTime(index, value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setTime(index, value);
		}
	}

    public void setTimestamp(int index, java.sql.Timestamp value) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "Warning: setTimestamp - index : " + index + " value : " + value);
			try
			{
				stmt.setTimestamp(index, value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setTimestamp(index, value);
		}
	}

    public void setAsciiStream(int index, java.io.InputStream value, int length) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setAsciiStream - index : " + index + " length : " + length);
			try
			{
				stmt.setAsciiStream(index, value, length);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setAsciiStream(index, value, length);
		}
	}

	@Deprecated
    public void setUnicodeStream(int index, java.io.InputStream value, int length) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "Warning: setUnicodeStream - index : " + index + " length : " + length);
			try
			{
				stmt.setUnicodeStream(index, value, length);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setUnicodeStream(index, value, length);
		}
	}

	protected boolean skipSetBlobs = false;
	protected String [] blobFiles;
	public void skipSetBlobs(boolean skipSetBlobs)
	{
		this.skipSetBlobs = skipSetBlobs;
	}
	
	public boolean getSkipSetBlobs()
	{
		return skipSetBlobs;
	}

	public String [] getBlobFiles()
	{
		return blobFiles;
	}





	public void setBLOBFile(java.sql.Blob blob, String fileName) throws SQLException
	{
		if	(fileName != null && !fileName.trim().equals("") && !fileName.toLowerCase().trim().endsWith("about:blank"))
		{
			if(con.getDBMS().getId() == GXDBMS.DBMS_ORACLE)
			{
				try
				{
					File file = new File(fileName);
					BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
					((GXDBMSoracle7)con.getDBMS()).setBlobData(blob, inputStream, (int) file.length());
					inputStream.close();
				}
				catch (Exception e)
				{
					if(e instanceof IOException)
					{									
						throw new SQLException("Can't find BLOB file " + fileName);
					}
					else
					{				
							if	(DEBUG && con.isLogEnabled()) con.logSQLException(con.getHandle(), e);
					}		
				}
			}
		}
	}
	
	public void setBLOBFile(int index, String fileName) throws SQLException
	{
		setBLOBFile(index, fileName, false);
	}

	public void setBLOBFile(int index, String fileName, boolean isMultiMedia, boolean downloadContent) throws SQLException
	{
		setBLOBFile(index, fileName, isMultiMedia);
	}

    public void setBLOBFile(int index, String fileName, boolean isMultiMedia) throws SQLException
	{
		if (isMultiMedia && Application.getGXServices().get(GXServices.STORAGE_SERVICE) != null)
		{
			fileName = "";
		}
    	
		if	(fileName != null && !fileName.trim().equals("") && !fileName.toLowerCase().trim().endsWith("about:blank") && Application.getGXServices().get(GXServices.STORAGE_SERVICE) == null)
		{
			fileName = com.genexus.GXutil.cutUploadPrefix(fileName);
			try
			{
				if (fileName.toLowerCase().startsWith("http://") || fileName.toLowerCase().startsWith("https://"))
				{
					int queryIndex = fileName.lastIndexOf('?');
					if (queryIndex > -1)
						fileName = fileName.substring(0, queryIndex + 1) + PrivateUtilities.encodeURL(fileName.substring(queryIndex + 1));
					URL fileURL = new URL(fileName);
					String blobPath = com.genexus.Preferences.getDefaultPreferences().getBLOB_PATH();
					fileName = com.genexus.PrivateUtilities.getTempFileName(blobPath, CommonUtil.getFileName(fileName), CommonUtil.getFileType(fileName), true);
					com.genexus.PrivateUtilities.InputStreamToFile(fileURL.openStream() ,fileName);						
				}
			}
			catch(MalformedURLException e)
			{
				throw new SQLException("Malformed URL " + fileName);
			}
			catch(IOException e)
			{
				throw new SQLException("An error occurred while downloading data from url: " + fileName + e.getMessage());
			}

			if (con.getContext() != null)
			{
				com.genexus.internet.HttpContext webContext = (HttpContext) con.getContext().getHttpContext();
				if(webContext != null)
				{
					if (webContext instanceof com.genexus.webpanels.HttpContextWeb) {
						fileName = ((com.genexus.webpanels.HttpContextWeb) webContext).getRealPath(fileName);
					}
					else
					{
						if (!webContext.getDefaultPath().isEmpty() && ! new File(fileName).isAbsolute())
						{
							if (fileName.startsWith(webContext.getContextPath()))
							{
								fileName = fileName.substring(webContext.getContextPath().length() +1);
							}

							fileName = webContext.getDefaultPath() + File.separator + fileName;
						}
					}
				}
			}
		}

		if(skipSetBlobs)
		{
			if(blobFiles == null)
			{
				blobFiles = new String[index];
			}
			else if(blobFiles.length < index)
			{
				String [] temp = blobFiles;
				blobFiles = new String[index];
				System.arraycopy(temp, 0, blobFiles, 0, temp.length);
			}
			blobFiles[index-1] = fileName;
		}
		else
		{
			if	(fileName != null && !fileName.trim().equals("") && !fileName.toLowerCase().trim().endsWith("about:blank"))
			{
					if (Application.getGXServices().get(GXServices.STORAGE_SERVICE) == null)
					{
						try
						{
							File file = new File(fileName);
							BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
							setBinaryStream(index, inputStream, (int) file.length());
						}
						catch (IOException e)
						{
							throw new SQLException("The filename does not exists in url " + fileName);
						}						
					}
					else
					{
							GXFile gxFile = new GXFile(fileName, true);
							if (gxFile.exists())
							{							
								InputStream is= gxFile.getStream();							
								setBinaryStream(index, is, (int) gxFile.getLength());
							}
							else
							{
								try
								{
									File localFile = new File(fileName);
									BufferedInputStream localInputStream = new BufferedInputStream(new FileInputStream(localFile));
									setBinaryStream(index, localInputStream, (int) localFile.length());
								}
								catch (IOException e)
								{
									throw new SQLException("The filename does not exists in url " + fileName);
								}								
							}							
					}
			}
			else
			{
				if (con.getDBMS().getId() == GXDBMS.DBMS_ORACLE)
				{
					BufferedInputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(new byte[] {0}));
					setBinaryStream(index, inputStream, (int) 1);
				}
				else
				{
					BufferedInputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(new byte[] {}));
					setBinaryStream(index, inputStream, (int) 0);								
				}
			}
		}
	}

    public void setBinaryStream(int index, java.io.InputStream value, int length) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "Warning: setBinaryStream - index : " + index + " length : " + length);
			try
			{
				stmt.setBinaryStream(index, value, length);
				streamsToClose.addElement(value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setBinaryStream(index, value, length);
			streamsToClose.addElement(value);
		}
	}

    public void clearParameters() throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "Warning: clearParameters");
			try
			{
				stmt.clearParameters();
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.clearParameters();
		}
	}

    public void setObject(int index, Object value, int targetSqlType, int scale) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "Warning: setObject - index : " + index + " value : " + value + " targetSqlType : " + targetSqlType + " scale: " + scale);
			try
			{
				stmt.setObject(index, value, targetSqlType, scale);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setObject(index, value, targetSqlType, scale);
		}
	}

    public void setObject(int index, Object value, int targetSqlType) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "Warning: setObject - index : " + index + " value : " + value + " targetSqlType : " + targetSqlType);
			try
			{
				stmt.setObject(index, value, targetSqlType);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setObject(index, value, targetSqlType);
		}
	}

    public void setObject(int index, Object value) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "Warning: setObject - index : " + index + " value : " + value );
			try
			{
				stmt.setObject(index, value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setObject(index, value);
		}
	}


    public void addBatch(Object[] recordBlock) throws SQLException {
            addBatch();
            batchRecords[recordCount] = recordBlock;
            recordCount++;
        }
    public void addBatch() throws SQLException
	{
		stmt.addBatch();
	}

    public void setCharacterStream(int parameterIndex, java.io.Reader reader, int length) throws SQLException
	{
		stmt.setCharacterStream(parameterIndex, reader, length);
	}

    public void setRef (int i, Ref x) throws SQLException
	{
		stmt.setRef(i, x);
	}

    public void setBlob (int i, Blob x) throws SQLException
	{
		stmt.setBlob(i, x);
	}

    public void setClob (int i, Clob x) throws SQLException
	{
		stmt.setClob(i, x);
	}

    public void setArray (int i, Array x) throws SQLException
	{
		stmt.setArray(i, x);
	}

    public ResultSetMetaData getMetaData() throws SQLException
	{
		return stmt.getMetaData();
	}

    public void setDate(int parameterIndex, java.sql.Date x, Calendar cal) throws SQLException
	{
		stmt.setDate(parameterIndex, x, cal);
	}

    public void setTime(int parameterIndex, java.sql.Time x, Calendar cal)  throws SQLException
	{
		stmt.setTime(parameterIndex, x, cal);
	}

    public void setTimestamp(int parameterIndex, java.sql.Timestamp x, Calendar cal) throws SQLException
	{
		stmt.setTimestamp(parameterIndex, x, cal);
	}

  	public void setNull (int paramIndex, int sqlType, String typeName) throws SQLException
	{
		stmt.setNull(paramIndex, sqlType, typeName);
	}


	// Metodos agregados en JDK1.4
	public void setURL(int i, URL x) throws SQLException
	{
		stmt.setURL(i, x);
	}
	
	public java.sql.ParameterMetaData getParameterMetaData() throws SQLException
	{
		return stmt.getParameterMetaData();
	}

        public void resetRecordCount(){
            recordCount = 0;
        }

        public int getRecordCount(){
            return recordCount;
        }

        public void close() throws SQLException{
            super.close();
        }

        public int getBatchSize(){
            return batchSize;
        }

        public void setBatchSize(int size){
            batchRecords = new Object[size];
            batchSize = size;
            batchStmt = true;
			resetRecordCount();
        }

        public Object[] getBatchRecords(){
         return batchRecords;
        }

        public Object getOnCommitInstance(){
            return onCommitInstance;
        }

        public String getOnCommitMethod(){
            return onCommitMethod;
        }

        public void setOnCommitInstance(Object value){
            onCommitInstance = value;
        }
        public void setOnCommitMethod(String mth){
            onCommitMethod = mth;
        }

        public boolean isBatch(){
            return batchStmt;
        }
		public void setAsciiStream(int arg0, InputStream arg1)
				throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void setAsciiStream(int arg0, InputStream arg1, long arg2)
				throws SQLException {
			
		}

		public void setBinaryStream(int arg0, InputStream arg1)
				throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void setBinaryStream(int arg0, InputStream arg1, long arg2)
				throws SQLException {
			// TODO Auto-generated method stub

		}

		public void setBlob(int arg0, InputStream arg1) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void setBlob(int arg0, InputStream arg1, long arg2)
				throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void setCharacterStream(int arg0, Reader arg1)
				throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void setCharacterStream(int arg0, Reader arg1, long arg2)
				throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void setClob(int arg0, Reader arg1) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void setClob(int arg0, Reader arg1, long arg2)
				throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void setNCharacterStream(int arg0, Reader arg1)
				throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void setNCharacterStream(int arg0, Reader arg1, long arg2)
				throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void setNClob(int arg0, NClob arg1) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void setNClob(int arg0, Reader arg1) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void setNClob(int arg0, Reader arg1, long arg2)
				throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void setNString(int arg0, String arg1) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void setRowId(int arg0, RowId arg1) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void setSQLXML(int arg0, SQLXML arg1) throws SQLException {
			// TODO Auto-generated method stub
			
		}

	
}
