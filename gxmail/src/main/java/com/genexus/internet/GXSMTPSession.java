
package com.genexus.internet;

public class GXSMTPSession 
{
	private ISMTPSession session ;
	private String host;
	private int port;
	private int errDisplay;
	private int authentication;
    private int secure;
	private String attachDir;
	private String userName;
	private String password;
	private int timeout;
	private int errCode;
	private String errDescription;
	private MailRecipient sender;
	private String authenticationProtocol;

	public GXSMTPSession()
	{
		try
		{		
			Class c = Class.forName("javax.mail.Session");
			session = new SMTPSessionJavaMail();
		}
		catch(Throwable e)
		{
			session = new SMTPSession();
		}

		setHost("");
		setPort(25);
		setAttachDir("");
		setUserName("");
		setPassword("");
		setTimeout(30);
		setAuthenticationProtocol("");
		sender = new MailRecipient();
		
		resetError();
	}

	public void setHost( String host )
	{
		this.host = host;
	}

	public String getHost()
	{
		return host;
	}

	public void setPort( int port)
	{
		this.port = port;
	}

	public short getPort()
	{
		return (short) port;
	}

	public void setErrDisplay(int errDisplay)
	{
		this.errDisplay = errDisplay;
	}

	public byte getErrDisplay()
	{
		return (byte) errDisplay;
	}

	public void setAuthentication(int authentication)
	{
		this.authentication = authentication;
	}

	public byte getAuthentication()
	{
		return (byte) authentication;
	}

    public void setSecure(int secure)
    {
		this.secure = secure;
    }

    public byte getSecure()
    {
		return (byte) secure;
    }

	public void setAttachDir(String attachDir)
	{
		this.attachDir = attachDir;
	}

	public String getAttachDir()
	{
		return attachDir;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public String getUserName()
	{
		return userName;
	}

	public void setSender(MailRecipient sender)
	{
		this.sender = sender;
	}

	public MailRecipient getSender()
	{
		return sender;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getPassword()
	{
		return password;
	}

	public void setAuthenticationProtocol(String authenticationProtocol)
	{
		this.authenticationProtocol = authenticationProtocol;
	}

	public String getAuthenticationProtocol()
	{
		return this.authenticationProtocol;
	}
		
	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	public short getTimeout()
	{
		return (short) timeout;															  
	}

	public short login()
	{
		resetError();

		session.login(this);
		return getErrCode();
	}

	public short send(GXMailMessage msg)
	{
		resetError();

		session.send(this, msg);
		return getErrCode();
	}

	public short logout()
	{
		session.logout(this);
		return getErrCode();
	}

	public short getErrCode()
	{
		return (short) errCode;
	}

	public String getErrDescription()
	{
		return errDescription;
	}

	public void resetError()
	{
		errCode = 0;
		errDescription = "";
	}

	public void exceptionHandler(GXMailException e)
	{
		errCode = e.getErrorCode();
		errDescription = e.getMessage();

		if	(errDisplay != 0)
			displayMessage(e.getMessage());
	}

	private void displayMessage(String message)
	{
		System.err.println(message);
	}
}

