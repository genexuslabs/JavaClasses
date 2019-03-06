package javapns.notification;

import java.io.*;

import javapns.communication.*;

/**
 * Basic implementation of the AppleNotificationServer interface,
 * intended to facilitate rapid deployment.
 * 
 * @author Sylvain Pedneault
 */
public class AppleNotificationServerBasicImpl extends AppleServerBasicImpl implements AppleNotificationServer {

	private String host;
	private int port;


	/**
	 * Communication settings for interacting with Apple's default production or sandbox notification server.
	 * This constructor uses the recommended keystore type "PCKS12".
	 * 
	 * @param keystore The keystore to use (can be a File, an InputStream, a String for a file path, or a byte[] array)
	 * @param password The keystore's password
	 * @param production true to use Apple's production servers, false to use the sandbox
	 * @throws FileNotFoundException
	 */
	public AppleNotificationServerBasicImpl(Object keystore, String password, boolean production) throws FileNotFoundException {
		this(keystore, password, ConnectionToAppleServer.KEYSTORE_TYPE_PKCS12, production);
	}


	/**
	 * Communication settings for interacting with Apple's default production or sandbox notification server.
	 * 
	 * @param keystore The keystore to use (can be a File, an InputStream, a String for a file path, or a byte[] array)
	 * @param password The keystore's password
	 * @param type The keystore's type
	 * @param production true to use Apple's production servers, false to use the sandbox
	 * @throws FileNotFoundException
	 */
	public AppleNotificationServerBasicImpl(Object keystore, String password, String type, boolean production) throws FileNotFoundException {
		this(keystore, password, type, production ? PRODUCTION_HOST : DEVELOPMENT_HOST, production ? PRODUCTION_PORT : DEVELOPMENT_PORT);
	}


	/**
	 * Communication settings for interacting with a specific Apple Push Notification Server.
	 * 
	 * @param keystore The keystore to use (can be a File, an InputStream, a String for a file path, or a byte[] array)
	 * @param password The keystore's password
	 * @param type The keystore's type
	 * @param host A specific APNS host
	 * @param port A specific APNS port
	 * @throws FileNotFoundException
	 */
	public AppleNotificationServerBasicImpl(Object keystore, String password, String type, String host, int port) throws FileNotFoundException {
		super(keystore, password, type);
		this.host = host;
		this.port = port;
	}


	public String getNotificationServerHost() {
		return host;
	}


	public int getNotificationServerPort() {
		return port;
	}

}
