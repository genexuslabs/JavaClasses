package javapns.communication;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;

import javax.net.ssl.*;

import org.bouncycastle.jce.provider.*;
import com.genexus.diagnostics.core.*;
/**
 * <h1>Class representing an abstract connection to an Apple server</h1>
 *
 * Communication protocol differences between Notification and Feedback servers are
 * implemented in {@link javapns.notification.ConnectionToNotificationServer} and {@link javapns.feedback.ConnectionToFeedbackServer}.
 *
 * @author Sylvain Pedneault
 */
public abstract class ConnectionToAppleServer {

	protected static final ILogger logger = LogManager.getLogger(ConnectionToAppleServer.class);

	/* The algorithm used by KeyManagerFactory */
	private static final String ALGORITHM = ((Security.getProperty("ssl.KeyManagerFactory.algorithm") == null) ? "sunx509" : Security.getProperty("ssl.KeyManagerFactory.algorithm"));

	/* The protocol used to create the SSLSocket */
	private static final String PROTOCOL = "TLS";

	/* PKCS12 */
	public static final String KEYSTORE_TYPE_PKCS12 = "PKCS12";
	/* JKS */
	public static final String KEYSTORE_TYPE_JKS = "JKS";

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	private KeyStore keyStore;
	private SSLSocketFactory socketFactory;
	private AppleServer server;


	/**
	 * Builds a connection to an Apple server.
	 *
	 * @param server
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws Exception
	 */
	public ConnectionToAppleServer(AppleServer server) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, Exception {
		this.server = server;

		this.keyStore = KeystoreManager.loadKeystore(server);
	}


	public AppleServer getServer() {
		return server;
	}


	public KeyStore getKeystore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, Exception {
		return keyStore;
		//		return KeystoreManager.loadKeystore(server);
	}


	public void setKeystore(KeyStore ks) {
		this.keyStore = ks;
	}


	/**
	 * Generic SSLSocketFactory builder
	 *
	 * @param trustManagers
	 * @return SSLSocketFactory
	 * @throws Exception
	 */
	protected SSLSocketFactory createSSLSocketFactoryWithTrustManagers(TrustManager[] trustManagers) throws Exception {

		logger.debug("Creating SSLSocketFactory");
		// Get a KeyManager and initialize it
		KeyStore keystore = getKeystore();
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(ALGORITHM);
		try {
			char[] password = KeystoreManager.getKeystorePasswordForSSL(server);
			kmf.init(keystore, password);
		} catch (Exception e) {
			e = KeystoreManager.getSimplerSSLException(e);
			throw e;
		}

		// Get the SSLContext to help create SSLSocketFactory
		SSLContext sslc = SSLContext.getInstance(PROTOCOL);
		sslc.init(kmf.getKeyManagers(), trustManagers, null);

		return sslc.getSocketFactory();
	}


	public abstract String getServerHost();


	public abstract int getServerPort();


	/**
	 * Return a SSLSocketFactory for creating sockets to communicate with Apple.
	 *
	 * @return SSLSocketFactory
	 * @throws KeyStoreException
	 * @throws NoSuchProviderException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws Exception
	 */
	public SSLSocketFactory createSSLSocketFactory() throws KeyStoreException, NoSuchProviderException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException, Exception {
		return createSSLSocketFactoryWithTrustManagers(new TrustManager[] { new ServerTrustingTrustManager() });
	}


	public SSLSocketFactory getSSLSocketFactory() throws Exception {
		if (socketFactory == null) socketFactory = createSSLSocketFactory();
		return socketFactory;
	}


	/**
	 * Create a SSLSocket which will be used to send data to Apple
	 * @return the SSLSocket
	 * @throws Exception
	 * @throws FileNotFoundException
	 */
	public SSLSocket getSSLSocket() throws Exception {
		SSLSocketFactory socketFactory = getSSLSocketFactory();
		logger.debug("Creating SSLSocket to " + getServerHost() + ":" + getServerPort());

		try {
			if (isProxySet()) {
				return tunnelThroughProxy(socketFactory);
			} else {
				return (SSLSocket) socketFactory.createSocket(getServerHost(), getServerPort());
			}
		} catch (Exception e) {
			throw e;
		}
	}


	private boolean isProxySet() {
		String httpsHost = System.getProperty("https.proxyHost");
		boolean isSet = httpsHost != null && httpsHost.length() > 0;
		return isSet;
	}


	private SSLSocket tunnelThroughProxy(SSLSocketFactory socketFactory) throws UnknownHostException, IOException {
		SSLSocket socket;

		// If a proxy was set, tunnel through the proxy to create the connection
		String tunnelHost = System.getProperty("https.proxyHost");
		Integer tunnelPort = Integer.getInteger("https.proxyPort").intValue();

		Socket tunnel = new Socket(tunnelHost, tunnelPort);
		doTunnelHandshake(tunnel, getServerHost(), getServerPort());

		/* overlay the tunnel socket with SSL */
		socket = (SSLSocket) socketFactory.createSocket(tunnel, getServerHost(), getServerPort(), true);

		/* register a callback for handshaking completion event */
		socket.addHandshakeCompletedListener(new HandshakeCompletedListener() {
			public void handshakeCompleted(HandshakeCompletedEvent event) {
				logger.debug("Handshake finished!");
				logger.debug("\t CipherSuite:" + event.getCipherSuite());
				logger.debug("\t SessionId " + event.getSession());
				logger.debug("\t PeerHost " + event.getSession().getPeerHost());
			}
		});

		return socket;
	}


	private void doTunnelHandshake(Socket tunnel, String host, int port) throws IOException {

		OutputStream out = tunnel.getOutputStream();

		String msg = "CONNECT " + host + ":" + port + " HTTP/1.0\n" + "User-Agent: BoardPad Server" + "\r\n\r\n";
		byte b[] = null;
		try { //We really do want ASCII7 -- the http protocol doesn't change with locale.
			b = msg.getBytes("ASCII7");
		} catch (UnsupportedEncodingException ignored) { //If ASCII7 isn't there, something serious is wrong, but Paranoia Is Good (tm)
			b = msg.getBytes();
		}
		out.write(b);
		out.flush();

		// We need to store the reply so we can create a detailed error message to the user.
		byte reply[] = new byte[200];
		int replyLen = 0;
		int newlinesSeen = 0;
		boolean headerDone = false; //Done on first newline

		InputStream in = tunnel.getInputStream();

		while (newlinesSeen < 2) {
			int i = in.read();
			if (i < 0) {
				throw new IOException("Unexpected EOF from proxy");
			}
			if (i == '\n') {
				headerDone = true;
				++newlinesSeen;
			} else if (i != '\r') {
				newlinesSeen = 0;
				if (!headerDone && replyLen < reply.length) {
					reply[replyLen++] = (byte) i;
				}
			}
		}

		/*
		 * Converting the byte array to a string is slightly wasteful
		 * in the case where the connection was successful, but it's
		 * insignificant compared to the network overhead.
		 */
		String replyStr;
		try {
			replyStr = new String(reply, 0, replyLen, "ASCII7");
		} catch (UnsupportedEncodingException ignored) {
			replyStr = new String(reply, 0, replyLen);
		}

		/* We check for Connection Established because our proxy returns HTTP/1.1 instead of 1.0 */
		if (replyStr.toLowerCase().indexOf("200 connection established") == -1) {
			throw new IOException("Unable to tunnel through. Proxy returns \"" + replyStr + "\"");
		}

		/* tunneling Handshake was successful! */
	}

}
