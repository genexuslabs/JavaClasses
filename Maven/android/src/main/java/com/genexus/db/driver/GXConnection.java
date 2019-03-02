// $Log: GXConnection.java,v $
// Revision 1.53  2006/11/07 22:21:52  alevin
// - Implementacion de la lectura de Clobs en oracle. Funciona si no se esta con la VM de MS y
//   si en el config.gx no se tiene LongVarCharAsOracleLong=n.
//
// Revision 1.52  2006/04/28 21:35:37  iroqueta
// El isJTATX no estaba considerando el caso de una aplic en 3 capas.
//
// Revision 1.51  2006/04/27 12:23:52  iroqueta
// El setAutoCommit al crear la conexion no se hace si se esta trabajando con JTA.
// El cleanTX al cerrar la conexion se tiene que hacer siempre con JTA, no solo cuando hay UncommitedChanges sino no se cierra la transaccion.
//
// Revision 1.50  2006/04/19 20:16:24  iroqueta
// Arreglo un errorcito del put anterior
//
// Revision 1.49  2006/04/19 20:08:20  iroqueta
// - Cuando se usa JTA el beginTransaction hay que hacerlo antes de obtener la conexion del pool, sino puede que en algunos casos (por ejemplo usando drivers de Oracle) no queden grabados los datos luego del commit (SAC 19965)
// Por eso saco el GXJTA.initTX de la clase DirectConnectionPool y lo pongo aca antes de obtener la conexion del pool
//
// - Antes de devolver la conexion hago rollback solo en el caso de que hayan uncommitedChanges.
// Ademas siempre pongo el autocommit en true antes de devolver la conexion para que no haga el rollback el container.
//
// - Seteo el isolationLevel solo si el que tiene la conexion es distinto al que le quiero indicar.
// Esto es para evitar el error "SET TRANSACTION must be first statement of transaction"
//
// - JTA solo se usa en el caso que se tenga definido mas de un dataStore.
//
// Revision 1.48  2006/03/14 17:52:51  iroqueta
// En el close_impl se estaba llamando al rollback de la conexion en lugar de llamar al metodo rollback de esta clase que hace mas cosas.
//
// Revision 1.47  2006/01/12 15:16:31  alevin
// - Agrego variable context y metodo getContext(). Se necesita, por ejemplo en la
//   clase GXResultSet para soportar path relativos en blobs.
//
// Revision 1.46  2006/01/10 13:32:18  alevin
// - Si AvoidDataTruncationError esta en 1, lo pongo en true en el
//   GXPreparedStatement para ver si se trunca o no en el setString.
//
// Revision 1.45  2005/11/28 14:16:03  gusbro
// - Agrego dropCursor
//
// Revision 1.44  2005/11/01 13:46:38  iroqueta
// Hago que el afterconnect solo se ejecute si el contexo no es vacio.
// En el unico lugar que llega el contexto vacio es en el dbconnection.
//
// Revision 1.43  2005/10/18 12:27:41  iroqueta
// Implementacion del before connect para las aplics que usan el pool de GeneXus.
// Para que se pueda usar se implemento un array de pools que se indexan por el string de conexion + el usuario de la conexion.
//
// Revision 1.42  2005/10/11 13:44:08  iroqueta
// Hago que el after connect no se dispare si no hay contexto
//
// Revision 1.41  2005/10/06 17:58:59  iroqueta
// Implementacion de la propiedad AfterConnect.
// La idea es que se pueda indicar un proc al cual llamar luego de obtener una conexion a la base de datos.
// En el caso de aplicaciones web o en 3 capas si la conexion es la misma que se tenia antes no se ejeucta el proc... solo se ejecuta si la conexion es distinta a la que se obtuvo la ultima vez.
// Al proc se le indica el datastore al cual se conecto la aplicacion
//
// Revision 1.40  2005/09/28 17:38:04  alevin
// - Si LongVarCharAsOracleLong esta en 1, la seteo en true enf el GXResultSet para
//   ver si se hace getClob o getString para los LongVarChar
//
// Revision 1.39  2005/09/08 16:16:19  iroqueta
// Implementacion del dump en XML para JMX
//
// Revision 1.38  2005/08/22 20:05:05  gusbro
// - Cambios en el manejo de empty de VarChar y LongVarChar
//
// Revision 1.37  2005/08/17 15:43:50  iroqueta
// Hago que el evento before connect solo se llame si es una aplicacion en 2 capas win o si es una aplic web o 3 capas HTTP y se usa el pool del servidor.
//
// Revision 1.36  2005/08/16 19:10:42  iroqueta
// Se crean nuevas variables JMX para monitorear a nivel de conexion.
// ObjectLastRequest - Indica cual fue el ultimo objeto que uso la conexion
// FinishExecute - Indica si ya finalizo de ejecutar la ultima sentencia.
//
// Revision 1.35  2005/08/04 18:49:06  gusbro
// - El metodo check no estaba funcionando bien
//
// Revision 1.34  2005/08/04 14:41:45  iroqueta
// En JMX muestro en los datos del usuario la ultima conexion que uso o tiene asiganda.
//
// Revision 1.33  2005/08/03 19:25:35  iroqueta
// Hago que el getDBMSId sea de tipo String
//
// Revision 1.32  2005/07/22 23:03:58  iroqueta
// Agrego ifdefs para que no de problemas JMX en .NET
//
// Revision 1.31  2005/07/21 15:10:58  iroqueta
// Implementacion de soporte de JMX
//
// Revision 1.30  2005/07/13 22:49:49  gusbro
// - Arreglo al put anterior dado que en la reorg el ModelContext viene con null
//
// Revision 1.29  2005/07/12 15:32:55  iroqueta
// Arreglo el put anterior.
// Se tiene que hacer setAutoCommit en false para el caso de Oarcle.
// Por lo tanto hago que el setAutoCommit solo se haga en el caso que no estoy en el contexto de un EJB manejado por el contenedor.
//
// Revision 1.28  2005/06/03 14:52:20  iroqueta
// Hago solo setAutoCommit en true.. ya no lo hago en false porque no tiene sentido y eso traia problemas en los EJBs manejados por el contenedor que no deja hace setAutoCommit ni en true ni el false
//
// Revision 1.27  2005/05/25 15:48:02  iroqueta
// Pongo un try finally en el metodo getStatement para que no quede el state.setInAssignment(false) sin hacer si el preparedStatementPool.getStatement da una exception
//
// Revision 1.26  2005/05/25 15:31:52  iroqueta
// Se implementa el metodo freeAllCursors para que sea llamado al hacer el disconnect de una conexion en el pool y asegurarnos que todos los contadores que indican si la conexion esta libre queden bien (Programacion defensiva)
//
// Revision 1.25  2005/05/23 18:55:25  iroqueta
// Siempre se hace un disconnect del DBConnection cuando se retorna de la llamada del proc en el evento before connect.
// Esto es por si el usuario se conecta dentro del procedimiento.
//
// Revision 1.24  2005/05/10 13:12:04  iroqueta
// Cambio en la implementacion del metodo before connect para que solo se tenga un parametro de tipo DBConnection.
//
// Revision 1.23  2005/05/04 21:47:48  iroqueta
// Implementacion del evento before commit
//
// Revision 1.22  2005/04/07 15:47:20  iroqueta
// Agrego metodo getDataSource para poder ser usado desde GXPreparedStatement para no tomar en cuenta los blancos en las comparaciones con Oracle y el driver de Oracle
//
// Revision 1.21  2005/03/28 22:44:32  gusbro
// - En el prepareStatement sin debug no se estaba pasando el currentOf
//
// Revision 1.20  2005/02/18 21:19:27  iroqueta
// Le hago llegar el ModelContext al GXConnection para poder pasarselo al proc que se llama para obtener el nombre del datasource al cual conectarse.
//
// Revision 1.19  2004/11/18 18:50:56  iroqueta
// Se agregan controles para no dejar cosas sin cerrar.
//
// Revision 1.18  2004/11/08 18:44:54  iroqueta
// Arreglo para que en el close se cierre siempre la conexion aunque el rollback de error por alguna razon.
//
// Revision 1.17  2004/09/28 11:45:40  iroqueta
// Arreglo para que en el pool de sentencias preparadas en caso de Iseries y sentencias for update no se haga el pool por la sentencia, sino por el nro de cursor.
//
// Revision 1.16  2004/08/26 17:45:01  iroqueta
// Se controla de no trabajar con transacciones si se tiene la preference de integridad transaccional en No y se usa JTA
//
// Revision 1.15  2004/08/06 16:11:48  iroqueta
// Cambio para soportar el cambiar la base de datos en tiempo de ejecucion
//
// Revision 1.14  2004/08/02 19:48:04  gusbro
// - Habían problemas de sincronización de threads
//
// Revision 1.13  2004/06/25 14:44:06  iroqueta
// Arreglos JTA
//
// Revision 1.12  2004/06/25 13:51:34  iroqueta
// JTA - solo se usa si se utiliza el pool de los servidores J2EE
//
// Revision 1.11  2004/06/23 21:59:23  iroqueta
// Arreglo para JTA
//
// Revision 1.10  2004/05/26 20:54:16  dmendez
// Se maneja correctamente las utls si no hay jta presente.
//
// Revision 1.9  2004/05/26 19:44:54  dmendez
// Faltaba manejar excepcion al tratar de abrir una utl con jta que no siempre esta disponible.
//
// Revision 1.8  2004/05/24 21:06:21  dmendez
// Soporte de JTA
//
// Revision 1.7  2004/03/03 14:36:26  gusbro
// - Cuando estoy con datasources JDBC intento utilizar la misma instancia de javax.sql.DataSource
//   porque sino si estoy en un thread nuevo (submit) no encuentra los datos del resource-reference
//
// Revision 1.6  2004/02/19 18:19:31  gusbro
// - Se soporta la property JDBCDataSource
//
// Revision 1.5  2003/12/30 22:54:12  gusbro
// - Cuando se utiliza un datasource del motor de servlets, no uso el cache de
//   prepared statements de GX porque lo maneja el motor.
//
// Revision 1.4  2003/04/21 18:11:48  aaguiar
// - Cambios en la implementacion del prepared statement cache
//
// Revision 1.3  2002/11/18 16:59:46  gusbro
// - cambio para poder compilar con J#
//
// Revision 1.2  2002/11/15 14:01:09  aaguiar
// - Se banca usar JDBC DataSources
//
// Revision 1.1.1.1  2002/04/17 20:08:02  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2002/04/17 20:08:02  gusbro
// GeneXus Java Olimar
//

package com.genexus.db.driver;

import java.sql.*;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.util.concurrent.Executor;

import com.genexus.Application;
import com.genexus.ApplicationContext;
import com.genexus.DebugFlag;
import com.genexus.CommonUtil;
import com.genexus.PrivateUtilities;
import com.genexus.common.classes.AbstractGXConnection;
import com.genexus.common.classes.IGXPreparedStatement;
import com.genexus.platform.INativeFunctions;
import com.genexus.platform.NativeFunctions;
import com.genexus.ModelContext;
import com.genexus.db.DBConnectionManager;
import com.genexus.db.UserInformation;
import com.genexus.db.Cursor;
import com.genexus.db.BatchUpdateCursor;

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


				DataSource dataSource1 = context.beforeGetConnection(handle, dataSource);
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

    public void commit() throws SQLException
	{

            for(int i=0; i<batchUpdateStmts.size(); i++)
            {
                BatchUpdateCursor cursor = (BatchUpdateCursor)batchUpdateStmts.get(i);
                if (cursor.pendingRecords()){
                    cursor.beforeCommitEvent();
                }
            }
            batchUpdateStmts.clear();

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
