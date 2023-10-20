package com.genexus.management;

import java.sql.SQLException;
import java.util.Date;

import com.genexus.db.DBConnectionManager;
import com.genexus.db.ServerUserInformation;

public class ServerUserInformationJMX implements ServerUserInformationJMXMBean{

  ServerUserInformation serverUserInfo;
  com.genexus.ModelContext context;

  public ServerUserInformationJMX(ServerUserInformation serverUserInfo, com.genexus.ModelContext context)
  {
    this.serverUserInfo = serverUserInfo;
    this.context = context;
  }

  static public void CreateServerUserInformationJMX(ServerUserInformation serverUserInfo, com.genexus.ModelContext context)
  {
    try
    {
      MBeanUtils.createMBean(serverUserInfo, context);
    }
    catch(Exception e)
    {
      System.err.println("Cannot register User Information MBean."+e.toString());
    }
  }
  
  static public void DestroyServerUserInformationJMX(ServerUserInformation serverUserInfo)
  {
    try
    {
      MBeanUtils.destroyMBean(serverUserInfo);
    }
    catch(Exception e)
    {
      System.err.println("Cannot destroy User Information MBean."+e.toString());
    }
  }  

  public int getId()
  {
	  return serverUserInfo.getHandle();
  }
  
  public String getIP()
  {
	  return serverUserInfo.getIP();
  }
  
  public Date getConnectedTime()
  {
	  return serverUserInfo.getConnectedSince();
  }
  
  public long getIdleSeconds()
  {
	  return serverUserInfo.getIdleSeconds();
  }
  
  public String getLastSQLStatement()
  {
	  return serverUserInfo.getLastSQL();
  }
  
  public Date getLastSQLStatementTime()
  {
	  return serverUserInfo.getLastSQLDateTime();
  }  
  
	public String getLastObject()
	{
		return serverUserInfo.getLastObjectExecuted();
	}   
  
  public boolean getWaitingForConnection()
  {
	  return serverUserInfo.getWaitingForConnection();
  }
  
  public Date getWaitingForConnectionTime()
  {
	  return serverUserInfo.getWaitingForConnectionSince();
  }
  
  public int getLastConnectionId()
  {
	  return serverUserInfo.getLastConnectionUsed();
  }
  
  public String getUserId()
  {
  	try
	{
		return DBConnectionManager.getInstance().getUserName(context, serverUserInfo.getHandle(), "DEFAULT");
	}
	catch (SQLException ex)
	{
		return "";
	}
  }
  
  public void disconnect()
  {
	  serverUserInfo.disconnectUser();
  }
}
