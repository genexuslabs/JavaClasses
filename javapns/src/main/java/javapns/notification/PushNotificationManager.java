package javapns.notification;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;

import javapns.communication.*;
import javapns.communication.exceptions.*;
import javapns.devices.*;
import javapns.devices.exceptions.*;
import javapns.devices.implementations.basic.*;

import javax.net.ssl.*;

import com.genexus.diagnostics.core.*;
/**
 * The main class used to send notification and handle a connection to Apple SSLServerSocket.
 * This class is not multi-threaded.  One instance per thread must be created.
 *
 * @author Maxime Pilon
 * @author Sylvain Pedneault
 * @author Others...
 */
public class PushNotificationManager {

	/*
	 * Number of milliseconds to use as socket timeout.
	 * Set to -1 to leave the timeout to its default setting.
	 */
	private int sslSocketTimeout = 30 * 1000;

	public static final ILogger logger = LogManager.getLogger(PushNotificationManager.class);

	/* Default retries for a connection */
	private static final int DEFAULT_RETRIES = 3;

	/* Special identifier that tells the manager to generate a sequential identifier for each payload pushed */
	private static final int SEQUENTIAL_IDENTIFIER = -1;

	/* Connection helper */
	private ConnectionToAppleServer connectionToAppleServer;

	/* The always connected SSLSocket */
	private SSLSocket socket;

	/* Default retry attempts */
	private int retryAttempts = DEFAULT_RETRIES;

	private int nextMessageIdentifier = 1;

	/*
	 * To circumvent an issue with invalid server certificates,
	 * set to true to use a trust manager that will always accept
	 * server certificates, regardless of their validity.
	 */
	private boolean trustAllServerCertificates = true;

	private boolean proxySet = false;

	/* The DeviceFactory to use with this PushNotificationManager */
	private DeviceFactory deviceFactory;

	private LinkedHashMap<Integer, PushedNotification> pushedNotifications = new LinkedHashMap<Integer, PushedNotification>();


	/**
	 * Constructs a PushNotificationManager with a default DeviceFactory;
	 * Must allow the device factory to be replaced later, to support IoC.
	 */
	public PushNotificationManager() {
		deviceFactory = new BasicDeviceFactory();
	}


	/**
	 * Constructs a PushNotificationManager using a supplied DeviceFactory
	 * @param deviceManager
	 */
	public PushNotificationManager(DeviceFactory deviceManager) {
		this.deviceFactory = deviceManager;
	}


	/**
	 * Initialize the connection and create a SSLSocket
	 * @param server The Apple Server to connect to.
	 * @throws Exception
	 */
	public void initializeConnection(AppleNotificationServer server) throws Exception {
		this.connectionToAppleServer = new ConnectionToNotificationServer(server);
		this.socket = connectionToAppleServer.getSSLSocket();
		logger.debug("Initialized Connection to Host: [" + server.getNotificationServerHost() + "] Port: [" + server.getNotificationServerPort() + "]: " + socket);
	}


	public void initializePreviousConnection() throws Exception {
		initializeConnection((AppleNotificationServer) this.connectionToAppleServer.getServer());
	}


	public void restartConnection(AppleNotificationServer server) throws Exception {
		stopConnection();
		initializeConnection(server);
	}


	private void restartPreviousConnection() throws Exception {
		try {
			logger.debug("Closing connection to restart previous one");
			this.socket.close();
		} catch (Exception e) {
			/* Do not complain if connection is already closed... */
		}
		initializePreviousConnection();
	}


	/**
	 * Read and process any pending error-responses, and then close the connection.
	 *
	 * @throws IOException
	 */
	public void stopConnection() throws Exception {
		processedFailedNotifications();
		try {
			logger.debug("Closing connection");
			this.socket.close();
		} catch (Exception e) {
			/* Do not complain if connection is already closed... */
		}
	}


	private int processedFailedNotifications() throws Exception {
		logger.debug("Reading responses");
		int responsesReceived = ResponsePacketReader.processResponses(this);
		while (responsesReceived > 0) {
			PushedNotification skippedNotification = null;
			List<PushedNotification> notificationsToResend = new ArrayList<PushedNotification>();
			boolean foundFirstFail = false;
			for (PushedNotification notification : pushedNotifications.values()) {
				if (foundFirstFail || !notification.isSuccessful()) {
					if (foundFirstFail) notificationsToResend.add(notification);
					else {
						foundFirstFail = true;
						skippedNotification = notification;
					}
				}
			}
			pushedNotifications.clear();
			int toResend = notificationsToResend.size();
			logger.debug("Found " + toResend + " notifications that must be re-sent");
			if (toResend > 0) {
				logger.debug("Restarting connection to resend notifications");
				restartPreviousConnection();
				for (PushedNotification pushedNotification : notificationsToResend) {
					sendNotification(pushedNotification, false);
				}
			}
			int remaining = responsesReceived = ResponsePacketReader.processResponses(this);
			if (remaining == 0) {
				logger.debug("No notifications remaining to be resent");
				return 0;
			}
		}
		return responsesReceived;
	}


	/**
	 * Send a notification to a single device and close the connection.
	 *
	 * @param device the device to be notified
	 * @param payload the payload to send
	 * @return a pushed notification with details on transmission result and error (if any)
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */
	public PushedNotification sendNotification(Device device, Payload payload) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, Exception {
		return sendNotification(device, payload, true);
	}


	/**
	 * Send a notification to a multiple devices in a single connection and close the connection.
	 *
	 * @param payload the payload to send
	 * @param devices the device to be notified
	 * @return a list of pushed notifications, each with details on transmission results and error (if any)
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */
	public List<PushedNotification> sendNotifications(Payload payload, List<Device> devices) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, Exception {
		List<PushedNotification> notifications = new Vector<PushedNotification>();
		for (Device device : devices)
			notifications.add(sendNotification(device, payload, false, SEQUENTIAL_IDENTIFIER));
		stopConnection();
		return notifications;
	}


	/**
	 * Send a notification to a multiple devices in a single connection and close the connection.
	 *
	 * @param payload the payload to send
	 * @param devices the device to be notified
	 * @return a list of pushed notifications, each with details on transmission results and error (if any)
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */
	public List<PushedNotification> sendNotifications(Payload payload, Device... devices) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, Exception {
		List<PushedNotification> notifications = new Vector<PushedNotification>();
		for (Device device : devices)
			notifications.add(sendNotification(device, payload, false, SEQUENTIAL_IDENTIFIER));
		stopConnection();
		return notifications;
	}


	/**
	 * Send a notification (Payload) to the given device
	 *
	 * @param device the device to be notified
	 * @param payload the payload to send
	 * @param closeAfter indicates if the connection should be closed after the payload has been sent
	 * @return a pushed notification with details on transmission result and error (if any)
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public PushedNotification sendNotification(Device device, Payload payload, boolean closeAfter) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, Exception {
		return sendNotification(device, payload, closeAfter, SEQUENTIAL_IDENTIFIER);
	}


	/**
	 * Send a notification (Payload) to the given device
	 *
	 * @param device the device to be notified
	 * @param payload the payload to send
	 * @param identifier a unique identifier which will match any error reported later (if any)
	 * @return a pushed notification with details on transmission result and error (if any)
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public PushedNotification sendNotification(Device device, Payload payload, int identifier) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, Exception {
		return sendNotification(device, payload, false, identifier);
	}


	/**
	 * Send a notification (Payload) to the given device
	 *
	 * @param device the device to be notified
	 * @param payload the payload to send
	 * @param closeAfter indicates if the connection should be closed after the payload has been sent
	 * @param identifier a unique identifier which will match any error reported later (if any)
	 * @return a pushed notification with details on transmission result and error (if any)
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @return TransmissionEnvelope an object that encapsulates all message transmission results
	 */
	public PushedNotification sendNotification(Device device, Payload payload, boolean closeAfter, int identifier) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, Exception {
		PushedNotification pushedNotification = new PushedNotification(device, payload, identifier);
		sendNotification(pushedNotification, closeAfter);
		return pushedNotification;
	}


	/**
	 * Actual action of sending a notification
	 *
	 * @param notification the ready-to-push notification
	 * @param closeAfter indicates if the connection should be closed after the payload has been sent
	 * @return a pushed notification with details on transmission result and error (if any)
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @return TransmissionEnvelope an object that encapsulates all message transmission results
	 */
	private void sendNotification(PushedNotification notification, boolean closeAfter) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, Exception {
		try {
			Device device = notification.getDevice();
			Payload payload = notification.getPayload();
			if (notification.getIdentifier() <= 0) notification.setIdentifier(newMessageIdentifier());
			if (!pushedNotifications.containsKey(notification.getIdentifier())) pushedNotifications.put(notification.getIdentifier(), notification);
			int identifier = notification.getIdentifier();

			String token = device.getToken();
			// even though the BasicDevice constructor validates the token, we revalidate it in case we were passed another implementation of Device
			BasicDevice.validateTokenFormat(token);
			//		PushedNotification pushedNotification = new PushedNotification(device, payload);
			byte[] bytes = getMessage(token, payload, identifier, notification);
			//		pushedNotifications.put(pushedNotification.getIdentifier(), pushedNotification);

			/* Special simulation mode to skip actual streaming of message */
			boolean simulationMode = payload.getExpiry() == 919191;

			boolean success = false;

			BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			int socketTimeout = getSslSocketTimeout();
			if (socketTimeout > 0) this.socket.setSoTimeout(socketTimeout);
			notification.setTransmissionAttempts(0);
			// Keep trying until we have a success
			while (!success) {
				try {
					logger.debug("Attempting to send notification: " + payload.toString() + "");
					logger.debug("  to device: " + token + "");
					notification.addTransmissionAttempt();
					try {
						if (!simulationMode) {
							this.socket.getOutputStream().write(bytes);
						} else {
							logger.debug("* Simulation only: would have streamed " + bytes.length + "-bytes message now..");
						}
					} catch (Exception e) {
						if (e != null) {
							if (e.toString().contains("certificate_unknown")) {
								throw new InvalidCertificateChainException(e.getMessage());
							}
						}
						throw e;
					}
					logger.debug("Flushing");
					this.socket.getOutputStream().flush();
					success = true;
					logger.debug("Notification sent on " + notification.getLatestTransmissionAttempt());
					notification.setTransmissionCompleted(true);

				} catch (IOException e) {
					// throw exception if we surpassed the valid number of retry attempts
					if (notification.getTransmissionAttempts() >= retryAttempts) {
						logger.error("Attempt to send Notification failed and beyond the maximum number of attempts permitted");
						notification.setTransmissionCompleted(false);
						notification.setException(e);
						logger.error("Delivery error", e);
						throw e;

					} else {
						logger.info("Attempt failed (" + e.getMessage() + ")... trying again");
						//Try again
						try {
							this.socket.close();
						} catch (Exception e2) {
							// do nothing
						}
						this.socket = connectionToAppleServer.getSSLSocket();
						if (socketTimeout > 0) this.socket.setSoTimeout(socketTimeout);
					}
				}
			}
		} catch (Exception ex) {
			notification.setException(ex);
			logger.error("Delivery error", ex);
			try {
				if (closeAfter) {
					logger.error("Closing connection after error");
					stopConnection();
				}
			} catch (Exception e) {
			}
		}
	}


	/**
	 * Add a device
	 * @param id The device id
	 * @param token The device token
	 * @throws DuplicateDeviceException
	 * @throws NullDeviceTokenException
	 * @throws NullIdException
	 */
	public void addDevice(String id, String token) throws DuplicateDeviceException, NullIdException, NullDeviceTokenException, Exception {
		logger.debug("Adding Token [" + token + "] to Device [" + id + "]");
		deviceFactory.addDevice(id, token);
	}


	/**
	 * Get a device according to his id
	 * @param id The device id
	 * @return The device
	 * @throws UnknownDeviceException
	 * @throws NullIdException
	 */
	public Device getDevice(String id) throws UnknownDeviceException, NullIdException {
		logger.debug("Getting Token from Device [" + id + "]");
		return deviceFactory.getDevice(id);
	}


	/**
	 * Remove a device
	 * @param id The device id
	 * @throws UnknownDeviceException
	 * @throws NullIdException
	 */
	public void removeDevice(String id) throws UnknownDeviceException, NullIdException {
		logger.debug("Removing Token from Device [" + id + "]");
		deviceFactory.removeDevice(id);
	}


	/**
	 * Set the proxy if needed
	 * @param host the proxyHost
	 * @param port the proxyPort
	 */
	public void setProxy(String host, String port) {
		proxySet = true;

		System.setProperty("http.proxyHost", host);
		System.setProperty("http.proxyPort", port);

		System.setProperty("https.proxyHost", host);
		System.setProperty("https.proxyPort", port);
	}


	/**
	 * Compose the Raw Interface that will be sent through the SSLSocket
	 * A notification message is
	 * COMMAND | TOKENLENGTH | DEVICETOKEN | PAYLOADLENGTH | PAYLOAD
	 * NEW!
	 * COMMAND | !Identifier! | !Expiry! | TOKENLENGTH| DEVICETOKEN | PAYLOADLENGTH | PAYLOAD
	 * See page 30 of Apple Push Notification Service Programming Guide
	 * @param deviceToken the deviceToken
	 * @param payload the payload
	 * @param message
	 * @return the byteArray to write to the SSLSocket OutputStream
	 * @throws IOException
	 */
	private byte[] getMessage(String deviceToken, Payload payload, int identifier, PushedNotification message) throws IOException, Exception {
		logger.debug("Building Raw message from deviceToken and payload");

		/* To test with a corrupted or invalid token, uncomment following line*/
		//deviceToken = deviceToken.substring(0,10);

		// First convert the deviceToken (in hexa form) to a binary format
		/*byte[] deviceTokenAsBytes = new byte[deviceToken.length() / 2];
		deviceToken = deviceToken.toUpperCase();
		int j = 0;
		for (int i = 0; i < deviceToken.length(); i += 2) {
			String t = deviceToken.substring(i, i + 2);
			int tmp = Integer.parseInt(t, 16);
			deviceTokenAsBytes[j++] = (byte) tmp;
		}*/

		byte[] deviceTokenAsBytes = com.genexus.util.Codecs.base64Decode(deviceToken.getBytes());

		// Create the ByteArrayOutputStream which will contain the raw interface
		byte[] payloadAsBytes = payload.getPayloadAsBytes();
		int size = (Byte.SIZE / Byte.SIZE) + (Character.SIZE / Byte.SIZE) + deviceTokenAsBytes.length + (Character.SIZE / Byte.SIZE) + payloadAsBytes.length;
		ByteArrayOutputStream bao = new ByteArrayOutputStream(size);

		// Write command to ByteArrayOutputStream
		// 0 = simple
		// 1 = enhanced
		byte b = 1;
		bao.write(b);

		// 4 bytes identifier (which will match any error packet received later on)
		bao.write(intTo4ByteArray(identifier));
		message.setIdentifier(identifier);

		// 4 bytes
		int requestedExpiry = payload.getExpiry();
		if (requestedExpiry <= 0) {
			bao.write(intTo4ByteArray(requestedExpiry));
			message.setExpiry(0);
		} else {
			long ctime = System.currentTimeMillis();
			long ttl = requestedExpiry * 1000; // time-to-live in milliseconds
			Long expiryDateInSeconds = ((ctime + ttl) / 1000L);
			bao.write(intTo4ByteArray(expiryDateInSeconds.intValue()));
			message.setExpiry(ctime + ttl);
		}

		// Write the TokenLength as a 16bits unsigned int, in big endian
		int tl = deviceTokenAsBytes.length;
		bao.write((byte) ((tl & 0xFF00) >> 8));
		bao.write((byte) (tl & 0xFF));

		// Write the Token in bytes
		bao.write(deviceTokenAsBytes);

		// Write the PayloadLength as a 16bits unsigned int, in big endian
		int pl = payloadAsBytes.length;
		int s1 = (pl & 0xFF00) >> 8;
		int s2 = pl & 0xFF;
		bao.write(s1);
		bao.write(s2);

		// Finally write the Payload
		bao.write(payloadAsBytes);

		logger.debug("Built raw message ID " + identifier);

		// Return the ByteArrayOutputStream as a Byte Array
		return bao.toByteArray();
	}


	/**
	 * Get the number of retry attempts
	 * @return int
	 */
	public int getRetryAttempts() {
		return this.retryAttempts;
	}


	private static final byte[] intTo4ByteArray(int value) {
		return ByteBuffer.allocate(4).putInt(value).array();
	}


	/**
	 * Set the number of retry attempts
	 * @param retryAttempts
	 */
	public void setRetryAttempts(int retryAttempts) {
		this.retryAttempts = retryAttempts;
	}


	/**
	 * Sets the DeviceFactory used by this PushNotificationManager.
	 * Usually useful for dependency injection.
	 * @param deviceFactory an object implementing DeviceFactory
	 */
	public void setDeviceFactory(DeviceFactory deviceFactory) {
		this.deviceFactory = deviceFactory;
	}


	/**
	 * Returns the DeviceFactory used by this PushNotificationManager.
	 * @return the DeviceFactory in use
	 */
	public DeviceFactory getDeviceFactory() {
		return deviceFactory;
	}


	public void setSslSocketTimeout(int sslSocketTimeout) {
		this.sslSocketTimeout = sslSocketTimeout;
	}


	public int getSslSocketTimeout() {
		return sslSocketTimeout;
	}


	public void setTrustAllServerCertificates(boolean trustAllServerCertificates) {
		this.trustAllServerCertificates = trustAllServerCertificates;
	}


	public boolean isTrustAllServerCertificates() {
		return trustAllServerCertificates;
	}


	/**
	 * Return a new sequential message identifier.
	 *
	 * @return a message identifier unique to this PushNotificationManager
	 */
	private int newMessageIdentifier() {
		int id = nextMessageIdentifier;
		nextMessageIdentifier++;
		return id;
	}


	public Socket getActiveSocket() {
		return socket;
	}


	public Map<Integer, PushedNotification> getPushedNotifications() {
		return pushedNotifications;
	}
}
