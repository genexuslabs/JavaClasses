package com.genexus.management;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

import com.genexus.db.driver.ConnectionPool;

public class ConnectionPoolJMX extends NotificationBroadcasterSupport implements ConnectionPoolJMXMBean{
	
	private long sequenceNumber=0;
  ConnectionPool connectionPool;
  private long lastUserWaitingForLongTimeNotif = 0L;
  private long lastPoollsFullNotif = 0L;

  public ConnectionPoolJMX(ConnectionPool connectionPool)
  {
    this.connectionPool = connectionPool;
	connectionPool.setConnectionPoolJMX(this);
  }

  static public void CreateConnectionPoolJMX(ConnectionPool connectionPool)
  {
    try
    {
      MBeanUtils.createMBean(connectionPool);
    }
    catch(Exception e)
    {
      System.err.println("Cannot register ConnectionPool MBean."+e.toString());
    }
  }

	public int getSize()
	{
		return connectionPool.getMaxPoolSize();
	}
	
	public void setSize(int maxPoolSize)
	{
		connectionPool.setMaxPoolSize(maxPoolSize);
	}
	
	public boolean getUnlimitedSize()
	{
		return connectionPool.getUnlimitedSize();
	}
	
	public int getConnectionCount()
	{
		return connectionPool.getActualPoolSize();
	}
	
	public int getFreeConnectionCount()
	{
		return connectionPool.getFreeConnectionCount();
	}
	
	public int getCreatedConnectionCount()
	{
		return connectionPool.getNumberConnectionsCreated();
	}
	
	public int getRecycledConnectionCount()
	{
		return connectionPool.getNumberConnectionsRecycled();
	}
	
	public int getDroppedConnectionCount()
	{
		return connectionPool.getNumberConnectionsDeleted();
	}
	
	public int getRequestCount()
	{
		return connectionPool.getNumberRequest();
	}
	
	public float getAverageRequestPerSec()
	{
		return connectionPool.getAverageNumberRequest();
	}

	public java.util.Date getLastRequestTime()
	{
		return connectionPool.getTimeLastRequest();
	}
	
	public int getWaitedUserCount()
	{
		return connectionPool.getNumberUsersWaits();
	}
	
	public int getWaitingUserCount()
	{
		return connectionPool.getNumberUsersWaiting();
	}
	
	public long getMaxUserWaitTime()
	{
		return connectionPool.getMaxUserWaitingTime();
	}
	
	public float getAverageUserWaitTime()
	{
		return connectionPool.getAverageUserWaitingTime();
	}
	
	public long getBeforeNotificationWaitTime()
	{
		return connectionPool.getUserMaxTimeWaitingBeforeNotif();
	}
	
	public void setBeforeNotificationWaitTime(long value)
	{
		connectionPool.setUserMaxTimeWaitingBeforeNotif(value);
	}	
	
	public void dumpPoolInformation()
	{
		connectionPool.dumpPoolInformation();
	}
	
	public void Recycle()
	{
		connectionPool.PoolRecycle();
	}
	
		
	//Notification
	public boolean getNotificationEnabled()
	{
		return connectionPool.getEnableNotifications();
	}
	
	public void setNotificationEnabled(boolean value)
	{
		connectionPool.setEnableNotifications(value);
	}
	
	public void PoolIsFull()
	{
		if (System.currentTimeMillis() - lastPoollsFullNotif > 1000L)
		{
			lastPoollsFullNotif = System.currentTimeMillis();
			Notification n = new Notification("com.genexus.managment.fullpool",this,sequenceNumber++,System.currentTimeMillis(),"The Connection Pool does not have available connections "); 
 
			sendNotification(n);
		}
	}
	
	public void UserWaitingForLongTime()
	{
		if (System.currentTimeMillis() - lastUserWaitingForLongTimeNotif > 1000L)
		{
			lastUserWaitingForLongTimeNotif = System.currentTimeMillis();
			Notification n = new Notification("com.genexus.managment.longtimeuserwaiting",this,sequenceNumber++,System.currentTimeMillis(),"User waiting a connection for a long time"); 
 
			sendNotification(n);
		}
	}
	
    public MBeanNotificationInfo[] getNotificationInfo() 
	{ 
        String[] types = new String[] {"com.genexus.managment.fullpool"}; 
        String name = Notification.class.getName(); 
        String description = "The Connection Pool does not have available connections "; 
        MBeanNotificationInfo info = new MBeanNotificationInfo(types, name, description); 
		
		types = new String[] {"com.genexus.managment.longtimeuserwaiting"}; 
		description = "User waiting a connection for a long time"; 
		MBeanNotificationInfo info1 = new MBeanNotificationInfo(types, name, description); 
        
		return new MBeanNotificationInfo[] {info, info1}; 
    }	
}
