// $Log: ConnectionPool.java,v $
// Revision 1.10  2005/09/08 16:16:19  iroqueta
// Implementacion del dump en XML para JMX
//
// Revision 1.9  2005/08/03 17:43:24  iroqueta
// El setMaxPoolSize solo lo hago si el pool no tiene tama�o ilimitado y el valor a asignar es mayor del valor actual.
// Ademas si lo hago notifico a todos los que estan esperando para que puedan obtener una conexion sin esperar a que se venza el timeout
//
// Revision 1.8  2005/07/22 21:12:31  iroqueta
// Agrego ifdefs para que no de problemas JMX en .NET
//
// Revision 1.7  2005/07/21 15:10:58  iroqueta
// Implementacion de soporte de JMX
//
// Revision 1.6  2005/04/21 13:48:00  iroqueta
// En el metodo disconnect(int handle) atrapo las SQLException y en caso de que haya alguna lo tiro al final para que haga todo lo que tiene que hacer el metodo y no termine por la mitad
//
// Revision 1.5  2005/02/18 21:19:27  iroqueta
// Le hago llegar el ModelContext al GXConnection para poder pasarselo al proc que se llama para obtener el nombre del datasource al cual conectarse.
//
// Revision 1.4  2004/08/02 19:48:42  gusbro
// - Hab�an problemas de sincronizaci�n de threads
//
// Revision 1.3  2004/06/25 13:51:34  iroqueta
// JTA - solo se usa si se utiliza el pool de los servidores J2EE
//
// Revision 1.2  2004/02/19 18:17:35  gusbro
// - Pongo publica la disconnect
//
// Revision 1.1.1.1  2001/11/16 14:32:26  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2001/11/16 14:32:26  gusbro
// GeneXus Java Olimar
//

package com.genexus.db.driver;
import java.sql.*;
import java.util.*;
import java.io.*;

import com.genexus.Application;
import com.genexus.DebugFlag;
import com.genexus.CommonUtil;
import com.genexus.util.*;
import com.genexus.PrivateUtilities;
import com.genexus.GXJTA;
import com.genexus.ModelContext;
import com.genexus.db.*;
import com.genexus.management.ConnectionPoolJMX;

public abstract class ConnectionPool
{
	protected static final boolean DEBUG       = DebugFlag.DEBUG;
	protected static final int RECYCLE_BY_CREATION       = 1;
	protected static final int RECYCLE_BY_IDLE           = 2;

	private final long WAIT_TIMEOUT = 30000;

	protected DataSource dataSource;
	protected Vector<GXConnection>  pool;
	protected String  user;
	protected String  password;
	protected int 	  maxPoolSize;
	protected boolean unlimitedSize;
	protected String  poolName;
	protected boolean recycleConnections;
	protected int recycleConnectionsType;
	protected long 	  recycleConnectionsTime;
	private int brokenConnections;

	abstract GXConnection createConnection(ModelContext context, int handle) throws SQLException;
	abstract GXConnection getSameConnection(int handle, boolean sticky);
	abstract boolean isAvailable(GXConnection con, int handle);
	abstract boolean isAvailableJMX(GXConnection con);
	abstract boolean isReadOnly();
	abstract boolean isEnabled();

	protected PoolDBConnectionState constate;
	private Object poolLock = new Object();
	private PrintStream out;
	
	//JMX properties
	int numberConnectionsCreated = 0;
	int numberConnectionsRecycled = 0;
	int numberConnectionsDeleted = 0;
	int numberRequest = 0;
	long timeFirstRequest;
	java.util.Date timeLastRequest;
	int numberUsersWaits = 0;
	int numberUsersWaiting = 0;
	Hashtable timeStartUserWait = new Hashtable();
	long maxTimeUserWait;
	float averageUserWaitingTime;
	ConnectionPoolJMX connectionPoolJMX;
	long UserMaxTimeWaitingBeforeNotif = 30000;
	boolean enableNotifications = true;
	
	ConnectionPool(DataSource dataSource, String user, String password)
	{
		pool = new Vector<GXConnection>();
		this.dataSource = dataSource;
		this.user 	    = user;
		this.password   = password;
		constate = new PoolDBConnectionState();
		
		//Enable JMX
		if (Application.isJMXEnabled())
			ConnectionPoolJMX.CreateConnectionPoolJMX(this);
	}

	PoolDBConnectionState getDBConnectionState()
	{
		return constate;
	}

	void createPoolStartup()
	{
		if	(dataSource.createPoolStartup && !unlimitedSize)
		{
			try
			{
				for (int i = 0; i < maxPoolSize; i++)
				{
					GXConnection con = createConnection(null, -1);
					pool.addElement(con);
					numberConnectionsCreated ++;

				}
			}
			catch (SQLException e)
			{
				System.err.println("Error creating " + poolName + " connections");
			}
		}
	}

	boolean mustRecycle(GXConnection con, boolean readOnly)
	{
		return 	recycleConnections && con.getPoolState().isRecyclable(readOnly) &&
				((recycleConnectionsType == RECYCLE_BY_CREATION && con.getConnectionTime() + recycleConnectionsTime < System.currentTimeMillis()) ||
				 (recycleConnectionsType == RECYCLE_BY_IDLE && con.getTimeLastRequest().getTime() + recycleConnectionsTime < System.currentTimeMillis()));
	}

	GXConnection getPoolConnection(int handle, boolean sticky)
	{
		synchronized(poolLock)
		{
		GXConnection con = getSameConnection(handle, sticky);
		if	(con != null)
			return con;

			
		for (int idx = 0; idx < pool.size(); )
		{
			con = (GXConnection) pool.elementAt(idx);

			if	(con.getError())
			{
					disconnectBrokenConnection(con);
				continue;
			}

			if	(!con.getPoolState().getInAssignment() && isAvailable(con, handle))
			{
				if	(mustRecycle(con, isReadOnly()))
				{
					if	(DEBUG)
						log(con.getHandle(), "Dropping " + poolName + " by timeout connection " + con.hashCode());

						dropUnusedConnection(con);
					continue;
				}
				else
				{
					if	(DEBUG)
						log(handle, "Reusing connection " + con.hashCode());

					if	(sticky)
					{
						con.getPoolState().setInAssignment(true);
						if (!constate.hasConnection(handle, con))
							constate.addConnection(handle, con);

						con.setHandle(handle);
					}

					return con;
				}
			}
			idx++;
		}
		}

		return null;
	}

	Connection checkOut(ModelContext context, int handle, boolean sticky) throws SQLException
	{
		if (numberRequest == 0)
			timeFirstRequest = System.currentTimeMillis();
		numberRequest ++;
		timeLastRequest = new java.util.Date();
		
		boolean firstTimeWaiting = true;
		
		GXConnection con = null;
		//dump();

		while (con == null)
		{
			synchronized (poolLock)
			{
				con = getPoolConnection(handle, sticky);

				if	(con == null && (unlimitedSize || pool.size() < maxPoolSize))
				{
					con = createConnection(context, handle);
					if	(con != null)
					{
						pool.addElement(con);
						numberConnectionsCreated ++;
						if	(sticky)
						{
							con.getPoolState().setInAssignment(true);

							con.setHandle(handle);
							constate.addConnection(handle, con);
						}
					}
				}

				if	(con == null)
				{
					if (Application.isJMXEnabled() && enableNotifications)
						connectionPoolJMX.PoolIsFull();
					((UserInformation)DBConnectionManager.getInstance().getUserInformation(handle)).setWaitingConnection(true);
					if	(DEBUG)
						log(handle, "Waiting for connection " + PrivateUtilities.getCurrentThreadId() + " unlimited " + unlimitedSize + " poolSize " + pool.size() + " maxPoolSize " + maxPoolSize);

					if (firstTimeWaiting)
					{
						numberUsersWaits ++;
						numberUsersWaiting ++;
						timeStartUserWait.put(new Integer(handle), new Long(System.currentTimeMillis()));
						firstTimeWaiting = false;
					}
					else
					{
						if (Application.isJMXEnabled())
							if ((UserMaxTimeWaitingBeforeNotif < (System.currentTimeMillis() - ((Long) timeStartUserWait.get(new Integer(handle))).longValue())) && enableNotifications)
								connectionPoolJMX.UserWaitingForLongTime();
					}
					notified = false;
					while (!notified)
					{
						try
						{
							poolLock.wait(WAIT_TIMEOUT);
						}
						catch (InterruptedException e)
						{
						}

						if	(!notified)
						{
							if	(DEBUG)
								log(handle, "Still waiting for connection " + PrivateUtilities.getCurrentThreadId());
						}
					}

					if	(DEBUG)
						log(handle, "Stopped waiting for connection " + PrivateUtilities.getCurrentThreadId());
					
				}

				if	(con != null)
				{
					con.setHandle(handle);
					
					// Esto va aca para que el PoolState quede 'igual' que al asignarlo.
					if	(DEBUG)
						log(con, handle, "Assigning connection to handle");
				}
			}
		}
		
		if (!firstTimeWaiting)
		{
			long userWait = System.currentTimeMillis() - ((Long) timeStartUserWait.get(new Integer(handle))).longValue();			
			timeStartUserWait.remove(new Integer(handle));
			averageUserWaitingTime = (averageUserWaitingTime + userWait) / numberUsersWaits; 
			if (userWait > maxTimeUserWait)
				maxTimeUserWait = userWait;
			numberUsersWaiting --;		
		}
		((UserInformation)DBConnectionManager.getInstance().getUserInformation(handle)).setWaitingConnection(false);
		return con;
	}

	boolean notified = false;
	void statusChanged(ConnectionPoolState state)
	{
		synchronized (poolLock)
		{
			notified = true;
			poolLock.notifyAll();
		}
	}

	public Enumeration<GXConnection> getConnections()
	{
		return pool.elements();
	}

	public GXConnection getConnectionById(int id)
	{
		// Esto se usa para el utilitario de administracion. Obviamente no es
		// muy eficiente, pero no importa mucho dado que no se ejecuta mucho.
		synchronized(poolLock)
		{
		for (int i = 0 ; i < pool.size(); i++)
			if	( ((GXConnection) pool.elementAt(i)).getId() == id)
				return (GXConnection) pool.elementAt(i);
		}

		return null;
	}

	public void dropConnectionById(int id) throws SQLException
	{
		// Esto se usa para el utilitario de administracion. Obviamente no es
		// muy eficiente, pero no importa mucho dado que no se ejecuta mucho.
		synchronized(poolLock)
		{
			for (int i = 0 ; i < pool.size(); )
			{
				GXConnection con = (GXConnection) pool.elementAt(i);
				if	( con.getId() == id)
				{
					pool.removeElementAt(i);
					con.close();
					numberConnectionsDeleted ++;
					continue;
				}
				i++;
			}
		}
	}

	private void disconnectBrokenConnection(GXConnection con)
	{
		try
		{
			log(con, 0 ,"Disconnecting connection with error");
			synchronized(poolLock)
			{
				pool.removeElement(con);
				numberConnectionsDeleted ++;
			}
		 	con.closeWithError();
		}
		catch (SQLException e)
		{
			System.err.println("Error closing connection " + con);
		}

		brokenConnections ++;
	}

	public int getBrokenConnectionCount()
	{
		return brokenConnections;
	}

	private void dropUnusedConnection(GXConnection con)
	{
		synchronized(poolLock)
		{
			pool.removeElement(con);
			numberConnectionsRecycled ++;
		}
		Thread connecionCloseThread = new Thread(new ConnecionCloseRunnable(con));
		connecionCloseThread.start();
	}

	void disconnect() throws SQLException
	{
		synchronized(poolLock)
		{
			for (int idx = 0; idx < pool.size(); idx ++)
			{
				((GXConnection) pool.elementAt(idx)).close();
			}
			pool = new Vector<GXConnection>();
		}
	}

	public void disconnect(int handle) throws SQLException
	{
		constate.removeConnection(handle, !isEnabled());
	}

	void disconnectOnException(int handle) throws SQLException
	{
		constate.errorConnection(handle);
	}

	protected void log(int handle, String text)
	{
		if	(DEBUG)
		{
			dataSource.getLog().logComment(GXDBDebug.POOL_ACTIVITY, handle, poolName + ": " + text);
		}
	}

	protected void log(Object source, int handle, String text)
	{
		if	(DEBUG)
		{
			dataSource.getLog().log(GXDBDebug.POOL_ACTIVITY, source, handle, poolName + ": " + text);
		}
	}

	boolean getRecycleConnections()
	{
		return recycleConnections;
	}

	long getRecycleConnectionsTime()
	{
		return recycleConnectionsTime;
	}

	void dump()
	{
		try
		{
			if	(out == null)
			{
				out = new PrintStream(new FileOutputStream("pool_" +  CommonUtil.getYYYYMMDDHHMMSS_nosep(new java.util.Date()) + ".log", true));
			}
			out.println("-> " + CommonUtil.getYYYYMMDDHHMMSS_nosep(new java.util.Date()));
			out.println("Pool 	  : " + poolName + " " + this);
			out.println("DataSource: " + dataSource.getName() + " - user : " + user) ;
			out.println("Size 	  : max " + maxPoolSize + " current " + pool.size() + " unlimited "+ unlimitedSize);
			out.println("Recycle   : " + recycleConnections + " time " + recycleConnectionsTime);

			synchronized(poolLock)
			{
				for (int i = 0; i < pool.size(); i++)
				{
					GXConnection con = (GXConnection) pool.elementAt(i);
					ConnectionPoolState state = con.getPoolState();

					out.println(i + " " + con.hashCode() + " oc " + state.getOpenCursors() + " uc " + state.getUserCount() + " uncomm " + state.getUncommitedChanges() + " assign " + state.getInAssignment());
					con.dump(out);
				}
			}

			constate.dump(out);
			out.println("<-");

		}
		catch (IOException e)
		{
		}

	}
	
	void dumpJMX()
	{
		String fileName = "Pool_" +  CommonUtil.getYYYYMMDDHHMMSS_nosep(new java.util.Date()) + ".xml";	  
		com.genexus.xml.XMLWriter writer = new com.genexus.xml.XMLWriter();
		writer.xmlStart(fileName);
		writer.writeStartElement("ConnectionPool_Information");
			writer.writeElement("UnlimitedSize", new Boolean(getUnlimitedSize()).toString());		
			writer.writeElement("Size", getMaxPoolSize());
			writer.writeElement("ConnectionCount", getActualPoolSize());			
			writer.writeElement("FreeConnectionCount", getFreeConnectionCount());
			writer.writeElement("CreatedConnectionCount", getNumberConnectionsCreated());
			writer.writeElement("RecycledConnectionCount", getNumberConnectionsRecycled());
			writer.writeElement("DroppedConnectionCount", getNumberConnectionsDeleted());			
			writer.writeElement("RequestCount", getNumberRequest());
			writer.writeElement("AverageRequestPerSec", getAverageNumberRequest());
			writer.writeElement("LastRequestTime", getTimeLastRequest().toGMTString());
			writer.writeElement("WaitingUserCount", getNumberUsersWaiting());
			writer.writeElement("WaitedUserCount", getNumberUsersWaits());
			writer.writeElement("MaxUserWaitTime", getMaxUserWaitingTime());
			writer.writeElement("AverageUserWaitTime", getAverageUserWaitingTime());															
			
			for (int i = 0; i < pool.size(); i++)
			{
				GXConnection con = (GXConnection) pool.elementAt(i);
				con.dump(writer);
			}
			
		writer.writeEndElement();		
		writer.close();		
	}	
	
	public DataSource getDataSource()
	{
		return dataSource;
	}
	
////////////////////////////////JMX operations/////////////////////////////////////
	public int getMaxPoolSize()
	{
		return maxPoolSize;
	}
	
	public void setMaxPoolSize(int maxPoolSize)
	{
		if (unlimitedSize || maxPoolSize <= this.maxPoolSize)
			return;
		synchronized(poolLock)
		{
			this.maxPoolSize = maxPoolSize;
			notified = true;
			poolLock.notifyAll();
		}
	}
	
	public boolean getUnlimitedSize()
	{
		return unlimitedSize;
	}
	
	public int getActualPoolSize()
	{
		return pool.size();
	}
	
	public int getFreeConnectionCount()
	{
		GXConnection con = null;
		int freeConnections = 0;
		for (int idx = 0; idx < pool.size(); idx++)
		{
			con = (GXConnection) pool.elementAt(idx);
			if (isAvailableJMX(con))
				freeConnections ++;
		}
		return freeConnections;
	}
	
	public int getNumberConnectionsCreated()
	{
		return numberConnectionsCreated;
	}
	
	public int getNumberConnectionsRecycled()
	{
		return numberConnectionsRecycled;
	}
	
	public int getNumberConnectionsDeleted()
	{
		return numberConnectionsDeleted;
	}
	
	public int getNumberRequest()
	{
		return numberRequest;
	}
	
	public float getAverageNumberRequest()
	{
		return numberRequest / ((System.currentTimeMillis() - timeFirstRequest) / 1000);
	}

	public java.util.Date getTimeLastRequest()
	{
		return timeLastRequest;
	}
	
	public int getNumberUsersWaits()
	{
		return numberUsersWaits;
	}
	
	public int getNumberUsersWaiting()
	{
		return numberUsersWaiting;
	}
	
	public long getMaxUserWaitingTime()
	{
		return maxTimeUserWait;
	}
	
	public float getAverageUserWaitingTime()
	{
		return averageUserWaitingTime;
	}
	
	public long getUserMaxTimeWaitingBeforeNotif()
	{
		return UserMaxTimeWaitingBeforeNotif;
	}
	
	public void setUserMaxTimeWaitingBeforeNotif(long value)
	{
		UserMaxTimeWaitingBeforeNotif = value;
	}
	
	public boolean getEnableNotifications()
	{
		return enableNotifications;
	}
	
	public void setEnableNotifications(boolean value)
	{
		enableNotifications = value;
	}	
	
	public void dumpPoolInformation()
	{
		dumpJMX();
	}
	
	public void PoolRecycle()
	{
		GXConnection con = null;
		
		synchronized(poolLock)
		{
			int poolSize = pool.size();
			for (int idx = 0; idx < poolSize;)
			{
				con = (GXConnection) pool.elementAt(0);

				if	(con.getError())
				{
						disconnectBrokenConnection(con);
						continue;
				}

				if	(!con.getPoolState().getInAssignment() && isAvailable(con, 0))
				{
					if	(DEBUG)
						log(con.getHandle(), "Dropping " + poolName + " by JMX Operation; connection " + con.hashCode());

					dropUnusedConnection(con);
				}
				idx++;
			}
		}
	}
	
	public void setConnectionPoolJMX(ConnectionPoolJMX connectionPoolJMX)
	{
		this.connectionPoolJMX = connectionPoolJMX;
	}
	public void runWithLock(Runnable runnable) {
		synchronized (poolLock) {
			runnable.run();
		}
	}
	public void removeElement(GXConnection con) {
		pool.removeElement(con);
	}
	
	public class ConnecionCloseRunnable implements Runnable
	{
		private GXConnection con;
		
		public ConnecionCloseRunnable(GXConnection con)
		{
			this.con = con;
		}
		
		public void run()
		{
			try
			{
			 	con.close();
			}
			catch (SQLException e)
			{
				System.err.println("Can't close connection " + con);
			}			
		}
	}	
}
