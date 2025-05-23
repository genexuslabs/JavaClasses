package com.genexus.webpanels;

import java.io.PrintWriter;

import com.genexus.Application;
import com.genexus.GXObjectBase;
import com.genexus.mock.GXMockProvider;
import com.genexus.servlet.IServletContext;
import com.genexus.servlet.ServletContext;
import com.genexus.servlet.http.IHttpServletRequest;
import com.genexus.servlet.http.HttpServletRequest;
import com.genexus.servlet.http.IHttpServletResponse;
import com.genexus.servlet.http.HttpServletResponse;
import com.genexus.xml.ws.WebServiceContext;
import com.genexus.xml.ws.handler.MessageContext;

import com.genexus.ModelContext;
import com.genexus.db.UserInformation;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.internet.HttpContext;
import com.genexus.ws.GXHandlerChain;

public abstract class GXWebProcedure extends GXObjectBase {
	private static final ILogger logger = LogManager.getLogger(GXWebProcedure.class);

	public static final int IN_NEW_UTL = -2;
	
	protected abstract void initialize();

	public GXWebProcedure(HttpContext httpContext)
	{
		super(httpContext);
	}

	public GXWebProcedure(WebServiceContext wsContext) {
		try {
			MessageContext msg = new MessageContext(wsContext);
			IHttpServletRequest request = new HttpServletRequest(msg.get(msg.getSERVLET_REQUEST()));
			IHttpServletResponse response = new HttpServletResponse(msg.get(msg.getSERVLET_RESPONSE()));
			IServletContext myContext = new ServletContext(msg.get(msg.getSERVLET_CONTEXT()));
			String messageBody = (String)msg.get(GXHandlerChain.GX_SOAP_BODY);
			HttpContext httpContext = new HttpContextWeb(request.getMethod(), request, response, myContext);
			httpContext.getHttpRequest().setSoapMessageBody(messageBody);
			init(httpContext, getClass());
		}
		catch(Throwable e) {
			logger.error("Could not initialize Web Service", e);
		}
	}

	public GXWebProcedure(int remoteHandle , ModelContext context)
	{
		this(false, remoteHandle ,context);
	}

	public GXWebProcedure(boolean inNewUTL, int remoteHandle , ModelContext context) {
		super(remoteHandle ,context);

		if(inNewUTL) {
			this.remoteHandle = IN_NEW_UTL;
		}
	}

	protected void initState(ModelContext context, UserInformation ui) {
		super.initState(context, ui);

		if(httpContext.getHttpSecure() == 0)httpContext.setHeader("pragma", "no-cache");

		if (!isBufferedResponse()) {
			httpContext.setResponseBufferMode(HttpContext.ResponseBufferMode.DISABLED);
		}
		initialize();
	}

	protected boolean isBufferedResponse() {
		return true;
	}

	protected void preExecute() {
		httpContext.setStream();
      	httpContext.GX_xmlwrt.setWriter(new PrintWriter(httpContext.getOutputStream()));
	}

   	protected void cleanup() {
      	super.cleanup();
      	if (httpContext != null && !httpContext.willRedirect() && httpContext.isLocalStorageSupported()) {
      		httpContext.deleteReferer();
      	}
   	}

	public boolean isMasterPage()
	{
		return false;
	}

	public void release() {
	}

	protected boolean isSpaSupported()
	{
		return false;
	}
	
	protected void callWebObject(String url)
	{
		httpContext.wjLoc = url;
	}

	protected void privateExecute() {
	}

	protected String[] getParametersInternalNames( ) {
		return null ;
	}

	protected void mockExecute() {
		if (GXMockProvider.getProvier() != null) {
			if (GXMockProvider.getProvier().handle(remoteHandle, context, this, getParametersInternalNames())) {
				cleanup();
				return;
			}
		}
		privateExecute( );
	}

	protected boolean batchCursorHolder(){
		return false;
	}
	protected void exitApp() {
		if (batchCursorHolder()) {
			try {
				Application.getConnectionManager().flushBuffers(remoteHandle, this);
			} catch (Exception exception) { ; }
		}
	}
}