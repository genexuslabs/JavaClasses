package com.genexus.management;

public interface ConnectionPoolJMXMBean
{
	int getSize();
	void setSize(int maxPoolSize);
	boolean getUnlimitedSize();
	int getConnectionCount();
	int getFreeConnectionCount();
	int getCreatedConnectionCount();	
	int getRecycledConnectionCount();
	int getDroppedConnectionCount();
	int getRequestCount();
	float getAverageRequestPerSec();
	java.util.Date getLastRequestTime();
	int getWaitedUserCount();
	int getWaitingUserCount();
	long getMaxUserWaitTime();
	float getAverageUserWaitTime();
	boolean getNotificationEnabled();
	void setNotificationEnabled(boolean value);
	long getBeforeNotificationWaitTime();
	void setBeforeNotificationWaitTime(long value);	
	
	void dumpPoolInformation();
	void Recycle();
}
