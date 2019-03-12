
package com.genexus.db;

import java.sql.SQLException;

import com.genexus.ModelContext;
import com.genexus.ServerPreferences;
import com.genexus.db.driver.GXConnection;

final class UtilConnectionManager extends DBConnectionManager
{
	static
	{
		if	(!com.genexus.ApplicationContext.getInstance().getReorganization())
			ServerPreferences.fileName = "client.cfg";
	}

	UtilConnectionManager()
	{
	}

	public UserInformation getNewUserInformation(Namespace namespace)
	{
		return new UtilUserInformation(namespace);
	}

	public void connect(int handle, String dataSource)
	{
	}

	public boolean isConnected(int handle, String dataSource)
	{
		return true;
	}

	public GXConnection getConnection(ModelContext context, int handle, String dataSource, boolean readOnly, boolean sticky) throws SQLException
	{
		return null;
	}

	public void dropAllCursors(int handle)
	{
		//for (Enumeration en = dataSourceConnections.elements(); en.hasMoreElements(); )
		//	((GXConnection) en.nextElement()).dropAllCursors();
	}

}
