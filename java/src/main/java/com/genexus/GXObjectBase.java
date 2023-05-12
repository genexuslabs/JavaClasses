package com.genexus;

import com.genexus.db.Namespace;
import com.genexus.db.UserInformation;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.internet.HttpContext;
import com.genexus.webpanels.HttpContextWeb;
import com.genexus.webpanels.WebApplicationStartup;

public abstract class GXObjectBase extends GXRestServiceWrapper{
	public static final ILogger logger = LogManager.getLogger(GXObjectBase.class);

	protected ModelContext context;
	protected HttpContext httpContext;
	protected LocalUtil    localUtil;
	protected int remoteHandle = -1;
	protected UserInformation ui;

	protected static final int SECURITY_GXOBJECT = 3;
	protected static final int SECURITY_HIGH = 2;
	protected static final int SECURITY_LOW  = 1;

	public abstract void webExecute();

	public GXObjectBase() {
	}

	/**
	 * Este constructor se usa cuando aun no tengo un ModelContext ni remoteHandle, pero
	 * si tengo el HttpContext. Basicamente es el punto de entrada en los servlets.
	 */
	public GXObjectBase(HttpContext httpContext) {
		init(httpContext, getClass());
	}

	/**
	 * Este constructor se usa cuando ya tengo un ModelContext y remoteHandle.
	 * Basicamente es el punto de entrada para webcomponents, webwrappers, etc.
	 */
	public GXObjectBase(int remoteHandle, ModelContext context) {
		this.context      = context;

		ui = (UserInformation) GXObjectHelper.getUserInformation(context, remoteHandle);
		this.remoteHandle = ui.getHandle();

		initState(context, ui);
	}

	protected void init(HttpContext httpContext, Class contextClass) {
		this.context = new ModelContext(contextClass);
		context.setHttpContext(httpContext);

		new WebApplicationStartup().init(contextClass, httpContext);

		ApplicationContext.getInstance().setPoolConnections(!Namespace.createNamespace(context).isRemoteGXDB());

		ui = (UserInformation) GXObjectHelper.getUserInformation(context, -1);
		ui.setAutoDisconnect(false);
		remoteHandle = ui.getHandle();

		initState(context, ui);
	}

	protected void initState(ModelContext context, UserInformation ui) {
		localUtil   = ui.getLocalUtil();
		httpContext = (HttpContext) context.getHttpContext();
		httpContext.setContext( context);
		httpContext.setCompression(getCompressionMode());
	}

	protected boolean getCompressionMode() {
		return context.getClientPreferences().getCOMPRESS_HTML();
	}

	public int setLanguage(String language) {
		int res = GXutil.setLanguage(language, context, ui);
		localUtil = ui.getLocalUtil();
		return res;
	}

	public int setTheme(String theme) {
		int res = GXutil.setTheme(theme, context);
		return res;
	}

	public void doExecute() throws Exception
	{
		try
		{
			preExecute();
			sendCacheHeaders();
			webExecute();
			httpContext.flushStream();
		}
		catch (Throwable e)
		{
			cleanup();
			throw e;
		}
		finally
		{
			finallyCleanup();
		}
	}

	protected void preExecute()
	{
		httpContext.responseContentType("text/html"); //default Content-Type
		httpContext.initClientId();
	}

	protected void sendCacheHeaders()
	{
		httpContext.getResponse().addDateHeader("Expires", 0);
		httpContext.getResponse().addDateHeader("Last-Modified", 0);
		if (this instanceof GXWebReport && ((GXWebReport)this).getOutputType()==GXWebReport.OUTPUT_PDF) {
			httpContext.getResponse().addHeader("Cache-Control", "must-revalidate,post-check=0, pre-check=0");
			//Estos headers se setean por un bug de Reader X que hace que en IE no se vea el reporte cuando esta embebido,
			//solo se ve luego de hacer F5.
		}
		else {
			httpContext.getResponse().addHeader("Cache-Control", "max-age=0, no-cache, no-store, must-revalidate");
		}
	}

	protected void finallyCleanup() {
		try {
			if (ui!= null)
				ui.disconnect();
		}
		catch (java.sql.SQLException e) {
			logger.error("Exception while disconecting ", e);
		}
		if (httpContext != null)
			httpContext.cleanup();
		cleanModelContext();
	}

	private void cleanModelContext() {
		try {
			((ThreadLocal)com.genexus.CommonUtil.threadCalendar).getClass().getMethod("remove", new Class[0]).invoke(com.genexus.CommonUtil.threadCalendar, (java.lang.Object[])new Class[0]);
			((ThreadLocal)com.genexus.ModelContext.threadModelContext).getClass().getMethod("remove", new Class[0]).invoke(com.genexus.ModelContext.threadModelContext, (java.lang.Object[])new Class[0]);
		}
		catch (NoSuchMethodException e) {
			logger.error("cleanModelContext", e);
		}
		catch (IllegalAccessException e) {
			logger.error("cleanModelContext", e);
		}
		catch (java.lang.reflect.InvocationTargetException e) {
			logger.error("cleanModelContext " + e.getTargetException(), e);
		}
	}

	protected void cleanup() {
		Application.cleanupConnection(remoteHandle);
	}

	public HttpContext getHttpContext() {
		return httpContext;
	}

	public void setHttpContext(HttpContext httpContext) {
		this.httpContext = (HttpContextWeb) httpContext;
	}

	public ModelContext getModelContext() {
		return context;
	}

	public int getRemoteHandle() {
		return remoteHandle;
	}

	public ModelContext getContext() {
		return context;
	}

	public void handleError() {
		new DefaultErrorHandler().handleError(context, remoteHandle);
	}

	public Object getParm( Object[] parms, int index)
	{
		return parms[index];
	}

	protected String formatLink(String jumpURL)
	{
		return formatLink(jumpURL, new String[]{});
	}

	protected String formatLink(String jumpURL, String[] parms)
	{
		return formatLink(jumpURL, parms, new String[]{});
	}

	protected String formatLink(String jumpURL, String[] parms, String[] parmsName)
	{
		return URLRouter.getURLRoute(jumpURL, parms, parmsName, httpContext.getRequest().getContextPath(), context.getPackageName());
	}
}
