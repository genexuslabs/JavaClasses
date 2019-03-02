// $Log: GXMailer.java,v $
// Revision 1.1  2002/04/18 19:26:48  gusbro
// Initial revision
//
// Revision 1.1.1.1  2002/04/18 19:26:48  gusbro
// GeneXus Java Olimar
//
package com.genexus.internet;


import com.genexus.CommonUtil;

import gxmail.Configuration;

import java.io.IOException;
import java.util.Hashtable;

public class GXMailer
{
	private IMailImplementation mailer;
	private String mode = "";
	private int lastError;
	private boolean  displayMessages;
	private int addressFormat;
	public static final String OFFICE = "Office";
	
	private static Hashtable<String, IMailImplementationFactory> MailProviders = new Hashtable<String, IMailImplementationFactory>() ;

	public GXMailer()
	{
		gxmmode("I");
	}

	public int gxmerror(int[] error)
	{
		error[0] = lastError;
		return lastError;	
	}

	public void gxmdspmsg (int value, int[] ret)
	{
		gxmdspmsg(value);
	}

	public void gxmdspmsg (int value)
	{
		displayMessages = (value != 0);
	}


	public void gxmmode(String mode, int[] out)
	{
		out[0] = gxmmode(mode);		
	}

	public int gxmmode(String mode)
	{
		lastError = 0;

		mode = mode.toUpperCase();

		if	(!this.mode.equals(mode))
		{
			if	(mailer != null)
			{
				mailer.cleanup();
			}

			if	(mode.equals("I"))
			{
				mailer = new InternetMail();
				this.mode = mode;
			}
			else if (mode.equals("M") || mode.equals("O"))
			{
				mailer = MailProviders.get(OFFICE).createImplementation();;
				mailer.setMode(mode);
				this.mode = mode;
			}
			else
			{
				lastError = 100;
			}
		}
		
		return lastError;
	}
		
	public void gxmchangefolder(String folder, int newMessages, int markAsRead, int[] ret) 
	{
		ret[0] = gxmchangefolder(folder, newMessages, markAsRead);
	}

	public int gxmchangefolder(String folder, int newMessages, int markAsRead) 
	{
		mailer.MAPIChangeFolder(folder, newMessages, markAsRead);
		exceptionHandler();

		return mailer.getErrCode();
	}

	public void gxmeditwindow (int val, int ret[])
	{
		ret[0] = gxmeditwindow(val);
	}

	public int gxmeditwindow (int val)
	{
		mailer.MAPIEditWindow(val);
		return lastError;
	}

	public void gxmloginmapi(String profile, int newMessages, int[] ret) 
	{
		ret[0] = gxmloginmapi(profile, newMessages);	
	}

	public int gxmloginmapi(String profile, int newMessages) 
	{
		return gxmloginmapi(profile, newMessages, 1);
	}

	public int gxmloginmapi(String profile)
	{
		return gxmloginmapi(profile, 1, 1);
	}

	public void gxmloginmapi(String profile, int newMessages, int markAsRead, int ret[]) 
	{
		ret[0] = gxmloginmapi(profile, newMessages, markAsRead);
	}

	public int gxmloginmapi(String profile, int newMessages, int markAsRead) 
	{
		mailer.MAPILogin(profile, newMessages, markAsRead);
		exceptionHandler();
		return mailer.getErrCode();
	}

	public void gxmloginpop3(String host, String user, String password, int newMessages, int delete, int timeout, int[] ret) 
	{
		ret[0] = gxmloginpop3(host, user, password, newMessages, delete, timeout) ;
	}

	public int gxmloginpop3(String host, String user, String password, int newMessages, int delete, int timeout) 
	{
		mailer.POP3Login(host, CommonUtil.getPort(host, 110), user, password, newMessages, delete, timeout);
		exceptionHandler();
		return mailer.getErrCode();
	}

	public void gxmloginsmtp(String host, String name, String address, String user, String password, int timeout, int[] ret) 
	{
		ret[0] = gxmloginsmtp(	host, name, address, user, password, timeout);
	}

	public int gxmloginsmtp(String host, String name, String address, String user, String password) 
	{
		return gxmloginsmtp(host, name, address, user, password, 30);
	}

	public int gxmloginsmtp(String host, String name, String address) 
	{
		return gxmloginsmtp(host, name, address, "", "", 30);
	}


	public int gxmloginsmtp(String host, String name, String address, String user, String password, int timeout) 
	{
		mailer.SMTPLogin(host, CommonUtil.getPort(host, 25), name, address, user, password, timeout, password.length() > 0?1:0);
		exceptionHandler();
		return mailer.getErrCode();
	}

	public void gxmsend(String to, String cc, String bcc, String subject, String message, String attachments, int[] ret) 
	{
		ret[0] = gxmsend(to, cc, bcc, subject, message, attachments);
	}

	public int gxmsend(String to, String cc, String bcc, String subject, String message) 
	{
		return gxmsend(to, cc, bcc, subject, message, "");
	}

	public int gxmsend(String to, String cc, String bcc, String subject, String message, String attachments) 
	{
		GXMailMessage msg = new GXMailMessage();
		try
		{
			msg.setTo(MailRecipientCollection.getFromString(to));
			msg.setCc(MailRecipientCollection.getFromString(cc));
			msg.setBcc(MailRecipientCollection.getFromString(bcc));
			msg.setSubject(subject);
			msg.setText(message);
			msg.setAttachments(StringCollection.getFromString(attachments));
			mailer.send(msg);
			exceptionHandler();
		}
		catch (IOException e)		
		{
		}
		return mailer.getErrCode();
	}

	public int gxmloff()
	{
		return gxmlogout();
	}

	public void gxmlogout(int[] out) 
	{
		out[0] = gxmlogout();
	}

	public int gxmlogout() 
	{
		mailer.logout();
		exceptionHandler();
		return mailer.getErrCode();
	}

	public int gxmreceive(String[] from, String[] to, String[] cc,  String[] subject, String[] text) 
	{
		return gxmreceive(from, to, cc, subject, text, new String[] { "" }, new java.util.Date[] { new java.util.Date()}, new java.util.Date[] { new java.util.Date()});
	}
	
	public int gxmreceive(String[] from, String[] to, String[] cc,  String[] subject, String[] text, String[] attach) 
	{
		return gxmreceive(from, to, cc, subject, text, attach, new java.util.Date[] { new java.util.Date()}, new java.util.Date[] { new java.util.Date()});
	}

	public int gxmreceive(String[] from, String[] to, String[] cc,  String[] subject, String[] text, String[] attach, java.util.Date[] sent) 
	{
		return gxmreceive(from, to, cc, subject, text, attach, sent, new java.util.Date[] { new java.util.Date()});
	}

	public void gxmreceive(String[] from, String[] to, String[] cc,  String[] subject, String[] text, String[] attach, java.util.Date[] sent, java.util.Date[] received, int[] ret) 
	{
		ret[0] = gxmreceive(from,  to, cc,  subject, text, attach, sent, received);
	}

	public int gxmreceive(String[] from, String[] to, String[] cc,  String[] subject, String[] text, String[] attach, java.util.Date[] sent, java.util.Date[] received) 
	{
		GXMailMessage msg = new GXMailMessage();
		
		mailer.receive(msg);

		from[0]    	= msg.getFrom().getRecipientString(addressFormat);
		to[0] 		= msg.getTo().getRecipientsString(addressFormat);
		cc[0] 		= msg.getCc().getRecipientsString(addressFormat);
		subject[0] 	= msg.getSubject();
		text[0] 	= msg.getText();
		attach[0] 	= msg.getAttachments().getString();
		sent[0] 	= msg.getDateSent();
		received[0] = msg.getDateReceived();
		
		exceptionHandler();

		return lastError;
	}
	
	public void gxmcount(int[] count, int[] ret) 
	{
		ret[0] = gxmcount(count);
	}

	public int gxmcount(int[] count) 
	{
		count[0] = mailer.getMessageCount();
		exceptionHandler();
		return mailer.getErrCode();
	}

	public void gxmaddressformat(int format, int[] ret)
	{
		ret[0] = gxmaddressformat(format);
	}

	public int gxmaddressformat(int format)
	{
		addressFormat = format;
		return mailer.getErrCode();
	}

	public void gxmattachdir (String dir, int[] ret)
	{
		ret[0] = gxmattachdir(dir);
	}

	public int gxmattachdir (String dir)
	{
		mailer.setAttachDir(dir);
		exceptionHandler();
		return mailer.getErrCode();
	}

	private void exceptionHandler()
	{
		lastError = mailer.getErrCode();

		if	(lastError != 0 && displayMessages)
		{
			mailer.displayMessage(mailer.getErrDescription());
		}
	}

	public void cleanup()
	{
		mailer.cleanup();
	}

	private String fromName;
	public int gxmlon(String fromName)
	{
		this.fromName = fromName;
		return lastError;
	}

	public int gxmlgout()
	{
		return gxmlogout();
	}

	public int gxmsnd(String toName, String mySubject, String myText, int UI)
	{
		return gxmsndb(fromName, toName, mySubject, myText, UI);
	}

	public int gxmsndb(String fromName, String to, String subject, String message, int window)
	{
		gxmeditwindow(window);
		mailer.SMTPLogin(getHost(), CommonUtil.getPort(getHost(), 25), fromName, fromName, "", "", 30, 0);
		int ret = gxmsend(to, "", "", subject, message);
		mailer.SMTPLogout();

		return ret;
	}

	public int gxmsend(String to, String cc, String bcc, String subject, String message, String attachments, int dummy, int window, String fromName)
	{
		gxmeditwindow(window);
		mailer.SMTPLogin(getHost(), CommonUtil.getPort(getHost(), 25), fromName, fromName, "", "", 30, 0);
		int ret = gxmsend(to, cc, bcc, subject, message, attachments);
		mailer.SMTPLogout();

		return ret;
	}

	private static String getHost()
	{
		String host ;
	
		host = Configuration.Preferences.getSMTP_HOST();

		if (host.length() == 0)
		{
			System.err.println("No SMTP server specified");
			host = "localhost";	 
		}

		return CommonUtil.getHost(host);
	}
	
	private static MimeEncoder mimeEncoder = new MimeEncoder();
	
	protected static String getEncodedString(String s)
    {
		try
		{
			String charset = getCharset();
			if(charset!= null && !charset.equals(""))
			{
				return mimeEncoder.encodeText(s, charset);
			}else
			{
				if (isAscii(s))
					return s;
				else 
					return mimeEncoder.encodeText(s, "UTF-8");
			}
		}
		catch(java.io.UnsupportedEncodingException ex) {}
        return s;
	}
	
	private static String getCharset()
	{
        try
        {
          String language = gxmail.Configuration.Preferences.getLANGUAGE();
          if (language.equalsIgnoreCase("jap")) {
            return "ISO-2022-JP";
          }
        }
        catch(Throwable e) {}
		return null;
	}

    private static boolean isAscii(String text)
    {
		for (int i = 0; i < text.length(); i++) {
			int c = text.charAt(i);
			if (c > 127)
			{
				return false;
			}
		}
        return true;
    }

	public static void registerImplementation(String key, IMailImplementationFactory mailFactory) {
		MailProviders.put(key, mailFactory);
	}

}