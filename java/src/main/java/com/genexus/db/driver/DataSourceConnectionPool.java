package com.genexus.db.driver;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import com.genexus.ModelContext;


public final class DataSourceConnectionPool implements IConnectionPool
{
	private DataSource dataSource;
	private ConcurrentHashMap<String, ConnectionPool> readOnlyPools ;
	private ConcurrentHashMap<String, ConnectionPool> readWritePools;
	private boolean readOnlyPoolEnabled;

	DataSourceConnectionPool(DataSource dataSource)
	{
		this.dataSource 	= dataSource;
		readWritePools  	= new ConcurrentHashMap<String, ConnectionPool>();
		readOnlyPoolEnabled = dataSource.roPoolEnabled;
		readOnlyPools   	= new ConcurrentHashMap<String, ConnectionPool>();
	}

	public Enumeration<String> getROPools()
	{
		return readOnlyPools.keys();
	}

	public Enumeration<String> getRWPools()
	{
		return readWritePools.keys();
	}

	public ConnectionPool getROConnectionPool(String user)
	{
		return (ConnectionPool) readOnlyPools.get(user);
	}

	public ConnectionPool getRWConnectionPool(String user)
	{
		return (ConnectionPool) readWritePools.get(user);
	}

	
	private ConnectionPool newConnectionPool(DataSource dataSource1, String user, String password, boolean readOnly) {
		ConnectionPool pool;
		if (dataSource1 == null)
			dataSource1 = dataSource;
		if (readOnly)
			pool = new ReadOnlyConnectionPool(dataSource1, user, password);
		else
			pool = new ReadWriteConnectionPool(dataSource1, user, password);
		return pool;
	}
	
	public Connection checkOut(ModelContext context, DataSource dataSource1, int handle, String user, String password, boolean readOnly, boolean sticky) throws SQLException
	{
		ConnectionPool pool = null;
		String key = user + password;
		ConcurrentHashMap<String, ConnectionPool> connectionPools = (readOnlyPoolEnabled && readOnly)? readOnlyPools : readWritePools;
		if	((pool = (ConnectionPool) connectionPools.get(key)) == null)
		{
   		pool = newConnectionPool(dataSource1, user, password, (readOnlyPoolEnabled && readOnly));
   		ConnectionPool previousPool = connectionPools.putIfAbsent(key, pool);
   		if (previousPool != null)
   			pool = previousPool;
   	}
		return pool.checkOut(context, handle, sticky);
	}

	public void disconnectOnException(int handle) throws SQLException
	{
		for (Enumeration<ConnectionPool> en = readWritePools.elements(); en.hasMoreElements(); )
			((ConnectionPool) en.nextElement()).disconnectOnException(handle);

		for (Enumeration<ConnectionPool> en = readOnlyPools.elements(); en.hasMoreElements(); )
			((ConnectionPool) en.nextElement()).disconnectOnException(handle);
	}

	public void disconnect(int handle) throws SQLException
	{
          SQLException disconnectException = null;
            for (Enumeration<ConnectionPool> en = readWritePools.elements(); en.hasMoreElements(); )
            {
              try
              {
                ( (ConnectionPool) en.nextElement()).disconnect(handle);
              }
              catch (SQLException e)
              {
                disconnectException = e;
              }
            }

            for (Enumeration<ConnectionPool> en = readOnlyPools.elements(); en.hasMoreElements(); )
            {
              try
              {
                ( (ConnectionPool) en.nextElement()).disconnect(handle);
              }
              catch (SQLException e)
              {
                disconnectException = e;
              }
            }

            if (disconnectException != null)
                   throw disconnectException;
	}

	public void disconnect() throws SQLException
	{
		for (Enumeration<ConnectionPool> en = readWritePools.elements(); en.hasMoreElements(); )
			en.nextElement().disconnect();

		for (Enumeration<ConnectionPool> en = readOnlyPools.elements(); en.hasMoreElements(); )
			en.nextElement().disconnect();
	}
	@Override
	public void runWithLock(Runnable runnable) {
		for (Enumeration<ConnectionPool> en = readWritePools.elements(); en.hasMoreElements(); )
			((ConnectionPool) en.nextElement()).runWithLock(runnable);
		for (Enumeration<ConnectionPool> en = readOnlyPools.elements(); en.hasMoreElements(); )
			((ConnectionPool) en.nextElement()).runWithLock(runnable);
	}
	@Override
	public void removeElement(GXConnection con) {
		for (Enumeration<ConnectionPool> en = readWritePools.elements(); en.hasMoreElements(); )
			((ConnectionPool) en.nextElement()).removeElement(con);
		for (Enumeration<ConnectionPool> en = readOnlyPools.elements(); en.hasMoreElements(); )
			((ConnectionPool) en.nextElement()).removeElement(con);
	}
}


