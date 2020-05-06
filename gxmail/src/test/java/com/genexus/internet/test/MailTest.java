package com.genexus.internet.test;

import com.genexus.internet.GXMailMessage;
import com.genexus.internet.GXSMTPSession;
import com.genexus.internet.MailRecipient;
import com.genexus.internet.SMTPSessionJavaMail;
//import com.genexus.specific.java.Connect;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MailTest {

//	static {
////		Connect.init();
////	}

	private String MAIL_FROM_ADDRESS = "gonzalogallotti@gmail.com";
	private String MAIL_FROM_NAME = "Sender";
	private String MAIL_TO_ADDRESS = MAIL_FROM_ADDRESS;
	private String MAIL_TO_NAME = "Receiver Name";
	private String MAIL_SMTP_HOST = "smtp.gmail.com";
	private String MAIL_PASSWORD = "ya29.a0Ae4lvC1gveWVHcBWKQZb-8DMsb6G4A1nvV_kc6dcf6EXGxC6Y3R3x0jcTx61B8-MB3vymnBw8YCmzbE_6OC6ww0JBxHBjt8BvTw0VL0T2FYXsb5P2pFMXKkd49mZFDoH1-flN3z0Lx4ZmXwNyCBI6HhCkOsOzAjqOiQ";

	@Test
	@Ignore
	public void SendEmailOAuth2() {
		SMTPSessionJavaMail client = new SMTPSessionJavaMail();
		GXSMTPSession session = new GXSMTPSession();
		session.setHost(MAIL_SMTP_HOST);
		session.setAuthentication(1);
		session.setAuthenticationProtocol("XOAUTH2");
		session.setSecure(0);
		session.getSender().setAddress(MAIL_FROM_ADDRESS);
		session.setUserName(MAIL_FROM_ADDRESS);
		session.setPassword(MAIL_PASSWORD);
		session.setPort(587);
		client.login(session);

		assertEquals(session.getErrDescription(),0, (int)session.getErrCode());

		GXMailMessage msg = new GXMailMessage();
		msg.setSubject("Test from Maven Standard Classes");
		msg.setText("Test from GeneXus Maven Builder");
		msg.getFrom().setAddress(MAIL_FROM_ADDRESS);
		msg.getFrom().setName(MAIL_FROM_NAME);

		MailRecipient recipient = new MailRecipient();
		recipient.setName(MAIL_TO_NAME);
		recipient.setAddress(MAIL_TO_ADDRESS);
		msg.getTo().add(recipient);

		client.send(session, msg);

		assertEquals(session.getErrDescription(),0, (int)session.getErrCode());

	}
}
