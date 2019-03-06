package javapns.feedback;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;

import javapns.devices.*;
import javapns.devices.exceptions.*;
import javapns.devices.implementations.basic.*;
import javax.net.ssl.*;

import com.genexus.diagnostics.core.*;


/**
 * Class for interacting with a specific Feedback Service.
 *
 * @author kljajo, dgardon, Sylvain Pedneault
 *
 */
public class FeedbackServiceManager {

	protected static final ILogger logger = LogManager.getLogger(FeedbackServiceManager.class);

	/* Length of the tuple sent by Apple */
	private static final int FEEDBACK_TUPLE_SIZE = 38;

	private boolean proxySet = false;

	private DeviceFactory deviceFactory;


	/**
	 * Constructs a FeedbackServiceManager with a supplied DeviceFactory.
	 */
	public FeedbackServiceManager(DeviceFactory deviceFactory) {
		this.setDeviceFactory(deviceFactory);
	}


	/**
	 * Constructs a FeedbackServiceManager with a default basic DeviceFactory.
	 */
	public FeedbackServiceManager() {
		this.setDeviceFactory(new BasicDeviceFactory());
	}


	/**
	 * Retrieve all devices which have un-installed the application w/Path to keystore
	 *
	 * @param server Connection information for the Apple server
	 * @return List of Devices
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws KeyManagementException
	 * @throws UnrecoverableKeyException
	 */
	/**
	 */
	public LinkedList<Device> getDevices(AppleFeedbackServer server) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, NoSuchProviderException, Exception {
		//		logger.debug( "Retrieving Devices from Host: [" + appleHost + "] Port: [" + applePort + "] with KeyStorePath [" + keyStorePath + "]/[" + keyStoreType + "]" );
		// Create the connection and open a socket
		ConnectionToFeedbackServer connectionHelper = new ConnectionToFeedbackServer(server);
		SSLSocket socket = connectionHelper.getSSLSocket();

		return getDevices(socket);
	}


	/**
	 * Retrieves the list of devices from an established SSLSocket.
	 *
	 * @param socket
	 * @return Devices
	 * @throws Exception
	 * @throws NullDeviceTokenException
	 * @throws NullIdException
	 * @throws DuplicateDeviceException
	 */
	private LinkedList<Device> getDevices(SSLSocket socket) throws Exception {

		// Compute
		LinkedList<Device> listDev = null;
		try {
			InputStream socketStream = socket.getInputStream();

			// Read bytes
			byte[] b = new byte[1024];
			ByteArrayOutputStream message = new ByteArrayOutputStream();
			int nbBytes = 0;
			// socketStream.available can return 0
			// http://forums.sun.com/thread.jspa?threadID=5428561
			while ((nbBytes = socketStream.read(b, 0, 1024)) != -1) {
				message.write(b, 0, nbBytes);
			}

			listDev = new LinkedList<Device>();
			byte[] listOfDevices = message.toByteArray();
			int nbTuples = listOfDevices.length / FEEDBACK_TUPLE_SIZE;
			logger.debug("Found: [" + nbTuples + "]");
			for (int i = 0; i < nbTuples; i++) {
				int offset = i * FEEDBACK_TUPLE_SIZE;

				// Build date
				int index = 0;
				int firstByte = 0;
				int secondByte = 0;
				int thirdByte = 0;
				int fourthByte = 0;
				long anUnsignedInt = 0;

				firstByte = (0x000000FF & ((int) listOfDevices[offset]));
				secondByte = (0x000000FF & ((int) listOfDevices[offset + 1]));
				thirdByte = (0x000000FF & ((int) listOfDevices[offset + 2]));
				fourthByte = (0x000000FF & ((int) listOfDevices[offset + 3]));
				index = index + 4;
				anUnsignedInt = ((long) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL;

				// Build device token length
				int deviceTokenLength = listOfDevices[offset + 4] << 8 | listOfDevices[offset + 5];

				// Build device token
				String deviceToken = "";
				int octet = 0;
				for (int j = 0; j < 32; j++) {
					octet = (0x000000FF & ((int) listOfDevices[offset + 6 + j]));
					deviceToken = deviceToken.concat(String.format("%02x", octet));
				}

				// Build device and add to list
				/* Create a basic device, as we do not want to go through the factory and create a device in the actual database... */
				Device device = new BasicDevice(deviceToken);
				listDev.add(device);
				logger.info("FeedbackManager retrieves one device :  " + new Date(anUnsignedInt * 1000) + ";" + deviceTokenLength + ";" + deviceToken + ".");
			}

			// Close the socket and return the list

		} catch (Exception e) {
			logger.debug("Caught exception fetching devices from Feedback Service");
			throw e;
		}

		finally {
			socket.close();
		}
		return listDev;
	}


	/**
	 * Set the proxy if needed
	 * @param host the proxyHost
	 * @param port the proxyPort
	 */
	public void setProxy(String host, String port) {
		this.proxySet = true;

		System.setProperty("http.proxyHost", host);
		System.setProperty("http.proxyPort", port);

		System.setProperty("https.proxyHost", host);
		System.setProperty("https.proxyPort", port);
	}


	public void setDeviceFactory(DeviceFactory deviceFactory) {
		this.deviceFactory = deviceFactory;
	}


	public DeviceFactory getDeviceFactory() {
		return deviceFactory;
	}

}
