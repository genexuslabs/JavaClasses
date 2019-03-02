package javapns.feedback;

import java.io.*;
import java.security.*;
import java.security.cert.*;

import javapns.communication.*;

/**
 * Class representing a connection to a specific Feedback Server.
 * 
 * @author Sylvain Pedneault
 */
public class ConnectionToFeedbackServer extends ConnectionToAppleServer {

	public ConnectionToFeedbackServer(AppleFeedbackServer feedbackServer) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, Exception {
		super(feedbackServer);
	}


	@Override
	public String getServerHost() {
		return ((AppleFeedbackServer) getServer()).getFeedbackServerHost();
	}


	@Override
	public int getServerPort() {
		return ((AppleFeedbackServer) getServer()).getFeedbackServerPort();
	}

}
