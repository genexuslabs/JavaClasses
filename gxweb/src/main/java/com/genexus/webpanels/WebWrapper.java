package com.genexus.webpanels;
import java.io.ByteArrayOutputStream;

import com.genexus.internet.HttpAjaxContext;
import com.genexus.servlet.http.IHttpServletRequest;
import com.genexus.ModelContext;
import com.genexus.internet.HttpContext;
import com.genexus.internet.HttpContextNull;
import com.genexus.internet.HttpRequest;
import org.apache.logging.log4j.Logger;

public class WebWrapper
{
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(WebWrapper.class);

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
		IHttpServletRequest httpSerReq = ((HttpContext) context.getHttpContext()).getRequest();
		try {
			context.setHttpContext(new HttpAjaxContext(httpSerReq));
			((HttpContext) context.getHttpContext()).setHttpRequest(httpReq);
			((HttpContext) context.getHttpContext()).setRequest(httpSerReq);
			((HttpContext) context.getHttpContext()).setContext(context);
			panel.httpContext = (HttpAjaxContext)context.getHttpContext();
			panel.httpContext.setCompression(false);
			panel.httpContext.setBuffered(false);
			panel.httpContext.useUtf8 = true;
			panel.httpContext.setOutputStream(new java.io.ByteArrayOutputStream());
		}
		catch (Exception e) {
			log.error("Failed to create WebWrapper", e);
		}
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
