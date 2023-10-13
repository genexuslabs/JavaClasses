
package com.genexus.internet;

import java.io.*;
import java.util.Hashtable;

import com.genexus.CommonUtil;
import com.genexus.ModelContext;
import com.genexus.IHttpContext;
import com.genexus.PrivateUtilities;
import com.genexus.com.IHttpResponse;
import com.genexus.webpanels.FileItemCollection;
import com.genexus.webpanels.HttpContextWeb;
import com.genexus.webpanels.HttpUtils;
import com.genexus.webpanels.WebUtils;

import org.apache.logging.log4j.Logger;

/**
* Esta clase esta disponible en los webprocs para grabar informacion en el response
*/

public class HttpResponse implements IHttpResponse
{
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(HttpResponse.class);

	private final int ERROR_IO = 1;

	private Hashtable<String, String> headers = new Hashtable<>();

	private int errCode;
	private String errDescription;
	private HttpContextWeb httpContext;

	public HttpResponse(HttpContext httpContext)
	{
		this.httpContext = (HttpContextWeb) httpContext;
		resetErrors();
	}

	public FileItemCollection getPostedparts()
	{
	 return httpContext.getPostedparts();
       }

	public void addHeader(String name, String value)
	{
		if(name.equalsIgnoreCase("Content-Disposition"))
		{
		  value = WebUtils.getEncodedContentDisposition(value, httpContext.getBrowserType());
		}

		httpContext.setHeader(name, value);
		headers.put(name.toUpperCase(), value);

		if	(name.equalsIgnoreCase("Content-type"))
		{
			httpContext.setContentType(value);
		}
		else if	(name.equalsIgnoreCase("Content-length"))
		{
			httpContext.getResponse().setContentLength((int) CommonUtil.val(value));
		}
	}

	public boolean isText()
	{
		return getHeader("Content-Type").startsWith("text/");
	}

	public String getHeader(String name)
	{
		String ret = headers.get(name.toUpperCase());
		return ret == null?"":ret;
	}

	public PrintWriter getWriter() throws IOException
	{
		return new PrintWriter(httpContext.getOutputStream());
	}

	public OutputStream getOutputStream() throws IOException
	{
		return httpContext.getOutputStream();
	}

	public void addString(String value)
	{
		httpContext._writeText(value);
	}

	public void addFile(String fileName)
	{
		resetErrors();

		InputStream source = null;
		try
		{

			if (ModelContext.getModelContext() != null && ! new File(fileName).isAbsolute())
			{
				IHttpContext webContext = ModelContext.getModelContext().getHttpContext();
				if ((webContext != null) && (webContext instanceof HttpContextWeb) && !fileName.isEmpty())
				{
					fileName = ModelContext.getModelContext().getHttpContext().getDefaultPath() + File.separator + fileName;
				}
			}

			source = new BufferedInputStream(new FileInputStream(fileName));

			int bytes_read;
			byte[] buffer = new byte[1024];
			while (true)
			{
				bytes_read = source.read(buffer);
			    if (bytes_read == -1) break;
				httpContext.getOutputStream().write(buffer, 0, bytes_read);
			}
                        source.close();
		}
		catch (FileNotFoundException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();

		}
		catch (IOException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
		finally
		{
			try{ if (source != null) source.close(); } catch (IOException ioe) { log.error("Failed to close source buffered input stream ", ioe); }
		}
	}

	private void resetErrors()
	{
		errCode = 0;
		errDescription = "";
	}

	public void setContentTypeType(String file)
	{
	  httpContext.setContentType(getContentType(file));
	}

	public static String getContentType( String file)
	{
            if (HttpContext.isKnownContentType(file))
            {
                return file;
            }
	  String contentType = HttpContext.getContentFromExt( file);
	  if (contentType != null)
	    return contentType;
	  String ext = PrivateUtilities.getExtension(file);
	  contentType = HttpContext.getContentFromExt( ext);
	  if (contentType == null)
	    return "text/html";
	  return contentType;
	}

	public byte respondFile(String file)
	{
		setContentTypeType(file);
		addFile(file);
		return 1;
	}

	public int getErrCode()
	{
		return errCode;
	}

	public String getErrDescription()
	{
		return errDescription;
	}
	
	public byte setCookie(HttpCookie cookie)
	{
		return httpContext.setCookie( cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getExpirationdate(), cookie.getDomain(), cookie.getSecure() ? 1 : 0, cookie.httpOnly);
	}

}
