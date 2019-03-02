package com.genexus.internet;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class HttpRequestNull extends HttpRequest
{	
	public HttpRequestNull(HttpContextNull httpContext)
	{	
		super(httpContext);
		setErrCode(1);
		setErrDescription("This is not an Http Web Request");
	}

	@Override
	public String getScriptName() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getString() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public void setSoapMessageBody(String messageBody) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toFile(String fileName) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getRequestURL() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public Reader getReader() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	
}
