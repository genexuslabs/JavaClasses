// $Log: ServerConnectionManager.java,v $
// Revision 1.1  2000/12/27 15:15:52  gusbro
// Initial revision
//
// Revision 1.1.1.1  2000/12/27 15:15:52  gusbro
// GeneXus Java Olimar
//
//
//   Rev 1.4   23 Sep 1998 19:48:26   AAGUIAR
//
//   Rev 1.3   10 Aug 1998 16:08:34   AAGUIAR
//- No se mata la conexi�n si est� ejecutando en el cliente.
//- Se cambiaron \n por System.getProperty("line.separator");
//
//   Rev 1.2   Jun 29 1998 14:29:30   AAGUIAR
//	-	Se hace un throw cuando hay errores de conexion.
//	-	Se trajo el metodo registerDriver y getNewConnection
//		del Application.java
//	-	Se optimizaron algunos loops
//
//   Rev 1.1   Jun 02 1998 08:55:52   AAGUIAR
//	-	Se cambiaron llamadas a m�todos que leian 
//		valores de preferences en Application por
//		m�todos que las leen en Preferences.

package com.genexus.db;

import java.sql.SQLException;


public class ServerConnectionManager
{
//	private static final boolean DEBUG       = DebugFlag.DEBUG;


	private volatile static ServerConnectionManager serverConnectionManager;
	private static Object objectLock = new Object();
	//protected Hashtable serverConnections;
	private Object handleLock = new Object();

	private ServerConnectionManager()
	{
		//serverConnections = new Hashtable(); 
	}

	/**
	* M�todo que devuelve la instancia de ServerConnectionManager. 
	*
	* Usa el pattern Double Checked Locking para prevenir lios de concurrencia
	*/

	public static ServerConnectionManager getInstance()
	{
		ServerConnectionManager manager = serverConnectionManager;
		if	(manager == null)
		{
			synchronized(objectLock)
			{
				if	(serverConnectionManager == null)
					manager = serverConnectionManager = new ServerConnectionManager();
			}
		}

		return manager;
	}
/*
	public Enumeration getServerConnections()
	{
		return userConnections.elements();
	}
*/
	public int getNewHandle(Namespace namespace, int orb, String IP, boolean autoDisconnect)
	{
		synchronized (handleLock)
		{
			ServerUserInformation sui = (ServerUserInformation) DBConnectionManager.getInstance().createUserInformation(namespace);

			sui.setProtocol((byte) orb);
			sui.setIP(IP);
			sui.setAutoDisconnect(autoDisconnect);

			return sui.getHandle();
		}
	}

	public synchronized ServerUserInformation getUserInformation(int handle)
	{
		return (ServerUserInformation) DBConnectionManager.getInstance().getUserInformation(handle);
	}

	public synchronized void connect(int handle, String dataSource)
	{
		ServerUserInformation userInfo = getUserInformation(handle);

		if	(userInfo == null)
		{
			System.err.println("Warning! Unknow handle requested a connect operation");
			return;
		}

		//userInfo.setDefaultUserAndPassword(dataSource);
	}
							
	public synchronized void connect(int handle, String dataSource, String user, String password) 
	{
		ServerUserInformation userInfo = getUserInformation(handle);

		if	(userInfo == null)
		{
			System.err.println("Warning! Unknow handle requested a connect operation");
			return;
		}

//		userInfo.user 	  = user;
//		userInfo.password = password;

		userInfo.setUserAndPassword(dataSource, user, password);
	}

	public void disconnect(int handle)
	{
		ServerUserInformation userInfo = getUserInformation(handle);

		if	(userInfo == null)
		{
			System.err.println("Warning! Unconnected handle requested a disconnect operation");
			return;
		}

		try
		{
			DBConnectionManager.getInstance().disconnect(handle);
		}
		catch (SQLException e)
		{}
 	}

	public synchronized void keepAlive(int handle)
	{
		ServerUserInformation userInfo = getUserInformation(handle);

		if	(userInfo == null)
		{
			System.err.println("Warning! Unconnected handle requested a keepAplive operation " + handle);
			return;
		}

		userInfo.setTimestamp();
	}
}

