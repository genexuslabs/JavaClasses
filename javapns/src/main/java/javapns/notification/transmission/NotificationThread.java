package javapns.notification.transmission;

import java.util.*;

import javapns.devices.*;
import javapns.notification.*;

/**
 * <h1>Pushes a payload to a large number of devices in a single separate thread</h1>
 * 
 * <p>No more than {@code maxNotificationsPerConnection} are pushed over a single connection.
 * When that maximum is reached, the connection is restarted automatically and push continues.
 * This is intended to avoid an undocumented notification-per-connection limit observed 
 * occasionnally with Apple servers.</p>
 * 
 * <p>Usage: once a NotificationThread is created, invoke {@code start()} to push the payload to all devices in a separate thread.</p>
 * 
 * <p>To run this code unthreaded, invoke {@code run()} directly (rarely used).</p>
 * 
 * @author Sylvain Pedneault
 */
public class NotificationThread extends Thread {

	private static final int DEFAULT_MAXNOTIFICATIONSPERCONNECTION = 200;

	private PushNotificationManager notificationManager;
	private AppleNotificationServer server;
	private Payload payload;
	private List<Device> devices;
	private int maxNotificationsPerConnection = DEFAULT_MAXNOTIFICATIONSPERCONNECTION;
	private long sleepBetweenNotifications = 0;
	private NotificationProgressListener listener;
	private int threadNumber = 1;
	private int nextMessageIdentifier = 1;
	private List<PushedNotification> notifications = new Vector<PushedNotification>();


	/**
	 * Create a grouped thread for pushing notifications to a list of devices
	 * and coordinating with a parent NotificationThreads object.
	 * 
	 * @param threads
	 * @param notificationManager
	 * @param server
	 * @param payload
	 * @param devices
	 */
	public NotificationThread(NotificationThreads threads, PushNotificationManager notificationManager, AppleNotificationServer server, Payload payload, List<Device> devices) {
		super(threads, "javapns notification thread (" + devices.size() + ")");
		this.notificationManager = notificationManager == null ? new PushNotificationManager() : notificationManager;
		this.server = server;
		this.payload = payload;
		this.devices = devices;
	}


	/**
	 * Create a grouped thread for pushing notifications to an array of devices
	 * and coordinating with a parent NotificationThreads object.
	 * 
	 * @param threads
	 * @param notificationManager
	 * @param server
	 * @param payload
	 * @param devices
	 */
	public NotificationThread(NotificationThreads threads, PushNotificationManager notificationManager, AppleNotificationServer server, Payload payload, Device... devices) {
		this(threads, notificationManager, server, payload, Arrays.asList(devices));
	}


	/**
	 * Create a standalone thread for pushing notifications to a list of devices.
	 * 
	 * @param notificationManager
	 * @param server
	 * @param payload
	 * @param devices
	 */
	public NotificationThread(PushNotificationManager notificationManager, AppleNotificationServer server, Payload payload, List<Device> devices) {
		this(null, notificationManager, server, payload, devices);
	}


	/**
	 * Create a standalone thread for pushing notifications to an array of devices.
	 * 
	 * @param notificationManager
	 * @param server
	 * @param payload
	 * @param devices
	 */
	public NotificationThread(PushNotificationManager notificationManager, AppleNotificationServer server, Payload payload, Device... devices) {
		this(notificationManager, server, payload, Arrays.asList(devices));
	}


	public void run() {
		int total = devices.size();
		if (listener != null) listener.eventThreadStarted(this);
		try {
			notificationManager.initializeConnection(server);
			for (int i = 0; i < total; i++) {
				Device device = devices.get(i);
				int message = newMessageIdentifier();
				PushedNotification notification = notificationManager.sendNotification(device, payload, false, message);
				notifications.add(notification);
				if (sleepBetweenNotifications > 0) sleep(sleepBetweenNotifications);
				if (i != 0 && i % maxNotificationsPerConnection == 0) {
					if (listener != null) listener.eventConnectionRestarted(this);
					notificationManager.restartConnection(server);
				}
			}
			notificationManager.stopConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (listener != null) listener.eventThreadFinished(this);
		/* Also notify the parent NotificationThreads, so that it can determine when all threads have finished working */
		if (getThreadGroup() instanceof NotificationThreads) ((NotificationThreads) getThreadGroup()).threadFinished(this);
	}


	/**
	 * Set a maximum number of notifications that should be streamed over a continuous connection
	 * to an Apple server.  When that maximum is reached, the thread automatically closes and
	 * reopens a fresh new connection to the server and continues streaming notifications.
	 * 
	 * Default is 200 (recommended).
	 * 
	 * @param maxNotificationsPerConnection
	 */
	public void setMaxNotificationsPerConnection(int maxNotificationsPerConnection) {
		this.maxNotificationsPerConnection = maxNotificationsPerConnection;
	}


	public int getMaxNotificationsPerConnection() {
		return maxNotificationsPerConnection;
	}


	/**
	 * Set a delay the thread should sleep between each notification.
	 * This is sometimes useful when communication with Apple servers is
	 * unreliable and notifications are streaming too fast.
	 * 
	 * Default is 0.
	 * 
	 * @param milliseconds
	 */
	public void setSleepBetweenNotifications(long milliseconds) {
		this.sleepBetweenNotifications = milliseconds;
	}


	public long getSleepBetweenNotifications() {
		return sleepBetweenNotifications;
	}


	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}


	public List<Device> getDevices() {
		return devices;
	}


	/**
	 * Get the number of devices that this thread pushes to.
	 * 
	 * @return the number of devices registered with this thread
	 */
	public int size() {
		return devices.size();
	}


	/**
	 * Provide an event listener which will be notified of this thread's progress.
	 * 
	 * @param listener any object implementing the NotificationProgressListener interface
	 */
	public void setListener(NotificationProgressListener listener) {
		this.listener = listener;
	}


	public NotificationProgressListener getListener() {
		return listener;
	}


	/**
	 * Set the thread number so that generated message identifiers can be made 
	 * unique across all threads.
	 * 
	 * @param threadNumber
	 */
	protected void setThreadNumber(int threadNumber) {
		this.threadNumber = threadNumber;
	}


	/**
	 * Return the thread number assigned by the parent NotificationThreads object, if any.
	 * 
	 * @return the unique number assigned to this thread by the parent group
	 */
	public int getThreadNumber() {
		return threadNumber;
	}


	/**
	 * Return a new sequential message identifier.
	 * 
	 * @return a message identifier unique to all NotificationThread objects
	 */
	public int newMessageIdentifier() {
		return (threadNumber << 24) | nextMessageIdentifier++;
	}


	/**
	 * Returns the first message identifier generated by this thread.
	 * 
	 * @return a message identifier unique to all NotificationThread objects
	 */
	public int getFirstMessageIdentifier() {
		return (threadNumber << 24) | 1;
	}


	/**
	 * Returns the last message identifier generated by this thread.
	 * 
	 * @return a message identifier unique to all NotificationThread objects
	 */
	public int getLastMessageIdentifier() {
		return (threadNumber << 24) | devices.size();
	}


	/**
	 * Returns list of all notifications pushed by this thread (successful or not).
	 * 
	 * @return a list of pushed notifications
	 */
	public List<PushedNotification> getPushedNotifications() {
		return notifications;
	}


	/**
	 * Returns list of all notifications that this thread attempted to push but that failed.
	 * 
	 * @return a list of failed notifications
	 */
	public List<PushedNotification> getFailedNotifications() {
		return PushedNotification.findFailedNotifications(getPushedNotifications());
	}


	/**
	 * Returns list of all notifications that this thread attempted to push and succeeded.
	 * 
	 * @return a list of failed notifications
	 */
	public List<PushedNotification> getSuccessfulNotifications() {
		return PushedNotification.findSuccessfulNotifications(getPushedNotifications());
	}

}
