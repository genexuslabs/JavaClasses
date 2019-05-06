package com.genexus.db.driver;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

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
	public String   dbmsName;
	public String  	informixDB;
	public int 	   	maxCursors;
	public String 	schema;
	public boolean  initializeNew;
	public GXDBMS 	dbms;

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

	private IConnectionPool connectionPool;
	private Hashtable connectionPools; 

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
		this(name, jdbcDriver, jdbcUrl, jdbcUser, jdbcPassword, jdbcLogCfg, useJdbcDataSource, useMultiJdbcDataSource,
				MultiJdbcDataSourceProc, jdbcDataSource, maxCursors, jdbcIsolationLevel, jdbcIntegrity, dbmsName, schema,
				initializeNew, jdbcAS400Lib, jdbcDBName, waitRecord, lockRetryCount, loginInServer, rwPoolSize, rwPoolSizeUnlimited,
				roPoolEnabled, roPoolSize, roPoolSizeUnlimited, roPoolUsers, createPoolStartup, forceAutocommit, connectStartup,
				rwPoolEnabled, roPoolRecycle, roPoolRecycleMins, rwPoolRecycle, rwPoolRecycleMins, rwPoolRecycleType, true);
	}
	

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
						int rwPoolRecycleType,
						boolean init)
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
                this.dbmsName = dbmsName;
                this.lockRetryCount = lockRetryCount;
        if (init)
        {
        	init();
        }
	}
	
	private void init()
	{
		setDBMS(dbmsName);
		dbms.setDatabaseName(this.jdbcDBName);
		if (forceAutocommit) dbms.setInReorg();

		if	(maxCursors <= 0)
		{
			throw new InternalError("The preference Maximum open cursors per connection has an invalid value (0)");
		}
		
		connectionPools = new Hashtable();
	}

	public void initialize()
	{
		if	(createPoolStartup)
		{
			System.err.println("Started to create connection pool ...");
			getConnectionPool();
			System.err.println("Finished to create connection pool.");
		}
	}

	public void cleanup() throws SQLException
	{
		getConnectionPool().disconnect();
		
		if (connectionPools.size() > 0)
		{
			for (Enumeration en = connectionPools.elements(); en.hasMoreElements(); )
            {
                ( (IConnectionPool) en.nextElement()).disconnect();
            }			
		}

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

	public void setInformixDB(String informixDB)
	{
		this.informixDB = informixDB;
	}

	public String getInformixDB()
	{
		return informixDB;
	}

	public void setAS400Package(String as400Package)
	{
		this.as400Package = as400Package;
	}

	public String getAS400Package()
	{
		return as400Package;
	}

	public void setAS400DateType(String as400DateType)
	{
		this.as400DateType = as400DateType;
	}

	public String getAS400DateType()
	{
		return as400DateType;
	}

	private void setDBMS(String dbmsName)
	{
		String className =  "com.genexus.db.driver.GXDBMS" + dbmsName.toLowerCase();
		try
		{
			dbms = (GXDBMS) Class.forName(className).getConstructor().newInstance();
		}
		catch (Exception e)
		{
			throw new InternalError("Unrecognized DBMS in configuration file : " + dbmsName + " / " + className);
		}
	}

	public synchronized IConnectionPool getConnectionPool()
	{
		if	(connectionPool == null)
		{
			if(useGXConnectionPool())
			{
				connectionPool = new DataSourceConnectionPool(this);
			}
			else
			{ // Si no debo usar un pool de conexiones
				connectionPool = new DirectConnectionPool(this);
			}
		}

		return connectionPool;
	}
	
	public IConnectionPool getConnectionPool(int handle)
	{
		if (connectionPools.size() == 0)
		{
			return getConnectionPool();
		}		
		else
		{
			for (Enumeration en = connectionPools.elements(); en.hasMoreElements(); )
            {
				IConnectionPool iconnPool = ((IConnectionPool) en.nextElement());
				for (Enumeration en1 = iconnPool.getRWPools(); en1.hasMoreElements(); )
				{
					String user = ((String) en1.nextElement());
					ConnectionPool connPool = iconnPool.getRWConnectionPool(user);
					for (Enumeration en2 = connPool.getConnections(); en2.hasMoreElements(); )
					{
						if (((GXConnection) en2.nextElement()).getHandle() == handle)
						{
							return iconnPool;
						}
					}					
				}
            }
			//Si no esta en ninguno de los pooles anteriores entoces esta en el default
			return getConnectionPool();
		}
	}	
	
	public void setConnectionPool(IConnectionPool connectionPool)
	{
		this.connectionPool = connectionPool;
	}

	public synchronized Hashtable getConnectionPools()
	{
		return connectionPools;
	}
	
	public void setConnectionPools(Hashtable connectionPools)
	{
		this.connectionPools = connectionPools;
	}	

	public synchronized IConnectionPool getConnectionPool(String connectionString)
	{
		IConnectionPool connPool = (IConnectionPool)connectionPools.get(connectionString);
		
		if (connPool == null)
		{
				connPool = new DataSourceConnectionPool(this);
				connectionPools.put(connectionString, connPool);
		}

		return connPool;
	}	

	public boolean usesJdbcDataSource()
	{	
		if (com.genexus.ApplicationContext.getInstance().getEJB()) 
		{ //Si estoy en el contexto de un EJB
			try
            {
              javax.naming.Context initCtx = new javax.naming.InitialContext();
              String trnType = (String) initCtx.lookup("java:comp/env/GX/TrnType");
              if (trnType.equals("CONTAINER")) // Si la TRN en el EJB es manejada por el contenedor
                  return true;
            }
            catch (javax.naming.NamingException e)
            {
                    throw new RuntimeException(e.getMessage());
            }
          }
		return useJdbcDataSource;
	}

        public boolean usesMultiJdbcDataSource()
        {
                return useMultiJdbcDataSource;
        }

	// Indica si debo usar el pool de conexiones de GX
	public boolean useGXConnectionPool()
	{
		// Si no estoy usando un datasource del motor de servlets
		// o si estoy en 3 capas HTTP stateful, debo usar el pool nuestro
		return(!usesJdbcDataSource() ||
		       com.genexus.Preferences.getDefaultPreferences().getREMOTE_CALLS() == com.genexus.Preferences.ORB_HTTP_STATEFUL);
	}

	public void disconnect(int handle) throws SQLException
	{
		if (connectionPools.size() == 0)
		{
			getConnectionPool().disconnect(handle);
		}
		else
		{
			for (Enumeration en = connectionPools.elements(); en.hasMoreElements(); )
            {
				IConnectionPool iconnPool = ((IConnectionPool) en.nextElement());
				for (Enumeration en1 = iconnPool.getRWPools(); en1.hasMoreElements(); )
				{
					String user = ((String) en1.nextElement());
					ConnectionPool connPool = iconnPool.getRWConnectionPool(user);
					for (Enumeration en2 = connPool.getConnections(); en2.hasMoreElements(); )
					{
						if (((GXConnection) en2.nextElement()).getHandle() == handle)
						{
							connPool.disconnect(handle);
							return;
						}
					}					
				}
            }
			//Si no esta en ninguno de los pooles anteriores entoces esta en el default
			getConnectionPool().disconnect(handle);
		}
	}

	public void disconnectOnException(int handle) throws SQLException
	{
		if (connectionPools.size() == 0)
		{
			getConnectionPool().disconnectOnException(handle);
		}
		else
		{
			for (Enumeration en = connectionPools.elements(); en.hasMoreElements(); )
            {
				ConnectionPool connPool = ((ConnectionPool) en.nextElement());
				for (Enumeration en1 = connPool.getConnections(); en1.hasMoreElements(); )
				{
					if (((GXConnection) en1.nextElement()).getHandle() == handle)
					{
						connPool.disconnectOnException(handle);
						return;
					}
				}
            }
			//Si no esta en ninguno de los pooles anteriores entoces esta en el default
			getConnectionPool().disconnectOnException(handle);
		}
	}

	public boolean getRWPoolEnabled()
	{
		return rwPoolEnabled;
	}

	public boolean getROPoolEnabled()
	{
		return roPoolEnabled;
	}

	public boolean getROPoolRecycle()
	{
		return roPoolRecycle;
	}

	public int getROPoolRecycleMins()
	{
		return roPoolRecycleMins;
	}

	public boolean getRWPoolRecycle()
	{
		return rwPoolRecycle;
	}

	public int getRWPoolRecycleMins()
	{
		return rwPoolRecycleMins;
	}
	
	public int getRWPoolRecycleType()
	{
		return rwPoolRecycleType;
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
	
	public DataSource copy()
	{
		DataSource copyDataSource = new DataSource(this.name, this.jdbcDriver, this.jdbcUrl, this.defaultUser, this.defaultPassword, this.jdbcLogCfg, this.useJdbcDataSource, this.useMultiJdbcDataSource,
		this.MultiJdbcDataSourceProc, this.jdbcDataSource, this.maxCursors, this.jdbcIsolationLevel, this.jdbcIntegrity, this.dbmsName, this.schema, this.initializeNew, this.jdbcAS400Lib,
		this.jdbcDBName, this.waitRecord, this.lockRetryCount, this.loginInServer, this.rwPoolSize, this.rwPoolSizeUnlimited, this.roPoolEnabled, this.roPoolSize, this.roPoolSizeUnlimited,
		this.roPoolUsers, this.createPoolStartup, this.forceAutocommit, this.connectStartup, this.rwPoolEnabled, this.roPoolRecycle, this.roPoolRecycleMins, this.rwPoolRecycle,
		this.rwPoolRecycleMins, this.rwPoolRecycleType, false);
		copyDataSource.setLog(this.getLog());
		copyDataSource.jdbcLogEnabled = this.jdbcLogEnabled;
		copyDataSource.setInformixDB(this.getInformixDB());
		copyDataSource.dbms = this.dbms;
		copyDataSource.setAS400Package(this.getAS400Package());
		copyDataSource.setAS400DateType(this.getAS400DateType());
		copyDataSource.setNamespace(this.getNamespace());
		copyDataSource.setConnectionPool(this.getConnectionPool());
		copyDataSource.setConnectionPools(this.getConnectionPools());
		return copyDataSource;
	}
	
	public String[] concatOp()
	{
		switch(dbms.getId())
		{
			case GXDBMS.DBMS_DB2:
				return new String[]{"", " CONCAT ", ""};
			case GXDBMS.DBMS_ORACLE:
			case GXDBMS.DBMS_HANA:
			case GXDBMS.DBMS_POSTGRESQL:
			case GXDBMS.DBMS_SQLITE:
				return new String[]{"", " || ", ""};
			case GXDBMS.DBMS_SQLSERVER:
			case GXDBMS.DBMS_SERVICE:
				return new String[]{"", " + ", ""};
			case GXDBMS.DBMS_AS400:
			case GXDBMS.DBMS_MYSQL:
			case GXDBMS.DBMS_INFORMIX:
			default:
				return new String[]{"CONCAT(", ", ", ")"};
		}
	}
	
//////////////////////////////////////////JMX Operations//////////////////////////////////
	public void ROPoolRecycle()
	{
		if (getConnectionPool().getROConnectionPool(defaultUser) != null)
			getConnectionPool().getROConnectionPool(defaultUser).PoolRecycle();
	}
	
	public void RWPoolRecycle()
	{
		if (getConnectionPool().getRWConnectionPool(defaultUser) != null)
			getConnectionPool().getRWConnectionPool(defaultUser).PoolRecycle();		
	}		
	
}

