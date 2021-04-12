package com.genexus.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.genexus.Application;
import com.genexus.ApplicationContext;
import com.genexus.ClientContext;
import com.genexus.ModelContext;
import com.genexus.db.driver.DataSource;
import com.genexus.db.driver.GXConnection;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.management.LocalUserInformationJMX;
import com.genexus.management.ServerUserInformationJMX;

public abstract class DBConnectionManager
{
	public static final ILogger logger = LogManager.getLogger(DBConnectionManager.class);
	
	public abstract GXConnection getConnection(ModelContext context, int handle, String dataSource, boolean readOnly, boolean sticky) throws SQLException;
	public abstract void dropAllCursors(int handle);
	public abstract boolean isConnected(int handle, String dataSource);

	protected abstract UserInformation getNewUserInformation(Namespace namespace);

	private static volatile DBConnectionManager connectionManager;
	protected ConcurrentHashMap<Integer, UserInformation> userConnections = new ConcurrentHashMap<Integer, UserInformation>();
	private static AtomicInteger mLastHandle = new AtomicInteger(0);


	private Object handleLock = new Object();
	private static Object staticHandleLock = new Object();
	private static boolean finishCreateDataBase = false;

	DBConnectionManager()
	{
	}

	/***
	 * Implement Double check pattern for singleton
	 * 
	 * Note the local variable manager, which seems unnecessary. 
	 * 
	 * This ensures that in cases where helper is already initialized (i.e., most of the time), 
	 * 
	 * the volatile field is only accessed once (due to "return manager;" instead of "return connectionManager;"), 
	 * 
	 * which can improve the method's overall performance by as much as 25 percent
	 * @return
	 */
	public static DBConnectionManager getInstance()
	{
		DBConnectionManager manager = connectionManager;
		if (manager == null) 
		{
			synchronized(staticHandleLock)
			{
				if	(connectionManager == null)
				{
					if	( ApplicationContext.getInstance().isGXUtility())
					{
						manager = connectionManager = new UtilConnectionManager();
					}
					else if	( ApplicationContext.getInstance().getPoolConnections() || ApplicationContext.getInstance().getReorganization())
					{
						manager = connectionManager = new ServerDBConnectionManager();
					}
					else
					{
						manager = connectionManager = new LocalDBConnectionManager();
					}
				}
				else
				{
					manager = connectionManager;
				}
			}
		}
		return manager;
	}


	public static void StartCreateDataBase()
	{
	}

	public static void EndCreateDataBase()
	{
		finishCreateDataBase = true;
	}


	public static void endDBConnectionManager()
	{
		connectionManager = null;
	}

	public Enumeration<UserInformation> getServerConnections()
	{
		return userConnections.elements();
	}


	public static DBConnectionManager getInstance(ModelContext context)
	{
		return getInstance();
	}

	public UserInformation createUserInformation(Namespace namespace)
	{
	
		UserInformation userInfo = getNewUserInformation(namespace);
		int handle = createNewHandle();
		synchronized (handleLock)
		{
			userInfo.setHandle(handle);
			userConnections.put(new Integer(handle), userInfo);
			
			ClientContext.setLocalUtil(userInfo.getLocalUtil());
			if	(ClientContext.getHandle() == -1)
				ClientContext.setHandle(handle);
		}

		//Enable JMX
		connectJMX(userInfo);

		if(Namespace.getConnectAtStartup())
		{ // Si debo realizar todas las conexion en startup
			Namespace.connectAll(userInfo.getHandle());
		}

		return userInfo; 
	}
	
	private void connectJMX(UserInformation userInfo) {
		if (Application.isJMXEnabled())
		{
			if (userInfo instanceof ServerUserInformation)
			{
				((ServerUserInformation)userInfo).setIP(ModelContext.getModelContext().getWorkstationId(userInfo.getHandle()));
				ServerUserInformationJMX.CreateServerUserInformationJMX((ServerUserInformation)userInfo, ModelContext.getModelContext());
			}
			else
				if (userInfo instanceof LocalUserInformation)
					LocalUserInformationJMX.CreateLocalUserInformationJMX((LocalUserInformation)userInfo);
		}
	}

	public String getUserName(ModelContext context, int handle, String dataSource) throws SQLException
	{
		return getConnection(context, handle, dataSource, true, false).getUserName();
	}

	public java.util.Date getServerDateTime(ModelContext context, int handle, String dataSource) throws SQLException
	{
		return getConnection(context, handle, dataSource, true, false).getDateTime();
	}

	public String getDBMSVersion(ModelContext context, int handle, String dataSource) throws SQLException
	{
		return getConnection(context, handle, dataSource, true, false).getDBMSVersion();
	}
	
	public String getDatabaseName(ModelContext context, int handle, String dataSource) throws SQLException
	{
		return getConnection(context, handle, dataSource, true, false).getDatabaseName();
	}	

	public void commit(ModelContext context, int handle, String dataSource) throws SQLException
	{
		if	(isConnected(handle, dataSource))
		{
			getConnection(context, handle, dataSource, false, true).commit();
		}
	}

	public void rollback(ModelContext context, int handle, String dataSource) throws SQLException
	{
		if	(isConnected(handle, dataSource))
			getConnection(context, handle, dataSource, false, true).rollback();
	}

	public void executeStatement(ModelContext context, int handle, String dataSource, String sqlSentence) throws SQLException
	{
		GXConnection con = getConnection(context, handle, dataSource, false, true);
		
		synchronized (staticHandleLock) {
			if (finishCreateDataBase)
			{
				finishCreateDataBase = false;
				con.disconnect();
				con = getConnection(context, handle, dataSource, false, true);
			}
		}

		con.setUncommitedChanges();
		con.getPoolState().setInAssignment(false);

		Statement stmt = con.createStatement();

		if (com.genexus.ApplicationContext.getInstance().getReorganization() &&
			con.getDataSource().dbms.getSupportsAutocommit() &&
			!con.getAutoCommit())
		{ //Hay algun bug al menos en el driver de INet por el cual queda mal el autocommit
			con.setAutoCommit(true);
		}

		try
		{
			stmt.executeUpdate(sqlSentence);
		}
		catch (SQLException e)
		{
			try
			{
				stmt.close();
			}
			catch (SQLException e1)
			{
				logger.fatal("Could not complete SQL Statement", e1);				
			}

			throw e;
		}

		stmt.close();
	}
//		getConnection(handle, dataSource, false).createStatement().executeUpdate(sqlSentence);
/*
	public synchronized GXPreparedStatement getStatement(int handle, String dataSource, int tableId, int operationId, String sqlSentence, boolean readOnly) throws SQLException
	{
		return (GXPreparedStatement) getConnection(handle, dataSource, readOnly).getStatement(tableId, operationId, sqlSentence, handle);
	}
*/
/*
	public synchronized GXPreparedStatement getStatement(int handle, String dataSource, String index, String sqlSentence, boolean readOnly, boolean currentOf) throws SQLException
	{
		return (GXPreparedStatement) getConnection(handle, dataSource, readOnly).getStatement(index, sqlSentence, currentOf, handle);
	}

	public synchronized GXCallableStatement getCallableStatement(int handle, String dataSource, String index, String sqlSentence, boolean readOnly) throws SQLException
	{
		return (GXCallableStatement) getConnection(handle, dataSource, readOnly).getCallableStatement(index, sqlSentence, handle);
	}
*/
	public UserInformation getUserInformation(int handle)
	{
		UserInformation ui = (UserInformation) userConnections.get(new Integer(handle));

		if	(ui == null)
			throw new IllegalArgumentException("Can't find user information for handle " + handle);

		return ui;
	}

	public UserInformation getUserInformationNoException(int handle)
	{
		return (UserInformation)userConnections.get(new Integer(handle));
	}

	public void disconnectOnException(int handle) throws SQLException, NullPointerException
	{
		UserInformation ui = getUserInformation(handle);
		if	(ui != null)
			ui.disconnectOnException();

		removeHandle(handle);

		unsuscribeJMX(ui);
	}
	
	private void unsuscribeJMX(UserInformation ui) {
		if (Application.isJMXEnabled())
		{
			if (ui instanceof ServerUserInformation)
				ServerUserInformationJMX.DestroyServerUserInformationJMX((ServerUserInformation)ui);
			else
				if (ui instanceof LocalUserInformation)
					LocalUserInformationJMX.DestroyLocalUserInformationJMX((LocalUserInformation)ui);
		}
	}

	public void disconnect(int handle) throws SQLException, NullPointerException
	{
		UserInformation ui = getUserInformation(handle);

		try
		{
			if	(ui != null) {
                ui.disconnect();
            }
		}
                finally{
                  removeHandle(handle);
	      unsuscribeJMX(ui);
                }
	}

	private void removeHandle(int handle)
	{
		synchronized (handleLock)
		{
			userConnections.remove(new Integer(handle));
		}
	}

	protected int createNewHandle()
	{
		return mLastHandle.incrementAndGet();
	}

	public int getFirstHandle()
	{
		for (Enumeration<Integer> en = userConnections.keys(); en.hasMoreElements(); )
			return en.nextElement().intValue();

		throw new InternalError("There arent any registered handles");
	}

	public int getClientHandle(int remoteHandle)
	{
		for (Enumeration<UserInformation> en = userConnections.elements(); en.hasMoreElements(); )
		{
			UserInformation ui = en.nextElement();

			if	(ui.hasRemoteHandle(remoteHandle))
				return ui.getHandle();
		}

		throw new InternalError("Can't find client handle for remote handle " + remoteHandle);
	}

 	public DataSource getDataSource(int handle, String dataSourceName)
	{
		return getUserInformation(handle).getNamespace().getDataSource(dataSourceName);
	}

	public DataSource getDataSourceNoException(int handle, String dataSourceName)
	{
		UserInformation ui = getUserInformationNoException(handle);
		if(ui != null)
		{
			return ui.getNamespace().getDataSource(dataSourceName);
		}
		return null;
	}

	public void disconnectAll()
	{
		Vector<Integer> handles = new Vector<Integer>();
		for(Enumeration<Integer> enum1 = userConnections.keys(); enum1.hasMoreElements();)
		{
			handles.addElement(enum1.nextElement());
		}
		for(Enumeration<Integer> enum1 = handles.elements(); enum1.hasMoreElements();)
		{
			try
			{
				disconnect(enum1.nextElement().intValue());
			}
			catch(Throwable e)
			{
				logger.warn("DBConnectionManager.disconnectAll: ", e);				
			}
		}
	}
}
