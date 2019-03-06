// $Log: UtilConnectionManager.java,v $
// Revision 1.2  2004/09/09 18:44:02  iroqueta
// Se implement� el soporte para que las TRNs de los EJBs puedan ser manejadas por el contenedor.
//
// Revision 1.1  2001/07/20 18:39:58  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/07/20 18:39:58  gusbro
// GeneXus Java Olimar
//

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
