package com.genexus.webpanels;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import com.genexus.fileupload.servlet.IServletFileUpload;
import com.genexus.servlet.*;
import com.genexus.servlet.http.Cookie;
import com.genexus.servlet.http.ICookie;
import com.genexus.servlet.http.IHttpServletRequest;
import com.genexus.servlet.http.IHttpServletResponse;
import com.genexus.servlet.http.HttpServletResponse;
import com.genexus.servlet.http.IHttpSession;

import com.genexus.*;
import com.genexus.fileupload.IFileItemIterator;
import com.genexus.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import com.genexus.internet.GXNavigationHelper;
import com.genexus.internet.HttpContext;
import com.genexus.internet.HttpRequest;
import com.genexus.internet.HttpRequestWeb;
import com.genexus.internet.HttpResponse;
import com.genexus.internet.MsgList;
import com.genexus.util.Base64;
import com.genexus.util.GXFile;
import com.genexus.util.GXServices;

import json.org.json.JSONException;
import json.org.json.JSONObject;

public class HttpContextWeb extends HttpContext {
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(HttpContextWeb.class);

	HttpResponse httpRes;
	HttpRequest httpReq;
	IServletContext servletContext;

	boolean useOldQueryStringFormat;
	protected Vector<String> parms;
	private Hashtable<String, String> namedParms;
	private Hashtable<String, String[]> postData;
	private boolean useNamedParameters;
	private int currParameter;

	private IHttpServletRequest request;
	private IHttpServletResponse response;
	private String requestMethod;
	protected String contentType = "";
	private boolean SkipPushUrl = false;
	private Hashtable<String, String> cookies;
	private boolean streamSet = false;
	private WebSession webSession;
	private FileItemCollection fileItemCollection;
	private IFileItemIterator lstParts;
	private boolean ajaxCallAsPOST = false;
	private boolean htmlHeaderClosed = false;
	private String sTmpDir;
	private boolean firstParConsumed = false;

	private static final Pattern USERAGENT_SEARCH_BOT = Pattern.compile("Googlebot|AhrefsBot|bingbot|MJ12bot",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern MULTIMEDIA_GXI_GRID_PATTERN = Pattern.compile("(\\w+)(_\\d{4})$");

	private static final Pattern EDGE_BROWSER_VERSION_REGEX = Pattern.compile(" Edge\\/([0-9]+)\\.",
			Pattern.CASE_INSENSITIVE);
	private static final String GXEVENT_PARM = "gxevent";

	private static final String SAME_SITE_NONE = "None";
	private static final String SAME_SITE_LAX = "Lax";
	private static final String SAME_SITE_STRICT = "Strict";
	private static final String SET_COOKIE = "Set-Cookie";

	public boolean isMultipartContent() {
		return ServletFileUpload.isMultipartContent(request);
	}

	public String getResource(String path) {
		if (path.length() == 0)
			return "";
		String protocol = "http";
		if (getHttpSecure() == 1) {
			protocol = "https";
		}

		int port = httpReq.getServerPort();
		String portS = (port == 80 || port == 443) ? "" : ":" + port;
		return protocol + "://" + httpReq.getServerHost() + portS + getResourceRelative(path);
	}

	public String getResourceRelative(String path) {
		return getResourceRelative(path, true);
	}

	public String getResourceRelative(String path, boolean includeBasePath) {
		if (Application.getGXServices().get(GXServices.STORAGE_SERVICE) != null && !path.isEmpty()) {
			GXFile gxFile = new GXFile(path);
			String pathURL = gxFile.getAbsolutePath();
			if (pathURL.toLowerCase().startsWith("http")) {
				return gxFile.getAbsolutePath();
			}
		}

		try {
			java.io.File file = new java.io.File(path);
			if (file.getPath().compareTo(file.getAbsolutePath()) != 0)
				return path;
		} catch (Exception e) {
			return path;
		}

		if (path.length() == 0)
			return "";
		String ContextPath = null;
		try {
			ContextPath = request.getContextPath();
		} catch (Exception e) {
			// on submit (there is no http context) request.getContextPath throws a
			// java.lang.NullPointerException
			System.err.println(e.toString());
		}
		;
		String Resource = path;
		String basePath = getDefaultPath();
		if (Resource.startsWith(basePath) && Resource.length() >= basePath.length())
			Resource = Resource.substring(basePath.length());
		if (ContextPath != null && !ContextPath.equals("") && Resource.startsWith(ContextPath))
			return Resource.replace('\\', '/');

		Resource = Resource.replace('\\', '/');

		if (includeBasePath)
		{
			if (Resource.startsWith("/"))
				Resource = ContextPath + Resource;
			else
				Resource = ContextPath + "/" + Resource;
		}
		else
		{
			if (Resource.startsWith("/"))
				Resource = Resource.substring(1);
		}
		
		String baseName = FilenameUtils.getBaseName(Resource);
		Resource = CommonUtil.replaceLast(Resource, baseName, PrivateUtilities.encodeFileName(baseName));
		return Resource;
	}

	private IFileItemIterator parseMultiParts() {
		if (lstParts == null) {
			try {
				sTmpDir = Preferences.getDefaultPreferences().getPRIVATE_PATH();
				if (request != null && ServletFileUpload.isMultipartContent(request)) {
					IServletFileUpload upload = new ServletFileUpload();
					upload.setUploadHeaderEncoding("UTF-8");
					lstParts = upload.getItemIterator(request);
				}
			} catch (Exception e) {
				log.error("parseMultiParts", e);
			}
		}
		return lstParts;
	}

	public FileItemCollection getPostedparts() {
		if (fileItemCollection == null) {
			fileItemCollection = new FileItemCollection(parseMultiParts(), sTmpDir);
		}
		return fileItemCollection;
	}

	public HttpContext copy() {
		try {
			HttpContextWeb o = new HttpContextWeb(requestMethod, request, response, servletContext);
			o.cookies = cookies;
			o.webSession = webSession;
			o.httpRes = httpRes;
			o.httpReq = httpReq;
			o.postData = postData;
			o.parms = parms;
			o.namedParms = namedParms;
			o.streamSet = streamSet;
			o.isCrawlerRequest = o.isCrawlerRequest();
			copyCommon(o);

			return o;
		} catch (java.io.IOException e) {
			return null;
		}
	}

	public HttpContextWeb(String requestMethod, IHttpServletRequest req, IHttpServletResponse res,
						  IServletContext servletContext) throws IOException {
		this(ClientContext.getModelContext().getClientPreferences().getProperty("UseNamedParameters", "1").equals("1"), requestMethod, req, res, servletContext);
	}

	public HttpContextWeb(boolean useNamedParameters, String requestMethod, IHttpServletRequest req, IHttpServletResponse res,
			IServletContext servletContext) throws IOException {
		this.request = req;
		this.response = res;

		if (isForward()) {
			requestMethod = "GET";
		}

		this.requestMethod = requestMethod;
		this.servletContext = servletContext;

		GX_msglist = new MsgList();
		postData = null;
		cookies = new Hashtable<String, String>();

		httpRes = new HttpResponse(this);
		httpReq = new HttpRequestWeb(this);
		webSession = new WebSession(req);
		this.GX_webresponse = httpRes;

		super.useUtf8 = true;
		parms = new Vector<>();
		namedParms = new Hashtable<>();
		this.useNamedParameters = useNamedParameters;
		loadParameters(req.getQueryString());
		isCrawlerRequest = isCrawlerRequest();

	}

	private void loadParameters(String value) {
		String value1;
		initpars();
		parms.clear();
		namedParms.clear();
		boolean oneParm = false;
		if (value != null && value.length() > 0) {
			useOldQueryStringFormat = !(useNamedParameters && removeInternalParms(value).contains("="));
			String[] elements;
			if (value.charAt(0) == '?')
				value1 = value.substring(1);
			else
				value1 = value;

			if (useOldQueryStringFormat)
				elements = value1.split(",");
			else
				elements = value1.split("&");

			oneParm = (elements.length > 0);
			for (int i = 0; i < elements.length; i++) {
				String parm = elements[i];
				if (parm.indexOf("gx-no-cache=") != -1)
					break;
				if (useOldQueryStringFormat)
					parms.addElement(parm);
				else {
					String parameterValue = "";
					if (parm.split("=").length > 1)
						parameterValue = parm.split("=")[1];
					parms.addElement(parameterValue);
					namedParms.put(parm.split("=")[0].toLowerCase(), parameterValue);
				}
			}
		}
		if (requestMethod.equalsIgnoreCase("POST") && oneParm && parms.size() == 0) {
			// Si es un call ajax hecho mediante un POST
			// solo va a tener un parametro que es el que
			// se usa para evitar el cache y no se agregó
			// a la lista de parametros (por eso en la condicion esta oneParm)
			ajaxCallAsPOST = tryLoadAjaxCallParms();
		}
	}

	public void initpars() {
		currParameter = -1;
	}

	private boolean tryLoadAjaxCallParms() {
		if (!isMultipartContent()) {
			Hashtable parsePostData = getPostData();
			if (parsePostData != null) {
				Object postParm = null;
				String gxEvent = "";
				postParm = parsePostData.get("GXEvent");
				if (postParm != null) {
					gxEvent = ((String[]) postParm)[0];
				}
				String gxAction = "";
				postParm = parsePostData.get("GXAction");
				if (postParm != null) {
					gxAction = ((String[]) postParm)[0];
				}
				if (gxEvent != null && !gxEvent.trim().equals("")) {
					try {
						Pattern pattern = Pattern.compile("GXParm([0-9]+)");
						Vector<String> indexedParms = new Vector<>();
						Enumeration postParms = parsePostData.keys();
						while (postParms.hasMoreElements()) {
							String name = (String) postParms.nextElement();
							Matcher matcher = pattern.matcher(name);
							if (matcher.matches()) {
								int parmIdx = Integer.parseInt(matcher.group(1));
								int sizeDiff = parmIdx - indexedParms.size();
								for (int i = 0; i <= sizeDiff; i++) {
									indexedParms.add("");
								}
								String parm = "";
								postParm = parsePostData.get(name);
								if (postParm != null) {
									parm = ((String[]) postParm)[0];
									indexedParms.setElementAt(parm, parmIdx);
								}
							}
						}
						parms.clear();
						parms.add(gxEvent);
						if (gxAction != null && !gxAction.trim().equals(""))
							parms.add(gxAction);
						for (int i = 0; i < indexedParms.size(); i++) {
							parms.add(indexedParms.elementAt(i));
						}
						return true;
					} catch (Throwable ex) {
						log.error("Reading Ajax as Post Data", ex);
					}
				}
			}
		}
		return false;
	}

	public void parseGXState(JSONObject tokenValues) {
		try {
			Iterator it = tokenValues.keys();
			while (it.hasNext()) {
				String key = (String) it.next();
				if (!isFileParm(key)) {
					HttpUtils.pushValue(getPostData(), key, tokenValues.get(key).toString());
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Hashtable<String, String[]> getPostData() {
		if (postData == null) {
			String contentType = request.getContentType();
			if (ajaxCallAsPOST || isForward()) {
				return new Hashtable<>();
			}

			try {
				if (contentType != null
						&& (contentType.contains("application/json") || contentType.contains("text/xml"))) {
					postData = new Hashtable<>();
				} else {
					if (ServletFileUpload.isMultipartContent(request))
						postData = parseMultipartPostData(getPostedparts());
					else
						postData = parsePostData(request, request.getInputStream());
				}
				Object value = postData.get("GXState");
				if (value != null) {
					try {
						String valuestr[] = (String[]) value;
						String decoded = valuestr[0];
						if (useBase64ViewState()) {
							decoded = new String(Base64.decode(decoded), "UTF8");
						}
						JSONObject tokenValues = null;
						try {
							tokenValues = new JSONObject(decoded);
						} catch (JSONException jex) {
							log.debug("GXState JSONObject error (1)", jex);
							char c = 0;
							decoded = decoded.replace(Character.toString(c), "");
							tokenValues = new JSONObject(decoded);
						}
						parseGXState(tokenValues);
					} catch (Exception ex) {
						log.debug("GXState JSONObject error (2)", ex);
					}
				}
			} catch (IOException e) {
				postData = new Hashtable<>();
				log.debug("GetPostData", e);
			}
		}

		return postData;
	}

	public String GetPar(String parameter) {
		if (useOldQueryStringFormat)
			return GetNextPar();
		else {
			String parm = namedParms.get(parameter.toLowerCase());
			if (!ajaxCallAsPOST && parm != null) {
				parm = GXutil.URLDecode(parm);
			}
			return parm == null? "" : parm;
		}
	}

	public String GetFirstPar(String parameter) {
		if (useOldQueryStringFormat)
			return GetNextPar();
		else {
			if (!firstParConsumed && namedParms.containsKey(GXEVENT_PARM)) {
				firstParConsumed = true;
				return GetPar(GXEVENT_PARM);
			}
			else
				return GetPar(parameter);
		}
	}

	public String GetNextPar() {
		currParameter++;
		String parm = "";
		if (currParameter < parms.size()) {
			parm = parms.elementAt(currParameter).toString();
			if (!ajaxCallAsPOST) {
				parm = GXutil.URLDecode(parm);
			}
		}
		return parm;
	}

	public byte setHeader(String header, String value) {
		response.setHeader(header, value.replace("\n", "%0a").replace("\r", "%0d"));
		return 0;
	}

	public void setDateHeader(String header, int value) {
		response.setDateHeader(header, value);
	}

	public void setRequestMethod(String method) {
		this.requestMethod = method;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public boolean isForward() {
		String callMethod = (String) getRequest().getAttribute("gx_webcall_method");
		if ((callMethod != null) && (callMethod.equalsIgnoreCase("forward"))) {
			return true;
		}
		return false;
	}

	public boolean forwardAsWebCallMethod() {
		if (context != null) {
			return ((Preferences)context.getPreferences()).getProperty("WEB_CALL_METHOD", "Redirect").equalsIgnoreCase("Forward");
		}
		return false;
	}

	private String contextPath;

	public String getContextPath() {
		if (contextPath == null) {
			if (servletContext.getServerInfo().startsWith("ApacheJServ")) {
				contextPath = "";
			} else {
				IContextPath path = ContextPath.getIContextPath(servletContext);
				try {
					contextPath = path == null ? "" : path.getContextPath(request);
				} catch (Exception e) {
					log.warn("Could not find getContextPath", e);
					// on submit (there is no http context) path.getContextPath throws a
					// java.lang.NullPointerException
				}
				;

				if (contextPath == null)
					contextPath = "";
			}
		}

		return contextPath;
	}

	public void setContextPath(String path) {}

	public String getRealPath(String path) {
		String realPath = path;

		File file = new File(path);
		if (!file.isAbsolute() || !file.exists()) {
			if (path.startsWith(getContextPath())) {
				path = path.substring(getContextPath().length());
			}
			realPath = servletContext.getRealPath(path);

			if (realPath == null) {
				realPath = getDefaultPath() + File.separator + path;
			}
		}

		return realPath;
	}

	public IHttpServletResponse getResponse() {
		return response;
	}

	public String getReferer() {
		if (!isLocalStorageSupported()) {
			String referer = getNavigationHelper(false).getRefererUrl(getRequestNavUrl());
			return referer == null ? "" : referer;
		} else {
			String temp = this.cgiGet("sCallerURL");
			String referer = (temp != null) ? temp : "";
			if (referer == "") {
				GXNavigationHelper nav = getNavigationHelper(false);
				String selfUrl = getRequestNavUrl();
				if (nav.count() > 0) {
					referer = nav.peekUrl(selfUrl);
				}
				if (referer == "" && request != null && request.getHeader("Referer") != null) {
					temp = request.getHeader("Referer");
					referer = (!selfUrl.equals(temp)) ? temp : referer;
				}
			}
			try {
				URL url = new URL(referer);
				String query = (StringUtils.isNotEmpty(url.getQuery())) ? "?" + url.getQuery() : "";
				referer = url.getPath() + query;
			} catch (Exception e) {

			}
			return referer;
		}
	}

	public short setWrkSt(int handle, String wrkst) {
		webPutSessionValue("GX_WRKST", wrkst.toUpperCase());
		return 1;
	}

	public String getApplicationId(int handle) {
		String appId = request.getHeader("GXApplicationIdentifier");
		if (appId != null)
			return appId;
		else
			return "";
	}

	public String getWorkstationId(int handle) {
		String wrkstId = (String) getSessionValue("GX_WRKST");

		if (wrkstId == null || wrkstId.equals("")) {
			wrkstId = request.getRemoteAddr();
		}

		return wrkstId;
	}

	public short setUserId(int handle, String user, String dataSource) {
		webPutSessionValue("GX_USERID", user.toUpperCase());
		return 1;
	}

	/**
	 * @deprecated use getUserId(String key, int handle,
	 *             com.genexus.db.IDataStoreProvider dataStore);
	 */
	public String getUserId(String key, ModelContext context, int handle, String dataSource) {

		if (key.equalsIgnoreCase("server") && !Application.getClientPreferences().getLOGIN_AS_USERID()) {
			return GXutil.userId(key, context, handle, dataSource);
		}

		String user = (String) getSessionValue("GX_USERID");

		if (user == null || user.length() == 0)
			return request.getRemoteUser() == null ? "" : request.getRemoteUser();

		return user;
	}

	public String getUserId(String key, ModelContext context, int handle, com.genexus.db.IDataStoreProvider dataStore) {

		if (key.equalsIgnoreCase("server") && !Application.getClientPreferences().getLOGIN_AS_USERID()) {
			return GXutil.userId(key, context, handle, dataStore);
		}

		String user = (String) getSessionValue("GX_USERID");

		if (user == null || user.length() == 0)
			return request.getRemoteUser() == null ? "" : request.getRemoteUser();

		return user;
	}

	public String getRemoteAddr() {
		String address = getHeader("X-Forwarded-For");
		if (address.length() > 0){
			return address;
		}
		address = request.getRemoteAddr();
		return address == null ? "" : address;
	}

	public boolean isSmartDevice() {
		String userAgent = request.getHeader("USER-AGENT");
		if (userAgent != null) {
			if ((userAgent.indexOf("Windows CE")) != -1)
				return true;
			else if ((userAgent.indexOf("iPhone")) != -1)
				return true;
			else if ((userAgent.indexOf("BlackBerry")) != -1)
				return true;
			else if ((userAgent.indexOf("Opera Mini")) != -1)
				return true;
		}
		return false;
	}

	public boolean isLocalStorageSupported() {
		boolean supported = true;
		try {
			String NotSupported = this.getCookie("GXLocalStorageSupport");
			supported = NotSupported == null || NotSupported.equals("");
			if (supported) {
				switch (getBrowserType()) {
				case BROWSER_IE:
					try {
						if (context != null && context.getClientPreferences() != null) {
							float ver = Float.parseFloat(getBrowserVersion());
							HTMLDocType docType = context.getClientPreferences().getDOCTYPE();
							boolean docTypeDefined = docType != HTMLDocType.NONE && docType != HTMLDocType.UNDEFINED;
							supported = ver >= 9 && docTypeDefined;
						}
					} catch (NumberFormatException e) {
					}
					break;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return supported;
	}

	public boolean exposeMetadata() {
		return context.getClientPreferences().getEXPOSE_METADATA();
	}
	public int getBrowserType() {
		String userAgent = request.getHeader("USER-AGENT");

		if (userAgent != null) {
			if (userAgent.toUpperCase().indexOf("CHROME") != -1) {
				return BROWSER_CHROME;
			} else if (userAgent.toUpperCase().indexOf("FIREFOX") != -1) {
				return BROWSER_FIREFOX;
			} else if ((userAgent.indexOf("Edge")) != -1) {
				return BROWSER_EDGE;
			} else if ((userAgent.indexOf("MSIE")) != -1) {
				if ((userAgent.indexOf("Windows CE")) != -1)
					return BROWSER_POCKET_IE;
				else
					return BROWSER_IE;
			} else if (userAgent.toUpperCase().indexOf("SAFARI") != -1) {
				return BROWSER_SAFARI;
			} else if ((userAgent.indexOf("Trident")) != -1) {
				return BROWSER_IE;
			} else if (userAgent.toUpperCase().indexOf("OPERA") != -1) {
				return BROWSER_OPERA;
			} else if (userAgent.toUpperCase().indexOf("MOZILLA/") != -1) {
				return BROWSER_NETSCAPE;
			} else if (userAgent.toUpperCase().indexOf("UP.Browser") != -1) {
				return BROWSER_UP;
			} else if (USERAGENT_SEARCH_BOT.matcher(userAgent).find()) {
				return BROWSER_INDEXBOT;
			}

		}

		return BROWSER_OTHER;
	}

	public boolean isIE55() {
		return (getBrowserType() == BROWSER_IE && getBrowserVersion().trim().startsWith("5"));
	}

	public String getBrowserVersion() {
		String userAgent = request.getHeader("USER-AGENT");

		int i, i2;

		switch (getBrowserType()) {
		case BROWSER_EDGE:
			Matcher matcher = EDGE_BROWSER_VERSION_REGEX.matcher(userAgent);
			if (matcher.find() && matcher.group(1) != null)
				return matcher.group(1);
			break;
		case BROWSER_POCKET_IE:
		case BROWSER_IE:
			i = userAgent.indexOf("MSIE");
			if (i >= 0) {
				i2 = userAgent.indexOf(";", i);
				if (i2 != -1) {
					String version = userAgent.substring(i + 4, i2).trim();
					return (version.startsWith("7") && userAgent.toLowerCase().indexOf("trident") >= 0) ? "8" : version;
				}
			} else {
				i = userAgent.indexOf("rv:");
				i2 = userAgent.indexOf(".", i);
				return userAgent.substring(i + 3, i2).trim();
			}
			break;

		case BROWSER_OPERA:
			// Mozilla/4.0 (Windows NT 4.0;US) Opera 3.60 [en]
			i = userAgent.indexOf("Opera") + 6;
			i2 = userAgent.indexOf(" ", i);
			if (i2 != -1)
				return userAgent.substring(i, i2).trim();

			break;
		case BROWSER_FIREFOX:
			i = userAgent.indexOf("Firefox/") + 8;
			i2 = userAgent.indexOf(" ", i);
			if (i2 != -1)
				return userAgent.substring(i, i2).trim();
			else
				return userAgent.substring(i).trim();
		case BROWSER_CHROME:
			i = userAgent.indexOf("Chrome/") + 7;
			i2 = userAgent.indexOf(" ", i);
			if (i2 != -1)
				return userAgent.substring(i, i2).trim();

			break;
		case BROWSER_SAFARI:
			i = userAgent.indexOf("Version/") + 8;
			i2 = userAgent.indexOf(" ", i);
			if (i2 != -1)
				return userAgent.substring(i, i2).trim();

			break;
		case BROWSER_NETSCAPE:
			i = userAgent.indexOf("Mozilla/") + 8;
			i2 = userAgent.indexOf(" ", i);
			if (i2 != -1)
				return userAgent.substring(i, i2).trim();

			break;
		case BROWSER_UP:
			i = userAgent.indexOf("UP.Browser/") + 8;
			i2 = userAgent.indexOf("-", i);
			if (i2 != -1)
				return userAgent.substring(i, i2).trim();

			break;
		}

		return "";
	}

	public Object getSessionValue(String name) {
		try {
			if (request != null) {
				Object obj = null;
				IHttpSession session = request.getSession(false);
				if (session != null) {
					try {
						obj = session.getAttribute(CommonUtil.upper(name));
					}
					catch (UnsupportedOperationException e)
					{
						//In some environments, websession is not supported
					}
				}
				return obj;
			}
		} catch (Exception e) {
			log.error(String.format("Failed getting sessionValue '%s'", name), e);
		}

		return "";
	}

	// ---- Set values
	public void webPutSessionValue(String name, Object value) {
		if (request != null) {
			try {
				request.getSession(true).setAttribute(CommonUtil.upper(name), value);
			}
			catch (UnsupportedOperationException e)
			{
				//In some environments, websession is not supported
			}
		}

	}

	public void webPutSessionValue(String name, long value) {
		if (request != null){
			try {
				request.getSession(true).setAttribute(CommonUtil.upper(name), new Long(value));
			}
			catch (UnsupportedOperationException e)
			{
				//In some environments, websession is not supported
			}
		}
	}

	public void webPutSessionValue(String name, double value) {
		if (request != null){
			try {
				request.getSession(true).setAttribute(CommonUtil.upper(name), new Double(value));
			}
			catch (UnsupportedOperationException e)
			{
				//In some environments, websession is not supported
			}
		}
	}

	public void webSessionId(String[] id) {
		id[0] = webSessionId();
	}

	public String webSessionId() {
		if (request != null)
			return request.getSession(true).getId();

		return "0";
	}

	public ICookie[] getCookies() {
		ICookie[] cookies = {};
		if (request != null) {
			try {
				cookies = (Cookie[])request.getCookies();
			} catch (Exception e) {
			}
		}
		return cookies;
	}

	public String getCookie(String name) {
		if (cookies.containsKey(name)) {
			return WebUtils.decodeCookie(cookies.get(name));
		}

		if (request != null) {
			try {
				ICookie[] cookies = request.getCookies();
				if (cookies != null) {
					for (int i = 0; i < cookies.length; i++) {
						ICookie cookie = cookies[i];
						if (cookie.getName().equalsIgnoreCase(name)) {
							return WebUtils.decodeCookie(cookie.getValue());
						}
					}
				}
			} catch (Exception e) {
				log.error("Failed getting cookie", e);
			}
		}

		return "";
	}

	public byte setCookieRaw(String name, String value, String path, java.util.Date expiry, String domain,
			double secure) {
		return setCookieRaw(name, value, path, expiry, domain, secure,
				com.genexus.Preferences.getDefaultPreferences().getcookie_httponly_default());
	}

	public byte setCookieRaw(String name, String value, String path, java.util.Date expiry, String domain,
			double secure, Boolean httpOnly) {
		if (response != null) {
			ICookie cookie = new Cookie(name, value);

			if (path.trim().length() > 0)
				cookie.setPath(path.trim());
			else
				cookie.setPath("/");

			if (!expiry.equals(CommonUtil.nullDate())) {
				long expiryTime = ((expiry.getTime() - new Date().getTime()) / 1000);
				if (expiryTime < 0) {
					expiryTime = 0;
				}
				cookie.setMaxAge((int) expiryTime);
			}

			if (domain.trim().length() > 0)
				cookie.setDomain(domain.trim());

			cookie.setSecure(secure != 0);
			if (servletContext.getMajorVersion() >= 3)
				cookie.setHttpOnly(httpOnly); // Requiere servlet version 3.0
			response.addCookie(cookie);

			cookies.put(name, value);
		}

		return 0;
	}

	public byte setCookie(String name, String value, String path, java.util.Date expiry, String domain, double secure,
			Boolean httpOnly) {
		return setCookieRaw(name, WebUtils.encodeCookie(value), path, expiry, domain, secure, httpOnly);
	}

	public byte setCookie(String name, String value, String path, java.util.Date expiry, String domain, double secure) {
		return setCookieRaw(name, WebUtils.encodeCookie(value), path, expiry, domain, secure);
	}

	public String getServerName() {
		String host = getHeader("X-Forwarded-Host");
		if (host.length() > 0){
			return host;
		}
		String serverNameProperty = ModelContext.getModelContext().getPreferences().getProperty("SERVER_NAME", "");
		if (!StringUtils.isBlank(serverNameProperty)) {
			return serverNameProperty;
		}
		if (request != null)
			return request.getServerName();

		return "";
	}

	public int getServerPort() {
		String port = getHeader("X-Forwarded-Port");
		if (port.length() > 0){
			return Integer.parseInt(port);
		}
		String serverPortProperty = ModelContext.getModelContext().getPreferences().getProperty("SERVER_PORT", "");
		if (!StringUtils.isBlank(serverPortProperty)) {
			return Integer.parseInt(serverPortProperty);
		}
		String serverNameProperty = ModelContext.getModelContext().getPreferences().getProperty("SERVER_NAME", "");
		if (serverNameProperty.indexOf(':') != -1) {
			return 80;
		}
		if (request != null) {
			return request.getServerPort();
		}
		return 80;
	}

	public String getScriptPath() {
		if (request != null) {
			String path = request.getRequestURI();
			if (path != null) {
				if (path.startsWith("http"))
					path = request.getServletPath();

				if (path != null) {

					int pos = path.lastIndexOf('/');

					if (pos >= 0)
						return path.substring(0, pos + 1);

					return path;
				}
			}
		}

		return "";
	}

	public int getHttpSecure() {
		String protocol = getHeader("X-Forwarded-Proto");
		if (protocol != null && !protocol.equals("")) {
			return protocol.equalsIgnoreCase("https") ? 1 : 0;
		}
		String serverProtocolProperty = ModelContext.getModelContext().getPreferences().getProperty("SERVER_PROTOCOL", "");
		if (!StringUtils.isBlank(serverProtocolProperty)) {
			return serverProtocolProperty.equalsIgnoreCase("https") ? 1: 0;
		}

		if (request != null && request.getScheme() != null)
			return request.getScheme().equalsIgnoreCase("http") ? 0 : 1;

		return 0;
	}

	public byte setContentType(String type) {
		contentType = type;
		if (type.equalsIgnoreCase("text/html") && useUtf8) {
			type += "; charset=utf-8";
		}
		response.setContentType(type);
		return 1;
	}

	public byte responseContentType(String type) {
		setContentType(type);

		return 0;
	}

	public String getHeader(String header) {
		String out = request.getHeader(header);
		if (out == null)
			return "";
		return out;
	}

	public void sendError(int error) {
		try {
			setHeader("Content-Encoding", "text/html");
			response.sendError(error);
		} catch (Exception e) {
			log.error("Error " + error, e);
		}
	}

	public void setQueryString(String qs) {
		loadParameters(qs);
	}

	private String removeInternalParms(String query) {
		query = removeEventPrefix(query);
		int idx = query.indexOf(GXNavigationHelper.POPUP_LEVEL);
		if (idx == 1)
			return "";
		if (idx > 1)
			query = query.substring(0, idx - 1);
		idx = query.indexOf("gx-no-cache=");
		if (idx >= 0) {
			idx = (idx > 0) ? idx - 1 : idx; // ","
			query = query.substring(0, idx);
		}
		return query;
	}

	public String getQueryString() {
		try {
			String query = request.getQueryString();
			if (query == null || query.length() == 0) {
				return "";
			}
			if (query.startsWith("?"))
				query = query.substring(1);
			return removeInternalParms(query);
		} catch (Exception ex) {
			return "";
		}
	}

	private String removeEventPrefix(String query) {
		if (isAjaxEventMode()) {
			int comIdx = query.indexOf(",");
			if (comIdx == -1)
				comIdx = query.indexOf("&");
			if (comIdx != -1)
				query = query.substring(comIdx + 1);
		}
		return query;
	}

	public String getPackage() {
		String sPath = request.getRequestURI();

		if (sPath.indexOf('.') >= 0)
			return sPath.substring(sPath.lastIndexOf('/') + 1, sPath.lastIndexOf('.') + 1);

		return "";
	}

	public String cgiGet(String parm) {
		Hashtable parsePostData = getPostData();
		// La parsePostData puede venir en null
		if (parsePostData != null) {
			Object val = parsePostData.get(parm.toUpperCase());

			if (val != null)
				return ((String[]) val)[0];

			val = parsePostData.get(parm);
			if (val != null)
				return ((String[]) val)[0];
		}

		return getHeader(parm);
	}

	public boolean isFileParm(String parm) {
		if (ServletFileUpload.isMultipartContent(request)) {
			return getPostedparts().hasitembyname(parm);
		}
		return false;
	}

	public String cgiGetFileName(String parm) {
		FileItemCollection files = getPostedparts();
		FileItem fileItem = files.itembyname(parm);

		if (fileItem != null) {
			String fileName = fileItem.getName();
			int dDelimIdx = fileName.lastIndexOf("\\");
			if ((dDelimIdx != -1) && (dDelimIdx < fileName.length() - 1)) {
				fileName = fileName.substring(dDelimIdx + 1);
			}

			return CommonUtil.getFileName(fileName);
		}

		return "";
	}

	public String cgiGetFileType(String parm) {
		FileItemCollection files = getPostedparts();
		FileItem fileItem = files.itembyname(parm);
		if (fileItem != null)
			return CommonUtil.getFileType(fileItem.getName());

		return "";
	}

	public void getMultimediaValue(String internalName, String[] blobVar, String[] uriVar) {
		String type = cgiGet(internalName + "Option");
		if (type.compareTo("file") == 0) {
			if (blobVar[0] != null && blobVar[0].trim().length() != 0) {
				String filename = cgiGetFileName(internalName);
				String filetype = cgiGetFileType(internalName);
				uriVar[0] = GXFile.getCompleteFileName(filename, filetype);
			}
			if (blobVar[0] == null || blobVar[0].trim().length() == 0)
				blobVar[0] = cgiGet(internalName + "_gxBlob");

			if (blobVar[0] == null || blobVar[0].trim().length() == 0)
				uriVar[0] = "";
		} else {
			Matcher matcher = MULTIMEDIA_GXI_GRID_PATTERN.matcher(internalName);
			if (matcher.matches()) {
				uriVar[0] = cgiGet(matcher.group(1) + "_GXI" + matcher.group(2));
			} else {
				uriVar[0] = cgiGet(internalName + "_GXI");
			}
			blobVar[0] = "";
		}
	}

	public void changePostValue(String ctrl, String value) {
		getPostData().put(ctrl, new String[] { value });
	}

	public void deletePostValue(String ctrl) {
		getPostData().remove(ctrl);
	}

	public void DeletePostValuePrefix(String sPrefix) {
		Hashtable<String, String[]> postData = getPostData();
		Set<String> keys = postData.keySet();
		Vector<String> toDelete = new Vector<>();
		for (String key : keys) {
			if (key != null && key.startsWith(sPrefix + "nRC_GXsfl_")) {
				toDelete.addElement(key);
			}
		}
		Iterator<String> itr = toDelete.iterator();
		while (itr.hasNext()) {
			postData.remove(itr.next());
		}
	}

	public HttpResponse getHttpResponse() {
		return httpRes;
	}

	public HttpRequest getHttpRequest() {
		return httpReq;
	}

	public void setHttpRequest(HttpRequest httpReq) {
		this.httpReq = httpReq;
	}

	public IHttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(IHttpServletRequest request) {
		this.request = request;
	}

	static private Hashtable<String, String[]> parseMultipartPostData(FileItemCollection fileItemCollection) {
		return com.genexus.webpanels.HttpUtils.parseMultipartPostData(fileItemCollection);
	}

	static public Hashtable<String, String[]> parsePostData(IHttpServletRequest request, IServletInputStream in) {
		try {
			// Nuestra versión del parsePostData utiliza UTF-8
			return com.genexus.webpanels.HttpUtils.parsePostData(in);
		} catch (IllegalArgumentException e) {
			return com.genexus.webpanels.HttpUtils.parsePostData(request);
		}
	}

	private static Boolean customRedirect;

	private boolean useCustomRedirect() {
		if (customRedirect == null) {
			String serverInfo = servletContext.getServerInfo();

			customRedirect = new Boolean(
					// serverInfo.startsWith("ApacheJServ") ||
					// serverInfo.startsWith("WebSphere Application Server for OS/390") ||
					System.getProperty("gx.custom.redirect") != null);
			// System.out.println("ServerInfo " + serverInfo + " CR " + customRedirect);
		}

		return customRedirect.booleanValue();
	}

	public String getDefaultPath() {
		String path = servletContext.getRealPath("/");

		if (path == null && servletContext.getAttribute(servletContext.getTEMPDIR()) != null) {
			return ((java.io.File) servletContext.getAttribute(servletContext.getTEMPDIR())).getAbsolutePath();
		}

		if (path == null) { // AWS LAMBDA SERVERLESS
			path = System.getenv("LAMBDA_TASK_ROOT");
			if (path == null)
				path = System.getProperty("LAMBDA_TASK_ROOT");
		}

		if (path.endsWith(File.separator)) {
			path = path.substring(0, path.length() - 1);
		}

		return path;
	}

	public void setDefaultPath(String path) {
	}

	public WebSession getWebSession() {
		return webSession;
	}

	private void redirect_http(String url) {
		Redirected = true;
		if (getResponseCommited())
			return;
		if (url != null && url.trim().length() != 0) {
			try {
				if (forwardAsWebCallMethod()) {
					IRequestDispatcher dispatcher = getRequest().getRequestDispatcher(url);
					if (dispatcher != null) {
						doForward(dispatcher);
					} else {
						// Si el dispatcher es nulo el recurso no esta disponible en el servlet context,
						// por lo tanto le hacemos un redirect
						doRedirect(url);
					}
				} else {
					pushUrlSessionStorage();
					if (useCustomRedirect()) {
						getResponse().setHeader("Location", url);
						getRequest().setAttribute("gx_webcall_method", "customredirect");
						getResponse().setStatus(HttpServletResponse.getSC_MOVED_TEMPORARILY());
					} else {
						doRedirect(url);
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("Redirect : " + e);
			}
		}
		setResponseCommited();
	}

	private void doRedirect(String url) throws IOException {
		getRequest().setAttribute("gx_webcall_method", "redirect");
		// getResponse().sendRedirect(url); No retornamos 302 sino 301, debido al SEO.
		response.setStatus(HttpServletResponse.getSC_MOVED_PERMANENTLY());
		response.setHeader("Location", url);
		sendCacheHeaders();
	}

	public void sendCacheHeaders() {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+00"));
		cal.setTime(new Date());
		long utcNow = cal.getTimeInMillis();
		getResponse().addDateHeader("Expires", utcNow);
		getResponse().addDateHeader("Last-Modified", utcNow);
		getResponse().addHeader("Cache-Control", "max-age=0, no-cache, no-store, must-revalidate");
	}

	private void doForward(IRequestDispatcher dispatcher) throws Exception {
		getRequest().setAttribute("gx_webcall_method", "forward");
		dispatcher.forward(getRequest(), getResponse());
	}

	public void ajax_rsp_command_close() {
		bCloseCommand = true;
		try {
			JSONObject closeParms = new JSONObject();
			closeParms.put("values", ObjArrayToJSONArray(this.getWebReturnParms()));
			closeParms.put("metadata", ObjArrayToJSONArray(this.getWebReturnParmsMetadata()));
			appendAjaxCommand("close", closeParms);
		} catch (JSONException ex) {
		}
	}

	private void pushUrlSessionStorage() {
		if (context != null && context.getHttpContext().isLocalStorageSupported() && !SkipPushUrl) {
			context.getHttpContext().pushCurrentUrl();
		}
		SkipPushUrl = false;
	}

	public boolean getHtmlHeaderClosed() {
		return this.htmlHeaderClosed;
	}

	public void closeHtmlHeader() {
		this.htmlHeaderClosed = true;
		this.writeTextNL("</head>");
	}

	public void redirect_impl(String url, GXWindow win) {
		if (!isGxAjaxRequest() && !isAjaxRequest() && win == null) {
			String popupLvl = getNavigationHelper(false).getUrlPopupLevel(getRequestNavUrl());
			String popLvlParm = "";
			if (popupLvl != "-1") {
				popLvlParm = (url.indexOf('?') != -1) ? (useOldQueryStringFormat? "," : "&") : "?";
				popLvlParm += com.genexus.util.Encoder.encodeURL("gxPopupLevel=" + popupLvl + ";");
			}

			if (isSpaRequest(true)) {
				pushUrlSessionStorage();
				getResponse().setHeader(GX_SPA_REDIRECT_URL, url + popLvlParm);
				sendCacheHeaders();
			} else {
				redirect_http(url + popLvlParm);
			}
		} else {

			try {
				if (win != null) {
					appendAjaxCommand("popup", win.GetJSONObject());
				} else if (!Redirected) {
					JSONObject jsonCmd = new JSONObject();
					jsonCmd.put("url", url);
					if (this.wjLocDisableFrm > 0) {
						jsonCmd.put("forceDisableFrm", this.wjLocDisableFrm);
					}
					appendAjaxCommand("redirect", jsonCmd);
					if (isGxAjaxRequest())
						dispatchAjaxCommands();
					Redirected = true;
				}
			} catch (JSONException e) {
				redirect_http(url);
			}
		}
	}

	private boolean isDocument(String url) {
		try {
			int idx = Math.max(url.lastIndexOf('/'), url.lastIndexOf('\\'));
			if (idx >= 0 && idx < url.length()) {
				url = url.substring(idx + 1);
			}
			idx = url.indexOf('?');
			String ext = url;
			if (idx >= 0) {
				ext = ext.substring(0, idx);
			}
			idx = url.lastIndexOf('.');
			if (idx >= 0) {
				ext = ext.substring(idx);
			} else {
				ext = "";
			}
			return (!ext.equals("") && !ext.startsWith(".aspx"));
		} catch (Exception ex) {
			log.error("isDocument error, url:" + url, ex);
			return false;
		}
	}

	public void redirect(String url) {
		redirect(url, false);
	}

	public void redirect(String url, boolean bSkipPushUrl) {
		SkipPushUrl = bSkipPushUrl;
		if (!Redirected) {
			redirect_impl(url, null);
		}
	}

	public void popup(String url) {
		popup(url, new Object[] {});
	}

	public void popup(String url, Object[] returnParms) {
		GXWindow win = new GXWindow();
		win.setUrl(url);
		win.setReturnParms(returnParms);
		newWindow(win);
	}

	public void newWindow(GXWindow win) {
		redirect_impl(win.getUrl(), win);
	}

	public void dispatchAjaxCommands() {
		Boolean isResponseCommited = getResponseCommited();
		if (!getResponseCommited()) {
			String res = getJSONResponsePrivate("");
			if (!isMultipartContent()) {
				response.setContentType("application/json");
			}
			sendFinalJSONResponse(res);
			setResponseCommited();
		}
	}

	public void sendFinalJSONResponse(String json) {
		boolean isMultipartResponse = !getResponseCommited() && isMultipartContent();
		if (isMultipartResponse) {
			_writeText(
					"<html><head></head><body><input type='hidden' data-response-content-type='application/json' value='");
		}
		_writeText(json);
		if (isMultipartResponse)
			_writeText("'/></body></html>");
	}

	public void setStream() {
		try {
			if (streamSet) {
				return;
			}

			streamSet = true;

			if (mustUseWriter()) {
				setWriter(getResponse().getWriter());
			} else {
				if (buffered) {
					buffer = new com.genexus.util.FastByteArrayOutputStream();
					setOutputStream(buffer);
				} else {
					setOutputStream(getResponse().getOutputStream().getOutputStream());
				}

				if (compressed) {
					String accepts = getHeader("Accept-Encoding");
					if (accepts != null && accepts.indexOf("gzip") >= 0) {
						setHeader("Content-Encoding", "gzip");
						setOutputStream(new GZIPOutputStream(getOutputStream()));
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public void flushStream() {
		proxyCookieValues();
		try {
			if (buffered) {
				// Esto en realidad cierra el ZipOutputStream, o el ByteOutputStream, no cierra
				// el del
				// servlet... Es necesario hacerlo, dado que sino el GZip no hace el flush de
				// los datos
				// que se grabaron al bytearray
				closeOutputStream();
				IHttpServletResponse response = getResponse();
				if (buffer != null && !response.isCommitted()) {
					IServletOutputStream stream = response.getOutputStream();
					response.setContentLength(buffer.size());
					buffer.writeToOutputStream(stream.getOutputStream());
					stream.close();
				}
			} else {
				closeOutputStream();
			}
		} catch (IOException e) {
			log.error("Error flushing stream", e);
		}
	}

	String sameSiteMode;
	private void proxyCookieValues() {
		if (sameSiteMode == null) {
			String sameSiteModeValue = Application.getClientPreferences().getProperty("SAMESITE_COOKIE", "");
			if (sameSiteModeValue.equals(SAME_SITE_NONE) || sameSiteModeValue.equals(SAME_SITE_LAX) || sameSiteModeValue.equals(SAME_SITE_STRICT))
				sameSiteMode = sameSiteModeValue;
			else
				sameSiteMode = "";
		}

		if (!sameSiteMode.equals("")) {
			addSameSiteCookieAttribute(getResponse());
		}
	}

	private void addSameSiteCookieAttribute(IHttpServletResponse response) {
		Collection<String> headers = response.getHeaders(SET_COOKIE);
		boolean firstHeader = true;
		for (String header : headers) {
			if (firstHeader) {
				response.setHeader(SET_COOKIE, String.format("%s; %s", header, "SameSite="+sameSiteMode));
				firstHeader = false;
				continue;
			}
			response.addHeader(SET_COOKIE, String.format("%s; %s", header, "SameSite="+sameSiteMode));
		}
	}

	public void cleanup() {
		ModelContext.deleteThreadContext();
		if (postData != null) {
			postData.clear();
			postData = null;
		}

		if (fileItemCollection != null) {
			for (int j = 0; fileItemCollection.getCount() > j; j++) {
				FileItem fileItem1 = fileItemCollection.item(j);
				fileItem1 = null;
			}
			fileItemCollection.clear();
			fileItemCollection = null;
			lstParts = null;
		}
	}

	public boolean isHttpContextNull() {return false;}
	public boolean isHttpContextWeb() {return true;}
}
