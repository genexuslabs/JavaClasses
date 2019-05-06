package com.genexus.webpanels;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.genexus.CommonUtil;
import com.genexus.ModelContext;
import com.genexus.PrivateUtilities;
import com.genexus.internet.HttpContext;
import com.genexus.internet.HttpContextNull;

public abstract class GXStaticWebPanel extends GXWebPanel
{
	private static Hashtable visitedLinks = new Hashtable();
	private static Vector copyList = new Vector();

	protected static String extension = "html";
	private String fileName ;

	public static final String STATIC_DIR      = "genexus.staticweb.dir";
	public static final String STATIC_DYNURL   = "genexus.staticweb.dynurl";
	public static final String STATIC_OVER     = "genexus.staticweb.overwrite";
	public static final String STATIC_LINKS    = "genexus.staticweb.links";
	public static final String STATIC_COPYDIR  = "genexus.staticweb.copydir";

	private static long startTime = System.currentTimeMillis();

	HttpContext oldHttpContext;

	public GXStaticWebPanel(HttpContext httpContext)
	{
		super(httpContext);
	}

	public GXStaticWebPanel(int remoteHandle, ModelContext context)
	{
		super(remoteHandle, context);
		oldHttpContext = (HttpContext) context.getHttpContext();
		httpContext = new HttpContextNull();

		context.setHttpContext(httpContext);
		httpContext.setBuffered(false);
	}

	protected void createFile(String fileName)
	{
	  	try
	  	{
			visitedLinks.put(fileName, "");
			((HttpContext) context.getHttpContext()).setOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));

			addMessage("Creating " + fileName + "...");

			this.fileName = fileName;
	  	}
	  	catch (IOException e)
	  	{
			throw new RuntimeException(e.getMessage());
	  	}
	}

	public static byte copyFiles()
	{
		String outputDir = PrivateUtilities.addLastPathSeparator(PrivateUtilities.getSystemProperty(STATIC_COPYDIR, ""));

		if	(!outputDir.trim().equals(""))
		{
			for (Enumeration en = copyList.elements(); en.hasMoreElements(); )
			{
				String fileName = (String) en.nextElement();
				System.out.println("Copying " + getStaticDir() + fileName + " to " + outputDir + fileName);
				PrivateUtilities.copyFileRetry(getStaticDir() + fileName, outputDir + fileName);
			}	
		}

		visitedLinks = new Hashtable();

		copyList.removeAllElements();
		return 0;
	}

	protected void addFileToCopy(String file)
	{	
		if	(!PrivateUtilities.getSystemProperty(STATIC_COPYDIR, "").equals(""))
			copyList.addElement(file);
	}

	protected int processParameters(String args[])
	{
		return 0;
	}

	public static byte isStaticOverwrite()
	{
		return PrivateUtilities.getSystemProperty(STATIC_OVER, "true").equals("true")?(byte) 1:0;
	}

	public static byte isExpandLinks()
	{
		return PrivateUtilities.getSystemProperty(STATIC_LINKS, "true").equals("true")?(byte) 1:0;
	}

	public static byte isStaticCreated(String link)
	{
		return visitedLinks.get(link) == null? (byte) 0 :(byte) 1;
	}

	protected byte wasVisited(String link)
	{
		if	( (PrivateUtilities.getSystemProperty(STATIC_OVER, "true").equals("false") && new File(getStaticDir() + link).exists()) || 
			  (PrivateUtilities.getSystemProperty(STATIC_LINKS, "true").equals("false")) )
		{
		 	return 1;
		}

		return visitedLinks.get(link) == null? 0 :(byte) 1;
	}

	protected static String getTime()
	{
		Date d = new Date();
		return 	   CommonUtil.padl(""+ d.getHours(), 2, "0") + ":" +
				   CommonUtil.padl(""+ d.getMinutes(), 2, "0") + ":" + 
				   CommonUtil.padl(""+ d.getSeconds(), 2, "0");
	}

	protected String encodeStaticParm(String parm)
	{
		return java.net.URLEncoder.encode(parm);
	}

	public static String getStaticDir()
	{
		return PrivateUtilities.addLastPathSeparator(WebUtils.getSystemProperty(STATIC_DIR));
	}

	private void addMessage(String msg)
	{
		System.out.println(msg);
	}

	protected void cleanup()
	{
		if	(isStaticGeneration)
			addMessage("Closing " + fileName + "...");

		super.cleanup();
		context.setHttpContext(oldHttpContext);
	}
}