package com.genexus.management;

import com.genexus.db.*;
import java.util.*;

public class LocalUserInformationJMX implements LocalUserInformationJMXMBean{

  LocalUserInformation localUserInfo;

  public LocalUserInformationJMX(LocalUserInformation localUserInfo)
  {
    this.localUserInfo = localUserInfo;
  }

  static public void CreateLocalUserInformationJMX(LocalUserInformation localUserInfo)
  {
    try
    {
      MBeanUtils.createMBean(localUserInfo);
    }
    catch(Exception e)
    {
      System.err.println("Cannot register User Information MBean."+e.toString());
    }
  }
  
  static public void DestroyLocalUserInformationJMX(LocalUserInformation localUserInfo)
  {
    try
    {
      MBeanUtils.destroyMBean(localUserInfo);
    }
    catch(Exception e)
    {
      System.err.println("Cannot destroy User Information MBean."+e.toString());
    }
  }  

  public int getId()
  {
	  return localUserInfo.getHandle();
  }
    
  public String getLastSQLStatement()
  {
	  return localUserInfo.getLastSQL();
  }
  
	public String getLastObject()
	{
		return localUserInfo.getLastObjectExecuted();
	}   
  
  public Date getLastSQLStatementTime()
  {
	  return localUserInfo.getLastSQLDateTime();
  }  
  
  public boolean getWaitingForConnection()
  {
	  return localUserInfo.getWaitingForConnection();
  }
  
  public Date getWaitingForConnectionTime()
  {
	  return localUserInfo.getWaitingForConnectionSince();
  }
  
  public int getLastConnectionId()
  {
	  return localUserInfo.getLastConnectionUsed();
  }  
  
  public void disconnect()
  {
	  localUserInfo.disconnectUser();
  }  
}
