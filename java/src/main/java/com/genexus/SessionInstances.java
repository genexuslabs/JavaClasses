
package com.genexus;

import com.genexus.internet.GXFTPSafe;
import com.genexus.internet.GXMailer;
import com.genexus.util.DelimitedFilesSafe;
import com.genexus.xml.XMLWriter;

public class SessionInstances implements ISessionInstances
{
	private GXMailer mailer;
	private GXFTPSafe ftp;
	private XMLWriter xml;
	private DelimitedFilesSafe delimited;

	public IGXMailer getMailer()
	{
		if	(mailer == null)
			mailer = new GXMailer();

		return mailer;
	}

	public IGXFTPSafe getFTP()
	{
		if	(ftp == null)
		{
			ftp = new GXFTPSafe();
		}

		return ftp;
	}

	public IXMLWriter getXMLWriter()
	{
		if	(xml == null)
			xml  = new XMLWriter();

		return xml;
	}
	
	public IDelimitedFilesSafe getDelimitedFiles()
	{
		if	(delimited == null)
			delimited = new DelimitedFilesSafe();

		return delimited;
	}

	public void cleanup()
	{
		if	(mailer != null)
			mailer.cleanup();
	}
}