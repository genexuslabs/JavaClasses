package com.genexus.db.driver;

import java.util.concurrent.atomic.AtomicInteger;

/**
 - La estrategia de syncrhonizing es hacerlo lo menos posible, a nivel de recurso, porque
   este objeto es 'pesado' en el connection pooling.
*/
public class ConnectionPoolState
{
	private GXConnection con;
	private volatile boolean inAssignment;
	private volatile boolean uncommitedChanges;
	private AtomicInteger 	 userCount = new AtomicInteger(0);

	private Object commitedChangesLock = new Object();
	private Object inAssignmentLock    = new Object();

	ConnectionPoolState(GXConnection con)
	{
		this.con = con;
	}

	GXConnection getConnection()
	{
		return con;
	}

	// User count
	void incUserCount()
	{
		userCount.incrementAndGet();
	}

	void decUserCount()
	{
		userCount.decrementAndGet();

		statusChanged(true);
	}

	public int getUserCount()
	{
		return userCount.get();
	}

	void setUncommitedChanges(boolean uncommitedChanges)
	{
		synchronized (commitedChangesLock)
		{
			this.uncommitedChanges = uncommitedChanges;
		}
		statusChanged(!uncommitedChanges);
	}

	boolean getUncommitedChanges()
	{
		synchronized (commitedChangesLock)
		{
			return uncommitedChanges;
		}
	}

	public void setInAssignment(boolean inAssignment)
	{	
		synchronized (inAssignmentLock)
		{
			this.inAssignment = inAssignment;
		}

		if	(!inAssignment)
		{
			statusChanged(!inAssignment);
		}
	}

	public boolean getInAssignment()
	{
		synchronized (inAssignmentLock)
		{
			return inAssignment;
		}
	}

	void closeCursor()
	{
		statusChanged(true);
	}

	void connectionError()
	{
		statusChanged(true);
	}

	int getOpenCursorsJMX()
	{
		return con.getOpenCursorsJMX();
	}	
	
	int getOpenCursors()
	{
		return con.getOpenCursors();
	}

	public boolean isRWAvailableJMX()
	{
		return getOpenCursorsJMX() == 0 && !getUncommitedChanges();
	}	
	
	public boolean isRWAvailable()
	{
		return getOpenCursors() == 0 && !getUncommitedChanges();
	}

	boolean isRecyclable(boolean readOnly)
	{
		// El usercount tambien se incluye, de modo de que no tenga que actualizar el 
		// PoolDBConnectionState cuando hago el remove de la conexion. Sino tendria que ir
		// a buscar donde esta...

		return !getUncommitedChanges() && getOpenCursors() == 0 && (getUserCount() == 0 || !readOnly);
	}

	private void statusChanged(boolean better)
	{
		// Este no puede ser synchronized porque el puedo quedar
		// en deadlock.

		if	(con.getPool() != null)
			con.getPool().statusChanged(this);

	}

	public String toString()
	{
		return "Cursors : " + getOpenCursors() + " | Users: " + userCount + " | Uncommited " + uncommitedChanges + " | Assignment " + inAssignment;
	}
}
