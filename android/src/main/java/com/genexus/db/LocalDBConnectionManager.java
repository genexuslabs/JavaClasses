// $Log: LocalDBConnectionManager.java,v $
// Revision 1.3  2005/05/04 21:46:31  iroqueta
// Hago llegar el contexto al constructor del GXConnection
//
// Revision 1.2  2004/09/09 18:44:02  iroqueta
// Se implement� el soporte para que las TRNs de los EJBs puedan ser manejadas por el contenedor.
//
// Revision 1.1  2001/07/20 18:39:06  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/07/20 18:39:06  gusbro
// GeneXus Java Olimar
//

package com.genexus.db;

import java.sql.SQLException;

import com.genexus.ModelContext;
import com.genexus.ServerPreferences;
import com.genexus.common.classes.AbstractModelContext;
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


	public GXConnection getConnection(AbstractModelContext context, int handle, String dataSource, boolean readOnly, boolean sticky) throws SQLException
	{
		return ((LocalUserInformation) getUserInformation(handle)).getConnection((ModelContext)context, dataSource);
	}

	public void dropAllCursors(int handle)
	{
		((LocalUserInformation) getUserInformation(handle)).dropAllCursors();
	}

}
