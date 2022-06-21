package com.genexus.db.driver;

import java.sql.*;
import java.util.Date;

import com.genexus.CommonUtil;

public class GXDBMSaccess implements GXDBMS
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

	public boolean isAlive(GXConnection con)
	{
		return true;
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

	public boolean useReadOnlyConnections()
	{
		return true;
	}

	public boolean ReferentialIntegrity(SQLException e)
	{
		return false;
	}

	public boolean EndOfFile(SQLException e)
	{
		return	(	e.getErrorCode() == 100
				);
	}

	public boolean DuplicateKeyValue(SQLException e)
	{
		return	(	e.getErrorCode() == 2627 ||		/*	Violation of %s constraint '%.*s': Attempt to insert duplicate key in object '%.*s'.*/
 					e.getErrorCode() == 2601			/* Attempt to insert duplicate key row in object '%.*s' with unique index '%.*s'*/
				);
	}

	public boolean ObjectLocked(SQLException e)
	{
		return	(	e.getErrorCode() == -903
				);
	}

	public boolean ObjectNotFound(SQLException e)
	{
	
		return	(	e.getErrorCode() == -1305 ||	/* Table not found */
					e.getErrorCode() == -1404 ||	/* Index not found */
					e.getErrorCode() == 3701 ||	/* Index not found */
					e.getErrorCode() == 4902 ||	/* Table not found in Alter statement */
					e.getErrorCode() == 3728 ||	/*  .. is not a constraint.*/
					e.getErrorCode() == 3727 		/* Constraint not found */

				);
	}

	public java.util.Date nullDate()
	{
		return CommonUtil.ymdhmsToT_noYL(1753, 1, 1, 0, 0, 0);
	}

	public boolean useDateTimeInDate()
	{
		return true;
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
		ResultSet rslt = con.getStatement("_ServerDT_", "SELECT GETDATE()", false).executeQuery();
		
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
		con.commit();
	}

	public void rollback(Connection con) throws SQLException
	{
		con.rollback();
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
		return false;
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
		return DBMS_ACCESS;
	}
        public int getLockRetryCount(int lockRetryCount, int waitRecord){
          return lockRetryCount;
        }
		
	public boolean connectionClosed(SQLException e)
	{
		return false;
	}		
}
