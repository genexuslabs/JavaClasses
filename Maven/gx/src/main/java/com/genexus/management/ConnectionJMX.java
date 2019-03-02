package com.genexus.management;

import java.util.*;

import com.genexus.db.driver.GXConnection;

public class ConnectionJMX implements ConnectionJMXMBean{

  GXConnection connection;

  public ConnectionJMX(GXConnection connection)
  {
    this.connection = connection;
  }

  static public void CreateConnectionJMX(GXConnection connection)
  {
    try
    {
      MBeanUtils.createMBean(connection);
    }
    catch(Exception e)
    {
      System.err.println("Cannot register Connection MBean."+e.toString());
    }
  }
  
  static public void DestroyConnectionJMX(GXConnection connection)
  {
    try
    {
      MBeanUtils.destroyMBean(connection);
    }
    catch(Exception e)
    {
      System.err.println("Cannot destroy Connection MBean."+e.toString());
    }
  }    

	public int getId()
	{
		return connection.getId();
	}
	
	public String getPhysicalId()
	{
		return connection.getDBMSId();
	}
		
	public Date getCreateTime()
	{
		return connection.getTimeCreated();
	}
	
	public Date getLastAssignedTime()
	{
		return connection.getTimeAssigned();
	}
	
	public int getLastUserAssigned()
	{
		return connection.getLastUserAssigned();
	}
	
	public boolean getError()
	{
		return connection.getError();
	}
	
	public boolean getInAssigment()
	{
		return connection.getInAssigment();
	}
	
	public boolean getAvailable()
	{
		return !getInAssigment() && getOpenCursorCount()==0 && !getUncommitedChanges();
	}
	
	public int getOpenCursorCount()
	{
		return connection.getOpenCursorsJMX();
	}
	
	public boolean getUncommitedChanges()
	{
		return connection.getUncommitedChanges();
	}
	
	public int getRequestCount()
	{
		return connection.getNumberRequest();
	}
	
	public Date getLastSQLStatementTime()
	{
		return connection.getTimeLastRequest();
	}
	
	public String getLastSQLStatement()
	{
		return connection.getSentenceLastRequest();
	}
	
	public String getLastObject()
	{
		return connection.getLastObjectExecuted();
	}
	
	public boolean getLastSQLStatementEnded()
	{
		return connection.getFinishExecute();
	}
	
	public void disconnect()
	{
		connection.disconnect();
	}
	
	public void dumpConnectionInformation()
	{
		connection.dump();
	}
}
