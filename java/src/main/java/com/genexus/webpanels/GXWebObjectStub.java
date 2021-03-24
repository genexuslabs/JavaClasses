package com.genexus.webpanels;

import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.genexus.*;
import com.genexus.db.Namespace;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.internet.HttpContext;
import com.genexus.security.GXResult;
import com.genexus.security.GXSecurityProvider;

public abstract class GXWebObjectStub extends HttpServlet
{
	public static ILogger logger = null;

	private static final boolean DEBUG       = DebugFlag.DEBUG;

	protected abstract void doExecute(HttpContext context) throws Exception;
	protected abstract void init(HttpContext context) throws Exception;
	protected abstract boolean IntegratedSecurityEnabled();
	protected abstract int IntegratedSecurityLevel();
	protected abstract String IntegratedSecurityPermissionPrefix();

	protected static final int SECURITY_GXOBJECT = 3;
	protected static final int SECURITY_HIGH = 2;
	protected static final int SECURITY_LOW  = 1;

	private static final int HTTP_RESPONSE_BUFFER_SIZE  = 131072;


	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException
	{
		callExecute("POST", req, res);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException
	{
		callExecute("GET", req, res);
	}

	public void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException
	{
		callExecute("DELETE", req, res);
	}

	public void doHead(HttpServletRequest req, HttpServletResponse res) throws ServletException
	{
		callExecute("HEAD", req, res);
	}

	public void doOptions(HttpServletRequest req, HttpServletResponse res) throws ServletException
	{
		callExecute("OPTIONS", req, res);
	}

	public void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException
	{
		callExecute("PUT", req, res);
	}

	public void doTrace(HttpServletRequest req, HttpServletResponse res) throws ServletException
	{
		callExecute("TRACE", req, res);
	}

	private void dumpRequestInfo(HttpContext httpContext)
	{
		HttpServletRequest request = httpContext.getRequest();
		StringBuffer sBuffer = new StringBuffer();
		String nl = System.getProperty("line.separator");
		sBuffer.append("Request Information");
		sBuffer.append(nl + "Url: ");
		sBuffer.append(request.getRequestURL());
		sBuffer.append(nl + "HttpHeaders: " + nl);
		for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();)
		{
			String header = headerNames.nextElement();
			sBuffer.append(header);
			sBuffer.append(":");
			sBuffer.append(request.getHeader(header));
		}
		sBuffer.append(nl + "HttpCookies: " + nl);
		Cookie[] cookies = httpContext.getCookies();
		if	(cookies != null)
		{
			for (int i = 0; i < cookies.length; i++)
			{
				sBuffer.append(cookies[i].getName());
				sBuffer.append(":");
				sBuffer.append(cookies[i].getValue());
			}
		}
		logger.debug(sBuffer.toString());
	}

	private void callExecute(String method, HttpServletRequest req, HttpServletResponse res) throws ServletException
	{
		initialize(req, res);
		HttpContext httpContext = null;
		try
		{
			String gxcfg = getServletContext().getInitParameter("gxcfg");
			if (gxcfg != null)
			{
				Class gxcfgClass = Class.forName(gxcfg);
				ModelContext.gxcfgPackageClass = gxcfgClass;
				ApplicationContext appContext = ApplicationContext.getInstance();
				appContext.setServletEngine(true);
				Application.init(gxcfgClass);
			}
			httpContext = new HttpContextWeb(method, req, res, getServletContext());
			if (DEBUG)
				dumpRequestInfo(httpContext);
			boolean useAuthentication = IntegratedSecurityEnabled();
			if (!useAuthentication)
			{
				callDoExecute(httpContext);
			}
			else
			{
				init(httpContext);
				if (IntegratedSecurityLevel() == SECURITY_GXOBJECT)
				{
					httpContext.doNotCompress(true);
				}
				new WebApplicationStartup().init(getClass(), httpContext);
				boolean[] flag = new boolean[]{false};
				boolean[] permissionFlag = new boolean[]{false};
				String reqUrl = req.getRequestURL().toString();
				String queryString = req.getQueryString();
				if (queryString != null)
				{
					reqUrl += "?"+queryString;
				}
				ModelContext modelContext = ModelContext.getModelContext(getClass());
				modelContext.setHttpContext(httpContext);
				ApplicationContext.getInstance().setPoolConnections(!Namespace.createNamespace(modelContext).isRemoteGXDB());
				String loginObject = Application.getClientContext().getClientPreferences().getProperty("IntegratedSecurityLoginWeb", "");
				loginObject = GXutil.getClassName(loginObject);
				String loginObjectURL = URLRouter.getURLRoute(loginObject.toLowerCase(), new String[]{}, new String[]{}, httpContext.getRequest().getContextPath(), modelContext.getPackageName());
				String permissionPrefix = IntegratedSecurityPermissionPrefix();
				if (IntegratedSecurityLevel() == SECURITY_GXOBJECT)
				{
					String token = req.getHeader("Authorization");
					if (token != null && token.length() > 0)
					{
						token = token.replace("OAuth ", "");
						GXResult result = GXSecurityProvider.getInstance().checkaccesstoken(-2, modelContext, token, flag);
					}
					else
					{
						token = "";
						GXSecurityProvider.getInstance().checksession(-2, modelContext, reqUrl, flag);
					}
					if(!flag[0])
					{
						String OauthRealm = "OAuth realm=\"" + httpContext.getRequest().getServerName() + "\"";
						httpContext.getResponse().addHeader("WWW-Authenticate", OauthRealm);
						httpContext.sendResponseStatus(401, "Not Authorized");
					}
					else
					{
						callDoExecute(httpContext);
					}
				}
				else if (IntegratedSecurityLevel() == SECURITY_LOW)
				{
					GXSecurityProvider.getInstance().checksession(-2, modelContext, reqUrl, flag);
					if(!flag[0])
					{
						httpContext.redirect(loginObjectURL, true);
					}
					else
					{
						callDoExecute(httpContext);
					}
				}
				else
				{
					GXSecurityProvider.getInstance().checksessionprm(-2, modelContext, reqUrl, permissionPrefix, flag, permissionFlag);
					if (permissionFlag[0])
					{
						callDoExecute(httpContext);
					}
					else
					{
						String notAuthorizedObject = Application.getClientContext().getClientPreferences().getProperty("IntegratedSecurityNotAuthorizedWeb", "");
						notAuthorizedObject = GXutil.getClassName(notAuthorizedObject);
						String notAuthorizedObjectURL = URLRouter.getURLRoute(notAuthorizedObject.toLowerCase(), new String[]{}, new String[]{}, httpContext.getRequest().getContextPath(), modelContext.getPackageName());
						if (flag[0])
						{
							httpContext.redirect(notAuthorizedObjectURL, true);
						}
						else
						{
							httpContext.redirect(loginObjectURL, true);
						}
					}
				}
			}
			httpContext.setResponseCommited();
			httpContext.flushStream();
		}
		catch (Throwable e)
		{
			if (!res.isCommitted())
				res.reset();
			logger.error("Web Execution Error", e);
			if (DEBUG && httpContext != null)
				dumpRequestInfo(httpContext);
			throw new ServletException(com.genexus.PrivateUtilities.getStackTraceAsString(e));
		}
	}

	private void initialize(HttpServletRequest req, HttpServletResponse res) {
		if (logger == null) {
			logger = com.genexus.specific.java.LogManager.initialize(req.getServletContext().getRealPath("/"), GXWebObjectStub.class);
		}
		setResponseBufferSize(res);
	}

	private void setResponseBufferSize(HttpServletResponse res) {
		Integer bSize = 0;
		try {
			bSize = Application.getClientContext().getClientPreferences().getHttpBufferSize();
			if (res != null && !res.isCommitted() && bSize > 0)
			{
				res.setBufferSize(bSize);
			}
		}
		catch (Throwable e) {
			logger.error("Could not set bufferSize", e);
		}
	}

	private void callDoExecute(HttpContext httpContext) throws Throwable
	{
		doExecute(httpContext);
	}
}
