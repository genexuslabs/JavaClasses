// $Log: InternetMail.java,v $
// Revision 1.1  2001/09/27 18:10:52  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/09/27 18:10:52  gusbro
// GeneXus Java Olimar
//
package com.genexus.internet;


import java.util.Date;

public class InternetMail implements IMailImplementation, GXInternetConstants
{
	private GXSMTPSession smtp;
	private GXPOP3Session pop3;

	private GXSMTPSession getSMTP()
	{
		if	(smtp == null)
		{
			smtp = new GXSMTPSession();
		}

		return smtp;
	}

	private GXPOP3Session getPOP3()
	{
		if	(pop3 == null)
		{
			pop3 = new GXPOP3Session();
		}

		return pop3 ;
	}
	
	private boolean pop3delete = false;

	public void POP3Login(String host, int port, String user, String password, int readSinceLast, int deleteOnRead, int timeout) 
	{
		GXPOP3Session pop3 = getPOP3();

		pop3.setHost(host);
		pop3.setUserName(user);
		pop3.setPort(port);
		pop3.setPassword(password);
		pop3.setNewMessages(readSinceLast);
		pop3.setTimeout(timeout);
		pop3delete = deleteOnRead != 0;

		pop3.login();
	}

	public int getMessageCount() 
	{
		return getPOP3().getItemCount();
	}

	public void receive(GXMailMessage msg)
	{
		getPOP3().receive(msg);
		if	(pop3delete)
		{
			getPOP3().delete();
		}
	}

	public void setAttachDir(String dir) 
	{
		getPOP3().setAttachDir(dir);
	}

	public void SMTPLogin(String host, int port, String name, String address, String user, String password, int timeout, int authentication) 
	{
		GXSMTPSession smtp = getSMTP();

		smtp.setHost(host);
		smtp.setPort(port);
		smtp.getSender().setName(name);
		smtp.getSender().setAddress(address);
		smtp.setUserName(user);
		smtp.setPassword(password);
		smtp.setTimeout(timeout);
		smtp.setAuthentication(authentication);

		smtp.login();
	}

	public void send(GXMailMessage msg)
	{
		smtp.send(msg);
	}

	public void logout() 
	{
		if	(smtp != null)
			getSMTP().logout();

		if	(pop3 != null)
			getPOP3().logout();
	}

	public void SMTPLogout() 
	{
		getSMTP().logout();
	}


	public void MAPIChangeFolder(String folder, int newMessages, int markAsRead) 
	{
		//throw new GXMailException("Can't change folder in Internet mail mode", MAIL_InvalidMode);
	}

	public void MAPIEditWindow(int val) 
	{
		if	(val == 1)
		{
			System.err.println("Can't show mail window in internet mode");
		}
	}

	public void MAPILogin(String profile, int newMessages, int markAsRead) //throws GXMailException
	{
		//throw new GXMailException("Can't do a MAPI login in Internet mail mode", MAIL_InvalidMode);
	}
	
	public void displayMessage(String message)
	{
		System.err.println(message);
	}

	public void setMode(String mode)
	{
	}

	public String getErrDescription()
	{
		if	(pop3 != null)
			return pop3.getErrDescription();
		
		if	(smtp != null)
			return smtp.getErrDescription();

		return "";
	}

	public int getErrCode()
	{
		if	(pop3 != null)
			return pop3.getErrCode();
		
		if	(smtp != null)
			return smtp.getErrCode();

		return 0;
	}

	public void cleanup()
	{
	}
}

