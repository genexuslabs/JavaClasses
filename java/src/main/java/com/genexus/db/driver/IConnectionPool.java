
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


