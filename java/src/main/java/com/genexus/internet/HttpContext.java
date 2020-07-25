package com.genexus.internet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.genexus.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;

import com.genexus.usercontrols.UserControlFactoryImpl;
import com.genexus.util.Codecs;
import com.genexus.util.Encryption;
import com.genexus.util.GXMap;
import com.genexus.util.ThemeHelper;
import com.genexus.webpanels.GXResourceProvider;
import com.genexus.webpanels.GXWebObjectBase;
import com.genexus.webpanels.WebSession;

import json.org.json.IJsonFormattable;
import json.org.json.JSONArray;
import json.org.json.JSONException;
import json.org.json.JSONObject;
import org.apache.commons.lang.StringEscapeUtils;

public abstract class HttpContext 
		extends HttpAjaxContext implements IHttpContext
{
    private static String GX_AJAX_REQUEST_HEADER = "GxAjaxRequest";
    private static String GX_SPA_REQUEST_HEADER = "X-SPA-REQUEST";
    protected static String GX_SPA_REDIRECT_URL = "X-SPA-REDIRECT-URL";
	private static String GX_SOAP_ACTION_HEADER = "SOAPAction";
	
    private static String CACHE_INVALIDATION_TOKEN;

    protected boolean PortletMode = false;
    protected boolean AjaxCallMode = false;
    protected boolean AjaxEventMode = false;
    protected boolean fullAjaxMode = false;
	public boolean drawingGrid = false;
	 
	
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

    public void ajax_sending_grid_row(com.genexus.webpanels.GXWebRow row)
    {
        if (isAjaxCallMode())
        {
            _currentGridRow = row;
        }
        else
        {
            _currentGridRow = null;
        }
    }

    public byte wbGlbDoneStart = 0;
				//nSOAPErr
	public HttpResponse GX_webresponse;


	private String clientId = "";
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

	private boolean isEnabled = true;
	private OutputStream out;
	private PrintWriter writer;
	private String userId;
	private boolean isBinary = false;
	private Boolean mustUseWriter;
	protected boolean isCrawlerRequest = false;
	private boolean validEncryptedParm = true;        
	private boolean encryptionKeySended = false;

	private Vector<String> javascriptSources = new Vector<>();
	private Vector<String> deferredFragments = new Vector<String>();

	private Vector<String> styleSheets = new Vector<>();
	private HashSet<String> deferredJavascriptSources = new HashSet<String>();
	private boolean responseCommited = false;
	private boolean wrapped = false;
	private int drawGridsAtServer = -1;
	private boolean ignoreSpa = false;

	private String serviceWorkerFileName = "service-worker.js";
	private Boolean isServiceWorkerDefinedFlag = null;

	private String webAppManifestFileName = "manifest.json";
	private Boolean isWebAppManifestDefinedFlag = null;

	private static HashMap<String, Messages> cachedMessages = new HashMap<String, Messages>();
	private String currentLanguage = null;

	private boolean isServiceWorkerDefined()
	{
		if (isServiceWorkerDefinedFlag == null)
		{
			isServiceWorkerDefinedFlag = Boolean.valueOf(checkFileExists(serviceWorkerFileName));
		}
		return isServiceWorkerDefinedFlag == Boolean.valueOf(true);
	}

	private boolean isWebAppManifestDefined()
	{
		if (isWebAppManifestDefinedFlag == null)
		{
			isWebAppManifestDefinedFlag = Boolean.valueOf(checkFileExists(webAppManifestFileName));
		}
		return isWebAppManifestDefinedFlag == Boolean.valueOf(true);
	}

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

	public static final int TYPE_RESET		= 0;
	public static final int TYPE_SUBMIT		= 1;
	public static final int TYPE_BUTTON 		= 2;
        
	public static final int BROWSER_OTHER		= 0;
	public static final int BROWSER_IE 		= 1;
	public static final int BROWSER_NETSCAPE 	= 2;
	public static final int BROWSER_OPERA		= 3;
	public static final int BROWSER_UP			= 4;
    public static final int BROWSER_POCKET_IE	= 5;
    public static final int BROWSER_FIREFOX		= 6;
    public static final int BROWSER_CHROME		= 7;
    public static final int BROWSER_SAFARI		= 8;
    public static final int BROWSER_EDGE		= 9;
    public static final int BROWSER_INDEXBOT		= 20;

	public abstract void cleanup();
	public abstract String getResourceRelative(String path);
	public abstract String getResourceRelative(String path, boolean includeBasePath);
	public abstract String getResource(String path);
	public abstract String GetNextPar();
	public abstract String GetPar(String parameter);
	public abstract String GetEventPar(String parameter);
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
	public abstract Cookie[] getCookies();
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
	public abstract HttpServletRequest getRequest();
	public abstract HttpServletResponse getResponse();
	public abstract void setRequest(HttpServletRequest request);
	public abstract Hashtable getPostData();
	public abstract WebSession getWebSession();
	public abstract void redirect(String url);
	public abstract void redirect(String url, boolean SkipPushUrl);
    public abstract void popup( String url);
    public abstract void popup( String url, Object[] returnParms);
    public abstract void newWindow( com.genexus.webpanels.GXWindow win);
	public abstract void setStream() ;
	public abstract void flushStream();

	public abstract void closeHtmlHeader();
	public abstract boolean getHtmlHeaderClosed();
	public abstract void ajax_rsp_command_close();
	public abstract void dispatchAjaxCommands();

	public abstract boolean isHttpContextNull();
	public abstract boolean isHttpContextWeb();

	public void AddDeferredFrags()
	{
		Enumeration<String> vEnum = deferredFragments.elements();
		while(vEnum.hasMoreElements())
		{
			writeTextNL(vEnum.nextElement());
		}
	}

	public void ClearJavascriptSources() {
		javascriptSources.clear();
	}
	
	public void AddJavascriptSource(String jsSrc, String urlBuildNumber, boolean userDefined, boolean isInlined)
	{
		if(!javascriptSources.contains(jsSrc))
		{
			urlBuildNumber = getURLBuildNumber(jsSrc, urlBuildNumber);
			javascriptSources.add(jsSrc);
			String queryString = urlBuildNumber;
			String attributes = "";
			if (userDefined)
			{
				queryString = "";
				attributes = "data-gx-external-script";
			}
			String fragment = "<script type=\"text/javascript\" src=\"" + oldConvertURL(jsSrc) + queryString + "\" " + attributes + "></script>" ;
            if (isAjaxRequest() || isInlined || jsSrc == "jquery.js" || jsSrc == "gxcore.js")
			{
	            writeTextNL(fragment);
            }
            else
			{
				deferredFragments.add(fragment);
            }
			// After including jQuery, include all the deferred Javascript files
			if (jsSrc.equals("jquery.js"))
			{
				for (String deferredJsSrc : deferredJavascriptSources)
				{
					AddJavascriptSource(deferredJsSrc, "", false, true);
				}
			}
			// After including gxgral, set the Service Worker Url if one is defined
			if (jsSrc.equals("gxgral.js") && isServiceWorkerDefined())
			{
				writeTextNL("<script>gx.serviceWorkerUrl = \"" + oldConvertURL(serviceWorkerFileName) + "\";</script>");
			}
		}
	}

	public void addWebAppManifest()
	{
		if (isWebAppManifestDefined())
		{
			writeTextNL("<link rel=\"manifest\" href=\"" + oldConvertURL(webAppManifestFileName) + "\">");
		}
	}

	private boolean checkFileExists(String fileName)
	{
		boolean fileExists = false;
		try
		{
			File file = new File(getDefaultPath() + staticContentBase + fileName);
			fileExists =  file.exists() && file.isFile();
			com.genexus.diagnostics.Log.info("Searching if file exists (" + fileName + "). Found: " + String.valueOf(fileExists));
		}
		catch (Exception e)
		{
			fileExists = false;
			com.genexus.diagnostics.Log.info("Failed searching for a file (" + fileName + ")");
		}
		return fileExists;
	}


	public void AddDeferredJavascriptSource(String jsSrc, String urlBuildNumber)
	{
		deferredJavascriptSources.add(oldConvertURL(jsSrc) + urlBuildNumber);
	}


	private String FetchCustomCSS()
	{
		String cssContent;
		cssContent = ApplicationContext.getcustomCSSContent().get(getRequest().getServletPath());
		if (cssContent == null)
		{
			String path = getRequest().getServletPath().replaceAll(".*/", "") + ".css";
			try 
			{
				InputStream istream = context.packageClass.getResourceAsStream(path);
				if (istream == null)
				{
					cssContent = "";
				}
				else
				{
					BOMInputStream bomInputStream = new BOMInputStream(istream);
					cssContent = IOUtils.toString(bomInputStream, "UTF-8");
				}
			}
			catch ( Exception e)
			{
				cssContent = "";
			}
			ApplicationContext.getcustomCSSContent().put(getRequest().getServletPath(), cssContent);
		}
		return cssContent;
	}

	public void AddThemeStyleSheetFile(String kbPrefix, String styleSheet, String urlBuildNumber)
	{
		String cssContent = FetchCustomCSS();
		boolean bHasCustomContent = ! cssContent.isEmpty();
		if (bHasCustomContent && !styleSheets.contains(getRequest().getServletPath()))
		{
			writeTextNL("<style id=\"gx-inline-css\">" + cssContent + "</style>");
			styleSheets.add(getRequest().getServletPath());
		}
		String[] referencedFiles = ThemeHelper.getThemeCssReferencedFiles(PrivateUtilities.removeExtension(styleSheet));
		for (int i=0; i<referencedFiles.length; i++)
		{
			String file = referencedFiles[i];
			String extension = PrivateUtilities.getExtension(file);
			if (extension != null)
			{
				if (extension.equals("css"))
					AddStyleSheetFile(file, urlBuildNumber, false, bHasCustomContent);
				else if (extension.equals("js"))
					AddDeferredJavascriptSource(file, urlBuildNumber);
			}
		}
		AddStyleSheetFile(kbPrefix + "Resources/" + getLanguage() + "/" + styleSheet, urlBuildNumber, true, bHasCustomContent);
	}
	
	public void AddThemeStyleSheetFile(String kbPrefix, String styleSheet)
	{
		AddThemeStyleSheetFile(kbPrefix, styleSheet, "");
	}

	public void AddStyleSheetFile(String styleSheet)
	{
		AddStyleSheetFile(styleSheet, "");
	}
	public void AddStyleSheetFile(String styleSheet, String urlBuildNumber)
	{
		urlBuildNumber = getURLBuildNumber(styleSheet, urlBuildNumber);
		AddStyleSheetFile(styleSheet, urlBuildNumber, false);
	}

	private String getURLBuildNumber(String styleSheet, String urlBuildNumber)
	{
		if(urlBuildNumber.isEmpty() && !GXutil.isAbsoluteURL(styleSheet))
		{
			return "?" + getCacheInvalidationToken();
		}
		else
		{
			return urlBuildNumber;
		}
	}

	private void AddStyleSheetFile(String styleSheet, String urlBuildNumber, boolean isGxThemeHidden)
	{
		AddStyleSheetFile( styleSheet, urlBuildNumber, isGxThemeHidden, false);
	}

	private void AddStyleSheetFile(String styleSheet, String urlBuildNumber, boolean isGxThemeHidden, boolean isDeferred)
	{
		if (!styleSheets.contains(styleSheet))
		{
			styleSheets.add(styleSheet);
			if (!this.getHtmlHeaderClosed() && this.isEnabled)
			{
				String sRelAtt = (isDeferred ? "rel=\"preload\" as=\"style\" " : "rel=\"stylesheet\"");
				if (isGxThemeHidden)
					writeTextNL("<link id=\"gxtheme_css_reference\" " + sRelAtt + " type=\"text/css\" href=\"" + oldConvertURL(styleSheet) + urlBuildNumber + "\" " + htmlEndTag(HTMLElement.LINK));
				else
					writeTextNL("<link " + sRelAtt + " type=\"text/css\" href=\"" + oldConvertURL(styleSheet) + urlBuildNumber + "\" " + htmlEndTag(HTMLElement.LINK));						
			}
			else
			{
				if (!isGxThemeHidden) this.StylesheetsToLoad.put(styleSheet);
			}
		}
	}
	
	public String getCacheInvalidationToken()
	{
		if (CACHE_INVALIDATION_TOKEN == null || CACHE_INVALIDATION_TOKEN.trim().length() == 0)
		{
			String token = ((Preferences)this.context.getPreferences()).getProperty("CACHE_INVALIDATION_TOKEN", "");
			if (token == null || token.trim().length() == 0)
			{
				CACHE_INVALIDATION_TOKEN = new java.text.DecimalFormat("#").format(CommonUtil.random() * 1000000);
			}
			else
			{
				CACHE_INVALIDATION_TOKEN = token;
			}
		}
		return CACHE_INVALIDATION_TOKEN;
	}

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
	public boolean drawGridsAtServer()
	{
		if (this.drawGridsAtServer == -1)
		{
			this.drawGridsAtServer = 0;
			if( ((Preferences)this.getContext().getPreferences()).propertyExists("DrawGridsAtServer"))
			{
				String prop = ((Preferences)this.getContext().getPreferences()).getProperty("DrawGridsAtServer", "no");
				if (prop.equalsIgnoreCase("always"))
				{
					this.drawGridsAtServer = 1;
				}
				else if (prop.equalsIgnoreCase("ie6"))
				{
					if (getBrowserType() == BROWSER_IE)
					{
						if (getBrowserVersion().startsWith("6"))
						{
							this.drawGridsAtServer = 1;
						}
					}
				}
			}
		}
		return (this.drawGridsAtServer == 1);
	}
			
			public int getButtonType()
			{
				if (this.drawGridsAtServer())
				{
					return TYPE_SUBMIT;
				}
				return TYPE_BUTTON;
			}
			
	public String getCssProperty(String propName, String propValue)
	{
			int browserType = getBrowserType();
			if (propName.equalsIgnoreCase("align"))
			{
					if (browserType == BROWSER_FIREFOX)
					{
						return "-moz-" + propValue;
					}
					else if (browserType == BROWSER_CHROME || browserType == BROWSER_SAFARI)
					{
						return "-khtml-" + propValue;
					}
			}
			return propValue;
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
		GXNavigationHelper helper = (GXNavigationHelper)getSessionValue(GX_NAV_HELPER);
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

	public void pushReferer()
	{
	}
	
	public void pushReferer(String url)
	{
		getNavigationHelper().pushUrl(url);
	}

	public void popReferer()
	{
		getNavigationHelper().popUrl(getRequestNavUrl());
	}
	
	public boolean isPopUpObject()
	{
		if (wrapped)
			return false;
		return !getNavigationHelper().getUrlPopupLevel(getRequestNavUrl()).equals("-1");
	}

	public void windowClosed()
	{
	String popupLevel = getNavigationHelper().getUrlPopupLevel(getRequestNavUrl());
		if (popupLevel.equals("-1"))
			popReferer();
		else
			deleteReferer(popupLevel);
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

	public void doAfterInit()
	{
	}
	
	public void printReportAtClient(String reportFile)
	{
		printReportAtClient(reportFile, "");
	}
	
	public void printReportAtClient(String reportFile, String printerRule)
	{
		addPrintReportCommand(getResource(reportFile), printerRule);
	}

	public boolean isGxAjaxRequest()
	{
		if (this.isMultipartContent())
		{
			return true;
		}
		String gxHeader = getRequest().getHeader(GX_AJAX_REQUEST_HEADER);
		if (gxHeader != null && gxHeader.trim().length() > 0)
		{
			return true;
		}
		return false;
	}

	private String getAjaxEncryptionKey()
	{
		if(getSessionValue(Encryption.AJAX_ENCRYPTION_KEY) == null)
		{
			if (!recoverEncryptionKey())
			{
				webPutSessionValue(Encryption.AJAX_ENCRYPTION_KEY, Encryption.getRijndaelKey());
			}
		}
		return (String)getSessionValue(Encryption.AJAX_ENCRYPTION_KEY);
	}
	
	private boolean recoverEncryptionKey()
	{
		if (getSessionValue(Encryption.AJAX_ENCRYPTION_KEY) == null)
		{
			String clientKey = getRequest().getHeader(Encryption.AJAX_SECURITY_TOKEN);
			if (clientKey != null && clientKey.trim().length() > 0)
			{
				boolean candecrypt[]=new boolean[1];
				clientKey = Encryption.decryptRijndael(Encryption.GX_AJAX_PRIVATE_IV + clientKey, Encryption.GX_AJAX_PRIVATE_KEY, candecrypt);
				if (candecrypt[0])
				{
					webPutSessionValue(Encryption.AJAX_ENCRYPTION_KEY, clientKey);
					return true;
				}else
				{
					return false;
				}
			}
		}
		return false;
	}

	public String DecryptAjaxCall(String encrypted)
	{            
		validEncryptedParm = false;
		if (isGxAjaxRequest())
		{
			String key = getAjaxEncryptionKey();
			boolean candecrypt[] = new boolean[1];
			String decrypted = Encryption.decryptRijndael(encrypted, key, candecrypt);
			validEncryptedParm = candecrypt[0];
			if (!validEncryptedParm)
			{
				CommonUtil.writeLogln( String.format("403 Forbidden error. Could not decrypt Ajax parameter: '%s' with key: '%s'", encrypted, key));
				sendResponseStatus(403, "Forbidden action");
				return "";
			}
			if (validEncryptedParm && !getRequestMethod().equalsIgnoreCase("post"))
			{
				setQueryString(decrypted);
				decrypted = GetNextPar();
			}
			return decrypted;
		}            
		return encrypted;
	}

	public boolean IsValidAjaxCall()
	{
		return IsValidAjaxCall(true);
	}

	public boolean IsValidAjaxCall(boolean insideAjaxCall)
	{         
		if (insideAjaxCall && !validEncryptedParm)
		{
			CommonUtil.writeLogln( "Failed IsValidAjaxCall 403 Forbidden action");
			sendResponseStatus(403, "Forbidden action");
			return false;
		}
		else if (!insideAjaxCall && isGxAjaxRequest() && !isFullAjaxMode() && ajaxOnSessionTimeout().equalsIgnoreCase("Warn"))
		{
			sendResponseStatus(440, "Session timeout");
			return false;
		}            
		return true;
	}
	
	public void sendResponseStatus(int statusCode, String statusDescription)
	{
		if (statusCode < 200 || statusCode >= 300)
		{
			getResponse().setHeader("Content-Encoding", "");
			if (statusCode != GXWebObjectBase.SPA_NOT_SUPPORTED_STATUS_CODE)
			{
				CommonUtil.writeLog("sendResponseStatus " + Integer.toString(statusCode) + " " + statusDescription);
			}
		}        	
		getResponse().setStatus(statusCode);
		try { getResponse().sendError(statusCode, statusDescription); }
		catch(Exception e) {
					System.err.println("E " + e);
					e.printStackTrace();
		}
		setAjaxCallMode();
		disableOutput();
	}
	
	private void sendReferer()
	{
		ajax_rsp_assign_hidden("sCallerURL", org.owasp.encoder.Encode.forUri(getReferer()));
	}
	
	private static String CLIENT_ID_HEADER = "GX_CLIENT_ID";
	
	public void initClientId()
	{			
		if (getWebSession() != null && this.getClientId().equals(""))
		{                    
			String _clientId = this.getCookie(CLIENT_ID_HEADER);
			if (_clientId == null || _clientId.equals(""))
			{
				_clientId = java.util.UUID.randomUUID().toString();
				this.setCookie(CLIENT_ID_HEADER, _clientId, "", new Date(Long.MAX_VALUE), "", 0);
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
	
	protected void addNavigationHidden()
	{    			
		if (this.isLocalStorageSupported())
		{			
			try {
				HiddenValues.put("GX_CLI_NAV", "true");		
			} catch (JSONException e) {						
			}
			GXNavigationHelper nav = this.getNavigationHelper();
			if (nav.count() > 0)
			{					
				String sUrl = this.getRequestNavUrl().trim();
				String popupLevel = nav.getUrlPopupLevel(sUrl);					
				try {
					HiddenValues.put("GX_NAV", nav.toJSonString(popupLevel));
				} catch (JSONException e) {						
				}
				nav.deleteStack(popupLevel);
			}
		}
	}

	public void SendWebComponentState()
	{
		AddStylesheetsToLoad();
	}

	public void SendState()
	{
		sendReferer();
		sendWebSocketParms();
		addNavigationHidden();    
		AddThemeHidden(this.getTheme());
		AddStylesheetsToLoad();
		AddResourceProvider(GXResourceProvider.PROVIDER_NAME);
		if (isSpaRequest())
		{
			writeTextNL("<script>gx.ajax.saveJsonResponse(" + getJSONResponse() + ");</script>");
		}
		else
		{				
			if (this.drawGridsAtServer())
			{
				writeTextNL("<script type=\"text/javascript\">gx.grid.drawAtServer=true;</script>");
			}
			skipLines(1);
			String value = HiddenValues.toString();
			if (useBase64ViewState())
			{
				try{
					value = Codecs.base64Encode(value, "UTF8");
				}catch(Exception ex){}
				writeTextNL("<script type=\"text/javascript\">gx.http.useBase64State=true;</script>");
			}
			writeText("<div><input type=\"hidden\" name=\"GXState\" value='");
			writeText(com.genexus.webpanels.WebUtils.htmlEncode(value, true));
			writeTextNL("'" + htmlEndTag(HTMLElement.INPUT) + "</div>");
		}
	}

	private void sendWebSocketParms()
	{
		if (!this.isAjaxRequest() || isSpaRequest())
		{
			ajax_rsp_assign_hidden("GX_WEBSOCKET_ID", clientId);
		}
	}
	

	public String getEncryptedSignature( String value, String key)
	{
		return encrypt64(CommonUtil.getHash( com.genexus.security.web.WebSecurityHelper.StripInvalidChars(value), com.genexus.cryptography.Constants.SECURITY_HASH_ALGORITHM), key);
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
            setCookie("GX_SESSION_ID", "", "", new Date(Long.MIN_VALUE), "", 0);
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
            setCookie("GX_SESSION_ID", "", "", new Date(Long.MIN_VALUE), "", 0);
            com.genexus.diagnostics.Log.debug( "440 Invalid encryption key");
            sendResponseStatus(440, "Session timeout");
        }
        return sRet;
    }

	public void SendAjaxEncryptionKey()
	{
		 if(!encryptionKeySended)
		 {
			 String key = getAjaxEncryptionKey();
			 ajax_rsp_assign_hidden(Encryption.AJAX_ENCRYPTION_KEY, key);
			 ajax_rsp_assign_hidden(Encryption.AJAX_ENCRYPTION_IV, Encryption.GX_AJAX_PRIVATE_IV);
			 try
			 {
				ajax_rsp_assign_hidden(Encryption.AJAX_SECURITY_TOKEN, Encryption.encryptRijndael(key, Encryption.GX_AJAX_PRIVATE_KEY));
			 }
			 catch(Exception exc) {}
			 encryptionKeySended = true;
		 }
	}

	public void SendServerCommands()
	{
		try {
			if (!isAjaxRequest() && commands.getCount() > 0)
			{
				HiddenValues.put("GX_SRV_COMMANDS", commands.getJSONArray());
			}
	  }
	  catch (JSONException e) {
	  }
	}

	public void ajax_req_read_hidden_sdt(String jsonStr, Object SdtObj)
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

	public void ajax_rsp_assign_prop_as_hidden(String Control, String Property, String Value)
	{
		if (!this.isAjaxRequest())
		{
			ajax_rsp_assign_hidden(Control + "_" + Property.substring(0, 1) + Property.substring(1).toLowerCase(), Value);
		}
	}

	public boolean IsSameComponent(String oldName, String newName)
	{
		if(oldName.trim().equalsIgnoreCase(newName.trim()))
		{
			return true;
		}
		else 
		{
			if(newName.trim().toLowerCase().startsWith(oldName.trim().toLowerCase() + "?"))
			{
				return true;
			}
			else
			{
				String packageName = context.getPackageName();
				String qoldName;
				String qnewName;

				if ( (oldName.indexOf(packageName) != 0) || (newName.indexOf(packageName) != 0)) 
				{
					qoldName = (oldName.indexOf(packageName) != 0) ? packageName + "." + oldName : oldName;
					qnewName = (newName.indexOf(packageName) != 0) ? packageName + "." + newName : newName;
					return IsSameComponent(qoldName, qnewName);
				}
			}
		}
		return false;
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
/*
	public void webGetSessionValue(String name, java.util.Date[] value)
	{
		Object o = getSessionValue(name);
		if	(o != null)
			System.arraycopy(o, 0, value, 0, value.length);
	}

	public void webGetSessionValue(String name, byte[][] value)
	{
		Object o = getSessionValue(name);
		if	(o != null)
			System.arraycopy(o, 0, value, 0, value.length);
	}

	public void webGetSessionValue(String name, short[][] value)
	{
		Object o = getSessionValue(name);
		if	(o != null)
			System.arraycopy(o, 0, value, 0, value.length);
	}

	public void webGetSessionValue(String name, int[][] value)
	{
		Object o = getSessionValue(name);
		if	(o != null)
			System.arraycopy(o, 0, value, 0, value.length);
	}

	public void webGetSessionValue(String name, long[][] value)
	{
		Object o = getSessionValue(name);
		if	(o != null)
			System.arraycopy(o, 0, value, 0, value.length);
	}

	public void webGetSessionValue(String name, float[][] value)
	{
		Object o = getSessionValue(name);
		if	(o != null)
			System.arraycopy(o, 0, value, 0, value.length);
	}

	public void webGetSessionValue(String name, double[][] value)
	{
		Object o = getSessionValue(name);
		if	(o != null)
			System.arraycopy(o, 0, value, 0, value.length);
	}

	public void webGetSessionValue(String name, String[][] value)
	{
		Object o = getSessionValue(name);
		if	(o != null)
			System.arraycopy(o, 0, value, 0, value.length);
	}

	public void webGetSessionValue(String name, java.util.Date[][] value)
	{
		Object o = getSessionValue(name);
		if	(o != null)
			System.arraycopy(o, 0, value, 0, value.length);
	}
*/

	public String getSubscriberId()
	{
		return getHeader("HTTP_X_UP_SUBNO");
	}

	public byte isMobileBrowser()
	{
		String accept = getHeader("HTTP_ACCEPT");

		return (accept.indexOf("wap") >= 0 || accept.indexOf("hdml") >= 0)?0: (byte) 1;
	}

	public void writeValueComplete(String text)
	{
		writeValueComplete(text, true, true, true);
	}

	public void writeValueSpace(String text)
	{
		writeValueComplete(text, false, false, true);
	}

	public void writeValueEnter(String text)
	{
		writeValueComplete(text, false, true, false);
	}

	/** Este writeValue tiene en consideracion los espacios consecutivos,
	 * los enter y los tabs
	 */
	public void writeValueComplete(String text, boolean cvtTabs, boolean cvtEnters, boolean cvtSpaces)
	{
		StringBuilder sb = new StringBuilder();
		boolean lastCharIsSpace = true; // Asumo que al comienzo el lastChar era space
		for (int i = 0; i < text.length(); i++)
		{
			char currentChar = text.charAt(i);
			switch (currentChar)
			{
				case (char) 34:
					sb.append("&quot;");
					break;
				case (char) 38:
					sb.append("&amp;");
					break;
				case (char) 60:
					sb.append("&lt;");
					break;
				case (char) 62:
					sb.append("&gt;");
					break;
				case '\t':
					sb.append(cvtTabs ? "&nbsp;&nbsp;&nbsp;&nbsp;" : ("" + currentChar));
					break;
				case '\r':
					if (cvtEnters && text.length() > i + 1 && text.charAt(i+1) == '\n'){
						sb.append(cvtEnters ? "<br>" : ("" + currentChar));
						i++;
					}					
					break;
				case '\n':
					sb.append(cvtEnters ? "<br>" : ("" + currentChar));
					break;
				case ' ':
					sb.append((lastCharIsSpace && cvtSpaces) ? "&nbsp;" : " ");
					break;
				default:
					sb.append("" + currentChar);
			}
			lastCharIsSpace = currentChar == ' ';
		}
		writeText(sb.toString());
	}

	public void writeValue(String text)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < text.length(); i++)
		{
			char currentChar = text.charAt(i);

			switch (currentChar)
			{
				case '"':
					sb.append("&quot;");
					break;
				case '&':
					sb.append("&amp;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
				default:
					sb.append(currentChar);
			}
		}
		writeText(sb.toString());
	}

	public void skipLines(long num)
	{
		for(; num > 0; num --)
		{
			 writeText("\n");
		}
	}

	public void setOutputStream(OutputStream out)
	{
		this.out = out;
	}

	public void writeTextNL(String text)
	{
		writeText(text + "\n");
	}

	public void renderUserControl(String controlType, String internalName, String htmlId, GXMap propbag)
	{
		propbag.put("ContainerName", htmlId);
		String ucServerContent = UserControlFactoryImpl.getInstance().renderControl(controlType, internalName, propbag);
		writeText( String.format("<div class=\"gx_usercontrol\" id=\"%s\">%s</div>", htmlId, ucServerContent));
	}

	public boolean useUtf8 = false;

	public void writeText(String text)
	{
            if (getResponseCommited())
                return;
		if (isEnabled == false)
                {
                    ajax_addCmpContent(text);
                    return;
                }
		_writeText(text);
	}

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
	}

	public OutputStream getOutputStream()
	{
		return out;
	}

	public void setWriter(PrintWriter writer)
	{
		this.writer = writer;
	}

/*	public PrintWriter getWriter()
	{
		return writer;
	}

*/
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
			System.err.println("E " + e);
			e.printStackTrace();
		}
	}

	public MsgList getMessageList()
	{
		return GX_msglist;
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

	private String staticContentBase = "";
	public void setStaticContentBase(String staticContentBase)
	{
		this.staticContentBase = staticContentBase;
	}
	public String getStaticContentBase()
	{
		return staticContentBase;
	}

	private String theme = "";
	public String getTheme()
	{
	    WebSession session = getWebSession();
	    if (session!=null){
			HashMap cThemeMap = (HashMap)session.getObjectAttribute("GXTheme");
			if (cThemeMap != null && cThemeMap.containsKey(theme))
				return (String)cThemeMap.get(theme);
		}
		
		if (theme.trim().length() == 0)
			theme = Preferences.getDefaultPreferences().getDefaultTheme();

		return theme;
	}
	public void setDefaultTheme(String t)
	{
		theme = t;
	}
	@SuppressWarnings("unchecked")
	public int setTheme(String t)
	{
	    WebSession session = getWebSession();
		if (session==null || t==null || t.equals(""))
			return 0;
		else
		{
			HashMap<String, String> cThemeMap = (HashMap<String, String>)session.getObjectAttribute("GXTheme");
			if (cThemeMap == null)
				cThemeMap = new HashMap<>();
			cThemeMap.put(theme, t);
			session.setObjectAttribute("GXTheme", cThemeMap);
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
				String resourceName = "messages." + languageCode + ".txt";
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
				messages = com.genexus.Messages.getMessages("messages." + languageCode + ".txt", Application.getClientLocalUtil().getLocale());
				addCachedLanguageMessage(language, messages);
			}
			return messages.getMessage(code);
		}

		private synchronized void addCachedLanguageMessage(String language, Messages msg) {
			cachedMessages.putIfAbsent(language, msg);
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
				String language = session != null ? session.getAttribute("GXLanguage") : null;
				if (language != null && !language.equals("")) {
					currentLanguage = language;
				} else {
					//Por ahora obtengo el del modelo, mas adelante puede haber uno por cada session
					currentLanguage = Application.getClientPreferences().getProperty("LANG_NAME", "English");
				}
			}
			return currentLanguage;
		}
		public String htmlEndTag(HTMLElement element)
		{
			HTMLDocType docType = context.getClientPreferences().getDOCTYPE();
			if ((docType == HTMLDocType.HTML4 || docType == HTMLDocType.NONE || docType == HTMLDocType.HTML4S) &&
				(element == HTMLElement.IMG ||
				element == HTMLElement.INPUT ||
				element == HTMLElement.META ||
				element == HTMLElement.LINK))
				return ">";
			else if (element == HTMLElement.OPTION)
				if (docType == HTMLDocType.XHTML1 || docType == HTMLDocType.HTML5)
					return "</option>";
				else
					return "";
			else
				return "/>";
		}
		public String htmlDocType()
		{
			HTMLDocType docType = context.getClientPreferences().getDOCTYPE();
			switch (docType)
			{
				case HTML4: 
					if (context.getClientPreferences().getDOCTYPE_DTD())
						return "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">";
					else 
						return "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">";
				case HTML4S: 
					if (context.getClientPreferences().getDOCTYPE_DTD())
						return "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">";
					else 
						return "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">";
				case XHTML1: 
					if (context.getClientPreferences().getDOCTYPE_DTD())
						return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";
					else 
						return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\">";
				case HTML5: return "<!DOCTYPE html>";
				default: return "";
			}
		}

		public int setLanguage(String language)
		{
			if (!language.isEmpty() && Application.getClientPreferences().getProperty("language|"+ language, "code", null)!=null)
			{
				this.currentLanguage = language;
				getWebSession().setAttribute("GXLanguage", language);
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
								{"tgz"	, "application/x-compressed"},
								{"zip"	, "application/x-zip-compressed"},
								{"gz"	, "application/x-gzip"}
								};

		public boolean willRedirect()
		{
			return wjLoc != null && wjLoc.trim().length() != 0;
		}
}
