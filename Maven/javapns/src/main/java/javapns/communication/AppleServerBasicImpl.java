package javapns.communication;

import java.io.*;

/**
 * A basic and abstract implementation of the AppleServer interface
 * intended to facilitate rapid deployment.
 * 
 * @author Sylvain Pedneault
 */
public abstract class AppleServerBasicImpl implements AppleServer {

	private final Object keystore;
	private final String password;
	private final String type;


	/**
	 * Constructs a AppleServerBasicImpl object.
	 * 
	 * @param keystore The keystore to use (can be a File, an InputStream, a String for a file path, or a byte[] array)
	 * @param password The keystore's password
	 * @param type The keystore type (typically PKCS12)
	 * @throws FileNotFoundException
	 */
	public AppleServerBasicImpl(Object keystore, String password, String type) throws FileNotFoundException {
		//this.input = KeystoreManager.streamKeystore(keystore);
		this.keystore = keystore;
		this.password = password;
		this.type = type;
	}


	public InputStream getKeystoreStream() {
		try {
			return KeystoreManager.streamKeystore(keystore);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}


	public String getKeystorePassword() {
		return password;
	}


	public String getKeystoreType() {
		return type;
	}

}
