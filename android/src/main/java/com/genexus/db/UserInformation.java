package com.genexus.db;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

import com.genexus.Application;
import com.genexus.LocalUtil;
import com.genexus.Messages;
import com.genexus.ModelContext;
import com.genexus.common.classes.AbstractUserInformation;
import com.genexus.db.driver.DataSource;
import com.genexus.db.driver.GXConnection;
import com.genexus.internet.HttpContext;

public abstract class UserInformation extends AbstractUserInformation
{
	public abstract void disconnectOnException() throws SQLException;
	public abstract void disconnect() throws SQLException;

	protected   Hashtable  orbHandles  = new Hashtable();
	private   Hashtable  properties  = new Hashtable();
	private   Hashtable  friendUserInformation;
	protected Hashtable  connections = new Hashtable();

	protected Namespace  namespace;
	protected LocalUtil  localUtil;
	protected int 		 handle;
	protected Messages	 messages;
	protected boolean autoDisconnect = true;
	protected long    timestamp;

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

		for	(Enumeration en = namespace.getDataSources(); en.hasMoreElements(); )
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

	public Enumeration getProperties()
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
		ConnectionInformation info = ((ConnectionInformation) connections.get(dataSourceName));

		if	(info == null)
		{
			info = new ConnectionInformation(null, null, user, password);
			connections.put(dataSourceName, info);
		}
		else
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
		String androidLanguage = com.artech.base.services.AndroidContext.ApplicationContext.getLanguageName();
		if (localUtil!=null && androidLanguage!=null)
		{
			String androidLanguageCode = Application.getClientPreferences().getProperty("language|" + androidLanguage, "code", (String)null);
			if (androidLanguageCode!=null && !localUtil._language.equals(androidLanguageCode) ) 
			{
				//	re create localUtil, language has changed.
				System.out.println("change LocalUtil language: " + androidLanguage + "");
				ModelContext tempContext = ModelContext.getModelContext();
				if (tempContext!=null)
				{
					int res = ((HttpContext) tempContext.getHttpContext()).setLanguage(androidLanguage);
					this.setLocalUtil(
							tempContext.getHttpContext().getLanguageProperty("decimal_point").charAt(0),
							tempContext.getHttpContext().getLanguageProperty("date_fmt"),
							tempContext.getHttpContext().getLanguageProperty("time_fmt"),
							tempContext.getClientPreferences().getYEAR_LIMIT(),
							tempContext.getHttpContext().getLanguageProperty("code"));
				}
			}
		}
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

	int remoteGXDBHandle = -1;

	public boolean hasRemoteHandle(int handle)
	{
		for (Enumeration en = orbHandles.keys(); en.hasMoreElements(); )
		{
			String location = (String) en.nextElement();
			if	(handle == ((Integer) orbHandles.get(location)).intValue())
				return true;
		}

		return false;
	}


	public int getFriendUserInformation(Namespace friendNS)
	{
		if	(friendUserInformation == null)
			friendUserInformation = new Hashtable();

		UserInformation friend = (UserInformation) friendUserInformation.get(friendNS.getName());

		if	(friend == null)
		{
			friend = DBConnectionManager.getInstance().createUserInformation(friendNS);
			friendUserInformation.put(friendNS.getName(), friend);
		}

		return friend.getHandle();
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
}

