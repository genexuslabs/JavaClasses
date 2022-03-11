
package com.genexus.db.driver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import com.genexus.CommonUtil;

public class GXDBMSGxTest implements GXDBMS
{
	GXDBMS GXDBMSImpl;
	public GXDBMSGxTest(GXDBMS innerInstance)
	{
		GXDBMSImpl = innerInstance;
	}

	public ResultSet executeQuery(PreparedStatement stmt, boolean hold) throws SQLException
	{
		System.out.println("Executing SQL sentence");
		return GXDBMSImpl.executeQuery(stmt, hold);
	}

	public int executeUpdate(PreparedStatement stmt) throws SQLException
	{
		System.out.println("Executing Update sentence");
		return GXDBMSImpl.executeUpdate(stmt);
	}

	public boolean execute(PreparedStatement stmt) throws SQLException
	{
		return GXDBMSImpl.execute(stmt);
	}

	public int[] executeBatch(Statement stmt) throws SQLException
	{
		return GXDBMSImpl.executeBatch(stmt);
	}

	public void setDatabaseName(String dbName)
	{
		GXDBMSImpl.setDatabaseName(dbName);
	}

	public String getDatabaseName()
	{
		return GXDBMSImpl.getDatabaseName();
	}

	public void setInReorg()
	{
		GXDBMSImpl.setInReorg();
	}

	public boolean isAlive(GXConnection con)
	{
		return GXDBMSImpl.isAlive(con);
	}

	public boolean DataTruncation(SQLException e)
	{
		return GXDBMSImpl.DataTruncation(e);
	}

	public void setDataSource(DataSource dataSource)
	{
		GXDBMSImpl.setDataSource(dataSource);
	}

	public boolean useReadOnlyConnections()
	{
		return GXDBMSImpl.useReadOnlyConnections();
	}

	public boolean EndOfFile(SQLException e)
	{
		return GXDBMSImpl.EndOfFile(e);
	}

	public boolean ReferentialIntegrity(SQLException e)
	{
		return GXDBMSImpl.ReferentialIntegrity(e);
	}

	public boolean DuplicateKeyValue(SQLException e)
	{
		return GXDBMSImpl.DuplicateKeyValue(e);
	}

	public boolean ObjectLocked(SQLException e)
	{
		return GXDBMSImpl.ObjectLocked(e);
	}

	public boolean ObjectNotFound(SQLException e)
	{
		return GXDBMSImpl.ObjectNotFound(e);
	}

	public java.util.Date nullDate()
	{
		return GXDBMSImpl.nullDate();
	}

	public boolean useDateTimeInDate()
	{
		return GXDBMSImpl.useDateTimeInDate();
	}

	public boolean useCharInDate()
	{
		return GXDBMSImpl.useCharInDate();
	}

	public void setConnectionProperties(java.util.Properties props)
	{
		GXDBMSImpl.setConnectionProperties(props);
	}

	public void onConnection(GXConnection con) throws SQLException
	{
		GXDBMSImpl.onConnection(con);
	}
	
	public java.util.Date serverDateTime(GXConnection con) throws SQLException
	{
		return GXDBMSImpl.serverDateTime(con);
	}
	
	public String serverVersion(GXConnection con) throws SQLException
	{
		return GXDBMSImpl.serverVersion(con);
	}
	
	public String connectionPhysicalId(GXConnection con)
	{
		return GXDBMSImpl.connectionPhysicalId(con);
	}	

	public boolean getSupportsAutocommit()
	{
		return GXDBMSImpl.getSupportsAutocommit();
	}

	public void commit(Connection con) throws SQLException
	{
		GXDBMSImpl.commit(con);
	}

	public void rollback(Connection con) throws SQLException
	{
		GXDBMSImpl.rollback(con);
	}

	public boolean ignoreConnectionError(SQLException e)
	{
		return GXDBMSImpl.ignoreConnectionError(e);
	}

	public boolean rePrepareStatement(SQLException e)
	{
		return GXDBMSImpl.rePrepareStatement(e);
	}

	public boolean getSupportsQueryTimeout()
	{
		return GXDBMSImpl.getSupportsQueryTimeout();
	}

	public boolean useStreamsInNullLongVarchar()
	{
		return GXDBMSImpl.useStreamsInNullLongVarchar();
	}

	public boolean useStreamsInLongVarchar()
	{
		return GXDBMSImpl.useStreamsInLongVarchar();
	}

	public int getId()
	{
		return GXDBMSImpl.getId();
	}

	public int getLockRetryCount(int lockRetryCount, int waitRecord)
	{
		return GXDBMSImpl.getLockRetryCount(lockRetryCount, waitRecord);
	}
		
	public boolean connectionClosed(SQLException e)
	{
		return GXDBMSImpl.connectionClosed(e);
	}
}
