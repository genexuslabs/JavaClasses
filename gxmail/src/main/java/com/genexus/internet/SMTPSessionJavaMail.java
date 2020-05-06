package com.genexus.internet;

import java.io.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.genexus.CommonUtil;

import javax.mail.internet.MimeBodyPart;
import javax.activation.*;

public final class SMTPSessionJavaMail implements GXInternetConstants,ISMTPSession
{
	static private boolean DEBUG = GXInternetConstants.DEBUG;
	
	static private PrintStream logOutput;

	protected String host;
	protected int port;
	private String message;
	private String senderAddress;
	private String senderName;
	private String subject = "[No Subject]";
	protected int timeout = 0;
	private String user = "";
	private String password = "";
	private boolean authenticate = false;
	private boolean secureConnection = false;
	private String recipient;
	private String cc;
	private String bcc;
	private String attachments = "";	
	private Session session;
	private Transport t;
	MimeMessage mailMessage;

	static
	{	
		if	(DEBUG)
		{
			try
			{
				logOutput = new PrintStream(new FileOutputStream(new File("_gx_smtp.log"), true));
			}
			catch (IOException e)
			{
				System.out.println("Can't open SMTP log file smtp.log");
			}
		}
	}
    	
	public SMTPSessionJavaMail()
	{
	}

	public void login(GXSMTPSession sessionInfo)
	{
		login(sessionInfo, true);
	}

	public void login(GXSMTPSession sessionInfo, boolean useTLS)
	{

		host = sessionInfo.getHost();
		port = sessionInfo.getPort();
		timeout = sessionInfo.getTimeout() * 1000;
		user = sessionInfo.getUserName();
		password = sessionInfo.getPassword();
		authenticate = sessionInfo.getAuthentication() != 0;
		secureConnection = sessionInfo.getSecure() != 0;
		
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", host);
		props.setProperty("mail.smtp.port", String.valueOf(port));
		props.setProperty("mail.smtp.connectiontimeout", String.valueOf(timeout));
		props.setProperty("mail.smtp.timeout", String.valueOf(timeout));
		props.setProperty("mail.smtp.ssl.enable", String.valueOf(secureConnection));

		if (sessionInfo.getAuthenticationMethod().length() > 0) {
			props.setProperty("mail.smtp.auth.mechanisms", sessionInfo.getAuthenticationMethod().toUpperCase());
		}

		if (useTLS)
		{
			props.setProperty("mail.smtp.starttls.enable", "true");
		}
		if (authenticate)
		{
			props.setProperty("mail.smtp.auth", "true");
			props.setProperty("mail.smtp.user", user);
		}
		session = Session.getInstance(props);
		if	(DEBUG)
		{
			session.setDebug(true);
			session.setDebugOut(logOutput);
		}
		try
		{	
			t = session.getTransport("smtp");
			if (authenticate)
			{			
					t.connect(user, password);
			}
			else
			{
				t.connect();
			}			
		}
		catch(NoSuchProviderException e)
		{
			log ("1 - Can't connect to host");
			log (e.getMessage());
			sessionInfo.exceptionHandler(new GXMailException("Can't connect to host - " + e.getMessage(), MAIL_CantLogin));
		}
		catch(MessagingException me)
		{
			if (useTLS)
			{
				login(sessionInfo, false);
			}
			else
			{
				log ("1 - Can't connect to host");
				log (me.getMessage());
				sessionInfo.exceptionHandler(new GXMailException("Can't connect to host - " + me.getMessage(), MAIL_CantLogin));				
			}
		}
	}

	public void send(GXSMTPSession sessionInfo, GXMailMessage msg)
	{
		try
		{
			if	(msg.getTo().getCount() == 0 && msg.getCc().getCount() == 0 && msg.getBcc().getCount() == 0)
			{
				throw new GXMailException("No main recipient specified", MAIL_NoRecipient);
			}
			
			mailMessage = new MimeMessage(session);
			try
			{
				mailMessage.setFrom(new InternetAddress(sessionInfo.getSender().getAddress(), sessionInfo.getSender().getName(), "UTF-8"));
				sendToMailRecipientCollection(Message.RecipientType.TO, msg.getTo());
				sendToMailRecipientCollection(Message.RecipientType.CC, msg.getCc());
				sendToMailRecipientCollection(Message.RecipientType.BCC, msg.getBcc());
				
				if (msg.getReplyto().getCount() > 0)
				{
					Address[] replyToAddresses = new Address[msg.getReplyto().getCount()];
					for (int i = 0 ; i < msg.getReplyto().getCount(); i++)
					{
						replyToAddresses[i] = new InternetAddress(msg.getReplyto().item(i+1).getAddress(), msg.getReplyto().item(i+1).getName(), "UTF-8");
					}			
					mailMessage.setReplyTo(replyToAddresses);
				}
				
				mailMessage.setSubject(msg.getSubject(), "UTF-8");
				
				Enumeration<String> keys = msg.getHeaders().keys();
				while(keys.hasMoreElements())
				{					
					String key = (String)keys.nextElement();
					mailMessage.setHeader(key,  (String)msg.getHeaders().get(key));					
				}
				
				if	(msg.getAttachments().getCount() != 0)
				{
					BodyPart messageBodyPart = new MimeBodyPart();
					if (msg.getText().length() != 0)
					{
						messageBodyPart.setText(msg.getText());
					}
					if (msg.getHtmltext().length() != 0)
					{
						messageBodyPart.setContent(msg.getHtmltext(), "text/html; charset=utf-8" );
					}				
 					Multipart multipart = new MimeMultipart();
        	multipart.addBodyPart(messageBodyPart);
        	
        	String attachDir = CommonUtil.addLastPathSeparator(sessionInfo.getAttachDir());
        	for (int i = 0; i < msg.getAttachments().getCount(); i++)
        	{
        		addAttachment(multipart, (String)msg.getAttachments().item(i + 1), attachDir);
        	}
        	
        	mailMessage.setContent(multipart);
				}
				else
				{
					if (msg.getText().length() != 0)
					{
						mailMessage.setText(msg.getText());
					}
					if (msg.getHtmltext().length() != 0)
					{
						mailMessage.setContent(msg.getHtmltext(), "text/html; charset=utf-8" );
					}
				}
				
				t.sendMessage(mailMessage, mailMessage.getAllRecipients());
			}
			catch (Exception ge)
			{
				log (ge.getMessage());
				throw new GXMailException("Error sending message - " + ge.getMessage(), MAIL_MessageNotSent);			
			}
		}
		catch (GXMailException e)
		{
			sessionInfo.exceptionHandler(e);
		}
	}

	private void sendToMailRecipientCollection(Message.RecipientType type, MailRecipientCollection recipients) throws Exception
	{
			for (int i = 0 ; i < recipients.getCount(); i++)
			{
				mailMessage.addRecipient(type, new InternetAddress(recipients.item(i+1).getAddress(), recipients.item(i+1).getName(), "UTF-8"));
			}
	}
	
	private void addAttachment(Multipart multipart, String fileNamePath, String attachDir) throws Exception
	{		
		String filename = fileNamePath;		
		if (!new File(fileNamePath).isAbsolute())
		{
			fileNamePath = attachDir + fileNamePath;
		}
		BodyPart messageBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(fileNamePath);
		messageBodyPart.setDataHandler(new DataHandler(source));
		if	(filename.lastIndexOf(File.separator) != -1)
		{
			filename = filename.substring(filename.lastIndexOf(File.separator) + 1);
		}
		messageBodyPart.setFileName(filename);
		multipart.addBodyPart(messageBodyPart);
   }	

	public void logout(GXSMTPSession sessionInfo)
	{
		try
		{
			t.close();
		}
		catch (MessagingException e)
		{
			log ("7 - IOException " + e.getMessage());
			sessionInfo.exceptionHandler(new GXMailException(e.getMessage(), MAIL_ConnectionLost));
		}
	}

	public void setSubject(String subject)
	{
		if	(subject.trim().length() == 0)
			this.subject = "[No Subject]";
		else
			this.subject = subject;
	}

	public void setTO(String recipient)
	{
		this.recipient = recipient;
	}

	public void setCC(String cc)
	{
		this.cc = cc;
	}

	public void setBCC(String bcc)
	{
		this.bcc = bcc;
	}

	public void setAttachments(String attachments)
	{
		this.attachments = attachments.trim();
	}

	protected void log(String text)
	{
		if	(DEBUG)
			if (logOutput != null)
			{
				logOutput.println(text);
				logOutput.flush();
			}
	}
}