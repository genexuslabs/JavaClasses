

package com.genexus.db;

import java.sql.SQLException;

import com.genexus.ModelContext;
import com.genexus.ServerPreferences;
import com.genexus.db.driver.GXConnection;

final class LocalDBConnectionManager extends DBConnectionManager
{
	static
	{
		if	(!com.genexus.ApplicationContext.getInstance().getReorganization())
			ServerPreferences.fileName = "client.cfg";
	}

	LocalDBConnectionManager()
	{
	}

	public UserInformation getNewUserInformation(Namespace namespace)
	{
		return new LocalUserInformation(namespace);
	}

	public void connect(ModelContext context, int handle, String dataSource) throws SQLException
	{
		((LocalUserInformation) getUserInformation(handle)).getConnection(context, dataSource);
	}

	public boolean isConnected(int handle, String dataSource)
	{
		return ((LocalUserInformation) getUserInformation(handle)).isConnected(dataSource);
	}


	public GXConnection getConnection(ModelContext context, int handle, String dataSource, boolean readOnly, boolean sticky) throws SQLException
	{
		return ((LocalUserInformation) getUserInformation(handle)).getConnection(context, dataSource);
	}

	public void dropAllCursors(int handle)
	{
		((LocalUserInformation) getUserInformation(handle)).dropAllCursors();
	}

}
