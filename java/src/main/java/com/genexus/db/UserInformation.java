package com.genexus.db;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import com.genexus.LocalUtil;
import com.genexus.Messages;
import com.genexus.ModelContext;
import com.genexus.common.classes.AbstractUserInformation;
import com.genexus.db.driver.DataSource;
import com.genexus.db.driver.GXConnection;


public abstract class UserInformation extends AbstractUserInformation
{
	public abstract void disconnectOnException() throws SQLException;
	public abstract void disconnect() throws SQLException;
	public abstract void flushBuffers() throws SQLException;

	private   	ConcurrentHashMap<String, Integer>  orbHandles  = new ConcurrentHashMap<String, Integer>();
	private   	ConcurrentHashMap<String, String>  properties  = new ConcurrentHashMap<String, String>();
	private   	ConcurrentHashMap<String, UserInformation>  friendUserInformation;
	protected 	ConcurrentHashMap<String, ConnectionInformation>  connections = new ConcurrentHashMap<String, ConnectionInformation>();
	private Object lockObject = new Object();
	protected Namespace  namespace;
	protected LocalUtil  localUtil;
	protected int 		 handle;
	protected Messages	 messages;
	protected boolean autoDisconnect = true;
	protected long    timestamp;
	private int remoteGXDBHandle = -1;


	protected   boolean continueThread = true;
	protected boolean compressData = true;
	protected String  serverUserId;

	//JMX properties
	private String lastSQL;
	private java.util.Date lastSQLDateTime;
	private boolean waiting = false;
	private java.util.Date waitingSince = null;
	private int lastConnectionUsed;
	private String lastObjectExecuted;

	UserInformation()
	{
	}

	public UserInformation(Namespace namespace)
	{
		this.namespace 	  = namespace;

		if	(namespace == null)
		{
			throw new IllegalArgumentException("Null namespace");
		}
		for	(Enumeration<DataSource> en = namespace.getDataSources(); en.hasMoreElements(); )
		{
			DataSource ds = (DataSource) en.nextElement();
			ConnectionInformation info = new ConnectionInformation();
			info.user	  = ds.defaultUser;
			info.password = ds.defaultPassword;
			connections.put(ds.name, info);
		}
	}

	public String getServerUserId()
	{
		return serverUserId;
	}

	public void setServerUserId(String serverUserId)
	{
		this.serverUserId = serverUserId;
	}

	public Enumeration<String> getProperties()
	{
		return properties.keys();
	}

	public String getProperty(String key)
	{
		String val = (String) properties.get(key.toUpperCase());

		if	(val == null)
			return "";

		return val;
	}

	public void setProperty(String key, String value)
	{
		properties.put(key, value);
	}

	public void setHandle(int handle)
	{
		this.handle = handle;
	}

	public boolean getCompressData()
	{
		return compressData;
	}

	public void setCompressData(boolean compressData)
	{
		this.compressData = compressData;
	}

	public void setAutoDisconnect(boolean autoDisconnect)
	{
		this.autoDisconnect = autoDisconnect;
	}
	public boolean getAutodisconnect()
	{
		return autoDisconnect;
	}

	public void setLocalUtil(char decimalPoint, String dateFormat, String timeFormat, int firstYear2K, String language)
	{
		localUtil = LocalUtil.getLocalUtil(decimalPoint, dateFormat, timeFormat, firstYear2K, language);
		messages  = localUtil.getMessages();
	}

	public void setUserAndPassword(String dataSourceName, String user, String password)
	{
		ConnectionInformation info = connections.putIfAbsent(dataSourceName, new ConnectionInformation(null, null, user, password));

		if	(info != null)
		{
			info.user = user;
			info.password = password;
		}
	}

	protected void putConnection(String dataSourceName, GXConnection rwConnection, GXConnection roConnection, String user, String password)
	{
		connections.put(dataSourceName, new ConnectionInformation(rwConnection, roConnection, user, password));
	}

	protected String getUser(String dataSource)
	{
		return ((ConnectionInformation) connections.get(dataSource)).user;
	}

	protected String getPassword(String dataSource)
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

	public synchronized long getTimestamp()
	{
		return timestamp;
	}

	public synchronized void setTimestamp()
	{
		timestamp = System.currentTimeMillis();
	}

	public Namespace getNamespace()
	{
		return namespace;
	}

	public int getHandle()
	{
		return handle;
	}

	public boolean hasRemoteHandle(int handle)
	{
		synchronized (lockObject) {
			for (Enumeration<String> en = orbHandles.keys(); en.hasMoreElements(); )
			{
				String location = en.nextElement();
				if	(handle == ((Integer) orbHandles.get(location)).intValue())
					return true;
			}
			return false;
		}
	}

	public int getFriendUserInformation(Namespace friendNS)
	{
		synchronized (lockObject) {
			if	(friendUserInformation == null)
				friendUserInformation = new ConcurrentHashMap<String, UserInformation>();

			UserInformation friend = friendUserInformation.get(friendNS.getName());
			if	(friend == null)
			{
				friend = DBConnectionManager.getInstance().createUserInformation(friendNS);
				friend = friendUserInformation.putIfAbsent(friendNS.getName(), friend);
			}
			return friend.getHandle();
		}
	}

	public void setLastSQL(String sql)
	{
		this.lastSQL = sql;
		this.lastSQLDateTime = new java.util.Date();
	}

	public void setLastObjectExecuted(String object)
	{
		lastObjectExecuted = object;
	}

	public void setWaitingConnection(boolean waiting)
	{
		if (waiting == true)
			waitingSince = new java.util.Date();
		else
			waitingSince = null;
		this.waiting = waiting;
	}

	public void setLastConnectionUsed(int connectionId)
	{
		this.lastConnectionUsed = connectionId;
	}
	
	com.genexus.GXSmartCacheProvider smartCacheProvider;
	public com.genexus.GXSmartCacheProvider getSmartCacheProvider()
	{
		if (smartCacheProvider == null)
			smartCacheProvider = new com.genexus.GXSmartCacheProvider();
		return smartCacheProvider;
	}	

//////////////////////////////////////////JMX Operations/////////////////////////////////////
	public String getLastSQL()
	{
		return lastSQL;
	}
	
	public String getLastObjectExecuted()
	{
		return lastObjectExecuted;
	}	
	
	public java.util.Date getLastSQLDateTime()
	{
		return lastSQLDateTime;
	}
	
	public boolean getWaitingForConnection()
	{
		return waiting;
	}
	
	public java.util.Date getWaitingForConnectionSince()
	{
		return waitingSince;
	}
	
	public int getLastConnectionUsed()
	{
		return lastConnectionUsed;
	}

	protected void createConnectionBatch(ModelContext context, DBConnection dbc) throws SQLException
	{
		DataSource dataSource = dbc.dataSource;
		short showPrompt = dbc.getShowprompt();
		String user = dataSource.defaultUser;
		String password = dataSource.defaultPassword;

		ConnectionInformation info = new ConnectionInformation();
		info.user = user;
		info.password = password;
		info.rwConnection = new GXConnection(context, info.user, info.password, dataSource);
		connections.put(dataSource.name, info);
	}

	protected void testConnectionBatch(ModelContext context, int handle, DBConnection dbc) throws SQLException
	{
		DataSource dataSource = dbc.dataSource;
		String user = dataSource.defaultUser;
		String password = dataSource.defaultPassword;

		GXConnection con = new GXConnection(context, user, password, dataSource);
		con.getJDBCConnection().close();
		con.close();
	}
}

