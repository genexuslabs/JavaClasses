package com.genexus.db.driver;

import com.genexus.ModelContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public final class DirectConnectionPool implements IConnectionPool
{
	private static final Enumeration enum1 = new Vector().elements();
	private DataSource dataSource;
	private Hashtable<Integer, GXConnection> connections = new Hashtable<Integer, GXConnection>();

	DirectConnectionPool(DataSource dataSource)
	{
		this.dataSource 	= dataSource;
	}

	public Enumeration getROPools()
	{
		return enum1;
	}

	public Enumeration getRWPools()
	{
		return enum1;
	}

	public ConnectionPool getROConnectionPool(String user)
	{
		return null;
	}

	public ConnectionPool getRWConnectionPool(String user)
	{
		return null;
	}

	public Connection checkOut(ModelContext context, DataSource dataSource1, int handle, String user, String password, boolean readOnly, boolean sticky) throws SQLException
	{
		Integer theHandle = new Integer(handle);
		GXConnection con = (GXConnection)connections.get(theHandle);
		if(con == null)
		{
			if (dataSource1 != null)
				con = new GXConnection(context, handle, user, password, dataSource1);
			else
				con = new GXConnection(context, handle, user, password, dataSource);
			connections.put(theHandle, con);
		}
		con.setHandle(handle);
		return con;
	}


	public void disconnectOnException(int handle) throws SQLException
	{
		disconnect(handle);
	}

	public void disconnect(int handle) throws SQLException
	{
		Integer theHandle = new Integer(handle);
		GXConnection con = (GXConnection)connections.remove(theHandle);
		if(con != null)
		{
			con.close();
		}
	}

	public void disconnect() throws SQLException
	{
               SQLException error = null;
		for (Enumeration<GXConnection> en = connections.elements(); en.hasMoreElements(); )
		{
                  try
                  {
                    ( (GXConnection) en.nextElement()).close();
                  }
                  catch(SQLException e)
                  {
                    error=e;
                  }
		}

		connections.clear();

                if (error!=null)
                  throw error;
	}

	@Override
	public void runWithLock(Runnable runnable) {
		runnable.run();
	}
	@Override
	public void removeElement(GXConnection con) {
	}
}
