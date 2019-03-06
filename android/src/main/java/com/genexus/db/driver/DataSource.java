/**
* Representa un DataSource GeneXus. Tiene asociado un connection pool.
*
*/

package com.genexus.db.driver;

import java.sql.SQLException;

import com.genexus.common.classes.AbstractDataSource;

public class DataSource extends AbstractDataSource
{

	public static String INFORMIX_DB_ANSI 		= "ANSI";
	public static String INFORMIX_DB_LOGGED 	= "Logged";
	public static String INFORMIX_DB_NOTLOGGED 	= "NotLogged";

	private GXDBDebug debugLog;

	public String  	defaultUser;
	public String  	defaultPassword;

	public String  	jdbcDriver;
	public String  	jdbcUrl;
	public String  	jdbcAS400Lib;
	public String  	jdbcDBName;
	public boolean 	jdbcLogEnabled;
	public int 	   	jdbcIsolationLevel;
	public boolean 	jdbcIntegrity;
	public String  	informixDB;
	public int 	   	maxCursors;
	public String 	name;
	public String 	schema;
	public boolean  initializeNew;
	public GXDBMSsqlite 	dbms;

	public int rwPoolSize;
	public boolean rwPoolSizeUnlimited;
	public boolean forceAutocommit;
	public int roPoolSize;
	public boolean roPoolSizeUnlimited;
	public int roPoolUsers;
	public boolean createPoolStartup;
	public boolean connectStartup;
	public boolean roPoolEnabled;

	private boolean rwPoolEnabled;
	private boolean roPoolRecycle;
	private int 	roPoolRecycleMins;
	private boolean rwPoolRecycle;
	private int 	rwPoolRecycleMins;
	private int 	rwPoolRecycleType;
	public  int		waitRecord;
    public  int	lockRetryCount;
    public boolean loginInServer;

	private JDBCLogConfig jdbcLogCfg;

	private boolean dataSourceLog = false;

	private String as400Package = "";
	private String as400DateType = "";

	public boolean useJdbcDataSource;
      	private boolean useMultiJdbcDataSource;
        public String MultiJdbcDataSourceProc;
	public String jdbcDataSource;
	
	private String namespace;

	public DataSource(
						String name,
						String jdbcDriver,
						String jdbcUrl,
						String jdbcUser,
						String jdbcPassword,
						JDBCLogConfig jdbcLogCfg,
						boolean useJdbcDataSource,
                                                boolean useMultiJdbcDataSource,
                                                String MultiJdbcDataSourceProc,
						String jdbcDataSource,
						int maxCursors,
						int jdbcIsolationLevel,
						boolean jdbcIntegrity,
						String dbmsName,
						String schema,
						boolean initializeNew,
						String jdbcAS400Lib,
						String jdbcDBName,
						int waitRecord,
                                                int lockRetryCount,
                                                boolean loginInServer,
						int rwPoolSize,
						boolean rwPoolSizeUnlimited,
						boolean roPoolEnabled,
						int roPoolSize,
						boolean roPoolSizeUnlimited,
						int roPoolUsers,
						boolean createPoolStartup,
						boolean forceAutocommit,
						boolean connectStartup,
						boolean rwPoolEnabled,
						boolean roPoolRecycle,
						int roPoolRecycleMins,
						boolean rwPoolRecycle,
						int rwPoolRecycleMins,
						int rwPoolRecycleType)
	{
		this.name 			     = name;
		this.jdbcDriver		     = jdbcDriver;
		this.jdbcUrl		     = jdbcUrl;
		this.defaultUser	     = jdbcUser;
		this.defaultPassword     = jdbcPassword;
		this.jdbcLogCfg			 = jdbcLogCfg;

		this.useJdbcDataSource = useJdbcDataSource;
                this.useMultiJdbcDataSource = useMultiJdbcDataSource;
                this.MultiJdbcDataSourceProc = MultiJdbcDataSourceProc;
		this.jdbcDataSource = jdbcDataSource;

		this.jdbcIsolationLevel  = jdbcIsolationLevel;
		this.jdbcIntegrity       = jdbcIntegrity;
		this.maxCursors		     = maxCursors;
		this.schema			     = schema;
		this.initializeNew	     = initializeNew;
		this.jdbcAS400Lib        = jdbcAS400Lib;
		this.jdbcDBName		   	 = jdbcDBName;
		this.rwPoolSize 		 = rwPoolSize;
		this.rwPoolEnabled		 = rwPoolSize == 0?false:rwPoolEnabled;
		this.rwPoolSizeUnlimited = this.rwPoolEnabled?rwPoolSizeUnlimited:true;
		this.roPoolEnabled 		 = roPoolSize == 0?false:roPoolEnabled;
		this.roPoolSize			 = roPoolSize;
		this.roPoolSizeUnlimited = roPoolEnabled?roPoolSizeUnlimited:true;
		this.roPoolUsers		 = roPoolUsers;
		this.createPoolStartup   = createPoolStartup ;
		this.forceAutocommit     = forceAutocommit;
		this.connectStartup      = connectStartup;

		this.roPoolRecycle		 = roPoolRecycle;
		this.roPoolRecycleMins	 = roPoolRecycleMins;
		this.rwPoolRecycle		 = rwPoolRecycle;
		this.rwPoolRecycleMins	 = rwPoolRecycleMins;
		this.rwPoolRecycleType	 = rwPoolRecycleType;

		this.waitRecord			 = waitRecord;

                this.loginInServer = loginInServer;

		setDBMS(dbmsName);
                this.lockRetryCount = lockRetryCount;
		dbms.setDatabaseName(this.jdbcDBName);
		if (forceAutocommit) dbms.setInReorg();

		if	(maxCursors <= 0)
		{
			throw new InternalError("The preference Maximum open cursors per connection has an invalid value (0)");
		}
	}

	public void initialize()
	{
	}

	public void cleanup() throws SQLException
	{
		if	(getLog() != null)
		{
			if	(jdbcLogCfg.getLevel() == JDBCLogConfig.LEVEL_CONNECTION)
				getLog().close(JDBCLogConfig.LEVEL_CONNECTION);
			else
				getLog().close(JDBCLogConfig.LEVEL_DATASOURCE);
		}
	}

	void setConnectionLog(GXConnection con)
	{
		if	(jdbcLogCfg.getLevel() == JDBCLogConfig.LEVEL_CONNECTION)
		{
			jdbcLogCfg.setConnectionName(Integer.toString(con.hashCode()));
			con.setLog(new GXDBDebug(jdbcLogCfg));
		}
		else
		{
			con.setLog(getLog());
		}
	}
        public int getLockRetryCount(){
          return dbms.getLockRetryCount(lockRetryCount, waitRecord);
	}

	public GXDBDebug getLog()
	{
		return debugLog;
	}

	public void setLog(GXDBDebug debugLog)
	{
		this.debugLog = debugLog;
	}

	private void setDBMS(String dbmsName)
	{
			dbms = new GXDBMSsqlite();
	}	

	public void disconnect(int handle) throws SQLException
	{
	}

	public void disconnectOnException(int handle) throws SQLException
	{
	}	

	public String getName()
	{
		return name;
	}
	
	public String getNamespace()
	{
		return namespace;
	}
	
	public void setNamespace(String namespace)
	{
		this.namespace = namespace;
	}		
	
	
	public String[] concatOp()
	{
		return new String[]{"", " || ", ""};
	}
	
}

