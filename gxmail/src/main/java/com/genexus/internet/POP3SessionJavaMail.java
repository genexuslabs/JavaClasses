package com.genexus.internet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import com.genexus.common.interfaces.SpecificImplementation;
import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.pop3.POP3Store;

public class POP3SessionJavaMail  implements GXInternetConstants,IPOP3Session
{

	static private boolean DEBUG = GXInternetConstants.DEBUG;
	static private PrintStream logOutput;

	private String user;
	private String password;
	private String attachmentsPath = "";
	protected String pop3Host = "192.168.0.1";
	protected int pop3Port = 110;
	private boolean deleteOnRead;
	private boolean readSinceLast;
	private boolean secureConnection;

	private int timeout;

	private int numOfMessages;
	private int lastReadMessage;
	private StringCollection attachs;
	
	private Session session;
	private POP3Store emailStore;
	private Boolean downloadAttachments = false;
	
	Message[] messages;
	POP3Folder emailFolder;

	static
	{
		if	(DEBUG)
		{
			try
			{
				logOutput = new PrintStream(new FileOutputStream(new File("_gx_pop3.log"), true));
			}
			catch (IOException e)
			{
				System.out.println("Can't open POP3 log file pop3.log");
			}
		}		
	}

	public POP3SessionJavaMail()
	{
	}

	public void login(GXPOP3Session sessionInfo)
	{
		this.pop3Host = sessionInfo.getHost();
		this.pop3Port = sessionInfo.getPort();
		this.timeout  = sessionInfo.getTimeout();
		this.user = sessionInfo.getUserName();
		this.password = sessionInfo.getPassword();
		this.deleteOnRead  = false;
		this.readSinceLast = sessionInfo.getNewMessages() != 0;
		this.secureConnection = sessionInfo.getSecure() != 0;
		
		timeout = timeout * 1000;
		Properties props = new Properties();
		props.setProperty("mail.pop3.host", pop3Host);
		props.setProperty("mail.pop3.port", String.valueOf(pop3Port));	
		props.setProperty("mail.pop3.connectiontimeout", String.valueOf(timeout));
		props.setProperty("mail.pop3.timeout", String.valueOf(timeout));		
		props.setProperty("mail.pop3.ssl.enable", String.valueOf(secureConnection));
		
		session = Session.getInstance(props);
		if	(DEBUG)
		{
			session.setDebug(true);
			session.setDebugOut(logOutput);
		}		
		try
		{
			emailStore = (POP3Store) session.getStore("pop3");
			emailStore.connect(user, password);
			
			emailFolder = (POP3Folder) emailStore.getFolder("INBOX");
			emailFolder.open(Folder.READ_WRITE);
			lastReadMessage = 0;
			
			if (readSinceLast)
			{
				numOfMessages = emailFolder.getNewMessageCount();
			}
			else
			{
				numOfMessages = emailFolder.getMessageCount();
			}
			messages = emailFolder.getMessages();
		}
		catch(NoSuchProviderException e)
		{
			log (e.getMessage());
			sessionInfo.exceptionHandler(new GXMailException("Can't connect to mail server", MAIL_CantLogin));
		}
		catch(MessagingException me)
		{
			log (me.getMessage());
			sessionInfo.exceptionHandler(new GXMailException("Can't connect to mail server", MAIL_CantLogin));			
		}		
	}

	public void logout(GXPOP3Session sessionInfo)
	{
		try
		{
			emailFolder.close(true);
			emailStore.close();
		}
		catch (MessagingException e)
		{
			log (e.getMessage());
			sessionInfo.exceptionHandler(new GXMailException(e.getMessage(), MAIL_ConnectionLost));
		}
	}

	public void delete(GXPOP3Session sessionInfo)
	{
		try
		{
			messages[lastReadMessage-1].setFlag(Flags.Flag.DELETED, true);
		}
		catch (MessagingException e)
		{
			log (e.getMessage());
			sessionInfo.exceptionHandler(new GXMailException(e.getMessage(), MAIL_ServerRepliedErr));
		}
	}

	public void skip(GXPOP3Session sessionInfo)
	{
		try
		{
			if	(lastReadMessage == numOfMessages)
				throw new GXMailException("No messages to receive", MAIL_NoMessages);
			
			++lastReadMessage;
		}
		catch (GXMailException e)
		{
			sessionInfo.exceptionHandler(e);
		}		
	}
								   
	public void receive(GXPOP3Session sessionInfo, GXMailMessage gxmessage)
	{
		try
		{
			gxmessage.clear();
			setAttachmentsPath(sessionInfo.getAttachDir());			
			
			if	(lastReadMessage +1 > numOfMessages)
				throw new GXMailException("No messages to receive", MAIL_NoMessages);
			
			Message message = messages[lastReadMessage++];
			gxmessage.setFrom(getMailRecipient((InternetAddress)message.getFrom()[0]));
			
						
			gxmessage.setTo(processRecipients(message, Message.RecipientType.TO));			
			gxmessage.setCc(processRecipients(message, Message.RecipientType.CC));
			gxmessage.setBcc(processRecipients(message, Message.RecipientType.BCC));
						
			MailRecipientCollection mailRecipient = new MailRecipientCollection();
			for (int i = 0 ; i < message.getReplyTo().length; i++)
			{				
				InternetAddress addr = ((InternetAddress)message.getReplyTo()[i]);
				mailRecipient.addNew(addr.getPersonal(), addr.getAddress());				
			}
			
			gxmessage.setReplyto(mailRecipient);
			
			gxmessage.setDateSent(message.getSentDate());
			gxmessage.setDateReceived(message.getReceivedDate() == null ? com.genexus.CommonUtil.now(): message.getReceivedDate());
			
			gxmessage.setSubject(message.getSubject());
			Hashtable headers = new Hashtable();
			for (Enumeration en = message.getAllHeaders(); en.hasMoreElements(); )
			{
				Header h = (Header) en.nextElement();
				headers.put(h.getName(), h.getValue());
			}
			gxmessage.setHeaders(headers);
			
			attachs = new StringCollection();
 			Object content = message.getContent();
 			if (content instanceof Multipart) 
 			{
 				handleMultipart((Multipart)content, gxmessage);
 			} 
 			else 
 			{
 				handlePart(message, gxmessage);
 			}
		}
		catch (GXMailException e)
		{
			sessionInfo.exceptionHandler(e);
		}
		catch (MessagingException e)
		{
			log (e.getMessage());
			sessionInfo.exceptionHandler(new GXMailException(e.getMessage(), MAIL_ServerRepliedErr));
		}
		catch (IOException e)
		{
			log (e.getMessage());
			sessionInfo.exceptionHandler(new GXMailException(e.getMessage(), MAIL_ServerRepliedErr));			
		}
	}
	
	private MailRecipient getMailRecipient(InternetAddress inetAdd)
	{
		return new MailRecipient(inetAdd.getPersonal(), inetAdd.getAddress());		
	}
	
	private MailRecipientCollection processRecipients(Message message, Message.RecipientType rType) throws MessagingException
	{
		MailRecipientCollection mailRecipient = new MailRecipientCollection();
		if (message.getRecipients(rType) != null)
		{
			for (int i = 0 ; i < message.getRecipients(rType).length; i++)
			{
				InternetAddress address = (InternetAddress)message.getRecipients(rType)[i];					
				mailRecipient.addNew(address.getPersonal(), address.getAddress());
			}			
		}
		return mailRecipient;
	}
	
  private void handleMultipart(Multipart multipart, GXMailMessage gxmessage) throws MessagingException, IOException 
  {
  	for (int i=0, n=multipart.getCount(); i<n; i++) 
  	{
  		handlePart(multipart.getBodyPart(i), gxmessage);
    }
  }
  
  private void handlePart(Part part, GXMailMessage gxmessage) throws MessagingException, IOException 
  {
    String disposition = part.getDisposition();
    boolean isXForwardedFor = part.getContent() instanceof MimeMessage &&		// Para soportar attachments de mails que llegan a la casilla con el header "X-Forwarded-For"
		Arrays.stream(part.getHeader("Content-Type")).filter(header -> header.toLowerCase().startsWith("message")).findFirst().orElse(null) != null;

    if (System.getProperties().getProperty("DonwloadAllMailsAttachment", null) != null && isXForwardedFor)
		handlePart((MimeMessage) part.getContent(),gxmessage);

    if (part.isMimeType("text/plain"))
    {
    	gxmessage.setText(part.getContent().toString());
    } 
    if (part.isMimeType("text/html")) 
    {
    	gxmessage.setHtmltext(part.getContent().toString());
    }
    if (part.isMimeType("multipart/*"))
    {
    	handleMultipart((Multipart)part.getContent(), gxmessage);
    }
    if (disposition==null && part.isMimeType("application/*"))
    {
    	disposition = "UNKNOWN";
    }    
    if (this.downloadAttachments && (disposition!=null && (disposition.equalsIgnoreCase(Part.ATTACHMENT) || disposition.equalsIgnoreCase(Part.INLINE) || disposition.equalsIgnoreCase("UNKNOWN")))) 
    {
    	String fileName = "";
		if (part.getFileName() != null || (isXForwardedFor && ((MimeMessage) part.getContent()).getFileName() != null))
				fileName = MimeUtility.decodeText(part.getContent() instanceof MimeMessage ? ((MimeMessage) part.getContent()).getFileName() : part.getFileName());
			else if (!(part.getContent() instanceof MimeMessage) || ((MimeMessage) part.getContent()).getFileName() == null)
				fileName = SpecificImplementation.GXutil.getTempFileName("tmp");
		
		String cid = getAttachmentContentId(part);
		if (disposition.equalsIgnoreCase(Part.INLINE) && !cid.isEmpty())
		{
			fileName = String.format("%s_%s", cid, fileName);
			String newHTML = gxmessage.getHtmltext().replace(cid, fileName);
			gxmessage.setHtmltext(newHTML);
		}

		saveFile(fileName, part.getContent() instanceof MimeMessage ? ((MimeMessage) part.getContent()).getInputStream() : part.getInputStream());
		attachs.add(attachmentsPath + fileName);
		gxmessage.setAttachments(attachs);
    }
  }
  
  private String getAttachmentContentId(Part part) throws MessagingException
  {
	  String cid = "";
	  String[] cids = part.getHeader("Content-ID");
	  if (cids != null && !cids[0].isEmpty() && cids[0].startsWith("<") && cids[0].endsWith(">"))
	  {
		  cid = cids[0].substring(1, cids[0].length() - 1);
	  }
	  return cid;
  }
  
  private void saveFile(String filename, InputStream input) throws IOException 
  {
    File file = new File(attachmentsPath + filename);
    FileOutputStream fos = new FileOutputStream(file);
    BufferedOutputStream bos = new BufferedOutputStream(fos);

    BufferedInputStream bis = new BufferedInputStream(input);
    int aByte;
    while ((aByte = bis.read()) != -1) 
    {
      bos.write(aByte);
    }
    bos.flush();
    bos.close();
    bis.close();
  }
  
	public String getNextUID() throws GXMailException
	{
		try
		{
			if	(lastReadMessage == numOfMessages)
				throw new GXMailException("No messages to receive", MAIL_NoMessages);
			
			int messageNum = lastReadMessage +1;
			return emailFolder.getUID(emailFolder.getMessage(messageNum));
		}
		catch (MessagingException e)
		{
			log (e.getMessage());
			throw new GXMailException(e.getMessage(), MAIL_ServerRepliedErr);
		}		
	}
	
	public int getMessageCount() throws GXMailException
	{
		try
		{
			if	 (readSinceLast)
				return emailFolder.getNewMessageCount();

			return emailFolder.getMessageCount();
		}
		catch (MessagingException e)
		{
			log (e.getMessage());
			throw new GXMailException(e.getMessage(), MAIL_ServerRepliedErr);
		}
	}

	public void setAttachmentsPath(String _attachmentsPath)
	{
		attachmentsPath = _attachmentsPath.trim();
		if (!attachmentsPath.equals(""))
		{
			this.downloadAttachments = true;
		}		
		if(!attachmentsPath.equals("") && !attachmentsPath.endsWith(File.separator))attachmentsPath += File.separator;				
	}
	
	private void log(String text)
	{
		if	(DEBUG)
			if (logOutput != null)
				logOutput.println(text);
	}
}