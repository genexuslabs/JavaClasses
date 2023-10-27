package com.genexus.management;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import org.apache.logging.log4j.Logger;
import com.genexus.internet.CustomPoolingHttpClientConnectionManager;
public class HTTPPoolJMX extends NotificationBroadcasterSupport implements HTTPPoolJMXMBean{

	private long sequenceNumber=0;
	CustomPoolingHttpClientConnectionManager connectionPool;
	private long lastUserWaitingForLongTimeNotif = 0L;
	private long lastPoollsFullNotif = 0L;
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(HTTPPoolJMX.class);

	public HTTPPoolJMX(CustomPoolingHttpClientConnectionManager connectionPool) {
		this.connectionPool = connectionPool;
	}

	static public void CreateHTTPPoolJMX(CustomPoolingHttpClientConnectionManager httpConnectionPool) {
		try {
			MBeanUtils.createMBean(httpConnectionPool);
		}
		catch(Exception e) {
			log.error("Failed to register HTTP connection pool MBean.", e);
		}
	}

	static public void DestroyHTTPPoolJMX(CustomPoolingHttpClientConnectionManager httpConnectionPool) {
		try {
			MBeanUtils.destroyMBean(httpConnectionPool);
		}
		catch(Exception e) {
			log.error("Failed to destroy HTTP connection pool MBean.", e);
		}
	}

	public int getNumberOfConnectionsInUse(){
		return connectionPool.getTotalStats().getAvailable();
	}

	public int getNumberOfRequestsWaiting(){
		return connectionPool.getTotalStats().getPending();
	}

	public int getNumberOfAvailableConnections(){
		return connectionPool.getTotalStats().getAvailable();
	}

	public int getMaxNumberOfConnections(){
		return connectionPool.getTotalStats().getMax();
	}

	public void PoolIsFull() {
		if (System.currentTimeMillis() - lastPoollsFullNotif > 1000L) {
			lastPoollsFullNotif = System.currentTimeMillis();
			Notification n = new Notification("com.genexus.managment.fullpool",this,sequenceNumber++,System.currentTimeMillis(),"The Connection Pool does not have available connections ");

			sendNotification(n);
		}
	}

	public void UserWaitingForLongTime() {
		if (System.currentTimeMillis() - lastUserWaitingForLongTimeNotif > 1000L)
		{
			lastUserWaitingForLongTimeNotif = System.currentTimeMillis();
			Notification n = new Notification("com.genexus.managment.longtimeuserwaiting",this,sequenceNumber++,System.currentTimeMillis(),"User waiting a connection for a long time");

			sendNotification(n);
		}
	}

	public MBeanNotificationInfo[] getNotificationInfo() {
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
