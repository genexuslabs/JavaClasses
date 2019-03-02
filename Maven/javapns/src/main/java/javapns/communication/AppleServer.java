package javapns.communication;

import java.io.*;

/**
 * Common interface of all classes representing a connection to any Apple server.
 * Use AppleNotificationServer and AppleFeedbackServer interfaces for specific connections.
 * 
 * @author Sylvain Pedneault
 */
public interface AppleServer {

	/**
	 * Returns a stream to a keystore.
	 * @return an InputStream
	 */
	public InputStream getKeystoreStream();


	/**
	 * Returns the keystore's password.
	 * @return a password matching the keystore
	 */
	public String getKeystorePassword();

	/**
	 * Returns the format used to produce the keystore (typically PKCS12).
	 * @return a valid keystore format identifier
	 */
	public String getKeystoreType();

}
