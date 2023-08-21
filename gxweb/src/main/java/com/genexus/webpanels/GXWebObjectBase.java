package com.genexus.webpanels;

import org.apache.commons.lang.StringUtils;

import com.genexus.*;
import com.genexus.configuration.ConfigurationManager;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.diagnostics.GXDebugInfo;
import com.genexus.diagnostics.GXDebugManager;
import com.genexus.internet.HttpAjaxContext;
import com.genexus.internet.GXInternetConstants;
import com.genexus.internet.HttpContext;
import com.genexus.ModelContext;
import com.genexus.security.GXSecurityProvider;

public abstract class GXWebObjectBase extends GXObjectBase implements GXInternetConstants
{
	public static final ILogger logger = LogManager.getLogger(GXWebObjectBase.class);

	private static String GX_SPA_GXOBJECT_RESPONSE_HEADER = "X-GXOBJECT";
    protected static String GX_SPA_MASTERPAGE_HEADER = "X-SPA-MP";
	protected static String GX_AJAX_MULTIPART_ID = "GXAjaxMultipart";

	protected boolean IntegratedSecurityEnabled() { return false;}
	protected int IntegratedSecurityLevel() { return 0;}
	protected String IntegratedSecurityPermissionPrefix() {return "";}

	protected HttpAjaxContext httpContext;

	public abstract void webExecute();
	public abstract boolean isMasterPage();

	public GXWebObjectBase()
	{
	}

	/**
	* Este constructor se usa cuando aun no tengo un ModelContext ni remoteHandle, pero
	* si tengo el HttpContext. Basicamente es el punto de entrada en los servlets.
	*/
	public GXWebObjectBase(HttpContext httpContext, Class contextClass)
	{
		init(httpContext, contextClass);
		castHttpContext();
	}

	/**
	* Este constructor se usa cuando aun no tengo un ModelContext ni remoteHandle, pero
	* si tengo el HttpContext. Basicamente es el punto de entrada en los servlets.
	*/
	public GXWebObjectBase(HttpContext httpContext)
	{
		super(httpContext);
		castHttpContext();
	}

	/**
	* Este constructor se usa cuando ya tengo un ModelContext y remoteHandle.
	* Basicamente es el punto de entrada para webcomponents, webwrappers, etc.
	*/
	public GXWebObjectBase(int remoteHandle, ModelContext context)
	{
		super(remoteHandle, context);
		castHttpContext();
	}

	private void castHttpContext() {
		this.httpContext = (HttpAjaxContext) super.httpContext;
	}
	/***
	 * Return the DefaultTheme for all WebPanels and Transactions.
	 * @return
	 */
	@SuppressWarnings("unused")
	protected void initializeTheme() {
		this.httpContext.setDefaultTheme(ConfigurationManager.getValue("Theme"));
	}

    protected boolean CheckCmpSecurityAccess()
    {
		boolean[] flag = new boolean[]{false};
		boolean[] permissionFlag = new boolean[]{false};
		String permissionPrefix = IntegratedSecurityPermissionPrefix();
        com.genexus.internet.HttpRequest req = ((HttpContext)ModelContext.getModelContext().getHttpContext()).getHttpRequest();
		if (req == null)
			return false;
		String reqUrl = req.getRequestURL();
		ModelContext modelContext = ModelContext.getModelContext(getClass());
		if (IntegratedSecurityLevel() == SECURITY_LOW || IntegratedSecurityLevel() == SECURITY_GXOBJECT)
		{
			GXSecurityProvider.getInstance().checksession(-2, modelContext, reqUrl, permissionFlag);
		}
		if (IntegratedSecurityLevel() != SECURITY_LOW && IntegratedSecurityLevel() != SECURITY_GXOBJECT)
		{
			GXSecurityProvider.getInstance().checksessionprm(-2, modelContext, reqUrl, permissionPrefix, flag, permissionFlag);
		}
		return permissionFlag[0];
    }

	protected void sendCacheHeaders()
	{
		if (httpContext.isSpaRequest()) {
			httpContext.getResponse().setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			httpContext.getResponse().setHeader("Pragma", "no-cache");
			httpContext.getResponse().setHeader("Expires", "0");
		}
		else {
			super.sendCacheHeaders();
		}
	}

	protected void sendAdditionalHeaders()
	{
		if (httpContext.isSpaRequest())
			sendSpaHeaders();
		if (httpContext.getBrowserType() == HttpContextWeb.BROWSER_IE && !httpContext.isPopUpObject())
		{
				String IECompMode = context.getClientPreferences().getIE_COMPATIBILITY();
				if (IECompMode.equals("EmulateIE7") && !httpContext.getBrowserVersion().startsWith("8") )
					return;
				if (StringUtils.isNotEmpty(IECompMode))
					httpContext.getResponse().addHeader("X-UA-Compatible", "IE=" + IECompMode);
		}
	}

	protected void sendSpaHeaders()
	{
		httpContext.getResponse().setHeader(GX_SPA_GXOBJECT_RESPONSE_HEADER, getPgmname().toLowerCase());
	}

	public void doExecute() throws Exception
	{
		try
		{
			if (isSpaRequest() && !isSpaSupported())
			{
				httpContext.sendResponseStatus(httpContext.SPA_NOT_SUPPORTED_STATUS_CODE, "SPA not supported by the object");
			}
			else
			{
				preExecute();
				sendCacheHeaders();
				webExecute();
				sendAdditionalHeaders();
				httpContext.flushStream();
			}
		}
		catch (Throwable e)
		{
			cleanup(); // Antes de hacer el rethrow, hago un cleanup del objeto
			throw e;
		}
		finally
		{
			finallyCleanup();
		}
	}

        public void executeUsercontrolMethod(String CmpContext, boolean IsMasterPage, String containerName, String methodName, String input, Object[] parms)
        {
            httpContext.executeUsercontrolMethod(CmpContext, IsMasterPage, containerName, methodName, input, parms);
        }

        public void setExternalObjectProperty(String CmpContext, boolean IsMasterPage, String containerName, String propertyName, Object value)
        {
            httpContext.setExternalObjectProperty(CmpContext, IsMasterPage, containerName, propertyName, value);
        }

        public void executeExternalObjectMethod(String CmpContext, boolean IsMasterPage, String containerName, String methodName, Object[] parms, boolean isEvent)
        {
            httpContext.executeExternalObjectMethod(CmpContext, IsMasterPage, containerName, methodName, parms, isEvent);
        }

	public String getPgmname()
	{
		return "";
	}

	public String getPgmdesc()
	{
		return "";
	}

	protected boolean isSpaRequest()
	{
		return httpContext.isSpaRequest();
	}

		protected boolean isSpaRequest(boolean ignoreFlag)
	{
		return httpContext.isSpaRequest(ignoreFlag);
	}

	protected boolean isSpaSupported()
	{
		return true;
	}


	protected void validateSpaRequest()
	{
		// SPA is disabled for objects without master page. However, SPA response headers are sent anyway, so the client side
		// replaces the full content using AJAX.
		if (isSpaRequest())
		{
			httpContext.disableSpaRequest();
			sendSpaHeaders();
		}
	}

	 protected String getPgmInstanceId(String cmpCtx)
     {
         return String.format("%s%s", cmpCtx, this.getPgmname().toUpperCase());
     }

	private GXDebugInfo dbgInfo = null;
	protected void trkCleanup()
	{
		if(dbgInfo != null)
			dbgInfo.onCleanup();
	}

	protected void initialize(int objClass, int objId, int dbgLines, long hash)
	{
		dbgInfo = GXDebugManager.getInstance().getDbgInfo(context, objClass, objId, dbgLines, hash);
	}

	protected void trk(int lineNro)
	{
		if(dbgInfo != null)
			dbgInfo.trk(lineNro);
	}

	protected void trk(int lineNro, int lineNro2)
	{
		if(dbgInfo != null)
			dbgInfo.trk(lineNro, lineNro2);
	}

	protected void trkrng(int lineNro, int lineNro2)
	{
		trkrng(lineNro, 0, lineNro2, 0);
	}

	protected void trkrng(int lineNro, int colNro, int lineNro2, int colNro2)
	{
		if(dbgInfo != null)
			dbgInfo.trkRng(lineNro, colNro, lineNro2, colNro2);
	}

	protected void callWebObject(String url)
	{
		httpContext.wjLoc = url;
	}


	public void popup(String url)
	{
	}

	public void popup(String url, Object[] returnParms)
	{
	}
}
