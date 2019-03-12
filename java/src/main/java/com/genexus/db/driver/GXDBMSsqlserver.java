
package com.genexus.db.driver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import com.genexus.CommonUtil;

public class GXDBMSsqlserver implements GXDBMS
{	
	public ResultSet executeQuery(PreparedStatement stmt, boolean hold) throws SQLException
	{
		return stmt.executeQuery();
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

	public boolean EndOfFile(SQLException e)
	{
		return	(	e.getErrorCode() == 100
				);
	}

	public boolean ReferentialIntegrity(SQLException e)
	{
		return (e.getErrorCode() == 547);
	}

	public boolean DuplicateKeyValue(SQLException e)
	{
		return	(	e.getErrorCode() == 2627 ||		/*	Violation of %s constraint '%.*s': Attempt to insert duplicate key in object '%.*s'.*/
 					e.getErrorCode() == 2601 			/* Attempt to insert duplicate key row in object '%.*s' with unique index '%.*s'*/
				);
	}

	public boolean ObjectLocked(SQLException e)
	{
		return	(	
					e.getErrorCode() == 1000 ||  /* "Lock request time-out period exceeded"  */
					e.getErrorCode() == 1222 ||  /* "Lock request time-out period exceeded"  */
					e.getErrorCode() == 903 
				);
	}

	public boolean ObjectNotFound(SQLException e)
	{
		return	(	e.getErrorCode() == 3704 ||	/* Table not found */
					e.getErrorCode() == 3703 ||	/* Index not found */
					e.getErrorCode() == 3701 ||	/* Index not found */
					e.getErrorCode() == 4902 ||	/* Table not found in Alter statement */
					e.getErrorCode() == 3728 ||	/*  .. is not a constraint.*/
					e.getErrorCode() == 3727 ||	/* Constraint not found */
					e.getErrorCode() == 1801 || /* Database already exists */
					e.getErrorCode() == 15032   /* Database already exists */
				);
	}

	public java.util.Date nullDate()
	{
		return CommonUtil.ymdhmsToT_noYL(1753, 1, 1, 0, 0, 0);
	}

	public boolean useDateTimeInDate()
	{
		if	(dataSource.jdbcDriver.startsWith("com.microsoft.jdbc.sqlserver") || 
			 dataSource.jdbcDriver.startsWith("com.ddtek.jdbc.sqlserver.SQLServerDriver"))
			return true;

		return false;
	}

	public boolean useCharInDate()
	{
		return false;
	}

	public void setConnectionProperties(java.util.Properties props)
	{
	}

	public boolean allowsCommitWithAutocommit = true;
	public void onConnection(GXConnection con) throws SQLException
	{
		if	(dataSource.waitRecord > 0)
		{
			Statement s1 = con.createStatement();
			s1.executeUpdate("SET LOCK_TIMEOUT " + (dataSource.waitRecord * 1000));
			s1.close();
			con.setCommitedChanges();
		}
																															
		if(con.getDataSource().jdbcDriver.startsWith("net.sourceforge.jtds.") || con.getDataSource().jdbcDriver.startsWith("com.microsoft.sqlserver.jdbc.SQLServerDriver"))
		{
			// @gusbro 28/04/05
			// Los drivers de jtds y los de Ms para SQLServer 2005 no permiten ejecutar un commit si autocommit esta en on			
			// Nota: por ahora no podemos directamente NO hacer el commit en todos los casos
			// porque por ejemplo con los drivers de I-net (una2000) al crear una tabla nueva en 
			// la reorg si no hacemos un commit (aunque haya autocommit) NO anda!
			allowsCommitWithAutocommit = false;
		}
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
		ResultSet rslt = con.getStatement("_ServerVERSION_", "SELECT CAST(SERVERPROPERTY('ResourceVersion') AS VARCHAR(20)), CAST(SERVERPROPERTY('productversion') AS VARCHAR(20))", false).executeQuery();
		
		rslt.next();
		String value = rslt.getString(1);
		if (rslt.wasNull())
		{
			value = rslt.getString(2);
		}
		rslt.close();

		return value.replaceAll("10.", "9.");
	}
	
	public String connectionPhysicalId(GXConnection con)
	{
		try
		{
			ResultSet rslt = con.getStatement("_ConnectionID_", "SELECT @@spid", false).executeQuery();

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
		if(allowsCommitWithAutocommit || !con.getAutoCommit())
		{
			con.commit();
		}		
	}

	public void rollback(Connection con) throws SQLException
	{
		if(allowsCommitWithAutocommit || !con.getAutoCommit())
		{
			con.rollback();
		}
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
		return DBMS_SQLSERVER;
	}
        public int getLockRetryCount(int lockRetryCount, int waitRecord){
          return lockRetryCount;
        }
		
	public boolean connectionClosed(SQLException e)
	{
		return (e.getSQLState() == "08S01");
	}		
}
