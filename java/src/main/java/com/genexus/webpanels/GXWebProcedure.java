package com.genexus.webpanels;

import java.io.PrintWriter;

import com.genexus.servlet.IServletContext;
import com.genexus.servlet.ServletContext;
import com.genexus.servlet.http.IHttpServletRequest;
import com.genexus.servlet.http.HttpServletRequest;
import com.genexus.servlet.http.IHttpServletResponse;
import com.genexus.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import com.genexus.ModelContext;
import com.genexus.db.UserInformation;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.internet.HttpContext;
import com.genexus.ws.GXHandlerChain;

public abstract class GXWebProcedure extends GXWebObjectBase
{
	private static final ILogger logger = LogManager.getLogger(GXWebProcedure.class);
	
	protected abstract void initialize();

	public GXWebProcedure(HttpContext httpContext)
	{
		super(httpContext);
	}

	public GXWebProcedure(WebServiceContext wsContext)
	{	
		try
		{
			MessageContext msg = wsContext.getMessageContext();
			IHttpServletRequest request = new HttpServletRequest(msg.get(MessageContext.SERVLET_REQUEST));
			IHttpServletResponse response = new HttpServletResponse(msg.get(MessageContext.SERVLET_RESPONSE));
			IServletContext myContext = new ServletContext(msg.get(MessageContext.SERVLET_CONTEXT));
			String messageBody = (String)msg.get(GXHandlerChain.GX_SOAP_BODY);
			HttpContext httpContext = new HttpContextWeb(request.getMethod(), request, response, myContext);
			httpContext.getHttpRequest().setSoapMessageBody(messageBody);
			init(httpContext, getClass());
		}
		catch(Throwable e)
		{
			logger.error("Could not initialize Web Service", e);
		}
	}
	
	public GXWebProcedure(int remoteHandle , ModelContext context)
	{
		super(remoteHandle ,context);
	}	

	protected void initState(ModelContext context, UserInformation ui)
	{
		super.initState(context, ui);

		if(httpContext.getHttpSecure() == 0)httpContext.setHeader("pragma", "no-cache");

		initialize();
	}

	protected void preExecute()
	{
		httpContext.setStream();
      	httpContext.GX_xmlwrt.setWriter(new PrintWriter(httpContext.getOutputStream()));
	}

   	protected void cleanup()
   	{
      	super.cleanup();
      	if (httpContext != null && !httpContext.willRedirect() && httpContext.isLocalStorageSupported())
      	{
      		httpContext.deleteReferer();
      	}
   	}

	public boolean isMasterPage()
	{
		return false;
	}


	public void release()
	{
	}

	protected boolean isSpaSupported()
	{
		return false;
	}
	
	protected void callWebObject(String url)
	{
		httpContext.wjLoc = url;
	}	
}