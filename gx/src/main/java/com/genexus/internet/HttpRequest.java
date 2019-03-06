// $Log: HttpRequest.java,v $
// Revision 1.3  2004/10/27 20:54:15  dmendez
// Soporte de upload control
//
// Revision 1.2  2004/07/29 19:30:21  iroqueta
// Arreglo para que funcione bien la ScriptName
//
// Revision 1.1  2002/04/19 18:10:30  gusbro
// Initial revision
//
// Revision 1.1.1.1  2002/04/19 18:10:30  gusbro
// GeneXus Java Olimar
//
package com.genexus.internet;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Date;
import java.util.Enumeration;

/**
* Esta clase esta disponible en los webprocs para leer la informacion del request.
*/
import com.genexus.CommonUtil;
import com.genexus.webpanels.FileItemCollection;
import com.genexus.webpanels.HttpContextWeb;

public abstract class HttpRequest implements IHttpRequest
{	
	public final int ERROR_IO = 1;
	private int errCode;
	private String errDescription;
	private String Referrer = "";
	protected HttpContext httpContext;
	
	public HttpRequest(HttpContext httpContext)
	{
		this.httpContext = httpContext;
		resetErrors();
	}

	public String getRepositoryPath()
	{
	  return httpContext.getRepositoryPath();
	}

	public void setRepositoryPath( String path)
	{
	  httpContext.setRepositoryPath( path);
	}

	public FileItemCollection getPostedparts()
	{
	  return ((HttpContextWeb)httpContext).getPostedparts();
	}

	public String getMethod()
	{
		return httpContext.getRequestMethod();
	}
	
	public abstract String getRequestURL();

	public String getHeader(String name)
	{
		String ret = httpContext.getHeader(name);
		return (ret == null)?"":ret;
	}

	public void getHeader(String name, String[] value)
	{
		String ret = httpContext.getHeader(name);
		value[0] = (ret == null)?"":ret;
	}

	public String getQuerystring()
	{
		return httpContext.getQueryString();
	}

	public void setReferrer( String value)
	{
		Referrer = value;
	}
	public String getReferer()
	{
		String referer = httpContext.getReferer();
		return 	 ("".equals(referer)) ? Referrer:referer;
	}

	public String getReferrer()
	{
		return getReferer();
	}

	public String getServerHost()
	{
		return httpContext.getServerName();
	}

	public int getServerPort()
	{
		return httpContext.getServerPort();
	}

	public byte getSecure()
	{
		return (byte) httpContext.getHttpSecure();
	}

	public String getScriptPath()
	{
		return httpContext.getScriptPath();
	}

	public String getRemoteAddress()
	{
		return httpContext.getRemoteAddr();
	}

	public abstract String getScriptName();
	
	public String getBaseURL()
	{
		return (getSecure() == 0? "http://": "https://") + getServerHost() + ":" + getServerPort() + getScriptPath();
	}

	public void getHeader(String name, long[] value)
	{
		String header = httpContext.getHeader(name);

		if	(header != null)
		{
			try
			{
				value[0] = (long) CommonUtil.val(header);
				return;
			}
			catch (Exception e)
			{
			}
		}

		value[0] = 0;
	}

	public void getHeader(String name, double[] value)
	{
		String header = httpContext.getHeader(name);

		if	(header != null)
		{
			try
			{
				value[0] = CommonUtil.val(header);
				return;
			}
			catch (Exception e)
			{
			}
		}

		value[0] = 0;
	}

	public void getHeader(String name, java.util.Date[] value)
	{
		String raw_date = httpContext.getHeader(name);

		value[0] = CommonUtil.nullDate();

		if (raw_date == null)
			return;

		// asctime() format is missing an explicit GMT specifier
		if (raw_date.toUpperCase().indexOf("GMT") == -1)
		    raw_date += " GMT";

		Date   date = CommonUtil.nullDate();

		try
		{
			try
		    {
			    date = new Date(raw_date);
		    }
			catch (IllegalArgumentException iae)
			{
			    // some servers erroneously send a number, so let's try that
			    long time;
			    try
				{
					time = Long.parseLong(raw_date);
				}
			    catch (NumberFormatException nfe)
				{
					throw iae;
				}	// give up

			    if (time < 0)
				    time = 0;

			    date = new Date(time * 1000L);
			}

	    }
		catch (Exception e)
		{
		}
	}

	public String getVariable(String key)
	{
		return httpContext.cgiGet(key);
	}

	public void getVariable(String key, String[] value)
	{
		value[0] = getVariable(key);
	}

	public void getVariable(String key, int[] value)
	{
		value[0] = (int) CommonUtil.val(getVariable(key));
	}

	public void getVariable(String key, double[] value)
	{
		value[0] = (double) CommonUtil.val(getVariable(key));
	}

	public String[] getVariables()
	{
		String[] keys = new String[httpContext.getPostData().size()];

		int idx = 0;
		for (Enumeration en = httpContext.getPostData().keys(); en.hasMoreElements(); )
		{
			keys[idx++] = (String) en.nextElement();
		}

		return keys;
	}

	public abstract Reader getReader() throws IOException;

	public abstract InputStream getInputStream() throws IOException;

	public abstract String getString();
	
	public abstract void setSoapMessageBody(String messageBody);

	public abstract void toFile(String fileName);

	private void resetErrors()
	{
		errCode = 0;
		errDescription = "";
	}

	public int getErrCode()
	{
		return errCode;
	}

	public String getErrDescription()
	{
		return errDescription;
	}
	
	public void setErrCode(int errCode){
		this.errCode = errCode;
	}
	
	public void setErrDescription(String errDsc){
		this.errDescription = errDsc;
	}
}
