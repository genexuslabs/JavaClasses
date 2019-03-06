// $Log: LocalUserInformation.java,v $
// Revision 1.5  2005/08/03 18:57:46  iroqueta
// Agrego soporte para poder desconectar un usuario por medio de JMX
//
// Revision 1.4  2005/05/04 21:46:31  iroqueta
// Hago llegar el contexto al constructor del GXConnection
//
// Revision 1.3  2005/02/16 19:35:43  iroqueta
// Paso el LoginInServer al datastore, antes estaba en el namespace
//
// Revision 1.2  2003/04/30 22:36:19  gusbro
// - Cambios para soportar el tipo de datos DBConnection en GX
//
// Revision 1.1.1.1  2002/05/08 19:11:32  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2002/05/08 19:11:32  gusbro
// GeneXus Java Olimar
//
package com.genexus.db;

import java.sql.SQLException;
import java.util.Enumeration;

import com.genexus.Application;
import com.genexus.ClientPreferences;
import com.genexus.CommonUtil;
import com.genexus.LocalUtil;
import com.genexus.Messages;
import com.genexus.ModelContext;
import com.genexus.db.driver.DataSource;
import com.genexus.db.driver.GXConnection;

public class LocalUserInformation extends UserInformation
{
	public LocalUserInformation(Namespace namespace)
	{
		super(namespace);

		ClientPreferences preferences = new ClientPreferences(namespace.iniFile);

		setLocalUtil(   preferences.getDECIMAL_POINT(),
						preferences.getDATE_FMT(),
						preferences.getTIME_FMT(),
						preferences.getYEAR_LIMIT(),
						preferences.getLANGUAGE());
	}
/*
	public LocalUserInformation(ModelContext context, Namespace namespace)
	{
		super(context, namespace);
	}
*/
/*
	public void setLocalUtil(char decimalPoint, String dateFormat, String timeFormat, int firstYear2K, String language)
	{
		localUtil = LocalUtil.getLocalUtil(decimalPoint, dateFormat, timeFormat, firstYear2K, language);
		messages  = localUtil.getMessages();
	}
*/
	public GXConnection getConnection(ModelContext context, String dataSourceName, String user, String password) throws SQLException
	{
		ConnectionInformation info = (ConnectionInformation) connections.get(dataSourceName);

		if	(info==null || info.rwConnection == null)
		{
			info = new ConnectionInformation();
			info.user = user;
			info.password = password;
			connections.put(dataSourceName, info);
			
			info.rwConnection = new GXConnection(context, info.user, info.password, namespace.getDataSource(dataSourceName));

		}

		return (GXConnection) info.rwConnection;
	}

	public boolean isConnected(String dataSourceName)
	{
		ConnectionInformation info = (ConnectionInformation) connections.get(dataSourceName);
		return info != null && info.rwConnection != null;
	}

	public GXConnection getConnection(ModelContext context, String dataSourceName) throws SQLException
	{
		ConnectionInformation info = (ConnectionInformation) connections.get(dataSourceName);
		boolean isClosed = false;
		try{
			if (info!=null && info.rwConnection != null && !((GXConnection) info.rwConnection).getError())
				isClosed = ((GXConnection) info.rwConnection).isClosed();
		}catch(SQLException ex)
		{
			System.err.println("Connection closed " + ex.getMessage());
			isClosed = true;
		}
		if	(info==null || info.rwConnection == null || ((GXConnection) info.rwConnection).getError() || isClosed)
		{
			info = new ConnectionInformation();
			connections.put(dataSourceName, info);

			createConnection(context, namespace.getDataSource(dataSourceName), info);
		}

		return (GXConnection) info.rwConnection;
	}
/*
	protected void putConnection(String dataSourceName, GXConnection rwConnection, GXConnection roConnection, String user, String password)
	{
		connections.put(dataSourceName, new ConnectionInformation(rwConnection, roConnection, user, password));
	}
*/
	public void disconnect() throws SQLException
	{
		for (Enumeration<ConnectionInformation> en = connections.elements(); en.hasMoreElements(); )
		{
			ConnectionInformation info = en.nextElement();

			if	(info.rwConnection != null)
			{
				try
				{
					info.rwConnection.close();
				}
				catch (SQLException e)
				{
					System.err.println("Error while closing a db connection " + e.getMessage());
				}
				finally
				{
					// Si no pongo esto en null siempre, si da un error al desconectarse despues
					// quiere volver a usar esta conexion. Es mejor que quede la otra conexion
					// en banda que que cancele le aplicacion...
					info.rwConnection = null;
				}
			}
		}
	}

	public void disconnectOnException() throws SQLException
	{
		disconnect();
	}

	private boolean setUserAndPassword(DataSource dataSource, String[] user, String[] password)
	{
		if	(dataSource!=null && dataSource.loginInServer)
		{
			user[0]     = dataSource.defaultUser;
			password[0] = dataSource.defaultPassword;
			return true;
		}

		return false;
	}




	private void createConnection(ModelContext context, DataSource dataSource, ConnectionInformation info) throws SQLException
	{
		String[] user	  = new String[1];
		String[] password = new String[1];

		int retries = 0;
		boolean ui = com.genexus.ApplicationContext.getInstance().isMsgsToUI();

		while (info.rwConnection == null && retries++ < 3 )
		{
			if	(setUserAndPassword(dataSource, user, password))
			{
				try
				{
					info.password 	  = password[0];
					info.user     	  = user[0];
					info.rwConnection = new GXConnection(context, handle, info.user, info.password, dataSource, info);
				}
				catch (SQLException e)
				{
					throw e;
				}
			}
			else
			{
				if	(ui)
					Application.exit();
			}

			if	(!ui)
				break;
		}

		if	(ui)
		{
			if	(retries > 3)
			{
			 	CommonUtil.msg(new java.awt.Frame(), "Can't connect to to database. Exiting ...");
			 	Application.exit();
			}
		}
	}


	public String getUser(String dataSource)
	{
		return ((ConnectionInformation) connections.get(dataSource)).user;
	}

	public String getPassword(String dataSource)
	{
		return ((ConnectionInformation) connections.get(dataSource)).password;
	}

	public LocalUtil getLocalUtil()
	{
		return localUtil;
	}

	public Messages getMessages()
	{
		return messages;
	}

	public Namespace getNamespace()
	{
		return namespace;
	}

	public void dropAllCursors()
	{
		for (Enumeration<ConnectionInformation> en = connections.elements(); en.hasMoreElements(); )
		{
			ConnectionInformation info = en.nextElement();

			if	(info.rwConnection != null)
				info.rwConnection.dropAllCursors();

			if	(info.roConnection != null)
				info.rwConnection.dropAllCursors();
		}
	}
	
	public void disconnectUser()
	{
	  try
	  {
		DBConnectionManager.getInstance().disconnect(getHandle());
	  }
	  catch(java.sql.SQLException e)
	  {
		  System.out.println(e.toString());
	  }
	}	
}
