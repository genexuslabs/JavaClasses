package com.genexus.internet;

/**
* Esta clase esta disponible en los webprocs para leer la informacion del request.
* En Android esta disponible para saber los datos del server online, no de un request en particular.
*/
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Date;
import java.util.Enumeration;

import com.artech.base.services.AndroidContext;
import com.genexus.CommonUtil;

public class HttpRequest implements IHttpRequest
{
	public final int ERROR_IO = 1;

	private int errCode;
	private String errDescription;
	private HttpContext httpContext;

	public HttpRequest(HttpContext httpContext)
	{
		this.httpContext = httpContext;
		resetErrors();
	}

	public String getRepositoryPath()
	{
		return "";
	}

	public void setRepositoryPath( String path)
	{
	}

	public String getMethod()
	{
		return httpContext.getRequestMethod();
	}

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

	public String getReferer()
	{
		String referer = httpContext.getReferer();
		return referer == null?"":referer;
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

	public String getScriptName()
	{
	  //String servletPath = httpContext.getRequest().getServletPath();
		//get servlet path from android
		String servletPath = AndroidContext.ApplicationContext.getRootUri();
		
	  return servletPath.substring(servletPath.lastIndexOf("/") +1 , servletPath.length());
	}
	
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

	public Reader getReader() throws IOException
	{
		//return httpContext.getRequest().getReader();
		return null;
	}

	public InputStream getInputStream() throws IOException
	{
		//return httpContext.getRequest().getInputStream();
		return null;
	}


	public String getString()
	{
		//try
		//{
			//return new String(PrivateUtilities.readToByteArray(httpContext.getRequest().getInputStream()));
		//}
		//catch (IOException e)
		//{
		//	errCode = ERROR_IO;
		//	errDescription = e.getMessage();
		//}

		return "";
	}

	public void toFile(String fileName)
	{
		try
		{
			//PrivateUtilities.InputStreamToFile(httpContext.getRequest().getInputStream(), fileName);
		}
		catch (Throwable e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
	}

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
}
