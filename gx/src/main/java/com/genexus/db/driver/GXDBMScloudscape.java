// FrontEnd Plus GUI for JAD
// DeCompiled : GXDBMSinformix.class

package com.genexus.db.driver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Properties;

import com.genexus.CommonUtil;

// Referenced classes of package com.genexus.db.driver:
//            DataSource, GXConnection, GXDBMS

public class GXDBMScloudscape implements GXDBMS
{

    private static final boolean DEBUG = false;
    private boolean logged;
    private boolean notlogged;
    private DataSource dataSource;

	public ResultSet executeQuery(PreparedStatement stmt, boolean hold) throws SQLException
	{
		return stmt.executeQuery();
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

    public void setInReorg()
    {
    }

	public boolean DataTruncation(SQLException e)
	{
		return false;
	}

    public boolean useReadOnlyConnections()
    {
        return true;
    }

    public void setDataSource(DataSource datasource)
    {
        dataSource = datasource;
        logged = datasource.getInformixDB().equals(DataSource.INFORMIX_DB_LOGGED);
    }

    public boolean DuplicateKeyValue(SQLException e)
    {
        return e.getSQLState().equals("23500");
    }

    public boolean ObjectNotFound(SQLException e)
    {
        return e.getSQLState().equals("42X05") || e.getSQLState().equals("42X65") || e.getSQLState().equals("42Y55")  ;
    }

    public boolean useDateTimeInDate()
    {
        return false;
    }

    public boolean useCharInDate()
    {
        return false;
    }

    public GXDBMScloudscape()
    {
        logged = true;
        notlogged = true;
    }

    public void onConnection(GXConnection gxconnection)
        throws SQLException
    {
        if(dataSource.getInformixDB().equals(DataSource.INFORMIX_DB_LOGGED))
        {
            Statement statement = gxconnection.createStatement();
            statement.executeUpdate("BEGIN WORK");
            statement.close();
			gxconnection.setCommitedChanges();
        }
    }

    public void setDatabaseName(String s)
    {
    }
	public String getDatabaseName()
	{
		return "";
	}    

    public void rollback(Connection connection)
        throws SQLException
    {
        connection.rollback();
        if(logged)
        {
            Statement statement = connection.createStatement();
            statement.executeUpdate("BEGIN WORK");
            statement.close();
			((GXConnection) connection).setCommitedChanges();
        }
    }

    public boolean ObjectLocked(SQLException e)
    {
        return e.getErrorCode() == -243 || e.getErrorCode() == -244 || e.getErrorCode()  == -245;
    }

    public boolean ignoreConnectionError(SQLException e)
    {
        return false;
    }

    public boolean ReferentialIntegrity(SQLException e)
    {
	return false;
    }

    public boolean EndOfFile(SQLException e)
    {
        return e.getErrorCode() == 100;
    }

    public void setConnectionProperties(Properties properties)
    {
    }

    public boolean getSupportsAutocommit()
    {
        return !dataSource.getInformixDB().equals(DataSource.INFORMIX_DB_ANSI);
    }

    public Date nullDate()
    {
        return CommonUtil.ymdhmsToT_noYL(1000, 1, 1, 0, 0, 0);
    }

    public void commit(Connection connection)
        throws SQLException
    {
        connection.commit();
        if(logged)
        {
            Statement statement = connection.createStatement();
            statement.executeUpdate("BEGIN WORK");
            statement.close();
			((GXConnection) connection).setCommitedChanges();
        }
    }

    public Date serverDateTime(GXConnection gxconnection)
        throws SQLException
    {
        ResultSet resultset = gxconnection.getStatement("_ServerDT_", "SELECT CURRENT YEAR TO SECOND FROM informix.SYSTABLES WHERE tabname = 'systables'", false).executeQuery();
        resultset.next();
        java.sql.Timestamp timestamp = resultset.getTimestamp(1);
        resultset.close();
        return timestamp;
    }
	
	public String serverVersion(GXConnection con) throws SQLException
	{
		return "";		
	}	
	
	public String connectionPhysicalId(GXConnection con)
	{
		return "";
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
		return DBMS_CLOUDSCAPE;
	}
        public int getLockRetryCount(int lockRetryCount, int waitRecord){
          return lockRetryCount;
        }
		
	public boolean connectionClosed(SQLException e)
	{
		return false;
	}		
}
