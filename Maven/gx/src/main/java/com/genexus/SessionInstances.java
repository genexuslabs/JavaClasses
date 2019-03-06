// $Log: SessionInstances.java,v $
// Revision 1.1  2001/10/30 17:13:22  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/10/30 17:13:22  gusbro
// GeneXus Java Olimar
//
package com.genexus;

import com.genexus.internet.GXFTPSafe;
import com.genexus.internet.GXMailer;
import com.genexus.util.DelimitedFilesSafe;
import com.genexus.xml.XMLWriter;

public class SessionInstances
{
	private GXMailer mailer;
	private GXFTPSafe ftp;
	private XMLWriter xml;
	private DelimitedFilesSafe delimited;

	public GXMailer getMailer()
	{
		if	(mailer == null)
			mailer = new GXMailer();

		return mailer;
	}

	public GXFTPSafe getFTP()
	{
		if	(ftp == null)
		{
			ftp = new GXFTPSafe();
		}

		return ftp;
	}

	public XMLWriter getXMLWriter()
	{
		if	(xml == null)
			xml  = new XMLWriter();

		return xml;
	}
	
	public DelimitedFilesSafe getDelimitedFiles()
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