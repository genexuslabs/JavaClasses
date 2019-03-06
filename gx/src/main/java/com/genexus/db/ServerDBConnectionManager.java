// $Log: ServerDBConnectionManager.java,v $
// Revision 1.7  2005/05/26 21:17:09  iroqueta
// En el killConnection hago catch de una exception mas general, para que termine el proceso
//
// Revision 1.6  2004/09/09 18:44:02  iroqueta
// Se implement� el soporte para que las TRNs de los EJBs puedan ser manejadas por el contenedor.
//
// Revision 1.5  2003/09/18 21:16:20  gusbro
// - Cambios para que la ejecucion del proc 'onDisconnect' no sea tenida en cuenta en modelos web
//
// Revision 1.4  2003/09/04 17:51:41  gusbro
// - Atrapo excepcion en onDisconnect
//
// Revision 1.3  2002/11/06 19:33:05  gusbro
// - fix: en el killConnections() el for tenia mal puesta la condicion de fin, ahora se podria sacar el try-catch que lo envuelve
//
// Revision 1.2  2002/09/23 18:02:39  gusbro
// - Al desconectar un usuario se ejecuta el proc indicado en el server.cfg con el entry (OnDisconnect)
//
// Revision 1.1.1.1  2001/10/29 17:16:30  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2001/10/29 17:16:30  gusbro
// GeneXus Java Olimar
//

package com.genexus.db;

import java.sql.SQLException;
import java.util.Enumeration;

import com.genexus.Application;
import com.genexus.ApplicationContext;
import com.genexus.ModelContext;
import com.genexus.ServerPreferences;
import com.genexus.db.driver.GXConnection;

final class ServerDBConnectionManager extends DBConnectionManager
{
	ServerDBConnectionManager(ModelContext context)
	{
		this();
	}

	ServerDBConnectionManager()
	{
		if	(com.genexus.ApplicationContext.getInstance().isApplicationServer())
		{
			try
			{
				new IdleConnectionKiller().start();
			}
			catch (Throwable e)
			{
				System.out.println("Can't start idle connection killer thread");
			}
		}
	}

	public UserInformation getNewUserInformation(Namespace namespace)
	{
		return new ServerUserInformation(namespace);
	}

	public boolean isConnected(int handle, String dataSource)
	{
		return ((ServerUserInformation) getUserInformation(handle)).isConnected(dataSource);
	}

	public GXConnection getConnection(ModelContext context, int handle, String dataSource, boolean readOnly, boolean sticky) throws SQLException
	{
		return ((ServerUserInformation) getUserInformation(handle)).getConnection(context, dataSource, readOnly, sticky);
	}

	public void dropAllCursors(int handle)
	{
	}

 	class IdleConnectionKiller extends Thread
	{
		int i;
		long connectionTimeoutMiliseconds;

		public IdleConnectionKiller ()
		{
			connectionTimeoutMiliseconds = 30000;
		}

		public void run()
		{
			while (true)
			{
				try
				{
					sleep(connectionTimeoutMiliseconds);
				}
				catch (InterruptedException e)
				{
				}

				killConnections();
			}
		}

		private void killConnections()
		{
			long now = System.currentTimeMillis();

			try
			{
				for (Enumeration<?> en = userConnections.elements(); en.hasMoreElements(); )
				{
					ServerUserInformation userInfo = (ServerUserInformation) en.nextElement();

					if (userInfo.getAutodisconnect() && userInfo.getTimestamp() < now - connectionTimeoutMiliseconds)
					{
						try
						{
							disconnect(userInfo.getHandle());
						}
						catch (Exception e)
						{
							System.err.println("Error while disconnecting " + e.getMessage());
						}
					}
				}
			}
			catch (Exception e)
			{
			}
		}
	}

	private static String onDisconnectProcName = "";
	public void disconnect(int handle)throws SQLException, NullPointerException
	{
		UserInformation ui = getUserInformation(handle);
		if(onDisconnectProcName != null && !ApplicationContext.getInstance().isServletEngine())
		{
			try
			{
				if(onDisconnectProcName.equals(""))
				{
					ServerPreferences pref = ServerPreferences.getInstance(com.genexus.Application.class);
					onDisconnectProcName = pref.getOnDisconnectProcName();
				}

				if(ui != null && onDisconnectProcName != null)
				{	// Veo si hay que llamar al proc de desconexi�n
					// El proc de desconexi�n es un proc que toma como parametro un entero (el handle de la conexi�n)
					try
					{
						com.genexus.db.DynamicExecute.dynamicExecute(new ModelContext(ServerDBConnectionManager.class), handle, Application.class, onDisconnectProcName, new Object[]{new Integer(handle)});
					}catch(Throwable e)
					{
						System.err.println("OnDisconnectProc -> ");
						e.printStackTrace();
					}
				}
			}catch(Throwable ex)
			{ // Si no puedo abrir el server.cfg ya no lo intento en el proximo disconnect
				onDisconnectProcName = null;
			}
		}
		super.disconnect(handle);
	}
}
