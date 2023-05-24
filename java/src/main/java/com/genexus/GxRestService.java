package com.genexus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.internet.MsgList;
import com.genexus.security.GXResult;
import com.genexus.security.GXSecurityProvider;
import com.genexus.servlet.IServletContext;
import com.genexus.servlet.http.IHttpServletRequest;
import com.genexus.servlet.http.IHttpServletResponse;
import com.genexus.webpanels.HttpContextWeb;

import json.org.json.JSONException;
import json.org.json.JSONObject;

abstract public class GxRestService extends GXObjectBase {
	private static ILogger logger = null;
	
	protected JSONObject errorJson;
	protected boolean error = false;
	protected String gamError;
	protected boolean forbidden = false;
	protected String permissionPrefix;
	
	protected abstract boolean IntegratedSecurityEnabled();
	protected abstract int IntegratedSecurityLevel();

	protected String ExecutePermissionPrefix(){
		return "";
	}

	protected boolean IsSynchronizer()
	{
		return false;
	}

	protected static final int SECURITY_LOW  = 1;
	protected static final String POST  = "POST";
	
	protected HttpContextWeb restHttpContext;
	protected com.genexus.ws.rs.core.IResponseBuilder builder;

	protected void init(String requestMethod) {
		initWrappedVars();
		init( requestMethod , myServletRequestWrapper, myServletResponseWrapper, myContextWrapper);
		ApplicationContext.getInstance().setServletEngineDefaultPath(myContextWrapper.getRealPath("/"));
	}

	public void init(String requestMethod, IHttpServletRequest myServletRequest, IHttpServletResponse myServletResponse, IServletContext myContext)
	{
		initLogger(myContext);
		try
		{
			String gxcfg = myContext.getInitParameter("gxcfg");
			Class gxcfgClass = getClass();
			if (gxcfg != null)
			{
				gxcfgClass = Class.forName(gxcfg);
				ApplicationContext appContext = ApplicationContext.getInstance();
				appContext.setServletEngine(true);
				Application.init(gxcfgClass);
			}
			restHttpContext = new HttpContextWeb(requestMethod, myServletRequest, myServletResponse, myContext);
			restHttpContext.doNotCompress(true);
			restHttpContext.setRestService();
			ModelContext.deleteThreadContext();
			super.init(restHttpContext, gxcfgClass);
			ModelContext modelContext = ModelContext.getModelContext(getClass());
			modelContext.setHttpContext(restHttpContext);
		}
		catch(Throwable e)
		{
			logger.error("Could not initialize Rest Service", e);
		}
	}

	private void initLogger(IServletContext myContext) {
		if (logger == null) {
			logger = com.genexus.specific.java.LogManager.initialize(myContext.getRealPath("/"), GxRestService.class);
		}
	}

	public void webExecute( )
   {
	}

   protected void cleanup()
   {
		GXutil.setThreadTimeZone(ModelContext.getModelContext().getClientTimeZone());
		super.cleanup();
	   super.finallyCleanup();
		WrapperUtils.requestBodyThreadLocal.remove();
	}
	
	public void ErrorCheck(IGxSilentTrn trn)
	{
		if (trn.Errors() == 1)
		{
			error = true;
			MsgList msg = trn.GetMessages();
			if (msg.getItemCount() > 0)
			{
				SetError("404", msg.getItemText(1));
			}
		}
	}
	protected void webException(Throwable th) {
		logger.error("Failed to complete execution of Rest Service:", th);
	}
	
	public void SetError(String code, String message)
	{
		try
		{
			JSONObject obj = new JSONObject();
			obj.put("code", code);
			obj.put("message", message);
			errorJson = new JSONObject();
			errorJson.put("error", obj);
		}
		catch(JSONException e)
		{
			logger.error("Invalid JSON", e);			
		}
	}
	private boolean isAuthenticated(IHttpServletRequest myServletRequest, int integratedSecurityLevel, boolean useAuthentication, String objPermissionPrefix)
	{
		if (!useAuthentication)
		{
			return true;
		}
		else
		{
			String token = myServletRequest.getHeader("Authorization");
			if (token == null)
			{
				gamError = "0";
				SetError(gamError, "This service needs an Authorization Header");
				return false;
			}
			else
			{
				GXResult result;
				token = myServletRequest.getHeader("Authorization").replace("OAuth ", "");	
				boolean[] flag = new boolean[]{false};
				boolean[] permissionFlag = new boolean[]{false};
				ModelContext modelContext = ModelContext.getModelContext(getClass());
				modelContext.setHttpContext(restHttpContext);
				if (integratedSecurityLevel == SECURITY_LOW)
				{
					result = GXSecurityProvider.getInstance().checkaccesstoken(remoteHandle, modelContext, token, flag);		

					if(!flag[0])
					{
						gamError = result.getCode();
						String message = result.getDescription();
						SetError(gamError, message);
						return false;
					}
					else
					{
						return true;
					}
				}
				else
				{
					result = GXSecurityProvider.getInstance().checkaccesstokenprm(remoteHandle, modelContext, token, objPermissionPrefix, permissionFlag, flag);
					if(flag[0])
					{
						return true;
					}
					else
					{
						gamError = result.getCode();
						String messagePermission = result.getDescription();
						SetError(gamError, messagePermission);
						if (permissionFlag[0])
						{
							forbidden = true;
						}
						return false;
					}
				}
			}
		}
	}
	
	public boolean isAuthenticated(IHttpServletRequest myServletRequest)
	{
		return isAuthenticated(myServletRequest, IntegratedSecurityLevel(), IntegratedSecurityEnabled(), permissionPrefix);
	}

	public boolean isAuthenticated(IHttpServletRequest myServletRequest, String synchronizer)
	{
		boolean validSynchronizer = false;
		try {
			if (synchronizer!= null && !synchronizer.equals(""))
			{
				synchronizer = synchronizer.toLowerCase() + "_services_rest";
				String packageName = Application.getClientContext().getClientPreferences().getPACKAGE();
				if (!packageName.equals(""))
					packageName += ".";
				Class<?> synchronizerClass = Class.forName(packageName + synchronizer);
				GxRestService synchronizerRestService = (GxRestService) synchronizerClass.getConstructor().newInstance();
				if (synchronizerRestService!=null && synchronizerRestService.IsSynchronizer()) {
					validSynchronizer = true;
					return isAuthenticated(myServletRequest, synchronizerRestService.IntegratedSecurityLevel(), synchronizerRestService.IntegratedSecurityEnabled(), synchronizerRestService.ExecutePermissionPrefix());
				}
			}
			return false;
		}
		catch(Exception e)
		{
			logger.error("Could not check user authenticated", e);			
			return false;
		}
		finally
		{
			if (!validSynchronizer)
				SetError("0", "Invalid Synchronizer " + synchronizer);
		}
	}
	
	public void setWWWAuthHeader(IHttpServletRequest myServletRequest, IHttpServletResponse myServletResponse)
	{
		String OauthRealm = "OAuth realm=\"" + myServletRequest.getServerName() + "\"";
		myServletResponse.addHeader("WWW-Authenticate", OauthRealm);
	}
	
	public boolean processHeaders(String queryId, IHttpServletRequest myServletRequest, IHttpServletResponse myServletResponse)
	{
		String language = myServletRequest.getHeader("GeneXus-Language");
		if (language != null)
		{
			setLanguage(language);
		}
		String theme = myServletRequest.getHeader("GeneXus-Theme");
		if (theme != null)
		{
			setTheme(theme);
		}
		String etag = myServletRequest.getMethod().equalsIgnoreCase(POST) ? null : myServletRequest.getHeader("If-Modified-Since");
		Date dt = Application.getStartDateTime();
		Date newDt = new Date();
		GXSmartCacheProvider.DataUpdateStatus status;
		Date[] newDt_arr = new Date[] { newDt };
		if (etag == null)
		{
			status = GXSmartCacheProvider.DataUpdateStatus.Invalid;
			GXSmartCacheProvider.CheckDataStatus(queryId, dt, newDt_arr);
		}
		else
		{
			dt = HTMLDateToDatetime(etag);
			status = GXSmartCacheProvider.CheckDataStatus(queryId, dt, newDt_arr);
		}
		newDt = newDt_arr[0];
		if (myServletResponse != null) // Temporary: Jersey Service called through AWS Lambda where HttpResponse is null.
			myServletResponse.addHeader("Last-Modified", DateTimeToHTMLDate(newDt)); 
		if (status == GXSmartCacheProvider.DataUpdateStatus.UpToDate)
		{
			return false;
		}
		return true;
	}
	
	Date HTMLDateToDatetime(String s)
	{
		// Formato fecha: RFC 1123
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", java.util.Locale.US);
			java.util.TimeZone tz = java.util.TimeZone.getTimeZone("GMT");
			sdf.setTimeZone(tz);
			return sdf.parse(s);
			
		}
		catch(ParseException p)
		{
			logger.warn("Could not parse RFC Date", p);
			return new Date();
		}
	}
	
	String DateTimeToHTMLDate(Date dt)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", java.util.Locale.US);
		java.util.TimeZone tz = java.util.TimeZone.getTimeZone("GMT");
		sdf.setTimeZone(tz);		
		return sdf.format(dt);
	}
}