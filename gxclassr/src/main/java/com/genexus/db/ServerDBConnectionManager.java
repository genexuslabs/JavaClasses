
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
