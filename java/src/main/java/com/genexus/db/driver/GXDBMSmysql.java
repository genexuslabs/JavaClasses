package com.genexus.db.driver;

import java.sql.*;
import java.util.Date;

import com.genexus.CommonUtil;

public class GXDBMSmysql implements GXDBMS
{
	private boolean forceAutoCommit;

	public ResultSet executeQuery(PreparedStatement stmt, boolean hold) throws SQLException
	{
		return stmt.executeQuery();
	}

	public int executeUpdate(PreparedStatement stmt) throws SQLException
	{
		return stmt.executeUpdate();
	}

	public boolean execute(PreparedStatement stmt) throws SQLException
	{
		return stmt.execute();
	}

	public int[] executeBatch(Statement stmt) throws SQLException
	{
		return stmt.executeBatch();
	}

	public boolean isAlive(GXConnection con)
	{
		try
		{
			serverDateTime(con);
		}
		catch (SQLException e)
		{
			return false;
		}

		return true;
	}

	private String dbName = "";

	public void setDatabaseName(String dbName)
	{
		this.dbName = dbName;
	}
	public String getDatabaseName()
	{
		return this.dbName;
	}
	
	public void setInReorg()
	{
		forceAutoCommit = true;
	}

	public boolean DataTruncation(SQLException e)
	{
		if (e != null && e.getSQLState() != null)
		{
			return (e.getErrorCode() == 0 && (e.getSQLState().equals("01004") || e.getSQLState().equals("22001")));
		}
		return false;
	}

	private DataSource dataSource;
	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	public boolean EndOfFile(SQLException e)
	{
		return (e.getErrorCode() == 100);
	}
	
	public boolean ReferentialIntegrity(SQLException e)
	{
		return (e.getErrorCode() == 1216);
	}
	
	public boolean DuplicateKeyValue(SQLException e)
	{
		return (e.getErrorCode() == 1062);
	}

	public boolean MaskedFileNotFound(SQLException e)
	{
		return (e.getErrorCode() == 1051) || (e.getErrorCode() == 1091) || (e.getErrorCode() == 1146);
	}

	public boolean ObjectLocked(SQLException e)
	{
		return false;
	}

	public boolean ObjectNotFound(SQLException e)
 	{
		return (e.getErrorCode() == 1025) || 
			   (e.getErrorCode() == 1051) || 
			   (e.getErrorCode() == 1091) || 
			   (e.getErrorCode() == 1146) ||
			   (e.getErrorCode() == 1006) ||  /* Can't create database */
			   (e.getErrorCode() == 1007) ||  /* Can't create database. database exists */
			   (e.getErrorCode() == 1360);    /* Trigger does not exist */
	}

	public java.util.Date nullDate()
	{
		return CommonUtil.ymdhmsToT_noYL(1000, 1, 1, 0, 0, 0);
	}

	public boolean useDateTimeInDate()
	{
		return false;
	}

	public boolean useCharInDate()
	{
		return false;
	}

	public void setConnectionProperties(java.util.Properties props)
	{
		if (forceAutoCommit)
		{
			props.put("relaxAutoCommit", "true" +  dataSource.jdbcAS400Lib);
		}
	}

	public void onConnection(GXConnection con) throws SQLException
	{
	}

	public java.util.Date serverDateTime(GXConnection con) throws SQLException
	{
		ResultSet rslt = con.getStatement("_ServerDT_", "SELECT NOW(3)", false).executeQuery();

		rslt.next();
		Date value = rslt.getTimestamp(1);
		rslt.close();

		return value;
	}
	
	public String serverVersion(GXConnection con) throws SQLException
	{
		ResultSet rslt = con.getStatement("_ServerVERSION_", "SELECT VERSION()", false).executeQuery();
		
		rslt.next();
		String value = rslt.getString(1);
		rslt.close();

		return value;
	}	
	
	public String connectionPhysicalId(GXConnection con)
	{
		try
		{
			ResultSet rslt = con.getStatement("_ConnectionID_", "SELECT CONNECTION_ID()", false).executeQuery();

			rslt.next();
			String value = rslt.getString(1);
			rslt.close();

			return value;
		}
		catch (SQLException e)
		{
			return "";
		}
	}	

	public boolean getSupportsAutocommit()
	{
		return true;
	}

	public void commit(Connection con) throws SQLException
	{
		if (con.getAutoCommit() == false)
			con.commit();
	}

	public void rollback(Connection con) throws SQLException
	{
		if (con.getAutoCommit() == false)
			con.rollback();
	}

	public boolean useReadOnlyConnections()
	{
		return true;
	}

	public boolean ignoreConnectionError(SQLException e)
	{
		return false;
	}
        
        public boolean rePrepareStatement(SQLException e)
        {
            return (e.getErrorCode() == 1615);
        }

	public boolean getSupportsQueryTimeout()
	{
		return true;
	}

	public boolean useStreamsInNullLongVarchar()
	{
		return false;
	}

	public boolean useStreamsInLongVarchar()
	{
		return false;
	}

	public int getId()
	{
		return DBMS_MYSQL;
	}

        public int getLockRetryCount(int lockRetryCount, int waitRecord){
          return lockRetryCount;
        }
		
	public boolean connectionClosed(SQLException e)
	{
		return (e.getSQLState() == "08S01");
	}		
}


