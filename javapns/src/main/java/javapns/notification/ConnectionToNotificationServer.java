package javapns.notification;

import java.io.*;
import java.security.*;
import java.security.cert.*;

import javapns.communication.*;

/**
 * Connection details specific to the Notification Service.
 * 
 * @author Sylvain Pedneault
 */
public class ConnectionToNotificationServer extends ConnectionToAppleServer {

	public ConnectionToNotificationServer(AppleNotificationServer server) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, Exception {
		super(server);
	}


	@Override
	public String getServerHost() {
		return ((AppleNotificationServer) getServer()).getNotificationServerHost();
	}


	@Override
	public int getServerPort() {
		return ((AppleNotificationServer) getServer()).getNotificationServerPort();
	}

}
