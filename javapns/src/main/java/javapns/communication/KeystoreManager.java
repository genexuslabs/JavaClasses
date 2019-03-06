package javapns.communication;

import java.io.*;
import java.security.*;
import java.security.cert.*;

import javapns.communication.exceptions.*;

/**
 * Class responsible for dealing with keystores.
 * 
 * @author Sylvain Pedneault
 */
class KeystoreManager {

	/**
	 * Loads a keystore.
	 * 
	 * @param server The server the keystore is intended for
	 * @return A loaded keystore
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws Exception
	 */
	public static KeyStore loadKeystore(AppleServer server) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, Exception {
		return loadKeystore(server, server.getKeystoreStream());
	}


	/**
	 * Loads a keystore.
	 * 
	 * @param server The server the keystore is intended for
	 * @param keystore The keystore to load (can be a File, an InputStream, a String for a file path, or a byte[] array)
	 * @return A loaded keystore
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws Exception
	 */
	public static KeyStore loadKeystore(AppleServer server, Object keystore) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, Exception {
		synchronized (server) {
			InputStream keystoreStream = streamKeystore(keystore);
			//System.out.println("Loading keystore from "+keystore+" synchronized on "+server);
			KeyStore keyStore;
			try {
				keyStore = KeyStore.getInstance(server.getKeystoreType());
				try {
					char[] password = KeystoreManager.getKeystorePasswordForSSL(server);
					keyStore.load(keystoreStream, password);
				} catch (Exception e) {
					e = getSimplerSSLException(e);
					throw e;
				}
			} finally {
				try {
					keystoreStream.close();
				} catch (Exception e) {
				}
			}
			return keyStore;
		}
	}


	static char[] getKeystorePasswordForSSL(AppleServer server) {
		String password = server.getKeystorePassword();
		if (password == null) password = "";
		//		if (password != null && password.length() == 0) password = null;
		char[] passchars = password != null ? password.toCharArray() : null;
		return passchars;
	}


	static Exception getSimplerSSLException(Exception e) {
		if (e != null) {
			String msg = e.toString();
			if (msg.contains("javax.crypto.BadPaddingException")) {
				return new InvalidKeystorePasswordException();
			}
			if (msg.contains("DerInputStream.getLength(): lengthTag=127, too big")) {
				return new InvalidKeystoreFormatException();
			}
			if (msg.contains("java.lang.ArithmeticException: / by zero") || msg.contains("java.security.UnrecoverableKeyException: Get Key failed: / by zero")) {
				return new InvalidKeystorePasswordException("Blank passwords not supported (#38).  You must create your keystore with a non-empty password.");
			}
		}
		return e;
	}


	/**
	 * Given an object representing a keystore, returns an actual stream for that keystore.
	 * Allows you to provide an actual keystore as an InputStream or a byte[] array,
	 * or a reference to a keystore file as a File object or a String path.
	 * 
	 * 
	 * @param keystore InputStream, File, byte[] or String (as a file path)
	 * @return A stream to the keystore.
	 * @throws FileNotFoundException
	 */
	public static InputStream streamKeystore(Object keystore) throws FileNotFoundException {
		if (keystore instanceof InputStream) return (InputStream) keystore;
		else if (keystore instanceof File) return new BufferedInputStream(new FileInputStream((File) keystore));
		else if (keystore instanceof String) return new BufferedInputStream(new FileInputStream((String) keystore));
		else if (keystore instanceof byte[]) return new ByteArrayInputStream((byte[]) keystore);
		else throw new IllegalArgumentException("Unsupported keystore reference: " + keystore);
	}

}
