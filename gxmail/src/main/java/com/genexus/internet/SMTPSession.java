
package com.genexus.internet;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PushbackInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.lang.StringUtils;

import com.genexus.CommonUtil;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.platform.INativeFunctions;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.protocol.BasicHttpContext;

final class SMTPSession implements GXInternetConstants,ISMTPSession
{
	private final int CONN_NORMAL = 0;
	private final int CONN_TLS = 1;
	private final int CONN_SSL = 2;
	
	private boolean DEBUG = GXInternetConstants.DEBUG;

    protected String host;
    protected int port;

	private PrintStream logOutput;

	private String recipient;
    private String senderAddress;
    private String senderName;
    private String message;
	private String subject = "[No Subject]";
	private String user = "";
	private String password = "";
	protected int timeout = 0;
	private boolean authenticate = false;
    private boolean secureConnection = false;

	private String cc;
	private String bcc;

	private String attachments = "";
    protected Socket sessionSock;

    protected InputStream inStream;
    protected DataOutputStream outStream;

	private static final String CRLF = "\r\n";

	private static String localHostName;
	private BASE64Encoder base64encoder = new BASE64Encoder();

	static
	{
		try
		{
			localHostName = java.net.InetAddress.getLocalHost().getHostName();
		}
		catch (Exception e)
		{
			localHostName = "unknown";
		}
	}

	public SMTPSession()
    {
		if	(DEBUG)
		{
			try
			{
				logOutput = new PrintStream(new FileOutputStream(new File("_gx_smtp.log")));
			}
			catch (IOException e)
			{
				System.out.println("Can't open SMTP log file smtp.log");
			}
		}
    }

	public SMTPSession(String host, String senderAddress, String message) throws GXMailException
    {
		this();

    	this.host = host;
        this.port = 25;     // default SMTP port is 25

        this.message = message;
        this.senderAddress = senderAddress;
		this.senderName = senderAddress;
		this.subject = "";
	}

	public void login(GXSMTPSession sessionInfo)
	{
		host 			= sessionInfo.getHost();
		port 			= sessionInfo.getPort();
		timeout 		= sessionInfo.getTimeout();
		user 			= sessionInfo.getUserName();
		password 		= sessionInfo.getPassword();
		authenticate 	= sessionInfo.getAuthentication() != 0;
        secureConnection = sessionInfo.getSecure() != 0;
		try
		{
                        connectAndLogin();
		}
		catch (GXMailException e)
		{
			sessionInfo.exceptionHandler(e);
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

        	commandOk("MAIL FROM: <" + sessionInfo.getSender().getAddress() + ">", "2", MAIL_MessageNotSent);

			// Now tell the server who we want to send a message to
			log("Send TO");
			sendToMailRecipientCollection(msg.getTo());

			log("Send CC");
			sendToMailRecipientCollection(msg.getCc());

			log("Send BC");
			sendToMailRecipientCollection(msg.getBcc());

			// Okay, now send the mail message

			commandOk("DATA", "3", MAIL_MessageNotSent);

			try
			{
				println(FROM + ": " + sessionInfo.getSender().getRecipientString());

				sendAllRecipients(msg.getTo(), GXInternetConstants.TO);
				sendAllRecipients(msg.getCc(), GXInternetConstants.CC);
				sendAllRecipients(msg.getReplyto(), GXInternetConstants.REPLY_TO);
				
				println("MIME-Version: 1.0");
	        	println(SUBJECT + ": " + GXMailer.getEncodedString(msg.getSubject()));
				java.text.SimpleDateFormat df;
			    df = new java.text.SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
				TimeZone ts = TimeZone.getDefault();
				String tsString;
				int offset = ts.getRawOffset() / 3600000;
				if (offset == 0)
					tsString = "-0000";
				else
				{
					int absOffset = java.lang.Math.abs(offset);
					if (absOffset < 10)
						if (absOffset == offset)
							tsString = "+" + "0" + String.valueOf(absOffset);
						else
							tsString = "-" + "0" + String.valueOf(absOffset);
					else
						tsString = String.valueOf(offset);
					tsString = tsString + "00";
				}
				String date = df.format((Date)new Date()) + " " + tsString;
				println(DATE 	+ ": " + date);
				
				Enumeration<String> keys = msg.getHeaders().keys();
				while(keys.hasMoreElements())
				{					
					String key = (String)keys.nextElement();
					println(key + " :" +  msg.getHeaders().get(key));
				}
				// Comento estos headers porque aparentemente dan problemas al enviar
				// mails a hotmail
				//println("X-Mailer: GeneXus Application");
				//println(PRIORITY + ": 3 (Normal)");
				String sTime = Long.toString(System.currentTimeMillis());

				if	(msg.getAttachments().getCount() != 0)
				{
					println("Content-Type: multipart/mixed;boundary=\"" +
								getStartMessageIdMixed(sTime) +
								"\"\r\n\r\nThis message is in MIME format. Since your mail reader does not understand\r\nthis format, some or all of this message may not be legible.\r\n\r\n" +
								getNextMessageIdMixed(sTime, false));

				}

				if	(msg.getHtmltext().length() != 0)
				{
					println("Content-Type: multipart/alternative;boundary=\"" +
								getStartMessageIdAlternative(sTime) +
								"\"\r\n\r\nThis message is in MIME format. Since your mail reader does not understand\r\nthis format, some or all of this message may not be legible.\r\n\r\n");
				}

				if	(msg.getText().length() != 0)
				{
					if	(msg.getHtmltext().length() != 0)
					{
						println(getNextMessageIdAlternative(sTime, false));
					}

					println("Content-Type:text/plain; charset=\"UTF-8\"\r\n");
					sendTextUTF8(msg.getText());
				}

				if	(msg.getHtmltext().length() != 0)
				{
					println("\r\n\r\n" + getNextMessageIdAlternative(sTime, false));
					println("Content-Type: text/html; charset=\"UTF-8\"\r\n");

					sendTextUTF8(msg.getHtmltext());
					println("");
					println(getNextMessageIdAlternative(sTime, true));
				}

				sendAttachments(sTime, msg.getAttachments(), CommonUtil.addLastPathSeparator(sessionInfo.getAttachDir()));

			}
			catch (IOException e)
			{
				log ("6 - IOException " + e.getMessage());
				throw new GXMailException(e.getMessage(), MAIL_ConnectionLost);
			}

			// A "." on a line by itself ends a message.

    		commandOk(CRLF + ".", "2", MAIL_MessageNotSent);
		}
		catch (GXMailException e)
		{
			sessionInfo.exceptionHandler(e);
		}
	}

	private void sendAllRecipients(MailRecipientCollection msgList, String cmd) throws IOException {
		if (msgList.getCount() > 0)
		{
			List<String> addresses = new ArrayList<String>();
			MailRecipient recipient;
			int addressFormat = 2;
			for (int i=1; i <= msgList.getCount(); i++)
			{
				recipient = msgList.item(i);
				addresses.add(recipient.getRecipientString(addressFormat));
				
			}			
			println(cmd   + ": " + StringUtils.join(addresses, ','));
		}
	}
	
	private void sendTextUTF8(String text) throws IOException
	{
		// Send each line of the message
  		StringTokenizer st = new StringTokenizer(text, "\n", true);
		String aux;

		while (st.hasMoreTokens())
		{
			aux = st.nextToken();

			// If the line begins with a ".", put an extra "." in front of it.

    		if (aux.charAt(0) == '.')
        		outStream.writeBytes(".");

			outStream.write(aux.getBytes("UTF8"));

			outStream.flush();
			log(aux);
		}
	}	


	public void logout(GXSMTPSession sessionInfo)
	{
		try
		{
			logout();
		}
		catch (GXMailException e)
		{
			sessionInfo.exceptionHandler(e);
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

	boolean isLoggedIn()
	{
		return sessionSock != null;
	}

	private void close() throws IOException
    {
		// Close down the session
		if (sessionSock != null)
		{
			sessionSock.close();
			sessionSock = null;
		}
    }

	private void sendToMailRecipientCollection(MailRecipientCollection recipients) throws GXMailException
	{
		try
		{
			String response;
			for (int i = 0 ; i < recipients.getCount(); i++)
			{
		    	response = doCommand("RCPT TO: <" + recipients.item(i + 1).getAddress() + ">");

		        if (response.charAt(0) != '2')
		        {
					log ("2 - Response invalid " + response);
		        	throw new GXMailException(response, MAIL_InvalidRecipient);
				}
			}
		}
		catch (IOException e)
		{
			log ("3 - IOException " + e.getMessage());
			throw new GXMailException(e.getMessage(), MAIL_ConnectionLost);
		}
	}

    private void connectAndLogin() throws GXMailException
    {
		if(secureConnection)
		{
			try
			{
				connectIndirectTLS();
			}
			catch(GXMailException e1)
			{
				closeSafe();
				try
				{
					connectDirectTLS();
				}
				catch(GXMailException e2)
				{
					closeSafe();
					connectSSL();
				}
			}
		}
		else
		{
			connectNormal();
		}
    }

    private void connectIndirectTLS() throws GXMailException
    {
		connect(CONN_TLS);
		doLogin(true);
    }

    private void connectDirectTLS() throws GXMailException
    {
		connect(CONN_TLS);
		doLogin(false);
    }

    private void connectSSL() throws GXMailException
    {
		connect(CONN_SSL);
		doLogin(false);
    }

    private void connectNormal() throws GXMailException
    {
		connect(CONN_NORMAL);
		doLogin(false);
    }

    private void connect(final int type) throws GXMailException
    {
            // Connect to the server
            SpecificImplementation.NativeFunctions.getInstance().executeWithPermissions(
                            new Runnable() {
                                    public void run()
                                    {
                                            try
                                            {
                                            sessionSock = getConnectionSocket(type);
                                                    log("Timeout: " + timeout);
                                                    sessionSock.setSoTimeout(timeout * 1000);
                                                    sessionSock.setTcpNoDelay(true);
                                            inStream = sessionSock.getInputStream();
                                            outStream = new DataOutputStream(sessionSock.getOutputStream());
                                            }
                                            catch (UnknownHostException e)
                                            {
                                            }
                                            catch (IOException e)
                                            {
                                            }
                                    }
                            }, INativeFunctions.CONNECT);

            if	(sessionSock == null)
            {
                    log ("1 - Can't connect to host");
                    throw new GXMailException("Can't connect to host", MAIL_CantLogin);
            }
    }

	private Socket getConnectionSocket(int type) throws UnknownHostException, IOException
	{
		InetAddress ipAddr = InetAddress.getByName(host.trim());
		SSLConnectionSocketFactory sslConn;
		switch(type)
		{
			case CONN_NORMAL:
				return new Socket(host.trim(), port);
			case CONN_TLS:
				sslConn = SSLConnConstructor.getSSLSecureInstance(new String[] { "TLSv1.1", "TLSv1.2" });
				return sslConn.createLayeredSocket(new Socket(host.trim(), port),ipAddr.getHostName(),port,new BasicHttpContext());
			case CONN_SSL:
				sslConn = SSLConnConstructor.getSSLSecureInstance(new String[] { "TLSv1" });
				return sslConn.createLayeredSocket(new Socket(host.trim(), port),ipAddr.getHostName(),port,new BasicHttpContext());
		}
		return new Socket(host.trim(), port);
	}

	private void doLogin(boolean startTLS) throws GXMailException
	{
		// After connecting, the SMTP server will send a response string. Make
		// sure it starts with a '2' (reponses in the 200's are positive
		// responses.

		try
		{
			String response = getResponse();
	        if (response.charAt(0) != '2') {
				log ("4 - Invalid Response " + response);
	        	throw new GXMailException(response, MAIL_CantLogin);
			}
		}
		catch (IOException e)
		{
			log ("5 - IOException " + e.getMessage());
	       	throw new GXMailException(e.getMessage(), MAIL_ConnectionLost);
		}

		if(!authenticate)
		{
			// Introduce ourselves to the SMTP server with a polite "HELO"
			commandOk("HELO " + localHostName);
            if(startTLS)
            {
              commandOk("STARTTLS");
            }
		}else
		{
			// @gusbro: Si quremos autenticarnos, debemos hacer un 'extended hello'
			// y no un HELO simple...
			commandOk("EHLO " + localHostName);
			doAuthLogin(startTLS);
		}
	}

	private void doAuthLogin(boolean startTLS) throws GXMailException
	{
		commandOk("AUTH LOGIN", "334", MAIL_AuthenticationError);
        if(startTLS)
        {
          commandOk("STARTTLS");
        }
		commandOk(base64encoder.encodeBuffer(user.getBytes()), "334", MAIL_AuthenticationError);
		commandOk(base64encoder.encodeBuffer(password.getBytes()), "235", MAIL_PasswordRefused);
	}

    private void closeSafe()
    {
      try
      {
        close();
      }
      catch(IOException ioex) {}
    }

	public void logout() throws GXMailException
	{
		commandOk("QUIT");
		try
		{
	    	close();
		}
		catch (IOException e)
		{
			log ("7 - IOException " + e.getMessage());
			throw new GXMailException(e.getMessage(), MAIL_ConnectionLost);
		}
	}

	private void commandOk(String message) throws GXMailException
	{
		commandOk(message, "2");
	}

	private void commandOk(String message, String startsWith, int error) throws GXMailException
	{
		try
		{
			String response = doCommand(message);

	        if (!response.startsWith(startsWith))
	        {
				log ("8 - Response Invalid " + response + " - " + startsWith);
	        	doCommand("RSET");
	        	throw new GXMailException(response, error);
			}
		}
		catch (IOException e)
		{
			log ("9 - IOException " + e.getMessage());
	        throw new GXMailException(e.getMessage(), MAIL_ConnectionLost);
		}
	}

	private void commandOk(String message, String startsWith) throws GXMailException
	{
		commandOk(message, startsWith, 0);
	}

	private void sendAttachments(String sTime, StringCollection attachments, String attachmentPath) throws GXMailException
	{
		try
		{
			if	(attachments.getCount() == 0)
			{
				return;
			}

			println("");
			for (int i = 0; i < attachments.getCount(); i++)
			{
				sendAttachment(sTime, attachments.item(i + 1), attachmentPath);
			}
			println(getNextMessageIdMixed(sTime, true));
		}
		catch (IOException e)
		{
			log ("10 - IOException " + e.getMessage());
			throw new GXMailException(e.getMessage(), MAIL_ConnectionLost);
		}
	}

	private String getStartMessageIdMixed(String sTime)
	{
		return getStartMessage(sTime, "MIXED");
	}

	private String getNextMessageIdMixed(String sTime, boolean end)
	{
		return getNextMessage(sTime, "MIXED", end);
	}

	private String getStartMessageIdAlternative(String sTime)
	{
		return getStartMessage(sTime, "ALTERNATIVE");
	}

	private String getNextMessageIdAlternative(String sTime, boolean end)
	{
		return getNextMessage(sTime, "ALTERNATIVE", end);
	}

	private String getStartMessage(String sTime, String sPrefix)
	{
		return "----_=_NextPart_" + sPrefix + "_" + sTime;
	}

	private String getNextMessage(String sTime, String sPrefix, boolean end)
	{
		return "--" + getStartMessage(sTime, sPrefix) + (end?"--":"");
	}

	private void sendAttachment(String sTime, String fileNamePath, String attachmentPath) throws GXMailException, IOException
	{
		InputStream is;
		String fileName = fileNamePath;

		if	(fileNamePath.lastIndexOf(File.separator) != -1)
			fileName = fileNamePath.substring(fileNamePath.lastIndexOf(File.separator) + 1);

   		try
   		{
   			is = new FileInputStream(attachmentPath + fileNamePath);
		}
		catch (FileNotFoundException e)
		{
			log ("11 - FileNotFound " + e.getMessage());
			throw new GXMailException("Can't find " + attachmentPath + fileNamePath, MAIL_InvalidAttachment);
		}

		println(getNextMessageIdMixed(sTime, false));
		println("Content-Type: " + "application/octet-stream");
		println("Content-Transfer-Encoding: " + "base64");
		println("Content-Disposition: " + "attachment; filename=\"" + GXMailer.getEncodedString(fileName) + "\"");
        println("");

	  	int BUFFER_SIZE = 4096;
		byte[] buffer = new byte[BUFFER_SIZE];
		OutputStream base64Output = new Base64OutputStream(outStream);
		int n = is.read(buffer, 0, BUFFER_SIZE);
		while (n >= 0) {
			base64Output.write(buffer, 0, n);
			n = is.read(buffer, 0, BUFFER_SIZE);
		}
		base64Output.flush();
		outStream.writeBytes(CRLF);
		outStream.flush();
	}

	private void println(String s) throws IOException
	{
		outStream.writeBytes(s + CRLF);
		outStream.flush();
		log(s);
	}

	private String doCommand(String commandString) throws IOException
    {
		// Send a command and wait for a response
		log(commandString);
        outStream.writeBytes(commandString + CRLF);
		outStream.flush();
		log("Write Ok");
        String response = getResponse();
        return response;
	}

	private char[] lineBuffer = new char[128];

    private String readLine() throws IOException
    {
		InputStream in = this.inStream;

		char buf[] = lineBuffer;

		if (buf == null)
		{
	    	buf = lineBuffer = new char[128];
		}

		int room = buf.length;
		int offset = 0;
		int c;

		loop:
		while (true)
		{
			c = in.read();
			logChar(c);
	    	switch (c )
	    	{
	      		case -1:
	      		case '\n':
					break loop;

	      		case '\r':
					int c2 = in.read();
					if (c2 != '\n')
					{
		    			if (!(in instanceof PushbackInputStream))
		    			{
							in = this.inStream = new PushbackInputStream(in);
		    			}
		    			((PushbackInputStream)in).unread(c2);
					}
					break loop;

	      		default:
					if (--room < 0)
					{
		    			buf = new char[offset + 128];
					    room = buf.length - offset - 1;
		    			System.arraycopy(lineBuffer, 0, buf, 0, offset);
					    lineBuffer = buf;
					}
					buf[offset++] = (char) c;
					break;
	    	}
		}

		if ((c == -1) && (offset == 0))
		{
	    	return null;
		}

		log("\nOUT: " + String.copyValueOf(buf, 0, offset));
		return String.copyValueOf(buf, 0, offset);
    }

	private String getResponse() throws IOException
    {
		// Get a response back from the server. Handles multi-line responses
		// and returns them as part of the string.
        String response = "";

        for (;;)
        {
			String line = readLine();

            if (line == null)
            {
            	throw new IOException("Bad response from server.");
            }

			// FTP response lines should at the very least have a 3-digit number

            if (line.length() < 3)
            {
            	throw new IOException("Bad response from server.");
            }
            response += line + CRLF;

			// If there isn't a '-' immediately after the number, we've gotten the
			// complete response. ('-' is the continuation character for FTP responses)

            if ((line.length() == 3) || (line.charAt(3) != '-'))
            {
            	return response;
			}
		}
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

	private void logChar(int text)
	{
		if	(DEBUG)
			if (logOutput != null)
			{
				logOutput.print((char) text);
				logOutput.flush();
			}
	}

}
