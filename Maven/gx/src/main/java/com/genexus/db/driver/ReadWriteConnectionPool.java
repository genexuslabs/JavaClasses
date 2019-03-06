// $Log: ReadWriteConnectionPool.java,v $
// Revision 1.4  2005/07/21 15:10:58  iroqueta
// Implementacion de soporte de JMX
//
// Revision 1.3  2005/02/18 21:19:27  iroqueta
// Le hago llegar el ModelContext al GXConnection para poder pasarselo al proc que se llama para obtener el nombre del datasource al cual conectarse.
//
// Revision 1.2  2003/05/07 16:03:17  aaguiar
// - Cambio menor en la IsAvailable
//
// Revision 1.1.1.1  2001/11/16 13:51:52  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2001/11/16 13:51:52  gusbro
// GeneXus Java Olimar
//

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
