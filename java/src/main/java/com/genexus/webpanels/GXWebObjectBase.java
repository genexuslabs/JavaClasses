package com.genexus.webpanels;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.TimeZone;

import com.genexus.*;
import com.genexus.configuration.ConfigurationManager;
import com.genexus.diagnostics.GXDebugInfo;
import com.genexus.diagnostics.GXDebugManager;
import org.apache.commons.lang.StringUtils;

import com.genexus.ModelContext;
import com.genexus.db.Namespace;
import com.genexus.db.UserInformation;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.internet.GXInternetConstants;
import com.genexus.internet.HttpContext;
import com.genexus.internet.IGxJSONSerializable;
import com.genexus.security.GXSecurityProvider;
import com.genexus.security.web.SecureTokenHelper;
import com.genexus.security.web.WebSecurityHelper;
import com.genexus.util.GXTimeZone;

import com.genexus.GXRestServiceWrapper;

public abstract class GXWebObjectBase extends GXRestServiceWrapper implements IErrorHandler, GXInternetConstants, ISubmitteable
{
	public static final ILogger logger = LogManager.getLogger(GXWebObjectBase.class);

	private static String GX_SPA_GXOBJECT_RESPONSE_HEADER = "X-GXOBJECT";
    protected static String GX_SPA_MASTERPAGE_HEADER = "X-SPA-MP";
	public static int SPA_NOT_SUPPORTED_STATUS_CODE = 530;
	protected static String GX_AJAX_MULTIPART_ID = "GXAjaxMultipart";

	protected boolean IntegratedSecurityEnabled() { return false;}
	protected int IntegratedSecurityLevel() { return 0;}
	protected String IntegratedSecurityPermissionPrefix() {return "";}

	protected static final int SECURITY_GXOBJECT = 3;
	protected static final int SECURITY_HIGH = 2;
	protected static final int SECURITY_LOW  = 1;

	public abstract void webExecute();
	public abstract boolean isMasterPage();

	protected ModelContext context;
	protected HttpContext  httpContext;
	protected LocalUtil    localUtil;
	protected int remoteHandle = -1;

	protected UserInformation ui;
	protected TimeZone timeZone;

	public Object getParm( Object[] parms, int index)
	{
	  return parms[index];
	}

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
	}

	/**
	* Este constructor se usa cuando aun no tengo un ModelContext ni remoteHandle, pero
	* si tengo el HttpContext. Basicamente es el punto de entrada en los servlets.
	*/
	public GXWebObjectBase(HttpContext httpContext)
	{
		init(httpContext, getClass());
	}

	/**
	* Este constructor se usa cuando ya tengo un ModelContext y remoteHandle.
	* Basicamente es el punto de entrada para webcomponents, webwrappers, etc.
	*/
	public GXWebObjectBase(int remoteHandle, ModelContext context)
	{
		this.context      = context;

		ui = (UserInformation) GXObjectHelper.getUserInformation(context, remoteHandle);
		this.remoteHandle = ui.getHandle();

		initState(context, ui);
	}

	/***
	 * Return the DefaultTheme for all WebPanels and Transactions.
	 * @return
	 */
	@SuppressWarnings("unused")
	protected void initializeTheme() {
		this.httpContext.setDefaultTheme(ConfigurationManager.getValue("Theme"));
	}

	protected void init(HttpContext httpContext, Class contextClass)
	{
		// @gusbro:06/06/05
		// El JDK.15 mantiene un TimeZone default por cada Thread, por lo cual el hack que haciemos
		// en la gxutil de setear el timeZone falla en motores de servlets
		// Lo que hacemos es setear el default aqui, ademas para ser mas 'prolijos' volvemos al valor
		// original al finalizar el request (esto ultimo ya no se hace, porque puede traer problemas)
		timeZone = GXTimeZone.getDefaultOriginal();
		this.context = new ModelContext(contextClass);
		context.setHttpContext(httpContext);

		new WebApplicationStartup().init(contextClass, httpContext);

		ApplicationContext.getInstance().setPoolConnections(!Namespace.createNamespace(context).isRemoteGXDB());

		ui = (UserInformation) GXObjectHelper.getUserInformation(context, -1);
		ui.setAutoDisconnect(false);
		remoteHandle = ui.getHandle();

		initState(context, ui);
	}

	protected void initState(ModelContext context, UserInformation ui)
	{
		localUtil   = ui.getLocalUtil();
		httpContext = (HttpContext) context.getHttpContext();
		httpContext.setContext( context);
		httpContext.setCompression(getCompressionMode());
	}

	protected boolean getCompressionMode()
	{
		return context.getClientPreferences().getCOMPRESS_HTML();
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

	protected void preExecute()
	{
		httpContext.responseContentType("text/html"); //default Content-Type
		httpContext.initClientId();
	}

	protected void sendCacheHeaders()
	{
		if (httpContext.isSpaRequest()) {
			httpContext.getResponse().setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			httpContext.getResponse().setHeader("Pragma", "no-cache");
			httpContext.getResponse().setHeader("Expires", "0");
		}
		else {
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
	}

	protected void sendAdditionalHeaders()
	{
		if (httpContext.isSpaRequest())
			sendSpaHeaders();
		if (httpContext.getBrowserType() == HttpContext.BROWSER_IE && !httpContext.isPopUpObject())
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
				httpContext.sendResponseStatus(SPA_NOT_SUPPORTED_STATUS_CODE, "SPA not supported by the object");
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
			handleException(e.getClass().getName(), e.getMessage(), CommonUtil.getStackTraceAsString(e));
			cleanup(); // Antes de hacer el rethrow, hago un cleanup del objeto
			throw e;
		}
		finally
		{
			finallyCleanup();
		}
	}

	protected void finallyCleanup()
	{
		try
		{
			if (ui!= null)
				ui.disconnect();
		}
		catch (java.sql.SQLException e)
		{
			logger.error("Exception while disconecting ", e);
		}
		if (httpContext != null)
			httpContext.cleanup();
		cleanModelContext();
	}

	private void cleanModelContext()
	{
		try
		{
			((ThreadLocal)com.genexus.CommonUtil.threadCalendar).getClass().getMethod("remove", new Class[0]).invoke(com.genexus.CommonUtil.threadCalendar, (java.lang.Object[])new Class[0]);
			((ThreadLocal)com.genexus.ModelContext.threadModelContext).getClass().getMethod("remove", new Class[0]).invoke(com.genexus.ModelContext.threadModelContext, (java.lang.Object[])new Class[0]);
		}
		catch (NoSuchMethodException e)
		{
			logger.error("cleanModelContext", e);
		}
		catch (IllegalAccessException e)
		{
			logger.error("cleanModelContext", e);
		}
		catch (java.lang.reflect.InvocationTargetException e)
		{
			logger.error("cleanModelContext " + e.getTargetException(), e);
		}
	}

	protected void cleanup()
	{
		Application.cleanupConnection(remoteHandle);
	}

	public HttpContext getHttpContext()
	{
		return httpContext;
	}

	public void setHttpContext(HttpContext httpContext)
	{
		this.httpContext = httpContext;
	}


	public ModelContext getModelContext()
	{
		return context;
	}

	public int getRemoteHandle()
	{
		return remoteHandle;
	}

	public void handleError()
	{
		new DefaultErrorHandler().handleError(context, remoteHandle);
	}

	public ModelContext getContext()
	{
		return context;
	}

        public int setLanguage(String language)
        {
            int res = GXutil.setLanguage(language, context, ui);
            this.localUtil = ui.getLocalUtil();
            return res;
        }
		public int setTheme(String theme)
		{
			int res = GXutil.setTheme(theme, context);
			return res;
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

	/**
	 *  @Hack: Tenemos que ejecutar el submit esto en otro thread, pues el submit es asincrono,
	 *        pero si creamos en el fuente generado el codigo del nuevo thread, se va a crear un
	 *        archivo nuevo xxx$N.class al compilar el xxx, que deberia ser tratado especialmente
	 *        en el makefile (copiado al directorio de servlets, etc); asi que delegamos la
	 *        creacion del thread a la gxclassr donde se llama al metodo a ser ejecutado
	 *		  Adem�s ahora manejamos un pool de threads en el submit
	 */
	public void callSubmit(final int id, Object [] submitParms)
	{
		com.genexus.util.SubmitThreadPool.submit(this, id, submitParms, context.submitCopy());
	}

	/** Este metodo es redefinido por la clase GX generada cuando hay submits
	 */
	public void submit(int id, Object [] submitParms, ModelContext ctx){
	}
	public void submit(int id, Object [] submitParms){
	}
	public void submitReorg(int id, Object [] submitParms) throws SQLException{
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


	private static String GX_SEC_TOKEN_PREFIX = "GX_AUTH";

       //Generates only with FullAjax and GAM disabled.
	public void sendSecurityToken(String cmpCtx)
	{
		if (this.httpContext.wjLoc == null || this.httpContext.wjLoc.equals("") )
		{
			this.httpContext.ajax_rsp_assign_hidden(getSecurityObjTokenId(cmpCtx), getObjectAccessWebToken(cmpCtx));
		}
	}

	private String getSecurityObjTokenId(String cmpCtx)
	{
		return GX_SEC_TOKEN_PREFIX + "_" + cmpCtx + getPgmname().toUpperCase();
	}

	 protected String getPgmInstanceId(String cmpCtx)
     {
         return String.format("%s%s", cmpCtx, this.getPgmname().toUpperCase());
     }

	private String getObjectAccessWebToken(String cmpCtx)
	{
		return WebSecurityHelper.sign(getPgmInstanceId(cmpCtx), "", "", SecureTokenHelper.SecurityMode.Sign, getSecretKey());
	}

	private String getSecretKey()
    {
    	//Some random SALT that is different in every GX App installation. Better if changes over time
       String hashSalt = com.genexus.Application.getClientContext().getClientPreferences().getREORG_TIME_STAMP();
       return WebUtils.getEncryptionKey(this.context, "") + hashSalt;
    }

	private String serialize(double Value, String Pic)
    {
		return serialize(localUtil.format(Value, Pic));
    }

	private String serialize(int Value, String Pic)
    {
		return serialize(localUtil.format(Value, Pic));
    }

	private String serialize(short Value, String Pic)
    {
		return serialize(localUtil.format(Value, Pic));
    }

	private String serialize(long Value, String Pic)
    {
		return serialize(localUtil.format(Value, Pic));
    }

	private String serialize(Object Value, String Pic)
    {
		if (!StringUtils.isBlank(Pic)) {
			if (Value instanceof Byte)
			{
				return serialize(localUtil.format(((Byte)Value).intValue(), Pic));
			}
			else
			{
				if (Value instanceof BigDecimal)
				{
					return serialize(localUtil.format((BigDecimal)Value, Pic));
				}
				else
				{
					if (Value instanceof Integer)
					{
						return serialize(localUtil.format(((Integer)Value).intValue(), Pic));
					}
					else
					{
						if (Value instanceof Short)
						{
							return serialize(localUtil.format(((Short)Value).shortValue(), Pic));
						}
						else
						{
							if (Value instanceof Long)
							{
								return serialize(localUtil.format(((Long)Value).longValue(), Pic));
							}
							else
							{
								if (Value instanceof Double)
								{
									return serialize(localUtil.format(((Double)Value).doubleValue(), Pic));
								}
								else
								{
									if (Value instanceof Float)
									{
										return serialize(localUtil.format(((Float)Value).floatValue(), Pic));
									}
									else
									{
										if (Value instanceof java.util.Date)
										{
											return serialize(localUtil.format((java.util.Date)Value, Pic));
										}
										else
										{
											if (Value instanceof String)
											{
												return serialize(localUtil.format((String)Value, Pic));
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
        return serialize(Value);
    }

	private String serialize(Object Value) {
		String strValue = "";
		if (Value instanceof BigDecimal){
			strValue = Value.toString();
			if (strValue.indexOf(".") != -1)
				strValue = strValue.replaceAll("0*$", "").replaceAll("\\.$", "");
		}
		else{
	        if (Value instanceof java.util.Date){
	        	strValue = "    /  /   00:00:00";
	    		if (!Value.equals(CommonUtil.resetTime( CommonUtil.nullDate()))) {
	    			strValue = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Value);
	    		}
	        }
	        else{
				if (Value instanceof com.genexus.xml.GXXMLSerializable) {
					strValue = ((com.genexus.xml.GXXMLSerializable) Value).toJSonString(false);
				}
				else if (Value instanceof IGxJSONSerializable) {
					strValue = ((IGxJSONSerializable) Value).toJSonString();
				}
				else {
					strValue = Value.toString();
				}
	        }
		}
		return strValue;
	}

    protected String getSecureSignedToken(String cmpCtx, Object Value)
    {
        return getSecureSignedToken(cmpCtx, serialize(Value));
	}

    protected String getSecureSignedToken(String cmpCtx, boolean Value)
    {
        return getSecureSignedToken(cmpCtx, Boolean.toString(Value));
    }

    protected String getSecureSignedToken(String cmpCtx, com.genexus.xml.GXXMLSerializable Value)
	{
        return getSecureSignedToken(cmpCtx, Value.toJSonString(false));
    }

    protected String getSecureSignedToken(String cmpCtx, String Value)
    {
    	return WebSecurityHelper.sign(getPgmInstanceId(cmpCtx), "", Value, SecureTokenHelper.SecurityMode.Sign, getSecretKey());
    }

    protected boolean verifySecureSignedToken(String cmpCtx, int Value, String picture, String token){
    	return verifySecureSignedToken(cmpCtx, serialize(Value, picture), token);
    }

    protected boolean verifySecureSignedToken(String cmpCtx, short Value, String picture, String token){
    	return verifySecureSignedToken(cmpCtx, serialize(Value, picture), token);
    }

    protected boolean verifySecureSignedToken(String cmpCtx, long Value, String picture, String token){
    	return verifySecureSignedToken(cmpCtx, serialize(Value, picture), token);
    }

    protected boolean verifySecureSignedToken(String cmpCtx, double Value, String picture, String token){
    	return verifySecureSignedToken(cmpCtx, serialize(Value, picture), token);
    }

    protected boolean verifySecureSignedToken(String cmpCtx, Object Value, String picture, String token){
    	return verifySecureSignedToken(cmpCtx, serialize(Value, picture), token);
    }

    protected boolean verifySecureSignedToken(String cmpCtx, Object Value, String token){
    	return verifySecureSignedToken(cmpCtx, serialize(Value), token);
    }

    protected boolean verifySecureSignedToken(String cmpCtx, boolean Value, String token){
		return verifySecureSignedToken(cmpCtx, Boolean.toString(Value), token);
    }

    protected boolean verifySecureSignedToken(String cmpCtx, com.genexus.xml.GXXMLSerializable Value, String token){
    	return verifySecureSignedToken(cmpCtx, Value.toJSonString(false), token);
    }

    protected boolean verifySecureSignedToken(String cmpCtx, String value, String token){
    	return WebSecurityHelper.verify(getPgmInstanceId(cmpCtx), "", value, token, getSecretKey());
    }

	protected boolean validateObjectAccess(String cmpCtx)
	{
		if (this.httpContext.useSecurityTokenValidation()){
			String jwtToken = this.httpContext.getHeader("X-GXAUTH-TOKEN");
			jwtToken = (StringUtils.isBlank(jwtToken) && this.httpContext.isMultipartContent())?
					this.httpContext.cgiGet("X-GXAUTH-TOKEN"):
					jwtToken;

			if (!verifySecureSignedToken(cmpCtx, "", jwtToken))
			{
				this.httpContext.sendResponseStatus(401, "Not Authorized");
				if (this.httpContext.getBrowserType() != HttpContextWeb.BROWSER_INDEXBOT) {
					logger.warn(String.format("Validation security token failed for program: %s - '%s'", getPgmInstanceId(cmpCtx), jwtToken ));
				}
				return false;
			}
		}
		return true;
	}

	public void handleException(String gxExceptionType, String gxExceptionDetails, String gxExceptionStack)
	{
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
}
