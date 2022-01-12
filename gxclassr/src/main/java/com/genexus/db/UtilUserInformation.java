package com.genexus.db;

import java.sql.SQLException;

import com.genexus.Application;
import com.genexus.LocalUtil;
import com.genexus.Messages;

//import com.genexus.*;

import com.genexus.db.driver.GXConnection;

public class UtilUserInformation extends UserInformation
{
	public UtilUserInformation(Namespace namespace)
	{
		setLocalUtil(   Application.getClientPreferences().getDECIMAL_POINT(),
						Application.getClientPreferences().getDATE_FMT(),
						Application.getClientPreferences().getTIME_FMT(),
						Application.getClientPreferences().getYEAR_LIMIT(),
						Application.getClientPreferences().getLANGUAGE());

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
	public GXConnection getConnection(String dataSourceName, String user, String password) throws SQLException
	{ 
		return null;
	}

	public GXConnection getConnection(String dataSourceName) 
	{
		return null;
	}
/*
	protected void putConnection(String dataSourceName, GXConnection rwConnection, GXConnection roConnection, String user, String password)
	{
		connections.put(dataSourceName, new ConnectionInformation(rwConnection, roConnection, user, password));
	}
*/
	public void disconnect() throws SQLException
	{
	}
	public void flushBuffers(java.lang.Object o) throws SQLException {
	}
	public void disconnectOnException() throws SQLException
	{
	}

	public String getUser(String dataSource)
	{
		return "";
	}

	public String getPassword(String dataSource)
	{
		return "";
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
}

