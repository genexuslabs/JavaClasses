package com.genexus.db.driver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface GXDBMS
{
	public static final int DBMS_ACCESS = 0;
	public static final int DBMS_AS400 = 1;
	public static final int DBMS_CLOUDSCAPE = 2;
	public static final int DBMS_DB2 = 3;
	public static final int DBMS_INFORMIX = 4;
	public static final int DBMS_MYSQL = 5;
	public static final int DBMS_ORACLE = 6;
	public static final int DBMS_POSTGRESQL = 7;
	public static final int DBMS_SQLSERVER = 8;
	public static final int DBMS_SQLITE = 9;
	public static final int DBMS_HANA = 10;
	public static final int DBMS_SERVICE = 11;

	boolean DataTruncation(SQLException e);
	boolean useReadOnlyConnections();
	boolean getSupportsAutocommit();
	boolean getSupportsQueryTimeout();
	void setDataSource(DataSource dataSource);
	boolean EndOfFile(SQLException GXSQLCODE);
	boolean DuplicateKeyValue(SQLException GXSQLCODE);
	boolean ReferentialIntegrity(SQLException GXSQLCODE);
	boolean ObjectLocked(SQLException GXSQLCODE);
	boolean ObjectNotFound(SQLException GXSQLCODE);
	java.util.Date nullDate();
	boolean useDateTimeInDate();
	boolean useStreamsInNullLongVarchar();
	boolean useStreamsInLongVarchar();
	boolean useCharInDate();
	void setConnectionProperties(java.util.Properties props);
	void onConnection(GXConnection con) throws SQLException;
	java.util.Date serverDateTime(GXConnection con) throws SQLException;
	String serverVersion(GXConnection con) throws SQLException;
	String connectionPhysicalId(GXConnection con);

	void commit(Connection con) throws SQLException;
	void rollback(Connection con) throws SQLException;
	ResultSet executeQuery(PreparedStatement stmt, boolean hold) throws SQLException;

	void setDatabaseName(String dbName);
	String getDatabaseName();
	void setInReorg();
        
        boolean rePrepareStatement(SQLException GXSQLCODE);
		boolean connectionClosed(SQLException GXSQLCODE);

	boolean ignoreConnectionError(SQLException GXSQLCODE);
	boolean isAlive(GXConnection con);
	
	int getId();
        int getLockRetryCount(int lockRetryCount, int waitRecord);
}


