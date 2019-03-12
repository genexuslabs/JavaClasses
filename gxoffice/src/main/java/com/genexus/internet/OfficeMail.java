package com.genexus.internet;

import com.genexus.gxoffice.MAPISession;
import com.genexus.gxoffice.IOfficeMail;
import com.genexus.gxoffice.OutlookSession;

public class OfficeMail implements IMailImplementation
{
	static
	{
		GXMailer.registerImplementation(GXMailer.OFFICE, new OfficeMailerFactory());
	}
	private IOfficeMail mail;
	private String 		mode;

	private IOfficeMail getMail() 
	{
		if	(mail == null)
		{
			if	(mode.equalsIgnoreCase("O"))
			{
				mail = new OutlookSession();
			}
			else
			{
				mail = new MAPISession();
			}
		}

		return mail;
	}

	public void cleanup()
	{
		if	(mail != null)
		{	
			mail.cleanup();
			mail = null;
		}	
	}

	private void throwOnError() throws GXMailException
	{
		if	(mail.getErrCode() != 0)
			throw new GXMailException(mail.getErrDescription(), mail.getErrCode());
	}

	public void POP3Login(String host, int port, String user, String password, int readSinceLast, int deleteOnRead, int timeout) 
	{
    	//getMail().LoginPOP3(handle, host, user, password, (short) readSinceLast, (short) deleteOnRead, (short) timeout);
	}

	public int getMessageCount() 
	{
		return (int) getMail().getCount();
	}

	public void receive(GXMailMessage msg)
	{
		getMail().Receive(msg);

		if	(markAsRead != 0)
			getMail().MarkAsRead();

		//throwOnError();
	}

	public void setAddressFormat(int format) 
	{
/*		try
		{
			getMail().setAddressFormat(handle, (short) format);
		}
		catch (GXMailException e)
		{
			System.err.println("setAddressFormat" + e.getMessage());
		}
*/	}

	public void setAttachDir(String dir) 
	{
		 getMail().setAttachDir(dir);
	}

	public void SMTPLogin(String host, int port, String name, String address, String user, String password, int timeout, int authentication) 
	{
	}

	public void send(GXMailMessage msg)
	{
    	getMail().Send(msg);
	}

	public void logout()
	{
    	((MAPISession) getMail()).Logout();
	}

	public void SMTPLogout()
	{
	}


	public void MAPIChangeFolder(String folder, int newMessages, int markAsRead) 
	{
		getMail().setNewMessages((short) newMessages);
		this.markAsRead = (short) markAsRead;
   		
   		getMail().ChangeFolder(folder);
	}

	public void MAPIEditWindow(int val) 
	{
	    getMail().setEditWindow((short) val);
	}

	short markAsRead = 0;
	public void MAPILogin(String profile, int newMessages, int markAsRead) 
	{
		getMail().setNewMessages((short) newMessages);
		this.markAsRead = (short) markAsRead;

		((MAPISession) getMail()).setProfile(profile);
    	((MAPISession) getMail()).Login();
	}
	
	public void displayMessage(String message)
	{
	   //	System.err.println(message);
	}

	public void setMode(String mode)
	{
		this.mode = mode;
	}

	public String getErrDescription()
	{
		return getMail().getErrDescription();
	}

	public int getErrCode()
	{
		return getMail().getErrCode();
	}

}