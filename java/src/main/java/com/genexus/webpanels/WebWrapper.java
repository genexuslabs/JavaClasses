package com.genexus.webpanels;
import java.io.ByteArrayOutputStream;
import javax.servlet.http.HttpServletRequest;
import com.genexus.ModelContext;
import com.genexus.internet.HttpContext;
import com.genexus.internet.HttpContextNull;
import com.genexus.internet.HttpRequest;

public class WebWrapper
{
	private String baseURL = "";
	private GXWebPanel panel;
	private ByteArrayOutputStream out;

	public void setBaseURL(String baseURL)
	{
		this.baseURL = baseURL;
	}

	public String getBaseURL()
	{
		return baseURL;
	}

	public void setSource(GXWebPanel panel)
	{
		this.panel = panel;
		ModelContext context = panel.getModelContext();
		HttpRequest httpReq = ((HttpContext) context.getHttpContext()).getHttpRequest();
		HttpServletRequest httpSerReq = ((HttpContext) context.getHttpContext()).getRequest();
		context.setHttpContext(new HttpContextNull());
		((HttpContext) context.getHttpContext()).setHttpRequest(httpReq);
		((HttpContext) context.getHttpContext()).setRequest(httpSerReq);
		((HttpContext) context.getHttpContext()).setContext(context);
		panel.setHttpContext(((HttpContext) context.getHttpContext()));
		panel.getHttpContext().setCompression(false);
		panel.getHttpContext().setBuffered(false);
		panel.getHttpContext().useUtf8 = true;
		panel.getHttpContext().setOutputStream(new java.io.ByteArrayOutputStream());
	}

	public GXWebPanel getSource()
	{
		return panel;
	}

	public String getresponse()
	{
		return panel.getresponse(baseURL);
	}
}
