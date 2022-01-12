package com.genexus.db.driver;

import java.sql.SQLException;
import java.util.Enumeration;

import com.genexus.ApplicationContext;
import com.genexus.ModelContext;

final class ReadWriteConnectionPool extends ConnectionPool
{
	ReadWriteConnectionPool(DataSource dataSource, String user, String password)
	{
		super(dataSource, user, password);

		maxPoolSize   = dataSource.rwPoolSize;
		unlimitedSize = dataSource.rwPoolSizeUnlimited;
		recycleConnections 	   = dataSource.getRWPoolRecycle();
		recycleConnectionsType 	   = dataSource.getRWPoolRecycleType();
		String recycleConnectionsTypeDesc = "by creation time";
		recycleConnectionsTime = ((long) dataSource.getRWPoolRecycleMins()) * 60000;

		if	(DEBUG)
		{
			if (recycleConnectionsType == RECYCLE_BY_IDLE)
				recycleConnectionsTypeDesc = "by idle time";
											
			log(0, "Creating pool size: " + maxPoolSize + ", unlimited: " + unlimitedSize + ", recycle " + recycleConnections + ", recycle type " + recycleConnectionsTypeDesc + ", recycle time " + recycleConnectionsTime );
		}

		poolName	  = "Pool";

		createPoolStartup();
	}

	boolean useSameConnectionForSameHandle()
	{
		return true;
	}

	boolean isAvailableJMX(GXConnection con)
	{
		return  (con.getPoolState().isRWAvailableJMX());
	}	
	
	boolean isAvailable(GXConnection con, int handle)
	{
		if (ApplicationContext.getInstance().getReorganization() == true)
		{
			return  (Thread.currentThread().hashCode() == con.getThread() || con.getPoolState().isRWAvailable());
		}
		else
		{		
			return  (con.getHandle() == handle || con.getPoolState().isRWAvailable());
		}
	}

	GXConnection createConnection(ModelContext context, int handle) throws SQLException
	{
	 	return new GXConnection(context, this, handle, user, password, dataSource);
	}

	GXConnection getSameConnection(int handle, boolean sticky)
	{
		Enumeration en = constate.getConnections(handle);

		if	(en != null)
		{
			for (; en.hasMoreElements(); )
			{
				GXConnection con = (GXConnection) en.nextElement();
				
				if (ApplicationContext.getInstance().getReorganization() == true && !(dataSource.dbms instanceof GXDBMSinformix) && Thread.currentThread().hashCode() != con.getThread())
				{
					continue;	
				}

				if	(con.getHandle() == handle)
				{
					if	(DEBUG)
					{
						log(handle, "Reusing pre-assigned connection " + con.hashCode());
					}

					if	(sticky)
						con.getPoolState().setInAssignment(true);

					return con;
				}
			}
		}

		return null;
	}

	boolean isReadOnly()
	{
		return false;
	}

	boolean isEnabled()
	{
		return dataSource.getRWPoolEnabled();
	}
}
