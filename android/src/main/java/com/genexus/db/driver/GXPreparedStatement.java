package com.genexus.db.driver;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
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

import org.apache.commons.io.FileUtils;

import com.artech.base.services.AndroidContext;
import com.genexus.AndroidLog;
import com.genexus.CommonUtil;
import com.genexus.DebugFlag;
import com.genexus.GXDbFile;
import com.genexus.common.classes.IGXPreparedStatement;


public class GXPreparedStatement extends GXStatement implements PreparedStatement, com.genexus.db.IFieldSetter, IGXPreparedStatement
{
	private static final boolean DEBUG       = DebugFlag.DEBUG;

	public static boolean longVarCharAsOracleLong = false;

	private static final String blobDBFilePrefix = "gxblobdata://";
	
	private PreparedStatement stmt;
	private String sqlSentence;
	private String cursorId;
    private boolean currentOf;
	private long creationTime;
	private java.util.Vector streamsToClose;
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
		this.streamsToClose = new java.util.Vector();
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
	* Dos threads podr�an querer ejecutar el siguiente m�todo al mismo tiempo
	* Podria asumir que stmt.executeQuery est� synchronized y no pasar�a nada
	* pero prefiero marcarlo como synchronized.
	*
	*/
    public synchronized ResultSet executeQuery(boolean hold) throws SQLException
	{
		
		// Consideraciones especiales :
		// el .incOpenCursor hay que hacerlo despues de obtener el resultset, porque
		// sino, si hay una SQLException deberia dar marcha atr�s.

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
		java.util.Enumeration e = streamsToClose.elements();
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

            int[] ret = null;
            SQLException sqlException = null;

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
	* simult�neamente, por lo que este m�todo no necesita ser synchronized.
	*/
    public int executeUpdate() throws SQLException
	{
		
		int ret = 0;
		SQLException sqlException = null;

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
		setBigDecimal(index, value.setScale(scale, value.ROUND_DOWN));
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
		String 	realValue  = CommonUtil.left(CommonUtil.rtrim(value), length);
		
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
			setLongVarchar(index, value);
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
		String 	realValue  = CommonUtil.rtrim(value);
		
		int 	realLength = realValue.length();
		
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setLongVarcharString - index : " + index + " length " + realLength);
			try
			{
				if	( (realLength > 0  && con.getDBMS().useStreamsInLongVarchar()    ) ||
				      (realLength == 0 && con.getDBMS().useStreamsInNullLongVarchar()))
				{
						stmt.setAsciiStream(index, new StringBufferInputStream(realValue), realLength);

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
					stmt.setAsciiStream(index, new StringBufferInputStream(realValue), realLength);
			}
			else
			{
				setStringRealLength(index, realValue, realLength);
			}
		}
	}

        private void setUnicodeStream(int index, String value) throws SQLException, UnsupportedEncodingException
        {
			byte [] unicodeBytes = value.getBytes("UnicodeBigUnmarked");
			stmt.setCharacterStream(index, new InputStreamReader(new ByteArrayInputStream(unicodeBytes), "UnicodeBigUnmarked"), unicodeBytes.length);
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
    	log(GXDBDebug.LOG_MAX, "setString - index : " + index + " original value : " + value);
		// In SQLite always complete with spaces the char data, like in oracle
	   value = CommonUtil.padr(value, length, " ");
		
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "setString - index : " + index + " value : " + value);
			try
			{
					stmt.setString(index, value);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
				stmt.setString(index, value);
		}
	}	

    public void setString(int index, String value) throws SQLException
	{
		if	(DEBUG) log(GXDBDebug.LOG_MAX, "Warning: setString: " + value);

		stmt.setString(index, value);
	}

	public void setGXDbFileURI(int index, String fileName, String blobPath, int length) throws SQLException
	{
		setGXDbFileURI(index, fileName, blobPath, length, null, null, false);
	}

	public void setGXDbFileURI(int index, String fileName, String blobPath, int length, String tableName, String fieldName) throws SQLException
	{
		setGXDbFileURI(index, fileName, blobPath, length, null, null, false);
	}

	public void setGXDbFileURI(int index, String fileName, String blobPath, int length, String tableName, String fieldName, boolean downloadContent) throws SQLException
    {
    	if (blobPath.trim().length() == 0)
    		setVarchar(index, fileName, length, false);
    	else
    	{
			String fileUri = "";
			boolean isLocalFile = false;
			// Fix path with " 
			if (blobPath.startsWith("\"") && blobPath.endsWith("\""))
			{
				blobPath = blobPath.substring(1, blobPath.length()-1);
			}
			// Fix relative blobPath
			if (blobPath.toLowerCase().startsWith("/publictempstorage") || blobPath.toLowerCase().startsWith("publictempstorage")
					|| blobPath.toLowerCase().contains("/publictempstorage/") )
			{
				isLocalFile = true;
								
				blobPath = AndroidContext.ApplicationContext.makeImagePath(blobPath);
			}
				
			// remove file:// prefix if exists
			if (blobPath.toLowerCase().startsWith("file://"))
			{
				URI myURI = URI.create(blobPath);
				if ("file".equalsIgnoreCase(myURI.getScheme()))
				{
					blobPath = myURI.getPath();
				}
			}
			
			// special case youtube url
			if (blobPath.toLowerCase().startsWith("http://www.youtube.com/"))
			{
				//cannot download , just keep the address.
				fileUri = blobPath;
			}
			else
			{
				if (blobPath.trim().length() != 0)
				{
					// url of file
					String fileNameNew = blobPath;
					File file = new File(fileNameNew);
					String blobBasePath = com.genexus.Preferences.getDefaultPreferences().getBLOB_PATH();
					
					// if file from resources copy to an actual file and keep reference.
					if (!(blobPath.toLowerCase().startsWith("http://") || blobPath.toLowerCase().startsWith("https://"))
							&& blobPath.toLowerCase().contains("resources")
							&& !isLocalFile)
					{
						InputStream is = null;
						int id = AndroidContext.ApplicationContext.getDataImageResourceId(blobPath); //$NON-NLS-1$
						if (id != 0)
						{
							//is = AndroidContext.ApplicationContext.openRawResource(id);
							//if (is!=null)
							//{
							// path outside database, should be unique.
							String fileResourceNameNew = "kbfile_" + blobPath.replace("/", "_");
							fileResourceNameNew = blobBasePath + "/" + CommonUtil.getFileName(fileResourceNameNew)+ "." + CommonUtil.getFileType(fileResourceNameNew);

							fileNameNew = fileResourceNameNew;
							file = new File(fileResourceNameNew);
							//}
						}
					}
					
					if (isLocalFile
						|| (downloadContent && (blobPath.toLowerCase().startsWith("http://") || blobPath.toLowerCase().startsWith("https://")) )
					)
					{
						// Local path in sdcard.
						//fileNameNew = blobBasePath + "/" + GXutil.getFileName(fileName)+ "." + GXutil.getFileType(fileName);
						if (fileName !=null && fileName.length()>0)
							fileNameNew = fileName;
				
						//store a relative path., in get add blob path again.
						fileNameNew = blobDBFilePrefix + CommonUtil.getFileName(fileNameNew)+ "." + CommonUtil.getFileType(fileNameNew);
					}
					else if (file.exists() && fileNameNew.startsWith(blobBasePath))
					{
						//store a relative path., in get add blob path again.
						fileNameNew = blobDBFilePrefix + CommonUtil.getFileName(fileNameNew)+ "." + CommonUtil.getFileType(fileNameNew);
					}
					fileUri = fileNameNew;
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
		setDateTime(index, value, onlyTime, false, false);
	}

	public void setDateTime(int index, java.util.Date value, boolean onlyTime, boolean hasmilliseconds) throws SQLException
	{
		setDateTime(index, value, onlyTime, false, hasmilliseconds);

	}

	public void setDateTime(int index, java.util.Date value, boolean onlyTime, boolean onlyDate, boolean hasmilliseconds) throws SQLException
	{
		if	(onlyTime && !value.equals(CommonUtil.nullDate()))
		{
			java.util.Date newValue = con.getNullDate();

			newValue.setHours(value.getHours());
			newValue.setMinutes(value.getMinutes());
			newValue.setSeconds(value.getSeconds());
			if (hasmilliseconds)
			 	value = CommonUtil.dtaddms(newValue, (double)(CommonUtil.millisecond(value)/1000.0));
			else	 			
				value = newValue;
		}

		if (!onlyDate)
			value = con.getContext().local2DBserver(value, hasmilliseconds);
		
		if	(DEBUG)
		{

			log(GXDBDebug.LOG_MAX, "setDateTime - index : " + index + " value : " + value + " isnull " + (value.equals(CommonUtil.nullDate())));
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
					log(GXDBDebug.LOG_MAX, "setDateTime - index : " + index + " time : " + new Timestamp(value.equals(CommonUtil.nullDate())?con.getNullDate().getTime():value.getTime()));
						stmt.setTimestamp( index, new Timestamp(saveNullDate(value)?con.getNullDate().getTime():value.getTime()));
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
				if (hasmilliseconds)
					stmt.setTimestamp( index, new Timestamp(saveNullDate(value)?con.getNullDate().getTime():value.getTime()));
				else
					stmt.setTimestamp( index, new Timestamp(saveNullDate(value)?con.getNullDate().getTime():CommonUtil.resetMillis(value).getTime()));
			}
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
			setDateTime(index, value, false, true,false);
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
			setDate(index, saveNullDate(value)?
										new java.sql.Date(con.getNullDate().getTime()):
										new java.sql.Date(value.getYear(), value.getMonth(), value.getDate()));
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
					setDateTime(index, value, false, true, false);
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
				setDateTime(index, value, false, true, false);
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

	public String [] getBlobFiles()
	{
		return blobFiles;
	}

	public void setBLOBFile(java.sql.Blob blob, String fileName) throws SQLException
	{
	}

	public void setBLOBFile(int index, String fileName) throws SQLException
	{
		setBLOBFile(index, fileName, false, false);
	}

	public void setBLOBFile(int index, String fileName, boolean isMultiMedia) throws SQLException
	{
		setBLOBFile(index, fileName, isMultiMedia, false);
	}

	public void setBLOBFile(int index, String fileName, boolean isMultiMedia, boolean downloadContent) throws SQLException
	{
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
			if	(fileName != null && !fileName.trim().equals(""))
			{
				if (fileName.toLowerCase().startsWith("http://www.youtube.com/"))
				{
					//cannot download , just keep the address.
					setVarchar(index, fileName, fileName.length() + 1, false);
					return;
				}
				// remove file:// prefix
				if (fileName.toLowerCase().startsWith("file://"))
				{
					URI myURI = URI.create(fileName);
					if ("file".equalsIgnoreCase(myURI.getScheme()))
					{
						fileName = myURI.getPath();
					}
				}
				
				String fileNameNew = fileName;
				boolean isLocalFile = false;
				String blobBasePath = com.genexus.Preferences.getDefaultPreferences().getBLOB_PATH();
				
				try
				{
					// Fix fileName with " 
					if (fileName.startsWith("\"") && fileName.endsWith("\""))
					{
						fileName = fileName.substring(1, fileName.length()-1);
					}
					// Fix relative fileName
					if (fileName.toLowerCase().startsWith("/publictempstorage") || fileName.toLowerCase().startsWith("publictempstorage")
							|| fileName.toLowerCase().contains("/publictempstorage/") )
					{
						isLocalFile = true;
												
						fileName = AndroidContext.ApplicationContext.makeImagePath(fileName);
					}
					// add token if necesary?
					//Boolean addToken = (fileName.compareTo(GXDbFile.removeTokenFromFileName(fileName)) == 0);
										
					if ( (fileName.toLowerCase().startsWith("http://") || fileName.toLowerCase().startsWith("https://"))
						&& (isLocalFile || downloadContent))
					{
						URL fileURL = new URL(fileName);

						//fileNameNew = GXDbFile.generateUri(fileName, addToken);
						fileNameNew = blobBasePath + "/" + CommonUtil.getFileName(fileName)+ "." + CommonUtil.getFileType(fileName);
						//fileName = com.genexus.PrivateUtilities.getTempFileName(blobPath, GXutil.getFileName(fileName), GXutil.getFileType(fileName));
						
						AndroidLog.debug("setBLOBFile downloading : "+ fileName + " - " + fileNameNew);
						
						CommonUtil.InputStreamToFile(fileURL.openStream() ,fileNameNew);						
					}
					// if file from resources copy to an actual file
					else if (!(fileName.toLowerCase().startsWith("http://") || fileName.toLowerCase().startsWith("https://"))
							&& fileName.toLowerCase().contains("resources")
							&& !isLocalFile)
					{
						InputStream is = null;
						int id = AndroidContext.ApplicationContext.getDataImageResourceId(fileName); //$NON-NLS-1$
						if (id != 0)
						{
							is = AndroidContext.ApplicationContext.openRawResource(id);
							if (is!=null)
							{
								// path outside database, should be unique.
								String fileResourceNameNew = "kbfile_" + fileName.replace("/", "_");
								fileResourceNameNew = blobBasePath + "/" + CommonUtil.getFileName(fileResourceNameNew)+ "." + CommonUtil.getFileType(fileResourceNameNew);

								// keep reference to new created file.
								fileNameNew = fileResourceNameNew;
								File myFile = new File(fileResourceNameNew);
								try {
									FileUtils.copyInputStreamToFile(is, myFile);
								} catch (IOException e) {
									AndroidLog.error("An error occurred while copying blob from resouces: " + fileName + " " + e.getMessage());
									e.printStackTrace();
									fileNameNew = fileName;
								}
							}
						}
					}
				}
				catch(MalformedURLException e)
				{
					throw new SQLException("Malformed URL " + fileName);
				}
				catch(IOException e)
				{
					//throw new SQLException("An error occurred while downloading data from url: " + fileName + e.getMessage());
					AndroidLog.error("An error occurred while downloading data from url: " + fileName + " " + e.getMessage());
					BufferedInputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(new byte[] {}));
					setBinaryStream(index, inputStream, (int) 0);	
					return;
				}

				File file = new File(fileNameNew);
				//BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
				//setBinaryStream(index, inputStream, (int) file.length());
			
				// In android always store the blob file in storage and keep a reference to it.
				if (isLocalFile && file.exists() || (!isLocalFile))
				{
					if (isLocalFile && file.exists() || file.exists() && fileNameNew.startsWith(blobBasePath))
					{
						//store a relative path., in get add blob path again.
						fileNameNew = blobDBFilePrefix + CommonUtil.getFileName(fileNameNew)+ "." + CommonUtil.getFileType(fileNameNew);
						// 	keep reference to file for blobs that only have this field and not gxi
						setVarchar(index, fileNameNew, fileNameNew.length() + 1, false);
					}
					else if (file.exists() && !fileNameNew.startsWith(blobBasePath))
					{
						// For BLOBS ONLY saved locally
						String fileExtension = CommonUtil.getFileType(fileNameNew);
						//Convert extension.
						if (fileExtension!=null && fileExtension.startsWith("."))
							fileExtension = fileExtension.substring(1);

						// path outside database, should be unique.
						String fileNameNewDb = GXDbFile.addTokenToFileName("binary", fileExtension);
						//Copy the blob to the database path and keep a reference
						fileNameNewDb = blobBasePath + "/" + CommonUtil.getFileName(fileNameNewDb)+ "." + CommonUtil.getFileType(fileNameNewDb);

						
						AndroidLog.debug("setBLOBFile copying : "+ fileNameNew + " - " + fileNameNewDb);
					
						File fileDestinyCopy = new File(fileNameNewDb);
						try {
							FileUtils.copyFile(file, fileDestinyCopy);
						} catch (IOException e) {
							AndroidLog.error("An error occurred while copying blob to DB: " + fileNameNew + " " + e.getMessage());
							e.printStackTrace();
						}
						
						//store a relative path., in get add blob path again.
						fileNameNewDb = blobDBFilePrefix + CommonUtil.getFileName(fileNameNewDb)+ "." + CommonUtil.getFileType(fileNameNewDb);
						// keep reference to file for blobs that only have this field and not gxi
						setVarchar(index, fileNameNewDb, fileNameNewDb.length() + 1, false);
					}
					else
					{
						// just keep the location in storage in gxi ., set value to empty
						BufferedInputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(new byte[] {}));
						setBinaryStream(index, inputStream, (int) 0);
					}
				}
				else
				{
					AndroidLog.error("The filename does not exists in url " + fileName);
					BufferedInputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(new byte[] {}));
					setBinaryStream(index, inputStream, (int) 0);	
					return;
				}
			}
			else
			{
					BufferedInputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(new byte[] {}));
					setBinaryStream(index, inputStream, (int) 0);								
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

    /*** New methods in Java's 1.6 PreparedStatement interface. These are stubs. ***/
	public void setRowId(int parameterIndex, RowId x) throws SQLException {}
	public void setNString(int parameterIndex, String value) throws SQLException {}
	public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {}
	public void setNClob(int parameterIndex, NClob value) throws SQLException {}
	public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {}
	public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {}
	public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {}
	public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {}
	public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {}
	public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {}
	public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {}
	public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {}
	public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {}
	public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {}
	public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {}
	public void setClob(int parameterIndex, Reader reader) throws SQLException {}
	public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {}
	public void setNClob(int parameterIndex, Reader reader) throws SQLException {}
	/*** End of new methods. ***/
}
