package com.genexus.db.driver;

import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Executor;

import com.genexus.Application;
import com.genexus.CommonUtil;
import com.genexus.DebugFlag;
import com.genexus.ModelContext;
import com.genexus.common.classes.AbstractGXConnection;
import com.genexus.common.classes.IGXPreparedStatement;
import com.genexus.db.BatchUpdateCursor;
import com.genexus.db.Cursor;
import com.genexus.db.DBConnectionManager;
import com.genexus.db.UserInformation;

public final class GXConnection extends AbstractGXConnection implements Connection
{
	private static final boolean DEBUG       = DebugFlag.DEBUG;

	private Connection con;
	private int handle = -1;
	private int previousHandle = -1;
	private int thread;
	private java.util.Date nullDate;

	private String  connectedUser;

	private DataSource dataSource;
	private PreparedStatementCache preparedStatementPool;
        private ArrayList batchUpdateStmts = new ArrayList();

	private GXDBDebug log;

	private long connectedTime ;
	private boolean error;

    private ModelContext context;

	private boolean doCommit;

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

	GXConnection( ModelContext context, int handle, String user, String password, DataSource dataSource) throws SQLException
	{
		this.thread = Thread.currentThread().hashCode();
		this.handle     = handle;
		this.dataSource = dataSource;

                this.context = context;

		if 	(System.getProperty("gx.jdbclog") != null)
		{
			try
			{
				DriverManager.setLogStream(new java.io.PrintStream(new java.io.FileOutputStream("_gx_jdbc_driver_log.log")));
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


				DataSource dataSource1 = (DataSource) context.beforeGetConnection(handle, dataSource);
				if (dataSource1 != null)
				{
					dataSource = dataSource1;
				}
		}

			connect(dataSource.jdbcDriver, dataSource.jdbcUrl, user, password);

		if	(DEBUG && isLogEnabled())
		{
			getLog().logWarnings(con.getWarnings ());

			DatabaseMetaData dma = con.getMetaData ();

			log(GXDBDebug.LOG_MIN, "Connected to     : " + dma.getURL());
			log(GXDBDebug.LOG_MIN, "                   " + dataSource.jdbcUrl);
			log(GXDBDebug.LOG_MIN, "Connection class : " + con.getClass().getName());
			log(GXDBDebug.LOG_MIN, "Database         : " + dma.getDatabaseProductName() + " version " + dma.getDatabaseProductVersion() );
			log(GXDBDebug.LOG_MIN, "Driver           : " + dma.getDriverName());
			log(GXDBDebug.LOG_MIN, "Version          : " + dma.getDriverVersion());
			log(GXDBDebug.LOG_MIN, "GX DBMS          : " + dataSource.dbms);
			log(GXDBDebug.LOG_MIN, "DataStore        : " + dataSource.name);
		}

		doCommit=true;


			if (com.genexus.ApplicationContext.getInstance().getReorganization())
				setAutoCommit(dataSource.dbms.getSupportsAutocommit());
			else
			{
				//En el caso de una aplicacion en dos capas hay que llamar al After Connect aca
				if(context != null)
				{
					context.afterGetConnection(handle, dataSource);
				}

				if (doCommit)
					setAutoCommit(!dataSource.jdbcIntegrity);
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

		// This must be executed after setting autocommit and transaction isolation values,
		// because for Informix it does a 'Begin Work'

		dataSource.dbms.onConnection(this);

	}

        public Connection getJDBCConnection()
        {
          return con;
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
	}

	public boolean getError()
	{
		return error;
	}

	private int GXToJDBCIsolationLevel(int gxIL)
	{																											 
			switch (gxIL)
			{
				case 1:
					return con.TRANSACTION_READ_COMMITTED;
				case 0:
					return con.TRANSACTION_READ_UNCOMMITTED;
				default:
					System.err.println("Invalid isolation level " + gxIL);
					return con.TRANSACTION_NONE;
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
				connectJDBCDriver(jdbcDriver, dbURL, user, password);
				if	(dataSource != null)
				{
						preparedStatementPool = new PreparedStatementCache(dataSource.maxCursors, this);
				}
		}
		catch (SQLException sqlException)
		{
			if	(!dataSource.dbms.ignoreConnectionError(sqlException))
			{
				if (Application.getShowConnectError())
				{
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
	
	private void connectJDBCDriver(String jdbcDriver, String dbURL, String user, String password) throws SQLException
	{
		_jdbcDriver = jdbcDriver;
		_dbURL    = dbURL;
		_user     = user;
		_password = password;
		prop = new Properties();

		if	(DEBUG && isLogEnabled())
		{
			log(GXDBDebug.LOG_MIN, "Trying to connect to : Driver  : " + jdbcDriver);
			log(GXDBDebug.LOG_MIN, "                       URL     : " + dbURL);
			log(GXDBDebug.LOG_MIN, "                       User    : " + user);
		}

		registerDriver (jdbcDriver);

		prop.put ("user"    , user);
		prop.put ("password", password);

		if	(dataSource != null)
		{
			dataSource.dbms.setConnectionProperties(prop);
		}

							try
							{
									con = DriverManager.getConnection (_dbURL, prop);
							}
							catch (SQLException e)
							{
								sqlE = e;
							}

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
		if (con != null)
			return con.hashCode();
		return super.hashCode();
	}

	
	public GXConnection()
	{
	}

	GXDBMSsqlite getDBMS()
	{
		return dataSource.dbms;
	}

	public long getAssignTime()
	{
		return assignTime;
	}

	boolean setPreviousHandle = false;
	
	void setHandle(int handle)
	{
		if (setPreviousHandle)
		{
			this.previousHandle = this.handle;
			setPreviousHandle = false;
		}
		this.handle = handle;
		this.assignTime = System.currentTimeMillis();
		((UserInformation)DBConnectionManager.getInstance().getUserInformation(handle)).setLastConnectionUsed(getId());
	}

	public int getHandle()
	{
		return handle;
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
                log(GXDBDebug.LOG_MIN, "re preparing statement " + cursor.getSQLSentence());
                preparedStatementPool.dropCursor((GXPreparedStatement) cursor.getStatement());
                GXPreparedStatement stmt = (GXPreparedStatement) this.getStatement(cursor.getCursorId(), cursor.getSQLSentence(), cursor.isCurrentOf(), handle);
                preparedStatementPool.setNotInUse(stmt);
        }
        
        public synchronized PreparedStatement getStatement(String index, String sqlSentence, boolean currentOf, int handle) throws SQLException
		{
			return getStatement(index, sqlSentence, currentOf, handle, false);
		}
            
	public synchronized PreparedStatement getStatement(String index, String sqlSentence, boolean currentOf, int handle, boolean batch) throws SQLException
	{
          PreparedStatement stmt = null;

            stmt = preparedStatementPool.getStatement(handle,
                index, sqlSentence, currentOf, false, batch);
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
		}
		else
		{
				stmt = preparedStatementPool.getCallableStatement(handle, index, sqlSentence);
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

		return userName;
	}

private void rollback_impl() throws SQLException
{
    dataSource.dbms.rollback(con);
}

public void rollback() throws SQLException
	{
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
	}

	private void commit_impl() throws SQLException
	{
		  dataSource.dbms.commit(con);
	}

	public void flushBatchCursors(java.lang.Object o) throws SQLException{
		Vector<Cursor> toRemove = new Vector();
		for (int i = 0; i < batchUpdateStmts.size(); i++) {
			BatchUpdateCursor cursor = (BatchUpdateCursor) batchUpdateStmts.get(i);
			if (cursor.pendingRecords()) {
				if (cursor.beforeCommitEvent(o))
					toRemove.add(cursor);
			}
		}
		if (toRemove.size()>0)
			batchUpdateStmts.removeAll(toRemove);
	}

    public void commit() throws SQLException
	{
		flushBatchCursors(null);

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
	}

    private void close_impl() throws SQLException
    {
          try
          {
			dropAllCursors();
					rollback();
			con.setAutoCommit(true);
          }
          finally
          {
              con.close();
          }
    }

    public void close() throws SQLException
	{
		if	(DEBUG)
		{
			log(GXDBDebug.LOG_MIN, "close");
			try
			{
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
			con.close();
		}

		handle = -1;
	}

	public void dropAllCursors()
	{
		preparedStatementPool.dropAllCursors();
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
			  DriverManager.registerDriver ((java.sql.Driver) Class.forName(className).newInstance());
			  lreturn = true;
		  }
		  catch (IllegalAccessException e)
		{
			  log(GXDBDebug.LOG_MIN, "registerDriver/IllegalAccessException : " + className);
		}
		  catch (InstantiationException e)
		{
			  log(GXDBDebug.LOG_MIN, "registerDriver/InstantiationException : " + className);
		}
		  catch (ClassNotFoundException e)
		  {
			  log(GXDBDebug.LOG_MIN, "registerDriver/ClassNotFoundException : " + className);
		  }
		catch (SQLException es)
		{
			log(GXDBDebug.LOG_MIN, "registerDriver/SQLException : " + es.getMessage() + " - SQLState " + es.getSQLState() + " - Error " + es.getErrorCode());
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

	void setNotInUse(GXPreparedStatement stmt)
	{
		preparedStatementPool.setNotInUse(stmt);
	}

	public void dropCursor(IGXPreparedStatement stmt)
	{
		preparedStatementPool.dropCursor((GXPreparedStatement) stmt);
	}

	public int getOpenCursors()
	{
		return preparedStatementPool.getUsedCursors();
	}

	boolean isNullDate(java.util.Date date)
	{
		return CommonUtil.resetTime(date).equals(nullDate);
	}

	boolean isNullDateTime(java.util.Date date)
	{
		return date.equals(nullDate);
	}

	public PreparedStatementCache getPreparedStatementPool()
	{
		return preparedStatementPool;
	}

	public java.util.Map getTypeMap()
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
    public void addBatchUpdate(Cursor cursor) {

        if (!batchUpdateStmts.contains(cursor)){
            batchUpdateStmts.add(cursor);
        }
	}

	/*** New methods in Java's 1.6 Connection interface. These are stubs. ***/
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {return null;}
    public java.sql.Array createArrayOf(String typeName, Object[] elements) throws SQLException {return null;}
    public Properties getClientInfo() throws SQLException {return null;}
    public String getClientInfo(String name) throws SQLException {return null;}
    public void setClientInfo(Properties properties) throws SQLClientInfoException {}
    public void setClientInfo(String name, String value) throws SQLClientInfoException {}
    public boolean isValid(int timeout) throws SQLException {return true;}
	public SQLXML createSQLXML() throws SQLException {return null;}
	public NClob createNClob() throws SQLException {return null;}
	public Blob createBlob() throws SQLException {return null;}
    public Clob createClob() throws SQLException {return null;}
    public boolean isWrapperFor(Class<?> iface) throws SQLException {return true;}
    public <T> T unwrap(Class<T> iface) throws SQLException {return null;}
    /*** End of new methods. ***/
	
	/** New methods in Java 7 **/
	public void setSchema(String s) throws SQLException { }

	public String getSchema() throws SQLException {
		return null;
	}

	public void abort(Executor executor) throws SQLException { }

	public void setNetworkTimeout(Executor executor, int i) throws SQLException { }

	public int getNetworkTimeout() throws SQLException {
		return 0;
	}
	/** End of new methods **/
}
