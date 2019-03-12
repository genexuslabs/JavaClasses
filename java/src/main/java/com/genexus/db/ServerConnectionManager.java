
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

