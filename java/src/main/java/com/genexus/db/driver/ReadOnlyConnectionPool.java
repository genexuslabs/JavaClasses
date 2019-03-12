

package com.genexus.db.driver;
import java.sql.SQLException;

import com.genexus.ModelContext;

final class ReadOnlyConnectionPool extends ConnectionPool
{
	int maxUsers;

	ReadOnlyConnectionPool(DataSource dataSource, String user, String password)
	{
		super(dataSource, user, password);

		maxPoolSize   		   = dataSource.roPoolSize;
		unlimitedSize 		   = dataSource.roPoolSizeUnlimited;
		maxUsers	  		   = dataSource.roPoolUsers;
		recycleConnections 	   = dataSource.getROPoolRecycle();
		recycleConnectionsTime = ((long) dataSource.getROPoolRecycleMins()) * 60 * 1000;

		if	(DEBUG)
		{
			log(0, "Creating read-only pool size: " + maxPoolSize + " unlimited: " + unlimitedSize + " max users: " + maxUsers + " recycle " + recycleConnections + " recycle time " + recycleConnectionsTime + " max cursors : " + dataSource.maxCursors);
		}
		poolName	  = "R/O Pool";
		createPoolStartup();
	}

	boolean useSameConnectionForSameHandle()
	{
		return false;
	}

	boolean isAvailableJMX(GXConnection con)
	{
		ConnectionPoolState state = con.getPoolState();

		if (state.getOpenCursorsJMX() >= dataSource.maxCursors)
		{
			return false;
		}

		// Si me quedan usuarios, o el usuario ya esta en esta conexion
		return (maxUsers == -1 || state.getUserCount() < maxUsers);
	}	
	
	boolean isAvailable(GXConnection con , int handle)
	{
		ConnectionPoolState state = con.getPoolState();

		if (state.getOpenCursors() >= dataSource.maxCursors)
		{
			return false;
		}

		// Si me quedan usuarios, o el usuario ya esta en esta conexion
		return (maxUsers == -1 || state.getUserCount() < maxUsers || constate.hasConnection(handle, con));
	}

	GXConnection createConnection(ModelContext context, int handle) throws SQLException
	{
		GXConnection con   = new GXConnection(context, this, handle, user, password, dataSource);

		if	(con.getDBMS().useReadOnlyConnections())
		{
			try
			{
				con.setReadOnly(true);
			}
			catch (SQLException e)
			{
				if	(DEBUG)
					dataSource.getLog().logComment(handle, "The driver doesn't support Read Only connection - using Read/Write connection");
			}
		}

		//con.setReadOnlyPool();
		return con;
	}

	GXConnection getSameConnection(int handle, boolean sticky)
	{
		return null;
	}

	boolean isReadOnly()
	{
		return true;
	}

	boolean isEnabled()
	{
		return dataSource.getROPoolEnabled();
	}
}

