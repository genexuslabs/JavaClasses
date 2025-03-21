package com.genexus.internet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import com.genexus.PrivateUtilities;
import com.genexus.WrapperUtils;
import com.genexus.webpanels.HttpContextWeb;
import org.apache.commons.io.IOUtils;

public class HttpRequestWeb extends HttpRequest
{
	private String messageBody;
	
	public HttpRequestWeb(HttpContextWeb httpContext)
	{	
		super(httpContext);				
	}

	public String getScriptName() {
		if (httpContext.getRequest() != null) {
			String requestURI = httpContext.getRequest().getRequestURI();
			if (requestURI != null) {
				return requestURI.substring(requestURI.lastIndexOf("/") + 1);
			}
		}
		return "";
	}
	
	public void toFile(String fileName)
	{
		try
		{
			PrivateUtilities.InputStreamToFile(getInputStream(), fileName);
		}
		catch (Throwable e)
		{
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
	}
	
	public String getString()
	{
		if (messageBody != null)
		{
			return messageBody;
		}

		if (httpContext.isRestService())
		{
			String restRequestBody = WrapperUtils.requestBodyThreadLocal.get();
			if (restRequestBody != null)
			{
				return restRequestBody;
			}
		}
		
		try (InputStream is = getInputStream())
		{
			String requestEncoding = "UTF-8";
			if (httpContext.getRequest().getCharacterEncoding() != null && httpContext.getRequest().getCharacterEncoding().length() > 0)
				requestEncoding = httpContext.getRequest().getCharacterEncoding();
			
			return new String(IOUtils.toByteArray(is), requestEncoding);
		}
		catch (IOException e)
		{
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}

		return "";
	}
	
	public void setSoapMessageBody(String messageBody)
	{
		this.messageBody = messageBody;
	}
	
	
	@Override
	public String getRequestURL()
	{	
		if (httpContext.getRequest() != null)
			return httpContext.getRequest().getRequestURL().toString();
		return "";
	}

	@Override
	public Reader getReader() throws IOException
	{
		return httpContext.getRequest().getReader();
	}

	byte[] streamByteArray = null;
	@Override
	public InputStream getInputStream() throws IOException
	{
		if (streamByteArray == null)
			streamByteArray = org.apache.commons.io.IOUtils.toByteArray(httpContext.getRequest().getInputStream().getInputStream());
		return new ByteArrayInputStream(streamByteArray);
	}

	public int getContentLength() {
		return httpContext.getRequest().getContentLength();
	}
}
