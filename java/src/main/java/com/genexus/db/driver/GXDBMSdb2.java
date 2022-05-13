package com.genexus.db.driver;

import java.sql.*;
import java.util.Date;

import com.genexus.CommonUtil;

public class GXDBMSdb2 implements GXDBMS
{	
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

	public void setDatabaseName(String dbName)
	{
	}
	public String getDatabaseName()
	{
		return "";
	}	
	public void setInReorg()
	{
	}

	public boolean DataTruncation(SQLException e)
	{
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
		return (e.getErrorCode() == -530);
	}

	public boolean DuplicateKeyValue(SQLException e)
	{
		return (e.getErrorCode() == -803);
	}
	
	public boolean MaskedFileNotFound(SQLException e)
	{
		return (e.getErrorCode() == -204);
	}	

	public boolean ObjectLocked(SQLException e)
	{
		// Quiza es -911 .. asi est� en VB, averiguar.
		return (e.getErrorCode() == -903);
	}
	
	public boolean ObjectNotFound(SQLException e)
 	{
		return (e.getErrorCode() == -204);
	}

	public java.util.Date nullDate()
	{
		return CommonUtil.ymdhmsToT_noYL(1, 1, 1, 0, 0, 0);
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
	}

	public void onConnection(GXConnection con) throws SQLException
	{
	}
	
	public java.util.Date serverDateTime(GXConnection con) throws SQLException
	{
		ResultSet rslt = con.getStatement("_ServerDT_", "SELECT CURRENT TIMESTAMP FROM SYSIBM.SYSTABLES WHERE NAME = 'SYSTABLES' AND CREATOR = 'SYSIBM'", false).executeQuery();
		
		rslt.next();
		Date value = rslt.getTimestamp(1);
		rslt.close();

		return value;
	}
	
	public String serverVersion(GXConnection con) throws SQLException
	{
		return "";		
	}	

	public String connectionPhysicalId(GXConnection con)
	{
		return "";
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
            return false;
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
		return DBMS_DB2;
	}

        public int getLockRetryCount(int lockRetryCount, int waitRecord){
          return lockRetryCount;
        }
		
	public boolean connectionClosed(SQLException e)
	{
		return (e.getSQLState() == "08S01");
	}		
}


