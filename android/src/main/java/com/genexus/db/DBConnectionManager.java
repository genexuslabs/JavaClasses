package com.genexus.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.genexus.ClientContext;
import com.genexus.CommonUtil;
import com.genexus.ModelContext;
import com.genexus.ModelContext;
import com.genexus.db.driver.DataSource;
import com.genexus.db.driver.GXConnection;

public abstract class DBConnectionManager
{
	public abstract GXConnection getConnection(ModelContext context, int handle, String dataSource, boolean readOnly, boolean sticky) throws SQLException;
	public abstract void dropAllCursors(int handle);
	public abstract boolean isConnected(int handle, String dataSource);

	protected abstract UserInformation getNewUserInformation(Namespace namespace);

	private static DBConnectionManager connectionManager;
	private static Hashtable managers = new Hashtable();
	private Object handleLock = new Object();

	protected Hashtable userConnections = new Hashtable();

	DBConnectionManager()
	{
	}

	private static Object staticHandleLock = new Object();
	
	public static DBConnectionManager getInstance()
	{
		synchronized(staticHandleLock)
		{
			if	(connectionManager == null)
			{
				connectionManager = new LocalDBConnectionManager();
			}
		}

		return connectionManager;
	}
	
	private static boolean finishCreateDataBase = false;
	
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

	public Enumeration getServerConnections()
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

		synchronized (handleLock)
		{
			int handle = createNewHandle();
			userInfo.setHandle(handle);
			userConnections.put(new Integer(handle), userInfo);
			ClientContext.setLocalUtil(userInfo.getLocalUtil());
			if	(ClientContext.getHandle() == -1)
				ClientContext.setHandle(handle);
		}
		
		if(Namespace.getConnectAtStartup())
		{ // Si debo realizar todas las conexion en startup
			Namespace.connectAll(userInfo.getHandle());
		}

		return userInfo;
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

	private Object createKBLock = new Object();
	
	public void executeStatement(ModelContext context, int handle, String dataSource, String sqlSentence) throws SQLException
	{
		GXConnection con = getConnection(context, handle, dataSource, false, true);
		synchronized (createKBLock) {
			if (finishCreateDataBase)
			{
				finishCreateDataBase = false;			
				con = getConnection(context, handle, dataSource, false, true);
			}
		}
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
				System.err.println(e1.getMessage());
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
	}

	public void disconnect(int handle) throws SQLException, NullPointerException
	{
		UserInformation ui = getUserInformation(handle);

		try
		{
			if	(ui != null){
                          ui.disconnect();
                        }
		}
                finally{
                  removeHandle(handle);			  
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
		while (true)
		{
			int handle = (int) (CommonUtil.rand() * Integer.MAX_VALUE);

			if	(userConnections.get(new Integer(handle)) == null)
				return handle;
		}
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
				System.err.println("DBConnectionManager.disconnectAll: " + e.toString());
			}
		}
	}
}
