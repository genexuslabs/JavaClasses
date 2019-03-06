package javapns.notification.transmission;

import java.util.*;

import javapns.devices.*;
import javapns.notification.*;

/**
 * <h1>Pushes a payload to a large number of devices using multiple threads</h1>
 * 
 * <p>The list of devices is spread evenly into multiple {@link javapns.notification.transmission.NotificationThread}s.</p>
 * 
 * <p>Usage: once a NotificationThreads is created, invoke {@code start()} to start all {@link javapns.notification.transmission.NotificationThread} threads.</p>
 * <p>You can provide a {@link javapns.notification.transmission.NotificationProgressListener} to receive events about the work being done.</p>

 * @author Sylvain Pedneault
 */
public class NotificationThreads extends ThreadGroup {

	private List<NotificationThread> threads = new Vector<NotificationThread>();
	private NotificationProgressListener listener;
	private int threadsRunning = 0;
	private Object finishPoint = new Object();


	/**
	 * Create the specified number of notification threads and spread the devices evenly between the threads.
	 * 
	 * @param server the server to push to
	 * @param payload the payload to push
	 * @param devices a very large list of devices
	 * @param numberOfThreads the number of threads to create to share the work
	 */
	public NotificationThreads(AppleNotificationServer server, Payload payload, List<Device> devices, int numberOfThreads) {
		super("javapns notification threads (" + numberOfThreads + " threads)");
		for (List<Device> deviceGroup : groupDevices(devices, numberOfThreads))
			threads.add(new NotificationThread(this, new PushNotificationManager(), server, payload, deviceGroup));
	}


	/**
	 * Create the specified number of notification threads and spread the devices evenly between the threads.
	 * Internally, this constructor uses a AppleNotificationServerBasicImpl to encapsulate the provided keystore, password and production parameters.
	 * 
	 * @param keystore the keystore to use (can be a File, an InputStream, a String for a file path, or a byte[] array)
	 * @param password the keystore's password
	 * @param production true to use Apple's production servers, false to use the sandbox
	 * @param payload the payload to push
	 * @param devices a very large list of devices
	 * @param numberOfThreads the number of threads to create to share the work
	 * @throws Exception 
	 */
	public NotificationThreads(Object keystore, String password, boolean production, Payload payload, List<Device> devices, int numberOfThreads) throws Exception {
		this(new AppleNotificationServerBasicImpl(keystore, password, production), payload, devices, numberOfThreads);
	}


	/**
	 * Spread the devices evenly between the provided threads.
	 * 
	 * @param server the server to push to
	 * @param payload the payload to push
	 * @param devices a very large list of devices
	 * @param threads a list of pre-built threads
	 */
	public NotificationThreads(AppleNotificationServer server, Payload payload, List<Device> devices, List<NotificationThread> threads) {
		super("javapns notification threads (" + threads.size() + " threads)");
		this.threads = threads;
		List<List<Device>> groups = groupDevices(devices, threads.size());
		for (int i = 0; i < groups.size(); i++)
			threads.get(i).setDevices(groups.get(i));
	}


	/**
	 * Spread the devices evenly between the provided threads.
	 * Internally, this constructor uses a AppleNotificationServerBasicImpl to encapsulate the provided keystore, password and production parameters.
	 * 
	 * @param keystore the keystore to use (can be a File, an InputStream, a String for a file path, or a byte[] array)
	 * @param password the keystore's password
	 * @param production true to use Apple's production servers, false to use the sandbox
	 * @param payload the payload to push
	 * @param devices a very large list of devices
	 * @param threads a list of pre-built threads
	 * @throws Exception 
	 */
	public NotificationThreads(Object keystore, String password, boolean production, Payload payload, List<Device> devices, List<NotificationThread> threads) throws Exception {
		this(new AppleNotificationServerBasicImpl(keystore, password, production), payload, devices, threads);
	}


	/**
	 * Use the provided threads which should already each have their group of devices to work with.
	 * 
	 * @param server the server to push to
	 * @param payload the payload to push
	 * @param threads a list of pre-built threads
	 */
	public NotificationThreads(AppleNotificationServer server, Payload payload, List<NotificationThread> threads) {
		super("javapns notification threads (" + threads.size() + " threads)");
		this.threads = threads;
	}


	/**
	 * Use the provided threads which should already each have their group of devices to work with.
	 * Internally, this constructor uses a AppleNotificationServerBasicImpl to encapsulate the provided keystore, password and production parameters.
	 * 
	 * @param keystore the keystore to use (can be a File, an InputStream, a String for a file path, or a byte[] array)
	 * @param password the keystore's password
	 * @param production true to use Apple's production servers, false to use the sandbox
	 * @param payload the payload to push
	 * @param threads a list of pre-built threads
	 * @throws Exception 
	 */
	public NotificationThreads(Object keystore, String password, boolean production, Payload payload, List<NotificationThread> threads) throws Exception {
		this(new AppleNotificationServerBasicImpl(keystore, password, production), payload, threads);
	}


	/**
	 * Create group of devices ready to be dispatched to worker threads.
	 * 
	 * @param devices a large list of devices
	 * @param threads the number of threads to group devices for
	 * @return
	 */
	private static List<List<Device>> groupDevices(List<Device> devices, int threads) {
		List<List<Device>> groups = new Vector<List<Device>>(threads);
		int total = devices.size();
		int devicesPerThread = (total / threads);
		if (total % threads > 0) devicesPerThread++;
		//System.out.println("Making "+threads+" groups of "+devicesPerThread+" devices out of "+total+" devices in total");
		for (int i = 0; i < threads; i++) {
			int firstDevice = i * devicesPerThread;
			if (firstDevice >= total) break;
			int lastDevice = firstDevice + devicesPerThread - 1;
			if (lastDevice >= total) lastDevice = total - 1;
			lastDevice++;
			//System.out.println("Grouping together "+(lastDevice-firstDevice)+" devices (#"+firstDevice+" to "+lastDevice+")");
			List<Device> threadDevices = devices.subList(firstDevice, lastDevice);
			groups.add(threadDevices);
		}
		return groups;
	}


	/**
	 * Start all notification threads.
	 * 
	 * This method returns immediately, as all threads start working on their own.
	 * To wait until all threads are finished, use the waitForAllThreads() method.
	 */
	public final synchronized NotificationThreads start() {
		if (threadsRunning > 0) throw new IllegalStateException("NotificationThreads already started (" + threadsRunning + " still running)");
		assignThreadsNumbers();
		for (NotificationThread thread : threads) {
			threadsRunning++;
			thread.start();
		}
		if (listener != null) listener.eventAllThreadsStarted(this);
		return this;
	}


	/**
	 * Configure in all threads the maximum number of notifications per connection.
	 * 
	 * As soon as a thread reaches that maximum, it will automatically close the connection,
	 * initialize a new connection and continue pushing more notifications.
	 * 
	 * @param notifications the maximum number of notifications that threads will push in a single connection (default is 200)
	 */
	public void setMaxNotificationsPerConnection(int notifications) {
		for (NotificationThread thread : threads)
			thread.setMaxNotificationsPerConnection(notifications);
	}


	/**
	 * Configure in all threads the number of milliseconds that threads should wait between each notification.
	 * 
	 * This feature is intended to alleviate intense resource usage that can occur when
	 * sending large quantities of notifications very quickly.

	 * @param milliseconds the number of milliseconds threads should sleep between individual notifications (default is 0)
	 */
	public void setSleepBetweenNotifications(long milliseconds) {
		for (NotificationThread thread : threads)
			thread.setSleepBetweenNotifications(milliseconds);
	}


	/**
	 * Get a list of threads created to push notifications.
	 * 
	 * @return a list of threads
	 */
	public List<NotificationThread> getThreads() {
		return threads;
	}


	/**
	 * Get the progress listener, if any is attached.
	 * @return a progress listener
	 */
	public NotificationProgressListener getListener() {
		return listener;
	}


	/**
	 * Attach an event listener to this object as well as all linked threads.
	 * 
	 * @param listener
	 */
	public void setListener(NotificationProgressListener listener) {
		this.listener = listener;
		for (NotificationThread thread : threads)
			thread.setListener(listener);
	}


	/**
	 * Worker threads invoke this method as soon as they have completed their work.
	 * This method tracks the number of threads still running, allowing us
	 * to detect when ALL threads have finished.
	 * 
	 * When all threads are done working, this method fires an AllThreadsFinished
	 * event to the attached listener (if one is present) and wakes up any
	 * object that is waiting for the waitForAllThreads() method to return.
	 * 
	 * @param notificationThread
	 */
	protected void threadFinished(NotificationThread notificationThread) {
		threadsRunning--;
		if (threadsRunning == 0) {
			if (listener != null) listener.eventAllThreadsFinished(this);
			try {
				synchronized (finishPoint) {
					finishPoint.notifyAll();
				}
			} catch (Exception e) {
			}
		}
	}


	/**
	 * Wait for all threads to complete their work.
	 * 
	 * This method blocks and returns only when all threads are done.
	 * 
	 * @throws InterruptedException
	 */
	public void waitForAllThreads() throws InterruptedException {
		try {
			synchronized (finishPoint) {
				finishPoint.wait();
			}
		} catch (IllegalMonitorStateException e) {
			/* All threads are most likely already done, so we ignore this */
		}
	}


	/**
	 * Assign unique numbers to worker threads.
	 * Thread numbers allow each thread to generate message identifiers that
	 * are unique to all threads in the group.
	 */
	private void assignThreadsNumbers() {
		int t = 1;
		for (NotificationThread thread : threads)
			thread.setThreadNumber(t++);
	}


	/**
	 * Get a list of all notifications pushed by all threads.
	 * 
	 * @return a list of pushed notifications
	 */
	public List<PushedNotification> getPushedNotifications() {
		int capacity = 0;
		for (NotificationThread thread : threads)
			capacity += thread.getPushedNotifications().size();
		List<PushedNotification> all = new Vector<PushedNotification>(capacity);
		for (NotificationThread thread : threads)
			all.addAll(thread.getPushedNotifications());
		return all;
	}


	/**
	 * Get a list of all notifications that all threads attempted to push but that failed.
	 * 
	 * @return a list of failed notifications
	 */
	public List<PushedNotification> getFailedNotifications() {
		return PushedNotification.findFailedNotifications(getPushedNotifications());
	}


	/**
	 * Get a list of all notifications that all threads attempted to push and succeeded.
	 * 
	 * @return a list of successful notifications
	 */
	public List<PushedNotification> getSuccessfulNotifications() {
		return PushedNotification.findSuccessfulNotifications(getPushedNotifications());
	}

}
