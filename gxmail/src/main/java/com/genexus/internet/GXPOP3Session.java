
package com.genexus.internet;

public class GXPOP3Session implements GXInternetConstants
{
	private IPOP3Session session;

	private String host;
	private short port;
	private String userName;
	private String password;
	private short timeout;

	private String attachDir;
	private short newMessages ;
    private short secure;
	private short delete;
	private short itemCount;

	private short errCode;
	private String errDescription;
	private short errDisplay;

	public GXPOP3Session()
	{
		try
		{		
			Class c = Class.forName("javax.mail.Session");
			session = new POP3SessionJavaMail();
		}
		catch(Throwable e)
		{
			session = new POP3Session();
		}
		

		setHost("");
		setPort(110);
		setAttachDir("");
		setUserName("");
		setPassword("");
		setTimeout(30);

		errDescription = "";
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public short getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = (short) port;
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getPassword()
	{
		return password;
	}

    public void setSecure(short secure)
    {
		this.secure = secure;
    }

    public short getSecure()
    {
		return secure;
    }

	public short getTimeout()
	{
		return timeout;
	}
	
	public void setTimeout(int timeout)
	{
		this.timeout = (short) timeout;
	}

	public String getAttachDir()
	{
		return attachDir;
	}

	public void setAttachDir(String attachDir)
	{
		this.attachDir = attachDir;
	}

	public short getNewMessages()
	{
		return newMessages;
	}

	public void setNewMessages(int newMessages)
	{
		this.newMessages = (short) newMessages;
	}

	public short getItemCount()
	{
		try
		{
			return (short) session.getMessageCount();
		}
		catch (GXMailException e)
		{
			exceptionHandler(e);
			return -1;
		}
	}

	public short getErrCode()
	{
		return errCode;
	}

	public String getErrDescription()
	{
		return errDescription;
	}

	public short getErrDisplay()
	{
		return errDisplay;
	}
	
	public void setErrDisplay(int errDisplay)
	{
		this.errDisplay = (short) errDisplay;
	}

    public short login()
	{
		session.login(this);
		return getErrCode();
	}

    public short logout()
	{
		session.logout(this);
		return getErrCode();
	}

    public short skip()
	{
		session.skip(this);
		return getErrCode();
	}

	public short getNextUID(String[] response)
	{
		try
		{
			String UID = session.getNextUID();
			response[0] = UID;
		}
		catch (GXMailException e)
		{
			exceptionHandler(e);
		}
		return getErrCode();
	}
		
    public short receive(GXMailMessage msg)
	{
		session.receive(this, msg);
		return getErrCode();
	}

	public short delete()
	{
		session.delete(this);
		return getErrCode();
	}

	public void exceptionHandler(GXMailException e)
	{
		errCode = (short) e.getErrorCode();
		errDescription = e.getMessage();

		if	(errDisplay != 0)
			displayMessage(e.getMessage());
	}

	private void displayMessage(String message)
	{
		System.err.println(message);
	}
}

