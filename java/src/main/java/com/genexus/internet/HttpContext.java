package com.genexus.internet;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import json.org.json.IJsonFormattable;
import json.org.json.JSONArray;
import json.org.json.JSONException;
import json.org.json.JSONObject;
import org.apache.logging.log4j.Logger;

import com.genexus.*;
import com.genexus.servlet.http.ICookie;
import com.genexus.servlet.http.IHttpServletRequest;
import com.genexus.servlet.http.IHttpServletResponse;
import com.genexus.webpanels.WebSession;

public abstract class HttpContext implements IHttpContext
{
	private static Logger logger = org.apache.logging.log4j.LogManager.getLogger(HttpContext.class);

	public static int SPA_NOT_SUPPORTED_STATUS_CODE = 530;
    private static String GX_SPA_REQUEST_HEADER = "X-SPA-REQUEST";
    protected static String GX_SPA_REDIRECT_URL = "X-SPA-REDIRECT-URL";
	private static String GX_SOAP_ACTION_HEADER = "SOAPAction";
	public static String GXTheme = "GXTheme";
	public static String GXLanguage = "GXLanguage";

    protected boolean PortletMode = false;
    protected boolean AjaxCallMode = false;
    protected boolean AjaxEventMode = false;
    protected boolean fullAjaxMode = false;
	protected boolean ajaxRefreshAsGET = false;
	public MsgList GX_msglist = new MsgList();

	public MsgList getMessageList() {
		return GX_msglist;
	}
	
	public void setFullAjaxMode()
	{	fullAjaxMode = true; }

	public boolean isFullAjaxMode()
	{	return fullAjaxMode; } 
	
    public void setPortletMode()
    { PortletMode = true; }

    public void setAjaxCallMode()
    { AjaxCallMode = true; }

	 public void setAjaxEventMode()
    { AjaxEventMode = true; }

    public boolean isPortletMode()
    { return PortletMode; }

    public boolean isAjaxCallMode()
    { return AjaxCallMode; }

    public boolean isAjaxEventMode()
    { return AjaxEventMode; }


    public boolean isAjaxRequest()
    { return isAjaxCallMode() || isAjaxEventMode() || isPortletMode(); }
    
    public boolean isSpaRequest(boolean ignoreFlag)
    {
        if (!ignoreFlag && ignoreSpa)
		{
			return false;
		}
        String header = getRequest()!=null ? getRequest().getHeader(GX_SPA_REQUEST_HEADER) : null;
        if (header != null)
            return header.compareTo("1") == 0;
        return false;
    }

    public boolean isSpaRequest()
    {
        return isSpaRequest(false);
    }	
	
	
	public void disableSpaRequest()
	{
		ignoreSpa = true;
	}
	
	public boolean isSoapRequest()
	{
		return getRequest() != null && getRequest().getHeader(GX_SOAP_ACTION_HEADER) != null;
	}

    public byte wbGlbDoneStart = 0;
				//nSOAPErr
	public HttpResponse GX_webresponse;


	protected String clientId = "";
	public String wjLoc = "";
	public int wjLocDisableFrm = 0;
	public String sCallerURL = "";
	public int nUserReturn = 0;
	public int wbHandled = 0;
	public com.genexus.xml.XMLWriter GX_xmlwrt = new com.genexus.xml.XMLWriter();

	protected com.genexus.util.FastByteArrayOutputStream buffer;
	protected boolean buffered;
	protected boolean compressed;
	protected boolean doNotCompress;
	protected ModelContext context;

	protected boolean isEnabled = true;
	private OutputStream out;
	private PrintWriter writer;
	private String userId;
	private boolean isBinary = false;
	private Boolean mustUseWriter;
	protected boolean isCrawlerRequest = false;

	private boolean responseCommited = false;
	protected boolean wrapped = false;
	protected int drawGridsAtServer = -1;
	private boolean ignoreSpa = false;

	private static HashMap<String, Messages> cachedMessages = new HashMap<String, Messages>();
	protected String currentLanguage = null;

	public boolean isOutputEnabled()
	{
		return this.isEnabled;
	}
		
	public void disableOutput()
	{
		isEnabled = false;
	}

	public void enableOutput()
	{
		isEnabled = true;
	}

	public ModelContext getContext()
	{
		return context;
	}

	public void setContext( ModelContext context)
	{
		this.context = context;
	}

	public String getClientId()
	{
		return this.clientId;
	}
	
	public void setClientId(String id)
	{
		this.clientId = id;
	}

	protected void copyCommon(HttpContext ctx)
	{
		ctx.wjLoc			=  wjLoc;
		ctx.sCallerURL		=  sCallerURL;
		ctx.nUserReturn		=  nUserReturn;
		ctx.wbHandled		=  wbHandled;
		ctx.GX_msglist		=  GX_msglist;
		ctx.GX_xmlwrt		=  GX_xmlwrt;
		ctx.buffer			=  buffer;
		ctx.buffered		=  buffered;
		ctx.compressed		=  compressed;
		ctx.out				=  out;
		ctx.writer			=  writer;
		ctx.userId			=  userId;
		ctx.staticContentBase = staticContentBase;
		ctx.isBinary = isBinary;
		ctx.mustUseWriter = mustUseWriter;
		ctx.responseCommited = responseCommited;
	}

	public abstract void cleanup();
	public abstract String getResourceRelative(String path);
	public abstract String getResourceRelative(String path, boolean includeBasePath);
	public abstract String getResource(String path);
	public abstract String GetNextPar();
	public abstract String GetPar(String parameter);
	public abstract String GetFirstPar(String parameter);
	public abstract HttpContext copy();
	public abstract byte setHeader(String header, String value);
	public abstract void setDateHeader(String header, int value);
	public abstract void setRequestMethod(String method);
	public abstract String getRequestMethod();
	public abstract String getReferer();
	public abstract short setWrkSt(int handle, String wrkst) ;
	public abstract String getWorkstationId(int handle) ;
	public abstract String getApplicationId(int handle) ;
	public abstract short setUserId(int handle, String user, String dataSource) ;
	/** 
	* @deprecated use getUserId(String key, int handle, com.genexus.db.IDataStoreProvider dataStore);
	* */
	public abstract String getUserId(String key, ModelContext context, int handle, String dataSource);
	public abstract String getUserId(String key, ModelContext context, int handle, com.genexus.db.IDataStoreProvider dataStore);
	public abstract String getRemoteAddr();
	public abstract boolean isLocalStorageSupported();
	public abstract boolean exposeMetadata();
	public abstract boolean isSmartDevice();
	public abstract int getBrowserType();
	public abstract boolean isIE55();
	public abstract String getDefaultPath();
	public abstract void setDefaultPath(String path);
	public abstract String getBrowserVersion();
	public abstract Object getSessionValue(String name);
	public abstract void webPutSessionValue(String name, Object value);
	public abstract void webPutSessionValue(String name, long value);
	public abstract void webPutSessionValue(String name, double value);
	public abstract void webSessionId(String[] id);
	public abstract String getContextPath();
	public abstract void setContextPath(String context);
	public abstract String webSessionId();
	public abstract String getCookie(String name);
	public abstract ICookie[] getCookies();
	public abstract byte setCookie(String name, String value, String path, java.util.Date expiry, String domain, double secure, Boolean httpOnly);
	public abstract byte setCookie(String name, String value, String path, java.util.Date expiry, String domain, double secure);
	public abstract byte setCookieRaw(String name, String value, String path, java.util.Date expiry, String domain, double secure);
	public abstract String getServerName();
	public abstract int getServerPort();
	public abstract String getScriptPath();
	public abstract int getHttpSecure();
	public abstract byte setContentType(String type);
	public abstract byte responseContentType (String type);
	public abstract String getHeader(String header);
	public abstract void sendError(int error);
	public abstract void setQueryString(String qs);
	public abstract String getQueryString();
	public abstract String getPackage();
	public abstract String cgiGetFileName(String parm);
	public abstract String cgiGetFileType(String parm);
	public abstract void getMultimediaValue(String internalName, String[] blobVar, String[] uriVar);
	public abstract String cgiGet(String parm);
    public abstract void changePostValue(String ctrl, String value);
	public abstract boolean isFileParm( String parm);
    public abstract void parseGXState(JSONObject tokenValues);
	public abstract void deletePostValue(String ctrl);
	public abstract void DeletePostValuePrefix(String sPrefix);
	public abstract HttpResponse getHttpResponse();
	public abstract HttpRequest getHttpRequest();
	public abstract void setHttpRequest(HttpRequest httprequest);
	public abstract IHttpServletRequest getRequest();
	public abstract IHttpServletResponse getResponse();
	public abstract void setRequest(IHttpServletRequest request);
	public abstract Hashtable getPostData();
	public abstract WebSession getWebSession();
	public abstract void setStream() ;
	public abstract void flushStream();
	public abstract boolean isHttpContextNull();
	public abstract boolean isHttpContextWeb();
	public abstract void redirect(String url);

	public void setWrapped(boolean wrapped)
	{
		this.wrapped = wrapped;
	}
	
	public boolean getWrapped()
	{
		return this.wrapped || this.isCrawlerRequest;
	}
		 
	public boolean isCrawlerRequest()
	{          
		if (getRequest() != null && getRequest().getQueryString() !=null && getRequest().getQueryString().contains("_escaped_fragment_"))
		{
			return true;
		}
		return false;
	}

	public void setResponseCommited()
	{
		responseCommited = true;
	}

	public boolean getResponseCommited()
	{
		return responseCommited;
	}
	
	public static String GX_NAV_HELPER = "GX_NAV_HELPER";
	
	protected GXNavigationHelper getNavigationHelper()
	{	
		return getNavigationHelper(true);
	}
	
	protected GXNavigationHelper getNavigationHelper(boolean useWebSession)
	{
		GXNavigationHelper helper;
		try
		{
			helper = (GXNavigationHelper) getSessionValue(GX_NAV_HELPER);
		}
		catch (Exception e)
		{
			//If it gives an error to recover the GXNavigationHelper from the session then I start with a new GXNavigationHelper to avoid a runtime error
			helper = null;
		}
		if (helper == null)
		{
			helper = new GXNavigationHelper();
			if (useWebSession)
			{
				webPutSessionValue(GX_NAV_HELPER, helper);
			}
		}
		return helper;
	}

	protected String getRequestNavUrl()
	{
		String sUrl = "";
		if (this.isAjaxRequest())
		{
			sUrl = getRequest().getHeader("REFERER");
		}
		else
		{
			sUrl = getRequest().getRequestURI();
			if (sUrl.indexOf('?') == -1)
			{
				String query = getRequest().getQueryString();
				if (query !=null) 
				  sUrl += "?" + getRequest().getQueryString();
			}
		}
		sUrl = (sUrl != null) ? sUrl : "";
		return sUrl;
	}

	public void deleteReferer(String popupLevel)
	{
		getNavigationHelper(false).deleteStack(popupLevel);
	}
	
	public void deleteReferer()
	{
		deleteReferer(getNavigationHelper(false).getUrlPopupLevel(getRequestNavUrl()));            
	}

	public void pushCurrentUrl()
	{
		if (getRequestMethod().equals("GET") && !isAjaxRequest())
		{
			String sUrl = getRequestNavUrl().trim();
			String topUrl = getNavigationHelper().peekUrl(sUrl);
			if (!topUrl.equals(sUrl) && !sUrl.equals(""))
			{	
				getNavigationHelper().pushUrl(sUrl);
			}
		}			
	}
	
	public void sendResponseStatus(int statusCode, String statusDescription)
	{
		if (statusCode < 200 || statusCode >= 300)
		{
			getResponse().setHeader("Content-Encoding", "");
			if (statusCode != SPA_NOT_SUPPORTED_STATUS_CODE)
			{
				CommonUtil.writeLog("sendResponseStatus " + Integer.toString(statusCode) + " " + statusDescription);
			}
		}        	
		getResponse().setStatus(statusCode);
		try {
			getResponse().sendError(statusCode, statusDescription);
		}
		catch(Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error("Could not send Response Error Code", e);
			}
		}
		setAjaxCallMode();
		disableOutput();
	}
	
	private static String CLIENT_ID_HEADER = "GX_CLIENT_ID";
	
	public void initClientId()
	{			
		if (!isSoapRequest() && getWebSession() != null && this.getClientId().equals(""))
		{                    
			String _clientId = this.getCookie(CLIENT_ID_HEADER);
			if (_clientId == null || _clientId.equals(""))
			{
				_clientId = java.util.UUID.randomUUID().toString();
				this.setCookie(CLIENT_ID_HEADER, _clientId, "/", new Date(Long.MAX_VALUE), "", getHttpSecure());
			}
			this.setClientId(_clientId);
		}            
	}
	
	public boolean useBase64ViewState()
	{
		return ((Preferences)this.context.getPreferences()).getProperty("UseBase64ViewState", "n").equals("y");
	}
	
	public boolean useSecurityTokenValidation()
	{
		return ((Preferences)this.context.getPreferences()).getProperty("ValidateSecurityToken", "y").equals("y");
	}

	public String getEncryptedSignature( String value, String key)
	{
		return encrypt64(CommonUtil.getHash( GXutil.StripInvalidChars(value), CommonUtil.SECURITY_HASH_ALGORITHM), key);
	}

	public String encrypt64(String value, String key)
    {
        String sRet = "";
        try
        {
            sRet = com.genexus.util.Encryption.encrypt64(value, key);
        }
        catch(com.genexus.util.Encryption.InvalidGXKeyException e)
        {
            setCookie("GX_SESSION_ID", "", "", new Date(Long.MIN_VALUE), "", getHttpSecure());
            com.genexus.diagnostics.Log.debug("440 Invalid encryption key");
            sendResponseStatus(440, "Session timeout");
        }
        return sRet;
    }

    public String decrypt64(String value, String key)
    {
        String sRet = "";
        try
        {
            sRet = com.genexus.util.Encryption.decrypt64(value, key);
        }
        catch (com.genexus.util.Encryption.InvalidGXKeyException e)
        {
            setCookie("GX_SESSION_ID", "", "", new Date(Long.MIN_VALUE), "", getHttpSecure());
            com.genexus.diagnostics.Log.debug( "440 Invalid encryption key");
            sendResponseStatus(440, "Session timeout");
        }
        return sRet;
    }

	public void webGetSessionValue(String name, byte[] value)
	{
		Object o = getSessionValue(name);
		if	(o != null)
		{
			value[0] = ((Long) o).byteValue();
		}
	}

	public void webGetSessionValue(String name, short[] value)
	{
		Object o = getSessionValue(name);
		if	(o != null)
			value[0] = ((Long) o).shortValue();
	}

	public void webGetSessionValue(String name, int[] value)
	{
		Object o = getSessionValue(name);
		if	(o != null)
			value[0] = ((Long) o).intValue();
	}

	public void webGetSessionValue(String name, long[] value)
	{
		Object o = getSessionValue(name);
		if	(o != null)
			value[0] = ((Long) o).longValue();
	}

	public void webGetSessionValue(String name, float[] value)
	{
		Object o = getSessionValue(name);
		if	(o != null)
			value[0] = ((Double) o).floatValue();
	}

	public void webGetSessionValue(String name, double[] value)
	{
		Object o = getSessionValue(name);
		if	(o != null)
			value[0] = ((Double) o).doubleValue();
	}

	public void webGetSessionValue(String name, String[] value)
	{
		Object o = getSessionValue(name);
		if	(o != null)
		{
			value[0] = (String) o;
		}
	}

	public String getSubscriberId()
	{
		return getHeader("HTTP_X_UP_SUBNO");
	}

	public void setOutputStream(OutputStream out)
	{
		this.out = out;
	}

	public boolean useUtf8 = false;

  public void _writeText(String text)
  {
	  _writeText(text, false);
  }
  
  public void _writeText(String text, boolean ignoreCommitedResponse)
  {
      if (!ignoreCommitedResponse && getResponseCommited())
                return;
  	try
		{
			if	(mustUseWriter())
			{
				writer.print(text);
			}
			else
			{
				writeBytes(useUtf8 ? text.getBytes("UTF8") : text.getBytes());
			}
		}
		catch (IOException e1)
		{
		}
  }

	public void setBinary(boolean isBinary)
	{
		this.isBinary = isBinary;
	}

	public boolean mustUseWriter()
	{
		if	(isBinary)
			return false;

		if	(mustUseWriter == null)
		{
			mustUseWriter = new Boolean(System.getProperty("os.name").startsWith("OS/390"));
		}

		return mustUseWriter.booleanValue();
	}


	public void writeBytes(byte[] bytes) throws IOException
	{
            out.write(bytes);
			if (getHttpResponse().getHeader("Transfer-Encoding").equalsIgnoreCase("chunked"))
				out.flush();
	}

	public OutputStream getOutputStream()
	{
		return out;
	}

	public void setWriter(PrintWriter writer)
	{
		this.writer = writer;
	}

	public void closeWriter()
	{
		if	(writer != null)
			writer.close();
	}

	public void closeOutputStream()
	{
		if	(out != null)
		try
		{
			out.close();
		}
		catch(IOException e) 
		{
			if (logger.isDebugEnabled()) {
				logger.debug("ERROR: When Closing Output Stream.", e);
			}
		}
	}

	public void setBuffered(boolean buffered)
	{
		this.buffered = buffered;
	}

	public void setCompression(boolean compressed)
	{
		if (doNotCompress)
			this.compressed = false;
		else
			this.compressed = compressed;
	}
	
	public void doNotCompress(boolean doNotCompress)
	{
		this.doNotCompress = doNotCompress;
	}	

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	protected String staticContentBase = "";
	public void setStaticContentBase(String staticContentBase)
	{
		this.staticContentBase = staticContentBase;
	}
	public String getStaticContentBase()
	{
		return staticContentBase;
	}

	private String theme = "";
	private boolean isDSO = false;

	public String getTheme()
	{
	    WebSession session = getWebSession();
	    if (session!=null){
			HashMap cThemeMap = (HashMap)session.getObjectAttribute(GXTheme);
			if (cThemeMap != null && cThemeMap.containsKey(theme))
				return (String)cThemeMap.get(theme);
		}
		
		if (theme.trim().length() == 0)
			theme = Preferences.getDefaultPreferences().getDefaultTheme();

		return theme;
	}

	public boolean getThemeisDSO() {
		return isDSO;
	}

	public void setDefaultTheme(String t){
		setDefaultTheme( t, false);
	}
	public void setDefaultTheme(String t, boolean isDSO)
	{
		theme = t;
		this.isDSO = isDSO;
	}
	@SuppressWarnings("unchecked")
	public int setTheme(String t)
	{
	    WebSession session = getWebSession();
		if (session==null || t==null || t.equals(""))
			return 0;
		else
		{
			HashMap<String, String> cThemeMap = (HashMap<String, String>)session.getObjectAttribute(GXTheme);
			if (cThemeMap == null)
				cThemeMap = new HashMap<>();
			cThemeMap.put(theme, t);
			session.setObjectAttribute(GXTheme, cThemeMap);
			return 1;
		}
	}
	public String getImagePath(String file, String KBId, String theme)
	{
		String result = ImagesPath.getImagePath(file, theme, KBId, this);
		if (result == null)
		{
			return file;
		}
		else
		{
			if (this instanceof com.genexus.webpanels.HttpContextWeb)
			{
				return staticContentBase + result;
			}
			else
			{
				return result;
			}
		}
	}

	public String getImageSrcSet(String baseImage)
	{
		return ImagesPath.getImageSrcSet(this, baseImage);
	}

	public String getBuildNumber(int buildN){
		return context.getClientPreferences().getBUILD_NUMBER(buildN);
	}

	public String convertURL(String file)
	{
		String url = "";
		if (file.equals(""))
		{
			return "";
		}

		if (file.indexOf(".") != -1)
		{
			url = oldConvertURL(file);
		}
		else
		{
			url = oldConvertURL(getImagePath(file, "", getTheme()));
		}
		
		if (this.getWrapped() && !this.isCrawlerRequest() && this.drawGridsAtServer != 1 && !((file.startsWith("http:")) || (file.startsWith("//")) || (file.length() > 2 && file.charAt(1) == ':')))
		{
			int idx = url.lastIndexOf("/");
			if (idx > 0)
				url = url.substring(idx+1, url.length());
		}
		return url;
	}

	public String oldConvertURL(String file)
	{
		String out = file.trim();

		if	((file.startsWith("http:")) || (file.startsWith("https:")) || (file.startsWith("data:")) || (file.startsWith("about:")) || (file.startsWith("//")) || (file.length() > 2 && file.charAt(1) == ':'))
		{
			return out;
		}

		if	(file.startsWith("/"))
		{
			if (file.startsWith(getContextPath()) || file.startsWith(getDefaultPath()))
				return out;
			return getContextPath() + out;
		}

		if	((staticContentBase.startsWith("http")) || (staticContentBase.length() > 2 && staticContentBase.charAt(1) == ':'))
		{
			return staticContentBase + out;
		}

		if (this instanceof com.genexus.webpanels.HttpContextWeb)
		{
			return getContextPath() + staticContentBase + out;
		}
		else
		{
			return out;
		}
	}

        public String getMessage(String code, String language)
        {
            if (language==null || language.equals(""))
				return getMessage(code);
			else 
			{
				String languageCode = Application.getClientPreferences().getProperty("language|"+ language.toLowerCase(), "code", "");
				if (languageCode==null || languageCode.equals(""))
				{
					languageCode=language.toLowerCase();
				}
				String resourceName = "messages." + languageCode.toLowerCase() + ".txt";
				Messages msgs = com.genexus.Messages.getMessages(resourceName, Application.getClientLocalUtil().getLocale());
				return msgs.getMessage(code);
			}
		}

		public String getMessage(String code)
		{
			String language = getLanguage();
			Messages messages = cachedMessages.get(language);
			if (messages == null) {
				String languageCode = Application.getClientPreferences().getProperty("language|" + language, "code", Application.getClientPreferences().getProperty("LANGUAGE", "eng"));
				messages = com.genexus.Messages.getMessages("messages." + languageCode.toLowerCase() + ".txt", Application.getClientLocalUtil().getLocale());
				addCachedLanguageMessage(language, messages);
			}
			return messages.getMessage(code);
		}

		private synchronized void addCachedLanguageMessage(String language, Messages msg) {
			cachedMessages.putIfAbsent(language, msg);
		}

		public int getYearLimit()
		{
			return Application.getClientPreferences().getYEAR_LIMIT();
		}

		public int getStorageTimezone()
		{
			return Application.getClientPreferences().getStorageTimezonePty();
		}

		public String getLanguageProperty(String property)
		{
			String _language = getLanguage();
			return Application.getClientPreferences().getProperty("language|"+ _language, property, "");
		}
		public String getLanguage()
		{
			if (currentLanguage == null) {
				WebSession session = getWebSession();
				String language = session != null ? session.getAttribute(GXLanguage) : null;
				if (language != null && !language.equals("")) {
					currentLanguage = language;
				} else {
					//Por ahora obtengo el del modelo, mas adelante puede haber uno por cada session
					currentLanguage = Application.getClientPreferences().getProperty("LANG_NAME", "English");
				}
			}
			return currentLanguage;
		}

		public int setLanguage(String language)
		{
			if (!language.isEmpty() && Application.getClientPreferences().getProperty("language|"+ language, "code", null)!=null)
			{
				this.currentLanguage = language;
				getWebSession().setAttribute(GXLanguage, language);
				ajaxRefreshAsGET = true;
				return 0;
			}else
			{
				return 1;
			}
		}

		public boolean checkContentType(String contentKey, String contentType, String fullPath)
		{
			if (contentType.trim().equals(""))
			{
				int lastDot = fullPath.lastIndexOf(".");
				if (lastDot != -1)
				{
					String ext = fullPath.substring(lastDot + 1);
					contentType = getContentFromExt(ext);
				}
			}
			if (contentType != null)
				return contentType.toLowerCase().startsWith(contentKey.toLowerCase() + "/");
			else
				return false;
		}

		public static boolean isKnownContentType(String type)
		{
			if (type != null)
			{
				for (int i = 0; i < contentTypes.length; i++)
				{
					if (contentTypes[i].length >= 2)
					{
						if (type.equalsIgnoreCase(contentTypes[i][1]))
							return true;
					}
				}
			}
			return false;
		}

		public static String getContentFromExt( String extension)
		{
		  if (extension != null)
		  {
			extension = extension.toLowerCase();
			for (int i = 0; i < contentTypes.length; i++) {
			  if (contentTypes[i][0].equals(extension.trim()))
				return contentTypes[i][1];
			}
		  }
		  return null;
		}
		
		int GX_NULL_TIMEZONEOFFSET = 9999;
		
	protected boolean restService = false;

	public void setRestService()
	{ restService = true; }

	public boolean isRestService()
	{ return restService; }		
		
		private static final String contentTypes[][] = {
								{"txt" 	, "text/plain"},
								{"rtx" 	, "text/richtext"},
								{"htm" 	, "text/html"},
								{"html" , "text/html"},
								{"xml" 	, "text/xml"},
								{"aif"	, "audio/x-aiff"},
								{"au"	, "audio/basic"},
								{"wav"	, "audio/wav"},
								{"bmp"	, "image/bmp"},
								{"gif"	, "image/gif"},
								{"jpe"	, "image/jpeg"},
								{"jpeg"	, "image/jpeg"},
								{"jpg"	, "image/jpeg"},
								{"jfif"	, "image/pjpeg"},
								{"tif"	, "image/tiff"},
								{"tiff"	, "image/tiff"},
								{"png"	, "image/x-png"},
								{"3gp"	, "video/3gpp"},
								{"3g2"	, "video/3gpp2"},
								{"mpg"	, "video/mpeg"},
								{"mpeg"	, "video/mpeg"},
								{"mov"	, "video/quicktime"},
								{"qt"	, "video/quicktime"},
								{"avi"	, "video/x-msvideo"},
								{"exe"	, "application/octet-stream"},
								{"dll"	, "application/x-msdownload"},
								{"ps"	, "application/postscript"},
								{"pdf"	, "application/pdf"},
								{"svg"	, "image/svg+xml"},
								{"tgz"	, "application/x-compressed"},
								{"zip"	, "application/x-zip-compressed"},
								{"gz"	, "application/x-gzip"},
								{"json"	, "application/json"}
								};

		public boolean willRedirect()
		{
			return wjLoc != null && wjLoc.trim().length() != 0;
		}

	public void readJsonSdtValue(String jsonStr, Object SdtObj)
	{
		try
		{
			IJsonFormattable jsonObj;
			if (jsonStr.startsWith("["))
				jsonObj = new JSONArray(jsonStr);
			else
				jsonObj = new JSONObject(jsonStr);
			((IGxJSONAble)SdtObj).FromJSONObject(jsonObj);
		}
		catch(JSONException exc) {}
	}
}
