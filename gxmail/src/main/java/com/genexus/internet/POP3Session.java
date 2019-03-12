
package com.genexus.internet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.TimeZone;

import com.genexus.CommonUtil;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.platform.INativeFunctions;

public class POP3Session  implements GXInternetConstants,IPOP3Session
{
	private final int CONN_NORMAL = 0;
	private final int CONN_TLS = 1;
	private final int CONN_SSL = 2;

	private boolean DEBUG = GXInternetConstants.DEBUG;
	private PrintStream logOutput;

	private String user;
	private String password;
	private String attachmentsPath = "";
	protected String pop3Host = "192.168.0.1";
	protected int pop3Port = 110;
	private boolean displayMessages;
	private boolean deleteOnRead;
	private boolean readSinceLast;
    private boolean secureConnection;

	private int lastError;
	private int timeout;

	private int numOfMessages;
	private int lastReadMessage;

	protected BufferedReader in = null;
	protected PrintWriter out   = null;
	protected Socket socket     = null;
				
	public static final byte[] lineSeparator = System.getProperty("line.separator").getBytes();
	public static final String CRLF = "\r\n";

	private Boolean downloadAttachments = false;
	
	public POP3Session()
	{
		if	(DEBUG)
		{
			try
			{
				logOutput = new PrintStream(new FileOutputStream(new File("_gx_pop3.log")));
			}
			catch (IOException e)
			{
				System.out.println("Can't open POP3 log file pop3.log");
			}
		}
	}

	public int error()
	{
		return lastError;
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
	
		try
		{
			connectAndLogin();
		}
		catch (GXMailException e)
		{
			sessionInfo.exceptionHandler(e);
		}
	}

    private void connectAndLogin() throws GXMailException
    {
		if(secureConnection)
		{
			try
			{
				connectSSL();
			}
			catch(GXMailException e1)
			{
				if (socket != null)
				{
					closeSafe();
				}
				connectTLS();
			}
		}
		else
		{
			connectNormal();
		}
    }

    private void connectSSL() throws GXMailException
    {
		connect(CONN_SSL);
		login();
    }

    private void connectTLS() throws GXMailException
    {
		connect(CONN_TLS);
		login();
    }

    private void connectNormal() throws GXMailException
    {
		connect(CONN_NORMAL);
		login();
    }

    private Socket getConnectionSocket(int type) throws UnknownHostException, IOException
    {
			System.setProperty("HTTPClient.sslUseTLS", "false");
			InetAddress ipAddr = InetAddress.getByName(pop3Host.trim());
			switch(type)
			{
			  case CONN_NORMAL:
			    return new Socket(pop3Host.trim(), pop3Port);
			  case CONN_TLS:
			    System.setProperty("HTTPClient.sslUseTLS", "true");
			    return HTTPClient.SSLManager.getSSLConnection(false).getSSLSocket(ipAddr, pop3Port);
			  case CONN_SSL:
			    HTTPClient.ISSLConnection connection = HTTPClient.SSLManager.getSSLConnection(false);
			    return connection.processSSLSocket(connection.getSSLSocket(ipAddr, pop3Port), ipAddr.getHostName(), pop3Port);
			}
			return new Socket(pop3Host.trim(), pop3Port);
    }

	public void logout(GXPOP3Session sessionInfo)
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

	public void delete(GXPOP3Session sessionInfo)
	{
		try
		{
			dele(lastReadMessage);
		}
		catch (GXMailException e)
		{
			sessionInfo.exceptionHandler(e);
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
		// TODO: Aqui podria pasar que hubiera entrado un nuevo mail desde que empec�
		// a leer, y lo mas razonable seria leerlo. Eso implicaria chequear de nuevo
		// la cantidad de mensajes que existen, y compararlo con la cantidad de mensajes
		// leidos, dependiendo del parametro de si hay que borrar o no los mensajes.

		try
		{
			setAttachmentsPath(sessionInfo.getAttachDir());  // Obtengo el AttachmentsPath
			
			if	(lastReadMessage == numOfMessages)
				throw new GXMailException("No messages to receive", MAIL_NoMessages);
			
			MailMessage message = retr(++lastReadMessage, attachmentsPath);			
			QuotedPrintableDecoder dec = new QuotedPrintableDecoder();
						
			gxmessage.setFrom(MailRecipient.getFromString(message.getField(GXInternetConstants.FROM)));
			gxmessage.setTo(MailRecipientCollection.getFromString(message.getField(GXInternetConstants.TO).trim()));
			gxmessage.setCc(MailRecipientCollection.getFromString(message.getField(GXInternetConstants.CC).trim()));
			gxmessage.setReplyto(MailRecipientCollection.getFromString(message.getField(GXInternetConstants.REPLY_TO).trim()));

			try {
				Date d = new Date(message.getField(GXInternetConstants.DATE));
				d = SpecificImplementation.GXutil.DateTimefromTimeZone(d, TimeZone.getDefault().getID(), SpecificImplementation.Application.getModelContext());
				gxmessage.setDateSent(d);
			} catch (IllegalArgumentException e) {
				gxmessage.setDateSent(CommonUtil.nullDate());
			}
			try {
				
				Date d = new Date(message.getReceivedDate());				
				d = SpecificImplementation.GXutil.DateTimefromTimeZone(d, TimeZone.getDefault().getID(), SpecificImplementation.Application.getModelContext());
				gxmessage.setDateReceived(d);
			} catch (IllegalArgumentException e) {
				gxmessage.setDateReceived(gxmessage.getDateSent());
			}

			gxmessage.setSubject(dec.decodeHeader(message.getField(GXInternetConstants.SUBJECT)));
			gxmessage.setHeaders(message.getKeys());
			gxmessage.setText(message.getText());
                        gxmessage.setHtmltext(message.getHtmlText());
			gxmessage.setAttachments(StringCollection.getFromString(message.getAttachments()));
		}
		catch (GXMailException e)
		{
			sessionInfo.exceptionHandler(e);
		}
		catch (IOException e)
		{
			setError(e);
			sessionInfo.exceptionHandler(new GXMailException(e.getMessage(), MAIL_ConnectionLost));
		}
	}
		
	public void setDisplayMessages(int displayMessages)
	{
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

	void login() throws GXMailException
	{
		doCommand( "USER " + user);
		doCommand( "PASS " + password);

		numOfMessages = getValue("STAT");

                try
                {
                  lastReadMessage = readSinceLast?getValue("LAST"):0;
                }
                catch (GXMailException e)
                {
                  throw new GXMailException("POP3 server does not support NewMessages = 1", MAIL_LastNotSupported);
		}

		lastError = 0;
	}


	public String getNextUID() throws GXMailException
	{
		if	(lastReadMessage == numOfMessages)
			throw new GXMailException("No messages to receive", MAIL_NoMessages);
		
		int messageNum = lastReadMessage +1;
		String reply = doCommand("UIDL " + messageNum);
		int pos1  = reply.indexOf(' ');
		int pos2  = reply.indexOf(' ', pos1 + 1);
		return reply.substring(pos2).trim();
	}
	
	public int getMessageCount() throws GXMailException
	{
		int ret = getValue("STAT");

		if	 (readSinceLast)
			return ret - getValue("LAST");

		return ret;
	}

	public boolean isLoggedIn()
	{
		return socket != null;
	}

	public void logout() throws GXMailException
	{
		try
		{
			doCommand("QUIT");
			this.socket.close();
			socket = null;
		}
		catch (IOException e)
		{
			throw new GXMailException(e.getMessage(), MAIL_ConnectionLost);
		}
	}

    private void closeSafe()
    {
      try
      {
          this.socket.close();
          socket = null;
      }
      catch (IOException e)
      {
      }
    }

	/**
	 * Open a TCP socket to the server.
	 */

	private void connect(final int type) throws GXMailException
	{
	  	try
	  	{ 
	  		SpecificImplementation.NativeFunctions.getInstance().executeWithPermissions(
					new Runnable() {
						public void run()
						{
							try
							{
		  						socket = getConnectionSocket(type);
							}
							catch (IOException e)
							{
							}
						}
					}, INativeFunctions.CONNECT);

			if	(socket == null)
				throw new GXMailException("Can't connect to mail server", MAIL_CantLogin);

			socket.setSoTimeout(timeout * 1000);
			socket.setTcpNoDelay(true);
					
			InputStream sin = socket.getInputStream();

			in  = new BufferedReader(new InputStreamReader(sin));
			out = new PrintWriter(socket.getOutputStream());

			doCommand(null);					
		}
	  	catch(SocketException e)
	  	{
			throw new GXMailException("Error opening the socket connection. " + e.getMessage(), MAIL_CantLogin);
	  	} 
		catch(UnknownHostException e)
		{
			throw new GXMailException("Error while opening socket: Host Unknown: " + pop3Host + " " +e.getMessage(), MAIL_CantLogin);
		}
		catch(IOException e)
		{
			throw new GXMailException("Error while trying to read or write. " + e.getMessage(), MAIL_CantLogin);
		}
	}

	private void dele(int i) throws GXMailException
	{
		doCommand( "DELE " + i);
	}

	private MailMessage retr(int i, String attachmentPath) throws GXMailException
	{
		doCommand("RETR " + i);
		return new MailMessage(new RFC822Reader(new RFC822EndReader(in, logOutput)), attachmentPath, this.downloadAttachments);
	}

	private int getValue(String command) throws GXMailException
	{
  		int pos1, pos2, res;
		String reply = doCommand(command);
				
		// Get the number of messages - Sample reply="+OK 2 234"

		reply = reply.trim();
		pos1  = reply.indexOf(' ');
		pos2  = reply.indexOf(' ', pos1 + 1);
		if	(pos2 > 0)
			res   = Integer.parseInt(reply.substring(pos1, pos2).trim());    
		else
			res   = Integer.parseInt(reply.substring(pos1).trim());    
		


	  return res;
	}

	private void msg(String msg)
	{
		System.err.println(msg);
	}

	private void setError(Exception e)
	{
		lastError = 1;
		if	(displayMessages) 
			msg(e.getMessage());
	}

	private void log(String text)
	{
		if	(DEBUG)
			if (logOutput != null)
				logOutput.println(text);
	}

	protected String doCommand(String commandString) throws GXMailException
    {
		try
		{
			if	(commandString != null)
			{
				if	(DEBUG)
					if	(!commandString.startsWith("PASS"))
						log("OUT : " + commandString);
					else
						log("OUT : PASS *****");

				out.print(commandString);
				out.print(CRLF);
				out.flush();
			}

			String reply = in.readLine();
			if (reply == null)
				throw new GXMailException("Server reply invalid: ", MAIL_ServerReplyInvalid);			
			reply = reply.trim();

                        if((commandString != null) && commandString.startsWith("RETR") && (reply.length() == 0))
                        {//Esto es porque hay casos en que antes de la respuesta viene una linea, en particular esta
                         //pasando con gmail.
                          String otherLine = in.readLine();
                          if (otherLine != null) { reply = otherLine.trim(); }
                        }

			if	(DEBUG)
				log("IN : " + reply);
		  
		  	// code change for ver 2.0 wherein there need not 
		  	// be any error message along with the error reply
		  	String serverReply = "";

		  	if ((reply.indexOf(' ')) != -1)
				serverReply = reply.substring(reply.indexOf(' '));
				
			// end code change		
		  
		  	if (reply.startsWith("-ERR")) 
			{
				throw new GXMailException("Server replied with an error: " + serverReply, MAIL_ServerRepliedErr);
			}

		  	if (reply.startsWith("+OK"))
				return reply;

			throw new GXMailException("Server reply invalid: " + reply, MAIL_ServerReplyInvalid);
		}
		catch (IOException e)
		{
			throw new GXMailException(e.getMessage(), MAIL_ConnectionLost);
		}
	}
}




/*
------=_NextPart_000_0003_01BEB695.8E0E8090
Content-Type: application/x-msexcel;
        name="XlsRep.xls"
Content-Transfer-Encoding: base64
Content-Disposition: attachment;
        filename="XlsRep.xls"

------=_NextPart_000_0003_01BEB695.8E0E8090
Content-Type: image/gif;
        name="BannerFILE1.gif"
Content-Transfer-Encoding: base64
Content-Disposition: attachment;
        filename="BannerFILE1.gif"

------=_NextPart_000_0003_01BEB695.8E0E8090
Content-Type: application/msword;
        name="Web Transactions.doc"
Content-Transfer-Encoding: base64
Content-Disposition: attachment;
        filename="Web Transactions.doc"

------=_NextPart_000_0003_01BEB695.8E0E8090
Content-Type: text/html;
        name="tst.html"
Content-Transfer-Encoding: quoted-printable
Content-Disposition: attachment;
        filename="tst.html"
*/