package com.genexus.db.driver;

import com.genexus.db.DBConnectionManager;
import com.genexus.util.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Dictionary;
import java.util.concurrent.ConcurrentHashMap;


import java.sql.SQLException;

class PoolDBConnectionState
{
	// Attenti, no puedo tener synchronized nada que dispare un statusChanged.
	private ConcurrentHashMap<Integer, ConcurrentHashMap<GXConnection, ConcurrentHashMap<GXResultSet, String>>> handles;

	private Dictionary mainConnections;
	private Object syncObject = new Object();
	
	PoolDBConnectionState()
	{
		handles = new ConcurrentHashMap<Integer, ConcurrentHashMap<GXConnection, ConcurrentHashMap<GXResultSet, String>>>();
	}
	Enumeration<GXConnection> getConnections(int handle)
	{
		synchronized (syncObject)
		{
			ConcurrentHashMap<GXConnection, ConcurrentHashMap<GXResultSet, String>> connections = handles.get(new Integer(handle));

			if	(connections == null)
				return null;

			return connections.keys();
		}
	}

	boolean hasConnection(int handle, GXConnection con)
	{
		boolean ret;
		synchronized (syncObject)
		{
			ConcurrentHashMap<GXConnection, ConcurrentHashMap<GXResultSet, String>>  connections = handles.get(new Integer(handle));

			if	(connections == null)
				return false;

			ret = connections.get(con) != null;
		}
		return ret;
	}

	void addConnection(int handle, GXConnection con)
	{
		synchronized (syncObject)
		{
			ConcurrentHashMap<GXConnection, ConcurrentHashMap<GXResultSet, String>> connections = handles.get(new Integer(handle));
			if (connections == null)
			{
				ConcurrentHashMap<GXConnection, ConcurrentHashMap<GXResultSet, String>> connections1 = new ConcurrentHashMap<GXConnection, ConcurrentHashMap<GXResultSet, String>>();
				connections1.putIfAbsent(con, new ConcurrentHashMap<GXResultSet, String>());
				handles.putIfAbsent(new Integer(handle), connections1);
			}
			else
			{
				connections.put(con, new ConcurrentHashMap<GXResultSet, String>());
			}
			con.getPoolState().incUserCount();
			//dump();
		}
	}

	void removeConnection(int handle, GXConnection con) throws SQLException
	{
		// Asumo que no va a pasar que desde dos threads simultaneos voy a querer
		// desconectar al mismo usuario. Si fuera a diferentes usuarios, el hashtable
		// es thread-safe asi que lo manejaria bien.
		// Si sincronizo ac� tengo que tener cuidado porque esto dispara 'statusChanged' en
		// el ConnectionPool, y eso puede quedar en deadlock con este.

		// Borra una conexion de un handle en particular
		synchronized (syncObject) {
		ConcurrentHashMap<GXConnection, ConcurrentHashMap<GXResultSet, String>> connections = handles.get(new Integer(handle));

		// Esta condicion es porque en algun caso podria pasar que no exista. Por ejemplo,
		// cuando doy de baja una conexion por timeout, intento
		if	(connections != null)
		{
			removeConnections(connections, con, con.getHandle() == handle);

			if	(connections.size() == 0)
			{
				handles.remove(new Integer(handle));
				}
			}

		}
			con.getPoolState().decUserCount();
	}
	
	private void removeDataSourceConnection(int handle, IConnectionPool pool, boolean closeConnections)
	{
			// Asumo que no va a pasar que desde dos threads simultaneos voy a querer
			// desconectar al mismo usuario. Si fuera a diferentes usuarios, el hashtable
			// es thread-safe asi que lo manejaria bien.
			final ConcurrentHashMap<GXConnection, ConcurrentHashMap<GXResultSet, String>> conns = handles.get(new Integer(handle));
			GXConnection con = null;
			GXConnection newCon = null;
	
			// Borra todas las conexiones de un handle y todos los resultsets
	
			// Puede ser null si el usuario nunca us� este pool...
			if	(conns != null)
			{
				
				for (Enumeration<GXConnection> en = conns.keys(); en.hasMoreElements();)
				{
					con = en.nextElement();
					try
					{
						newCon = (GXConnection)con.getPool().checkOut(con.getContext(), handle, true);					
						if (newCon.getId() == con.getId())
						{					
							boolean mustDecUserCount = false;
							try
							{
								mustDecUserCount = conns.get(con).isEmpty();
								if (!removeConnections(conns, newCon, true)) 
								{
									// Si hay cursores, el decUserCount lo hace la removeResultSet al dar de baja
									//el ultimo cursor.
									con.getPoolState().decUserCount();
								}
							}
							catch (SQLException e)
							{
								if (mustDecUserCount)
									con.getPoolState().decUserCount();
							}
						}
						else
						{
							newCon.getPoolState().setInAssignment(false);
						}
					}
					catch (SQLException e) 
					{}					
		                
					if (closeConnections) 
					{
						try 
						{
							pool.removeElement(con);
							con.close();
						} 
						catch (SQLException e) 
						{
						}
					}
				}
			}
			handles.remove(new Integer(handle));
		//dump();
	}

	public void removeConnection(final int handle, final boolean closeConnections) throws SQLException
	{
		DataSource ds;
		Enumeration<DataSource> dss = DBConnectionManager.getInstance().getUserInformation(handle).getNamespace().getDataSources();
		while ((ds = dss.nextElement() )!= null) {
			IConnectionPool pool = ds.getConnectionPool(handle);
			removeDataSourceConnection(handle, pool, closeConnections);
		}
	}

	private boolean removeConnections(ConcurrentHashMap<GXConnection, ConcurrentHashMap<GXResultSet, String>> connections, GXConnection con, boolean rollback) throws SQLException
	{

          SQLException disconnectException = null;
		boolean ret = false;
		ConcurrentHashMap<GXResultSet, String> resultSets  = connections.get(con);

		for (Enumeration<GXResultSet> en = resultSets.keys(); en.hasMoreElements();)
		{
                  try
                  {
                    ( (GXResultSet) en.nextElement()).close();
                  }
                  catch (SQLException e)
                  {
                    disconnectException = e;
                  }
                  ret = true;
		}

		if	((con.getPoolState().getUncommitedChanges()  || (rollback && (con.getDataSource().dbms instanceof GXDBMSmysql || con.getDataSource().dbms instanceof GXDBMSpostgresql))) && (rollback || !con.getDataSource().jdbcIntegrity))
		{
                  try
                  {
                    con.rollback();
                  }
                  catch (SQLException e)
                  {
                    disconnectException = e;
                  }
		}		
		else
		{
			con.getPoolState().setInAssignment(false);
		}		

                if (disconnectException != null)
                   throw disconnectException;
                else
                  return ret;
		//connections.remove(con);
	}
	void removeResultSet(int handle, GXConnection con, GXResultSet rslt)
	{
		boolean removeUser = false;

		synchronized (syncObject)
		{
			ConcurrentHashMap<GXConnection, ConcurrentHashMap<GXResultSet, String>> connections = handles.get(new Integer(handle));

			if	(connections != null)
			{
				ConcurrentHashMap<GXResultSet, String> resultSets  = connections.get(con);
				// En algunos casos de serverDate y serverTime el resultSets puede ser vacio.
				if (resultSets != null)
				{				
					resultSets.remove(rslt);

					if	(resultSets.size() == 0)
					{
						if	(!con.getPoolState().getUncommitedChanges() && con.getPool().isEnabled() )
						{
							if (con.getDBMS().getId() != GXDBMS.DBMS_POSTGRESQL && con.getDBMS().getId() != GXDBMS.DBMS_MYSQL)
							{
								connections.remove(con);
								if	(connections.size() == 0)
								{
									handles.remove(new Integer(handle));									
								}
							}
							removeUser = true;
						}
					}
				}
			}
		}

		if	(removeUser)
			con.getPoolState().decUserCount();
	}

	void errorConnection(int handle)
	{
		// Borra todas las conexiones de un handle y todos los resultsets
		ConcurrentHashMap<GXConnection, ConcurrentHashMap<GXResultSet, String>> connections = handles.get(new Integer(handle));

		// Podria no tener ninguna conexion, si el error se dio at connect time.
		if	(connections != null)
		{
			for (Enumeration<GXConnection> en = connections.keys(); en.hasMoreElements();)
			{
				GXConnection con = (GXConnection) en.nextElement();
				if (con.getHandle() == handle)
				{
					con.setError();

					try
					{
						removeConnections(connections, con, con.getHandle() == handle);
					}
					catch (SQLException e)
					{
					}
				}
			}
		}
	}

	void addResultSet(int handle, GXConnection con, GXResultSet rslt)
	{
		synchronized (syncObject)
		{
			ConcurrentHashMap<GXConnection, ConcurrentHashMap<GXResultSet, String>> connections = handles.get(new Integer(handle));

			// Esto es asi porque en serverNow y UserId no se crea un usuario, pero en algunos
			// casos si se crea un resultset..
			if	(connections != null)
			{
				ConcurrentHashMap<GXResultSet, String> resultSets  = connections.get(con);
				// En algunos casos de serverDate y serverTime el resultSets puede ser vacio.
				if (resultSets != null)
				{
					resultSets.put(rslt, "");
				}
			}
		}
		//dump();
	}


	void dump(java.io.PrintStream out)
	{
		synchronized (syncObject)
		{
			for (Enumeration<Integer> en = handles.keys(); en.hasMoreElements();)
			{
				Object handle = en.nextElement();
				out.println("Handle " + handle);

				for (Enumeration<GXConnection> en1 = (handles.get(handle)).keys(); en1.hasMoreElements();)
				{
					GXConnection connection = (GXConnection) en1.nextElement();

					out.println("\tConnection " + connection.hashCode());
					out.println("\t\tState: " + connection.getPoolState());

					for (Enumeration<GXResultSet> en2 = ((handles.get(handle)).get(connection)).keys(); en2.hasMoreElements();)
					{
						out.println("\t\tResultset " + en2.nextElement());
					}
				}
			}
		}
	}
}
