// $Log: IConnectionPool.java,v $
// Revision 1.2  2004/09/09 18:44:02  iroqueta
// Se implement� el soporte para que las TRNs de los EJBs puedan ser manejadas por el contenedor.
//
// Revision 1.1  2004/02/13 20:21:28  gusbro
// - Cambios para que en modelos web si se esta usando un datasource del motor
//   de servlets que no se use el pool de conexiones
//

package com.genexus.db.driver;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;

import com.genexus.ModelContext;

public interface IConnectionPool
{
	public Enumeration getROPools();
	public Enumeration getRWPools();
	public ConnectionPool getROConnectionPool(String user);
	public ConnectionPool getRWConnectionPool(String user);
	public Connection checkOut(ModelContext context, DataSource dataSource, int handle, String user, String password, boolean readOnly, boolean sticky) throws SQLException;
	public void disconnectOnException(int handle) throws SQLException;
	public void disconnect(int handle) throws SQLException;
	public void disconnect() throws SQLException;
	public void runWithLock(Runnable runnable);
	public void removeElement(GXConnection con);
}


