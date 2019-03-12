
package com.genexus.internet;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;

import com.genexus.CommonUtil;
import com.genexus.PrivateUtilities;
import com.genexus.com.IHttpResponse;
import com.genexus.webpanels.FileItemCollection;
import com.genexus.webpanels.HttpContextWeb;

/**
* Esta clase esta disponible en los webprocs para grabar informacion en el response
*/

public class HttpResponse implements IHttpResponse
{
	private final int ERROR_IO = 1;

	private Hashtable headers = new Hashtable();

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
		  value = getEncodedContentDisposition(value);
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
        
	private String getEncodedContentDisposition(String value)
	{
		int filenameIdx = value.toLowerCase().indexOf("filename");
		if(filenameIdx != -1)
		{
			int eqIdx = value.toLowerCase().indexOf("=", filenameIdx);
			if (eqIdx != -1)
			{
				String filename = value.substring(eqIdx + 1).trim();
				value = value.substring(0, eqIdx + 1) + PrivateUtilities.URLEncode(filename, "UTF8");
			}
		}
		return value;
	}

	public boolean isText()
	{
		return getHeader("Content-Type").startsWith("text/");
	}

	public String getHeader(String name)
	{
		String ret = (String) headers.get(name.toUpperCase());
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

		try
		{
			InputStream source = new BufferedInputStream(new FileInputStream(fileName));

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
