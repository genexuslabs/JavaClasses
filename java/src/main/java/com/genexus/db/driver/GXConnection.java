package com.genexus.db.driver;

import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import com.genexus.*;
import com.genexus.common.classes.AbstractGXConnection;
import com.genexus.common.classes.IGXPreparedStatement;
import com.genexus.db.BatchUpdateCursor;
import com.genexus.db.ConnectionInformation;
import com.genexus.db.Cursor;
import com.genexus.db.DBConnectionManager;
import com.genexus.db.UserInformation;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.management.ConnectionJMX;
import com.genexus.platform.INativeFunctions;
import com.genexus.platform.NativeFunctions;

import javax.xml.crypto.Data;

public final class GXConnection extends AbstractGXConnection implements Connection
{
	public static final ILogger logger = LogManager.getLogger(GXConnection.class);
	
	private static final boolean DEBUG       = DebugFlag.DEBUG;

	private String SunJdbcOdbcDriver = "sun.jdbc.odbc.JdbcOdbcDriver";
	private String MSJdbcOdbcDriver  = "com.ms.jdbc.odbc.JdbcOdbcDriver";

	private ConnectionPool pool;
	private Connection con;
	private int handle = -1;
	private int previousHandle = -1;
	private int thread;
	private java.util.Date nullDate;

	private String  connectedUser;

	private DataSource dataSource;
	private IPreparedStatementCache preparedStatementPool;
    private Vector<Cursor> batchUpdateStmts = new Vector<Cursor>();

	private ConnectionPoolState state;
	private GXDBDebug log;

	private long connectedTime ;
	private boolean error;

    private ModelContext context;

	private boolean doCommit;

	protected boolean bridge;
	protected String _dbURL, _user, _password, _jdbcDriver;
	protected Properties prop;
	protected SQLException sqlE;
	private long assignTime;
	private boolean locked = false;
	private long timeLock = 0;

	//JMX Properties
	int numberRequest = 0;
	java.util.Date timeLastRequest;
	String sentenceLastRequest;
	String lastObjectExecuted;
	boolean finishExecute = true;

	public GXConnection(DataSource dataSource, Connection con)
	{
		this.con = con;
		this.dataSource = dataSource;

		nullDate = dataSource.dbms.nullDate();
	}

	public GXConnection(ModelContext context, String user, String password, DataSource dataSource) throws SQLException
	{
		this( context, -1, user, password, dataSource);
	}

	GXConnection(ModelContext context, ConnectionPool pool, int handle, String user, String password, DataSource dataSource) throws SQLException
	{
		this( context, handle, user, password, dataSource);
		this.pool = pool;
	}
	
	GXConnection( ModelContext context, int handle, String user, String password, DataSource dataSource) throws SQLException
	{
		this(context, handle, user, password, dataSource, null);
	}
	
	public GXConnection( ModelContext context, int handle, String user, String password, DataSource dataSource, ConnectionInformation info) throws SQLException
	{
		this.thread = Thread.currentThread().hashCode();
		this.handle     = handle;
		this.dataSource = dataSource;

                this.context = context;

		if 	(System.getProperty("gx.jdbclog") != null)
		{
			try
			{
				DriverManager.setLogWriter(new java.io.PrintWriter(new java.io.FileOutputStream("_gx_jdbc_driver_log.log")));
			} catch (java.io.IOException e){}
		}

		nullDate = dataSource.dbms.nullDate();
		dataSource.setConnectionLog(this);

		if (context != null)
		{
			if(context.getPreferences().getProperty("CompatibleEmptyStringAsNull", "0").equals("1"))
			{
				GXPreparedStatement.addSpaceToEmptyVarChar = false;
			}

			if(context.getPreferences().getProperty("AvoidDataTruncationError", "0").equals("1"))
			{
				GXPreparedStatement.avoidDataTruncationError = true;
			}

			if(context.getPreferences().getProperty("LongVarCharAsOracleLong", "0").equals("1"))
			{
				GXResultSet.longVarCharAsOracleLong = true;
				GXPreparedStatement.longVarCharAsOracleLong = true;
			}

			if(context.getPreferences().getProperty("DontGetConId", "0").equals("1"))
			{
				dbmsId = "";
			}

			//En el caso de una aplicacion en dos capas hay que llamar al Before Connect aca
			if(!ApplicationContext.getInstance().getPoolConnections())
			{
				DataSource dataSource1 = (DataSource) context.beforeGetConnection(handle, dataSource);
				if (dataSource1 != null)
				{
					dataSource = dataSource1;
					user = dataSource.defaultUser;
					password = dataSource.defaultPassword;					
				}
			}
		}

		if(dataSource.usesJdbcDataSource())
		{
			connect(null, dataSource.jdbcDataSource, user, "<JDBC DataSource defined>");
		}
		else
		{
			connect(dataSource.jdbcDriver, dataSource.jdbcUrl, user, password);
		}

		if	(DEBUG && isLogEnabled())
		{
			getLog().logWarnings(con.getWarnings ());

			DatabaseMetaData dma = con.getMetaData ();

			String version;
			try
			{
				version = dma.getDatabaseProductVersion();
			}
			catch (SQLException e)
			{
				version = "";
			}

			log(GXDBDebug.LOG_MIN, "Connected to     : " + dma.getURL());
			log(GXDBDebug.LOG_MIN, "                   " + dataSource.jdbcUrl);
			log(GXDBDebug.LOG_MIN, "Connection class : " + con.getClass().getName());
			log(GXDBDebug.LOG_MIN, "Database         : " + dma.getDatabaseProductName() + " version " + version);
			log(GXDBDebug.LOG_MIN, "Driver           : " + dma.getDriverName());
			log(GXDBDebug.LOG_MIN, "Version          : " + dma.getDriverVersion());
			log(GXDBDebug.LOG_MIN, "GX DBMS          : " + dataSource.dbms);
			log(GXDBDebug.LOG_MIN, "DataStore        : " + dataSource.name);
		}

		doCommit=true;
          if (context != null && context.getSessionContext() != null)
          { //Si estoy en el contexto de un EJB
            try
            {
              javax.naming.Context initCtx = new javax.naming.InitialContext();
              String trnType = (String) initCtx.lookup("java:comp/env/GX/TrnType");
              if (trnType.equals("CONTAINER")) // Si la TRN en el EJB es manejada por el contenedor
                  doCommit = false;
            }
            catch (javax.naming.NamingException e)
            {
                    throw new RuntimeException(e.getMessage());
            }
          }

		if (com.genexus.ApplicationContext.getInstance().getReorganization() && 
			getDBMS().getId() == GXDBMS.DBMS_INFORMIX &&
			dataSource.jdbcDBName.equals(""))
		{
			//Este es el caso de cuando se crea la base de datos en Informix que no hay que hacer setAutoCommit.
		}
		else
		{
			if (com.genexus.ApplicationContext.getInstance().getReorganization())
				setAutoCommit(dataSource.dbms.getSupportsAutocommit());
			else
			{
				if (doCommit && !(dataSource.usesJdbcDataSource() && GXJTA.isJTATX(handle, context)))
					setAutoCommit(!dataSource.jdbcIntegrity);
			}
		}

		try
		{
			if (con.getTransactionIsolation() != GXToJDBCIsolationLevel(dataSource.jdbcIsolationLevel))
				setTransactionIsolation(GXToJDBCIsolationLevel(dataSource.jdbcIsolationLevel));
		}
		catch (SQLException sqlException)
		{
			log(GXDBDebug.LOG_MIN, "Error setting transaction isolation to " + GXToJDBCIsolationLevel(dataSource.jdbcIsolationLevel));
			if	(isLogEnabled()) logSQLException(handle, sqlException);
		}

		connectedUser = user;
		this.connectedTime = System.currentTimeMillis();
		state = new ConnectionPoolState(this);

		// This must be executed after setting autocommit and transaction isolation values,
		// because for Informix it does a 'Begin Work'

		dataSource.dbms.onConnection(this);

		//Enable JMX
		if (Application.isJMXEnabled() && context != null)
			ConnectionJMX.CreateConnectionJMX(this);

		if (!com.genexus.ApplicationContext.getInstance().getReorganization())
		{
			//En el caso de una aplicacion en dos capas hay que llamar al After Connect aca
			if(context != null && !com.genexus.ApplicationContext.getInstance().getPoolConnections())
			{
				if (info != null)
					info.rwConnection = this;			
				context.afterGetConnection(handle, dataSource);
			}
		}
		String DBMSId = getDBMSId();
		if (!DBMSId.equals("")) 
		{
			log(GXDBDebug.LOG_MIN, "Physical Id      : " + DBMSId);
		}
	}

        public Connection getJDBCConnection()
        {
          return con;
        }

		public int getNetworkTimeout()
                      throws SQLException
		{
			return 0;			  
		}

		
	public void setNetworkTimeout(Executor executor,
                     int milliseconds)
                       throws SQLException
	{
						   
	}

	public DataSource getDataSource()
	{
		return dataSource;
	}

	public ModelContext getContext()
	{
		return context;
	}

	public long getConnectionTime()
	{
		return connectedTime;
	}

	public void setError()
	{
		this.error = true;
		state.connectionError();
	}

	public boolean getError()
	{
		return error;
	}

	private int GXToJDBCIsolationLevel(int gxIL)
	{
		if(!(dataSource.dbms instanceof GXDBMSinformix && dataSource.getInformixDB().equals(DataSource.INFORMIX_DB_NOTLOGGED)))
		{
			switch (gxIL)
			{
				case 1:
					return Connection.TRANSACTION_READ_COMMITTED;
				case 0:
					return Connection.TRANSACTION_READ_UNCOMMITTED;
				default:
					System.err.println("Invalid isolation level " + gxIL);
					return Connection.TRANSACTION_NONE;
			}
		}
		else
		{
			return Connection.TRANSACTION_NONE;
		}
	}
	public int getId()
	{
		return hashCode();
	}

	private void connect(String jdbcDriver, String dbURL, String user, String password) throws SQLException
	{
		if	(dataSource != null)
		{
			dataSource.dbms.setDataSource(dataSource);
		}

		try
		{
			if	(jdbcDriver == null)
			{
				connectJDBCDataSource(dbURL);
				preparedStatementPool = new DirectPreparedStatement(this);
			}
			else
			{
				connectJDBCDriver(jdbcDriver, dbURL, user, password);
				if	(dataSource != null)
				{
					if	(System.getProperty("gx.useoldcache") == null)
					{
						preparedStatementPool = new PreparedStatementCache(dataSource.maxCursors, this);
					}
					else
						preparedStatementPool = new CursorFactory(dataSource.maxCursors, this);
				}
			}
		}
		catch (SQLException sqlException)
		{
			if	(!dataSource.dbms.ignoreConnectionError(sqlException))
			{
				if (Application.getShowConnectError())
				{
					logger.fatal("Error connecting to database at " + new java.util.Date(), sqlException);
					
					System.err.println("Error connecting to database at " + new java.util.Date());
					System.err.println("Driver  : " + jdbcDriver);
					System.err.println("URL     : " + dbURL);
					System.err.println("User    : " + user);
					System.err.println("Error   : " + sqlException.getMessage());
					System.err.println("Connection : " + con);
					try
					{
						System.err.println("Java classpath    : " + System.getProperty("java.class.path"));
					}
					catch (SecurityException e)
					{
					}
				}

				 if	(DEBUG && isLogEnabled())
					 logSQLException(handle, sqlException);

				throw sqlException;
			}
		}
	}

	String externalConnectionManager = "com.genexus.util.ExternalConnectionManager";
	
	private void connectJDBCDriver(String jdbcDriver, String dbURL, String user, String password) throws SQLException
	{
		_jdbcDriver = jdbcDriver;
		_dbURL    = dbURL;
		_user     = user;
		_password = password;
		prop = new Properties();

		if	(jdbcDriver.equals(""))
			jdbcDriver = SunJdbcOdbcDriver;

		if	(DEBUG && isLogEnabled())
		{
			log(GXDBDebug.LOG_MIN, "Trying to connect to : Driver  : " + jdbcDriver);
			log(GXDBDebug.LOG_MIN, "                       URL     : " + dbURL);
			log(GXDBDebug.LOG_MIN, "                       User    : " + user);
		}

		// Si es el jdbc-odbc bridge, me fijo cual existe de los dos y uso ese.
		bridge = (jdbcDriver.equals(SunJdbcOdbcDriver) || jdbcDriver.equals(MSJdbcOdbcDriver));

		if	(bridge)
		{
			if (PrivateUtilities.isClassPresent(MSJdbcOdbcDriver))
				registerDriver (MSJdbcOdbcDriver);
			else
				if	(PrivateUtilities.isClassPresent(SunJdbcOdbcDriver))
					registerDriver (SunJdbcOdbcDriver);
		}
		else
		{
			  registerDriver (jdbcDriver);
		}

		prop.put ("user"    , user);
		prop.put ("password", password);

		if	(dataSource != null)
		{
			dataSource.dbms.setConnectionProperties(prop);
		}

		if	(dataSource != null && dataSource.dbms instanceof GXDBMSas400 && com.genexus.ApplicationContext.getInstance().getReorganization())
		{
			_dbURL =  _dbURL + "/" + dataSource.jdbcDBName;
		}

		NativeFunctions.getInstance().executeWithPermissions(
				new Runnable() {
					public void run()
					{
						if (PrivateUtilities.isClassPresent(externalConnectionManager))
						{
							if	(DEBUG && isLogEnabled())
							{
								log(GXDBDebug.LOG_MIN, "Trying to connect using external connection manager to : Driver  : " + _jdbcDriver);
							}			
							try
							{
								Class<?> c = Class.forName(externalConnectionManager);
								Method m = c.getMethod("getConnection", new Class[]{String.class, String.class, String.class, String.class, Properties.class});
								con = (Connection) m.invoke(null, new Object[]{_jdbcDriver, _dbURL, _user, _password, prop});
							}
							catch(Exception e)
							{
								logger.fatal("Could not connect to JDBCDriver", e);								
							}				
						}
						else
						{
							try
							{
								// Esto es por el bug que reportaron los de Inet.
								if	(DriverManager.getLoginTimeout() < 0)
									DriverManager.setLoginTimeout(0);
								if	(bridge)
									con = DriverManager.getConnection (_dbURL, _user, _password);
								else
								{
									con = DriverManager.getConnection (_dbURL, prop);
								}

							}
							catch (SQLException e)
							{
								sqlE = e;
							}
						}
					}
				}, INativeFunctions.CONNECT);

		if	(sqlE != null)
			throw sqlE;
	}

	@Override
	public boolean equals(Object o)
	{
		if(o instanceof GXConnection && con != null && o != null)
		{ // Dos GXConnections son iguales si sus conexiones son iguales
			return con.equals(((GXConnection)o).con);
		}
		else return super.equals(o);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	private final static ConcurrentHashMap<String, javax.sql.DataSource> JDBCDataSourceMappings = new ConcurrentHashMap<String, javax.sql.DataSource>();
		
	private void connectJDBCDataSource(String dataSourceName) throws SQLException
	{
		if	(DEBUG && isLogEnabled())
		{
			log(GXDBDebug.LOG_MIN, "Trying to connect to : DataSource : " + dataSourceName);
		}

		try
		{

			if (context.getSessionContext() != null) //estoy en el contexto de un EJB
			{
				try
				{
					javax.naming.Context initCtx = new javax.naming.InitialContext();
					String trnType = (String) initCtx.lookup("java:comp/env/GX/TrnType");
					if (!trnType.equals("CONTAINER")) // Si la TRN en el EJB no es manejada por el contenedor
						GXJTA.initTX(getLog(), dataSource.jdbcIntegrity);
				}
				catch (javax.naming.NamingException e)
				{
					throw new RuntimeException(e.getMessage());
				}
			}
			else
			{
				if (dataSource.usesJdbcDataSource() && GXJTA.isJTATX(handle, context))
					GXJTA.initTX(getLog(), dataSource.jdbcIntegrity);
			}

			javax.sql.DataSource ds = (javax.sql.DataSource)JDBCDataSourceMappings.get(dataSourceName);
			if(ds != null)
			{
				try
				{
					if (!setDataSourceOracleFixedString(ds))
					{
						con = ds.getConnection();
					}
					return;
				}catch(SQLException sqle)
				{ // Si hay alguna excepcion al pedir una conexi�n del datasource que teniamos
				  // en el mapping, lo elimino y obtengo de nuevo.
//					log(GXDBDebug.LOG_MIN, "Couldn't get connection: " + sqle.getMessage());
					log(GXDBDebug.LOG_MIN, "Looking up datasource: " + dataSourceName);
					JDBCDataSourceMappings.remove(dataSourceName);
				}
			}

			javax.naming.Context ic = new javax.naming.InitialContext();
			ds = (javax.sql.DataSource) ic.lookup(dataSourceName);
			JDBCDataSourceMappings.put(dataSourceName, ds);

			if (!setDataSourceOracleFixedString(ds))
			{				
				con = ds.getConnection();
			}
		}
		catch (javax.naming.NamingException e)
		{
			throw new RuntimeException(e.getMessage());
		}
	}
	
	private boolean setDataSourceOracleFixedString(javax.sql.DataSource ds) throws SQLException
	{
			if (dataSource.dbms instanceof GXDBMSoracle7) {
				try {
					Class<?> oracleDataStourceClass = Class.forName("oracle.jdbc.pool.OracleDataSource");
					if (ds.isWrapperFor(oracleDataStourceClass)) {
						Object oracle_datasource = ds.unwrap(oracleDataStourceClass);
						Properties dataSourceProps = new Properties();
						dataSource.dbms.setConnectionProperties(dataSourceProps);
						Method setConnectionPropertiesMth = oracle_datasource.getClass().getMethod("setConnectionProperties", new Class[]{Properties.class});
						setConnectionPropertiesMth.invoke(oracle_datasource, new Object[]{dataSourceProps});
						javax.sql.DataSource dataSource = (javax.sql.DataSource) oracle_datasource;
						con = dataSource.getConnection();
						return true;
					}
				} catch (ClassNotFoundException cex) {
					if (isLogEnabled()) log(handle, "setDataSourceOracleFixedString class oracle.jdbc.pool.OracleDataSource does not exist on classpath");
				} catch (Exception ex) {
					log(GXDBDebug.LOG_MIN, "Error setting oracle FixedString");
					if (isLogEnabled()) logSQLException(handle, ex);
				}
			}
			return false;		
	}

	public GXConnection()
	{
	}

	public static void check(String jdbcDriver, String jdbcURL, String jdbcUser, String jdbcPassword, String DBMS)
	{
		try
		{
			GXConnection c = new GXConnection();
			JDBCLogConfig jdbcLogConfig = new JDBCLogConfig("check", false, false, 0, false, "", 0);
			c.setLog(new GXDBDebug(jdbcLogConfig));
			c.dataSource = new DataSource("check", jdbcDriver, jdbcURL, jdbcUser, jdbcPassword, jdbcLogConfig, false, false, "", "", 1, 0, false, DBMS, "", false, "", "", 0, 0, false, 0, false, false, 0, false, 0, false, false, false, false, false, 0, false, 0, 1);

			c.connect(jdbcDriver.trim(), jdbcURL.trim(), jdbcUser.trim(), jdbcPassword.trim());
			c.con.close();

			for (Enumeration en = DriverManager.getDrivers(); en.hasMoreElements(); )
				DriverManager.deregisterDriver((Driver) en.nextElement());
			c.close();

			CommonUtil.msg(new java.awt.Frame(), "Sucessfully connected!");
		}
		catch (SQLException e)
		{
			CommonUtil.msg(new java.awt.Frame(), "Connection failed! " + e.getMessage());
		}
	}

	GXDBMS getDBMS()
	{
		return dataSource.dbms;
	}

	public long getAssignTime()
	{
		return assignTime;
	}

	boolean setPreviousHandle = false;
	
	private Object lockObject = new Object();
	void setHandle(int handle)
	{
		synchronized (lockObject) {
		if (setPreviousHandle)
		{
			this.previousHandle = this.handle;
			setPreviousHandle = false;
		}
		this.handle = handle;
		this.thread = Thread.currentThread().hashCode();
		this.assignTime = System.currentTimeMillis();
		((UserInformation)DBConnectionManager.getInstance().getUserInformation(handle)).setLastConnectionUsed(getId());
	}

	}
	public int getHandle()
	{
		synchronized (lockObject) {
		return handle;
	}
	}
	public int getPreviousHandle()
	{		
		setPreviousHandle = true;		
		return previousHandle;
	}	

	public int getThread()
	{
		return this.thread;
	}

	final java.util.Date getNullDate()
	{
		return dataSource.dbms.nullDate();
	}

	void setPool(ConnectionPool pool)
	{
		this.pool = pool;
	}

	public void setLocked(boolean locked)
	{
		this.locked = locked;
		timeLock = System.currentTimeMillis();
	}

	public boolean getLocked()
	{
		// true si locked y paso mas de 30 segundos.
		return locked && timeLock != 0 && timeLock < System.currentTimeMillis() - 30000;
	}

    public Statement createStatement(int a, int b) throws SQLException
	{
		return createStatement(handle);
	}

    public Statement createStatement() throws SQLException
	{
		return createStatement(handle);
	}

    public Statement createStatement(int handle) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MIN, "createStatement");
			try
			{
				GXStatement stmt = new GXStatement(con.createStatement(), this, handle);

				   // El JDBC Connect de Merant para SQLServer no lo soporta.. lo ignoramos.
				try
				{
					if	(getDBMS().getSupportsQueryTimeout())
					{
						stmt.setQueryTimeout(0);
					}
				}
				catch (SQLException e)
				{
				}

				return stmt;
			}
			catch (SQLException sqlException)
			{
				if	(isLogEnabled()) logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{
			GXStatement stmt = new GXStatement(con.createStatement(), this, handle);

			// El JDBC Connect de Merant para SQLServer no lo soporta.. lo ignoramos.
			try
			{
				if	(getDBMS().getSupportsQueryTimeout())
				{
					stmt.setQueryTimeout(0);
				}
			}
			catch (SQLException e)
			{
			}

			return stmt;
		}
	}
    
        public synchronized void rePrepareStatement(Cursor cursor) throws SQLException
        {
            try
            {
                log(GXDBDebug.LOG_MIN, "re preparing statement " + cursor.getSQLSentence());
                preparedStatementPool.dropCursor((GXPreparedStatement) cursor.getStatement());
                GXPreparedStatement stmt = (GXPreparedStatement) this.getStatement(cursor.getCursorId(), cursor.getSQLSentence(), cursor.isCurrentOf(), handle);
                preparedStatementPool.setNotInUse(stmt);
            }
            finally
            {
                  state.setInAssignment(false);
            }
        }
        
        public synchronized PreparedStatement getStatement(String index, String sqlSentence, boolean currentOf, int handle) throws SQLException
		{
			return getStatement(index, sqlSentence, currentOf, handle, false);
		}
            
	public synchronized PreparedStatement getStatement(String index, String sqlSentence, boolean currentOf, int handle, boolean batch) throws SQLException
	{
          PreparedStatement stmt = null;
          try
          {
            stmt = preparedStatementPool.getStatement(handle,
                index, sqlSentence, currentOf, false, batch);
          }
          finally
          {
              state.setInAssignment(false);
          }
		return stmt;
	}

	public PreparedStatement getStatement(String index, String sqlSentence, boolean currentOf) throws SQLException
	{
		return getStatement(index, sqlSentence, currentOf, handle, false);
	}
        public PreparedStatement getStatement(String index, String sqlSentence, boolean currentOf, boolean batch) throws SQLException
        {
                return getStatement(index, sqlSentence, currentOf, handle, batch);
	}

	public synchronized CallableStatement getCallableStatement(String index, String sqlSentence, int handle) throws SQLException
	{
		CallableStatement stmt;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MIN, "getCallableStatement " + sqlSentence);
			try
			{
				stmt = preparedStatementPool.getCallableStatement(handle, index, sqlSentence);
			}
			catch (SQLException sqlException)
			{
				if	(isLogEnabled()) logSQLException(handle, sqlException);
				throw sqlException;
			}
			finally
			{
              state.setInAssignment(false);
			}
		}
		else
		{
			try
			{
				stmt = preparedStatementPool.getCallableStatement(handle, index, sqlSentence);
			}
			finally
			{
              state.setInAssignment(false);
			}
		}
		return stmt;
	}

	public synchronized CallableStatement getCallableStatement(String index, String sqlSentence) throws SQLException
	{
		return getCallableStatement(index, sqlSentence, handle);
	}

    public PreparedStatement prepareStatement(String sql, int a, int b) throws SQLException
	{
		return prepareStatement(sql, handle, "", false);
	}

    public PreparedStatement prepareStatement(String sql) throws SQLException
	{
		return prepareStatement(sql, handle, "", false);
	}

    public PreparedStatement prepareStatement(String sql, int handle, String cursorId) throws SQLException
    {
    		return prepareStatement(sql, handle, cursorId, false);
    }

    public PreparedStatement prepareStatement(String sql, int handle, String cursorId, boolean currentOf) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MIN, "prepareStatement - sql = " + sql);
			try
			{
				GXPreparedStatement stmt = new GXPreparedStatement(con.prepareStatement(sql), this, handle, sql, cursorId, currentOf);
				log(GXDBDebug.LOG_MIN, "prepareStatement - id  = " + GXDBDebug.getJDBCObjectId(stmt) );
				return stmt;
			}
			catch (SQLException sqlException)
			{
				if	(isLogEnabled()) logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{
			return new GXPreparedStatement(con.prepareStatement(sql), this, handle, sql, cursorId, currentOf);
		}
	}

    public void setAutoCommit(boolean autoCommit) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MIN, "setAutoCommit - autoCommit = " + autoCommit);
			try
			{
				con.setAutoCommit(autoCommit);
			}
			catch (SQLException sqlException)
			{
				if	(isLogEnabled()) logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{
			con.setAutoCommit(autoCommit);
		}
	}
	
	private boolean useOldDateTimeFuntion = true;
	
	public boolean getUseOldDateTimeFuntion()
	{
		return useOldDateTimeFuntion;
	}
	
	public void setDontUseNewDateTimeFunction()
	{
		useOldDateTimeFuntion = false;
	}

	public java.util.Date getDateTime() throws SQLException
	{
		return dataSource.dbms.serverDateTime(this);
	}
	
	public String getDBMSVersion() throws SQLException
	{
		return dataSource.dbms.serverVersion(this);
	}
	
	public String getDatabaseName()
	{
		return dataSource.dbms.getDatabaseName();
	}		

	public String getUserName() throws SQLException
	{
		String userName;

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "getUserName");

			try
			{
				userName = getMetaData().getUserName();
			}
			catch (SQLException sqlException)
			{
				if	(isLogEnabled()) logSQLException(handle, sqlException);
				throw sqlException;
			}
			log(GXDBDebug.LOG_MAX, "getUserName returned " + userName);
		}
		else
		{
			userName = getMetaData().getUserName();
		}

		if	(userName == null)
		{
			userName = connectedUser;
			if	(DEBUG)
			{
				log(GXDBDebug.LOG_MIN, "getUserName - using connected user");
			}
		}
		
		if (getDBMS().getId() == GXDBMS.DBMS_MYSQL)
		{
			int i = userName.indexOf("@");
			if (i != -1)
				userName = userName.substring(0, i);
		}		

		return userName;
	}

private void rollback_impl() throws SQLException
{
  if(dataSource.usesJdbcDataSource() && GXJTA.isJTATX(handle, context))
    GXJTA.rollback();
  else
    dataSource.dbms.rollback(con);
}

public void rollback() throws SQLException
	{
		batchUpdateStmts.clear();
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MIN, "rollback");
			try
			{
				rollback_impl();
			}
			catch (SQLException sqlException)
			{
				if	(isLogEnabled()) logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{
			rollback_impl();
		}

		setCommitedChanges();
		state.setInAssignment(false);
	}

	private void commit_impl() throws SQLException
	{
		if(dataSource.usesJdbcDataSource() && GXJTA.isJTATX(handle, context))
		  GXJTA.commit();
		else
		  dataSource.dbms.commit(con);
	}
	public void flushBatchCursors() throws SQLException{
		for (int i = 0; i < batchUpdateStmts.size(); i++) {
			BatchUpdateCursor cursor = (BatchUpdateCursor) batchUpdateStmts.get(i);
			if (cursor.pendingRecords()) {
				cursor.beforeCommitEvent();
			}
		}
		batchUpdateStmts.clear();
	}

    public void commit() throws SQLException
	{
		flushBatchCursors();

		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MIN, "commit");
			try
			{
			  commit_impl();
			}
			catch (SQLException sqlException)
			{
				if	(isLogEnabled()) logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{
			commit_impl();
		}

		setCommitedChanges();
		state.setInAssignment(false);
	}

    private void close_impl() throws SQLException
    {
      if(dataSource.usesJdbcDataSource() && GXJTA.isJTATX(handle, context))
	  {
		  dropAllCursors();
		  GXJTA.cleanTX(dataSource.jdbcIntegrity);
		  con.close();
	  }
	  else
	  {
          try
          {
			dropAllCursors();
            if (! (dataSource.dbms instanceof GXDBMSinformix && dataSource.getInformixDB().equals(DataSource.INFORMIX_DB_NOTLOGGED)))
			{
				if (getUncommitedChanges() || dataSource.dbms instanceof GXDBMSmysql)
				{
					rollback();
				}
			}
			con.setAutoCommit(true);
          }
          finally
          {
              con.close();
          }
	  }
    }

    public void close() throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MIN, "close");
			try
			{
				//Destroy JMX
				if (Application.isJMXEnabled())
					ConnectionJMX.DestroyConnectionJMX(this);
				close_impl();
			}
			catch (SQLException sqlException)
			{
				if	(isLogEnabled()) logSQLException(handle, sqlException);
				throw sqlException;
			}

			flushLog();
			getLog().close(JDBCLogConfig.LEVEL_CONNECTION);
		}
		else
		{
			//Destroy JMX
			if (Application.isJMXEnabled())
				ConnectionJMX.DestroyConnectionJMX(this);

			// We don't rollback, because some DB don't support the command (Not-Logged Informix), and
			// it's supposed to be done implicitly.
			close_impl();
		}

		handle = -1;
	}

    public void closeWithError() throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MIN, "Closing broken connection");

			try
			{
				//Destroy JMX
				if (Application.isJMXEnabled())
					ConnectionJMX.DestroyConnectionJMX(this);
				con.close();
			}
			catch (SQLException sqlException)
			{
				if	(isLogEnabled()) logSQLException(handle, sqlException);
				throw sqlException;
			}

			flushLog();
		}
		else
		{
			//Destroy JMX
			if (Application.isJMXEnabled())
				ConnectionJMX.DestroyConnectionJMX(this);
			con.close();
		}

		handle = -1;
	}

	public void dropAllCursors()
	{
		preparedStatementPool.dropAllCursors();
		state.closeCursor();
	}

    public boolean isClosed() throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MAX, "isClosed");
			try
			{
				return con.isClosed();
			}
			catch (SQLException sqlException)
			{
				if	(isLogEnabled()) logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{
			return con.isClosed();
		}
	}

    public void setReadOnly(boolean readOnly) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MIN, "setReadOnly - readOnly = " + readOnly);
			try
			{
				con.setReadOnly(readOnly);
			}
			catch (SQLException sqlException)
			{
				if	(isLogEnabled()) logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{
			con.setReadOnly(readOnly);
		}
	}

    public void setTransactionIsolation(int level) throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MIN, "setTransactionIsolation - level " + level);
			try
			{
				con.setTransactionIsolation(level);
			}
			catch (SQLException sqlException)
			{
				if	(isLogEnabled()) logSQLException(handle, sqlException);
				throw sqlException;
			}
		}
		else
		{
			con.setTransactionIsolation(level);
		}
	}

    public SQLWarning getWarnings() throws SQLException
	{
		return con.getWarnings();
	}

    public void clearWarnings() throws SQLException
	{
		con.clearWarnings();
	}


	private boolean registerDriver(String className)
	{
		boolean lreturn = false;
		  try
		  {
		  	Class.forName(className);
			  lreturn = true;
		  }
		  catch (ClassNotFoundException e)
		  {
			  log(GXDBDebug.LOG_MIN, "registerDriver/ClassNotFoundException : " + className);
		  }

		return lreturn;
	}

	private boolean deregisterDriver(String className)
	{
		boolean lreturn = false;
		  try
		  {
			  DriverManager.deregisterDriver ((java.sql.Driver) Class.forName(className).newInstance());
			  lreturn = true;
		  }
		  catch (IllegalAccessException e)
		{
			  System.err.println("registerDriver/IllegalAccessException : " + className);
		}
		  catch (InstantiationException e)
		{
			  System.err.println("registerDriver/InstantiationException : " + className);
		}
		  catch (ClassNotFoundException e)
		  {
			  System.err.println("registerDriver/ClassNotFoundException : " + className);
		  }
		catch (SQLException es)
		{
			System.err.println("registerDriver/SQLException : " + es.getMessage() + " - SQLState " + es.getSQLState() + " - Error " + es.getErrorCode());
		  }

		return lreturn;
	}

	public String nativeSQL(String sql) throws SQLException
	{
		return con.nativeSQL(sql);
	}

	public int getTransactionIsolation() throws SQLException
	{
		return con.getTransactionIsolation();
	}

	public boolean isReadOnly() throws SQLException
	{
		return con.isReadOnly();
	}

	public CallableStatement prepareCall(String sql, int a, int b) throws SQLException
	{
		return prepareCall(sql);
	}

	public GXCallableStatement prepareCall(String sql, int handle) throws SQLException
	{
		return new GXCallableStatement(con.prepareCall(sql), this, handle, sql);
	}

	public CallableStatement prepareCall(String sql) throws SQLException
	{
		return prepareCall(sql, handle);
	}

	public DatabaseMetaData getMetaData() throws SQLException
	{
		return con.getMetaData();
	}

	public boolean getAutoCommit() throws SQLException
	{
		return con.getAutoCommit();
	}

	public void setCatalog(String catalog) throws SQLException
	{
		con.setCatalog(catalog);
	}

	public String getCatalog() throws SQLException
	{
		return con.getCatalog();
	}

	public ConnectionPoolState getPoolState()
	{
		return state;
	}

	ConnectionPool getPool()
	{
		return pool;
	}

	public boolean getUncommitedChanges()
	{
		return state.getUncommitedChanges();
	}

	void setCommitedChanges()
	{
		state.setUncommitedChanges(false);
	}

	public void setUncommitedChanges()
	{
		state.setUncommitedChanges(true);
	}

	void setNotInUse(GXPreparedStatement stmt)
	{
		preparedStatementPool.setNotInUse(stmt);
		state.closeCursor();
	}

	public void dropCursor(IGXPreparedStatement stmt)
	{
		try
		{
			preparedStatementPool.dropCursor((GXPreparedStatement) stmt);
			state.closeCursor();
		}
		finally
		{
			state.setInAssignment(false);
		}
	}

	public int getOpenCursors()
	{
		return preparedStatementPool.getUsedCursors();
	}

	boolean isNullCleanDate(java.util.Date date)
	{
		return date.equals(nullDate);
	}
	boolean isNullDate(java.util.Date date)
	{
		return CommonUtil.resetTime(date).equals(nullDate);
	}

	boolean isNullDateTime(java.util.Date date)
	{
		return date.equals(nullDate);
	}

	public IPreparedStatementCache getPreparedStatementPool()
	{
		return preparedStatementPool;
	}

	public java.util.Map<String,Class<?>> getTypeMap()
	{
		return null;
	}

	public void setTypeMap(java.util.Map map)
	{
	}

	void dump(java.io.PrintStream out)
	{
		preparedStatementPool.dump(out);
	}

	void dump(com.genexus.xml.XMLWriter writer)
	{
		writer.writeStartElement("Connection_Information");
			writer.writeAttribute("Id", getId());
			writer.writeElement("PhysicalId", getDBMSId());
			writer.writeElement("CreateTime", getTimeCreated().toString());
			writer.writeElement("LastAssignedTime", getTimeAssigned().toString());
			writer.writeElement("LastUserAssigned", getLastUserAssigned());
			writer.writeElement("LastUserAssignedName", getUserId());
			writer.writeElement("Error", new Boolean(getError()).toString());
			writer.writeElement("Available", new Boolean(!getInAssigment() && getOpenCursorsJMX()==0 && !getUncommitedChanges()).toString());
			writer.writeElement("OpenCursorCount", getOpenCursorsJMX());
			writer.writeElement("UncommitedChanges", new Boolean(getUncommitedChanges()).toString());
			writer.writeElement("RequestCount", getNumberRequest());
			writer.writeStartElement("LastSQLStatement");
				writer.writeCData(getSentenceLastRequest());
			writer.writeEndElement();
			writer.writeElement("LastSQLStatementTime", getTimeLastRequest().toString());
			writer.writeElement("LastSQLStatementEnded", new Boolean(getFinishExecute()).toString());
			writer.writeElement("LastObject", getLastObjectExecuted());
		writer.writeEndElement();
	}

	private String getUserId()
	{
		try
		{
			return DBConnectionManager.getInstance().getUserName(context, getLastUserAssigned(), "DEFAULT");
		}
		catch (SQLException ex)
		{
			return "";
		}
	}

	public GXDBDebug getLog()
	{
		return log;
	}

	void setLog(GXDBDebug log)
	{
		this.log = log;
	}

	void log(int level, String text)
	{
		if	(DEBUG)
		{
			getLog().log(level, this, handle, text);
		}
	}

	void log(int level, Object obj, String text)
	{
		if	(DEBUG)
		{
			getLog().log(level, obj, handle, text);
		}
	}

	void flushLog()
	{
		if	(DEBUG)
		{
			getLog().flushLog();
		}
	}

	boolean isLogEnabled()
	{
		if	(DEBUG)
		{
			if	(getLog() != null)
				return getLog().isLogEnabled();
		}

		return false;
	}

	void logSQLException(int handle, Exception e)
	{
		if	(DEBUG)
		{
			getLog().logException(handle, e, this);
		}		
	}

///////////////////////////////////////////////////////////////////////////////////////	
	 // Metodos agregados en el JDK1.4

	public void setHoldability(int x) throws SQLException
	{
		con.setHoldability(x);
	}
	public int getHoldability() throws SQLException
	{
		return con.getHoldability();
	}
	public Savepoint setSavepoint() throws SQLException
	{
		return con.setSavepoint();
	}
	public Savepoint setSavepoint(String x) throws SQLException
	{
		return con.setSavepoint(x);
	}
	public void rollback(Savepoint x) throws SQLException
	{
		con.rollback(x);
	}
	public void releaseSavepoint(Savepoint x) throws SQLException
	{
		con.releaseSavepoint(x);
	}
	public Statement createStatement(int a, int b, int c) throws SQLException
	{
		return con.createStatement(a, b, c);
	}
	public PreparedStatement prepareStatement(String s, int a, int b, int c) throws SQLException
	{
		return con.prepareStatement(s, a, b, c);
	}
	
	public CallableStatement prepareCall(String s, int a, int b, int c) throws SQLException
	{
		return con.prepareCall(s, a, b, c);
	}
	public PreparedStatement prepareStatement(String s, int []c) throws SQLException
	{
		return con.prepareStatement(s, c);
	}
	public PreparedStatement prepareStatement(String s, String []c) throws SQLException
	{
		return con.prepareStatement(s, c);
	}
	public PreparedStatement prepareStatement(String s, int i) throws SQLException
	{
		return con.prepareStatement(s, i);
	}
///////////////////////////////////////////////////////////////////////////////////////	

	public void incNumberRequest()
	{
		numberRequest ++;
		timeLastRequest = new java.util.Date();
	}

	public void setSentenceLastRequest(String sentenceLastRequest)
	{
		this.sentenceLastRequest = sentenceLastRequest;
	}

	public void setLastObjectExecuted(String object)
	{
		lastObjectExecuted = object;
	}

	public void setFinishExecute( boolean finishExecute)
	{
		this.finishExecute = finishExecute;
	}

////////////////////////////////JMX operations/////////////////////////////////////
	String dbmsId;
	public String getDBMSId()
	{
		if (dbmsId == null)
		{
			dbmsId = dataSource.dbms.connectionPhysicalId(this);
		}
		return dbmsId;
	}

	public java.util.Date getTimeCreated()
	{
		return new java.util.Date(connectedTime);
	}

	public java.util.Date getTimeAssigned()
	{
		return new java.util.Date(assignTime);
	}

	public int getLastUserAssigned()
	{
		return handle;
	}

	public boolean getInAssigment()
	{
		return getPoolState().getInAssignment();
	}

	public int getNumberRequest()
	{
		return numberRequest;
	}

	public java.util.Date getTimeLastRequest()
	{
		return timeLastRequest;
	}

	public int getOpenCursorsJMX()
	{
		return preparedStatementPool.getUsedCursorsJMX();
	}

	public String getSentenceLastRequest()
	{
		return sentenceLastRequest;
	}

	public String getLastObjectExecuted()
	{
		return lastObjectExecuted;
	}

	public boolean getFinishExecute()
	{
		return 	finishExecute;
	}

	public void disconnect()
	{
		try
		{
			getPool().dropConnectionById(getId());
		}
		catch(SQLException e)
		{
			logger.error("Failed to drop Connection ", e);
		}

	}

	public void dump()
	{
		String fileName = "Connection_" + getId() + "_" +  CommonUtil.getYYYYMMDDHHMMSS_nosep(new java.util.Date()) + ".xml";
		com.genexus.xml.XMLWriter writer = new com.genexus.xml.XMLWriter();
		writer.xmlStart(fileName);
		dump(writer);
		writer.close();
	}
    public void addBatchUpdate(Cursor cursor) {

        if (!batchUpdateStmts.contains(cursor)){
            batchUpdateStmts.add(cursor);
        }
    }
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public <T> T unwrap(Class<T> arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Blob createBlob() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Clob createClob() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public NClob createNClob() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public SQLXML createSQLXML() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Struct createStruct(String arg0, Object[] arg1) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Properties getClientInfo() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getClientInfo(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isValid(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public void setClientInfo(Properties arg0) throws SQLClientInfoException {
		// TODO Auto-generated method stub
		
	}

	public void setClientInfo(String arg0, String arg1)
			throws SQLClientInfoException {
		// TODO Auto-generated method stub
		
	}

	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public void abort(Executor executor)
           throws SQLException {
		   
	}

	public void setSchema(String schema)
               throws SQLException {
			   
	}
	public String getSchema()
                 throws SQLException {
		return null;		 
	}
}
