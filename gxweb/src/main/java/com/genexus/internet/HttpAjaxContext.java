package com.genexus.internet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;

import com.genexus.*;
import com.genexus.common.interfaces.IGXWebGrid;
import com.genexus.common.interfaces.IGXWindow;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.servlet.IServletContext;
import com.genexus.servlet.http.IHttpServletRequest;
import com.genexus.servlet.http.IHttpServletResponse;
import com.genexus.util.Codecs;
import com.genexus.util.Encryption;
import com.genexus.util.ThemeHelper;
import com.genexus.webpanels.DynAjaxEventContext;
import com.genexus.common.interfaces.IGXWebRow;

import com.genexus.webpanels.HttpContextWeb;
import com.genexus.webpanels.WebUtils;
import json.org.json.IJsonFormattable;
import json.org.json.JSONArray;
import json.org.json.JSONException;
import json.org.json.JSONObject;

public class HttpAjaxContext extends HttpContextWeb
{
	private static String GX_AJAX_REQUEST_HEADER = "GxAjaxRequest";

	public static final int TYPE_RESET		= 0;
	public static final int TYPE_SUBMIT		= 1;
	public static final int TYPE_BUTTON 		= 2;

	public static final ILogger logger = LogManager.getLogger(HttpAjaxContext.class);
	private JSONArray AttValues = new JSONArray();
	private JSONArray PropValues = new JSONArray();
	protected JSONObject HiddenValues = new JSONObject();
	protected JSONObject Messages = new JSONObject();
	private JSONObject WebComponents = new JSONObject();
	private Hashtable<Integer, JSONObject> LoadCommands = new Hashtable<>();
	private ArrayList Grids = new ArrayList();
	private Hashtable<String, Integer> DicGrids = new Hashtable<String, Integer>();
	private JSONObject ComponentObjects = new JSONObject();
	protected GXAjaxCommandCollection commands = new GXAjaxCommandCollection();
	protected IGXWebRow _currentGridRow = null;
	protected JSONArray StylesheetsToLoad = new JSONArray();
	private Vector<String> styleSheets = new Vector<>();
	private boolean validEncryptedParm = true;
	private Vector<String> javascriptSources = new Vector<>();
	private Vector<String> deferredFragments = new Vector<String>();
	private HashSet<String> deferredJavascriptSources = new HashSet<String>();
	private String themekbPrefix;
	private String themestyleSheet;
	private String themeurlBuildNumber;
	private Vector<Object> userStyleSheetFiles = new Vector<Object>();
	private String serviceWorkerFileName = "service-worker.js";
	private Boolean isServiceWorkerDefinedFlag = null;
	private String webAppManifestFileName = "manifest.json";
	private Boolean isWebAppManifestDefinedFlag = null;
	private boolean encryptionKeySended = false;
	private boolean htmlHeaderClosed = false;
	public boolean drawingGrid = false;
	private static String CACHE_INVALIDATION_TOKEN;

	protected boolean bCloseCommand = false;
	protected String formCaption = "";
	private Object[] returnParms = new Object[] {};
	private Object[] returnParmsMetadata = new Object[] {};

	private Stack<GXCmpContent> cmpContents = new Stack<>();

	private String _ajaxOnSessionTimeout = "Ignore";
	public void setAjaxOnSessionTimeout( String ajaxOnSessionTimeout){ this._ajaxOnSessionTimeout = ajaxOnSessionTimeout;}
	public String ajaxOnSessionTimeout(){ return _ajaxOnSessionTimeout;}

	DynAjaxEventContext dynAjaxEventContext = new DynAjaxEventContext();

	public DynAjaxEventContext getDynAjaxEventContext() {
		return dynAjaxEventContext;
	}

	private boolean isJsOutputEnabled = true;

	public HttpAjaxContext(String requestMethod, IHttpServletRequest req, IHttpServletResponse res,
						  IServletContext servletContext) throws IOException {
		super(requestMethod, req, res, servletContext);
	}

	public HttpContext copy() {
		try {
			HttpAjaxContext ctx = new HttpAjaxContext(requestMethod, request, response, servletContext);
			super.copyCommon(ctx);

			return ctx;
		} catch (java.io.IOException e) {
			return null;
		}
	}

	public void ajax_sending_grid_row(IGXWebRow row) {
		if (isAjaxCallMode()) {
			_currentGridRow = row;
		}
		else {
			_currentGridRow = null;
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
			String sUncachedURL = oldConvertURL(styleSheet) + urlBuildNumber;
			String sLayerName = styleSheet.replace("/", "_").replace(".","_");
			if (!this.getHtmlHeaderClosed() && this.isEnabled)
			{
				String sRelAtt = (isDeferred ? "rel=\"preload\" as=\"style\" " : "rel=\"stylesheet\"");
				if (isGxThemeHidden)
					writeTextNL("<link id=\"gxtheme_css_reference\" " + sRelAtt + " type=\"text/css\" href=\"" + sUncachedURL + "\" " + htmlEndTag(HTMLElement.LINK));
				else
				{
					if (getThemeisDSO())
					{
						writeTextNL("<style data-gx-href=\""+ sUncachedURL + "\"> @import url(\"" + sUncachedURL + "\") layer(" + sLayerName + ");</style>");
					}
					else
					{
						writeTextNL("<link " + sRelAtt + " type=\"text/css\" href=\"" + sUncachedURL + "\" " + htmlEndTag(HTMLElement.LINK));
					}
				}
			}
			else
			{
				if (!isGxThemeHidden) this.StylesheetsToLoad.put(oldConvertURL(styleSheet) + urlBuildNumber);
			}
		}
	}

	public void disableJsOutput()
	{
		isJsOutputEnabled = false;
	}

	public void enableJsOutput()
	{
		isJsOutputEnabled = true;
	}

	public boolean isJsOutputEnabled()
	{
		return isJsOutputEnabled;
	}

	public boolean isRedirected()
	{
	  return Redirected;
	}

	public boolean isCloseCommand()
	{
	  return bCloseCommand;
	}

	protected int nCmpDrawLvl = 0;

	private void doAjaxRefresh(String command)
	{
		try
		{
				String refreshMethod = "POST";
				if (ajaxRefreshAsGET)
				{
					refreshMethod = "GET";
				}
			appendAjaxCommand(command, refreshMethod);
		}
		catch (JSONException ex)
		{
		}
	}

	public void doAjaxRefresh()
	{
		doAjaxRefresh("refresh");
	}

	public void doAjaxRefreshForm()
	{
		doAjaxRefresh("refresh_form");
	}

	public void doAjaxRefreshCmp(String sPrefix)
	{
		try
		{
				appendAjaxCommand("cmp_refresh", sPrefix);
		}
		catch (JSONException ex)
		{
		}
	}

	public void doAjaxLoad(int SId, IGXWebRow row)
	{
		try
		{
			JSONObject JSONRow = new JSONObject();
			JSONRow.put("grid", SId);
			JSONRow.put("props", row.getParentGrid().GetJSONObject());
			JSONRow.put("values", row.getParentGrid().GetValues());
			appendLoadData(SId, JSONRow);
		}
		catch (JSONException ex)
		{
		}
	}

	public void doAjaxAddLines(int SId, int lines)
	{
		try
		{
			JSONObject JSONData = new JSONObject();
			JSONData.put("grid", SId);
			JSONData.put("count", lines);
			appendAjaxCommand("addlines", JSONData);
		}
		catch (JSONException ex)
		{
		}
	}

	public void doAjaxSetFocus(String controlName)
	{
		try
		{
			appendAjaxCommand("set_focus", controlName);
		}
		catch (JSONException ex)
		{
		}
	}

	protected Object[] getWebReturnParms()
	{
		return this.returnParms;
	}

	protected Object[] getWebReturnParmsMetadata()
	{
		return this.returnParmsMetadata;
	}

	public void setWebReturnParms(Object[] retParms)
	{
		this.returnParms = retParms;
	}

	public void setWebReturnParmsMetadata(Object[] retParmsMetadata)
	{
		this.returnParmsMetadata = retParmsMetadata;
	}

	public void appendAjaxCommand(String cmdType, Object cmdData) throws JSONException
	{
		commands.AppendCommand(new GXAjaxCommand(cmdType, cmdData));
	}

	public void appendLoadData(int SId, JSONObject Data) throws JSONException
	{
		LoadCommands.put(SId, Data);
	}

	public void executeUsercontrolMethod(String CmpContext, boolean IsMasterPage, String containerName, String methodName, String input, Object[] parms)
	{
			GXUsercontrolMethod method = new GXUsercontrolMethod(CmpContext, IsMasterPage, containerName, methodName, input, parms);
			commands.AppendCommand(new GXAjaxCommand("ucmethod", method.GetJSONObject()));
	}

	public void setExternalObjectProperty(String CmpContext, boolean IsMasterPage, String containerName, String propertyName, Object value)
	{
			JSONObject obj = new JSONObject();
			try
			{
				obj.put("CmpContext", CmpContext);
				obj.put("IsMasterPage", IsMasterPage);
				obj.put("ObjectName", containerName);
				obj.put("PropertyName", propertyName);
				obj.put("Value", value);
			} catch (JSONException ex) {
			}
			commands.AppendCommand(new GXAjaxCommand("exoprop", obj));
	}

	public void executeExternalObjectMethod(String CmpContext, boolean IsMasterPage, String containerName, String methodName, Object[] parms, boolean isEvent)
	{
			JSONObject obj = new JSONObject();
			try
			{
				obj.put("CmpContext", CmpContext);
				obj.put("IsMasterPage", IsMasterPage);
				obj.put("ObjectName", containerName);
				obj.put("Method", methodName);
				obj.put("Parms", HttpAjaxContext.ObjArrayToJSONArray(parms));
				obj.put("IsEvent", isEvent);
			} catch (JSONException ex) {
			}
			commands.AppendCommand(new GXAjaxCommand("exomethod", obj));
	}

	public void printReportAtClient(String reportFile)
	{
		printReportAtClient(reportFile, "");
	}

	public void printReportAtClient(String reportFile, String printerRule)
	{
		addPrintReportCommand(getResource(reportFile), printerRule);
	}

	protected void addPrintReportCommand(String reportFile, String printerRule)
	{
		JSONObject obj = new JSONObject();
		try
		{
			obj.put("reportFile", reportFile);
			obj.put("printerRule", printerRule);
		}
		catch (JSONException e) { }
		commands.AppendCommand(new GXAjaxCommand("print", obj));
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

	protected void ajax_addCmpContent( String content)
	{
		if (nCmpDrawLvl > 0)
			(cmpContents.peek()).addContent(content);
	}

	public void ajax_rspStartCmp( String CmpId)
	{
	  if (isJsOutputEnabled)
	  {
		  try
		  {
			  WebComponents.put(CmpId, "");
		  }
		  catch (JSONException ex) { }
	  }
	  nCmpDrawLvl++;
	  cmpContents.push(new GXCmpContent(CmpId));
	}

	public void ajax_rspEndCmp()
	{
	  nCmpDrawLvl--;
	  try
	  {
		  GXCmpContent cmp = cmpContents.pop();
		  WebComponents.put(cmp.getId(), cmp.getContent());
		  if (isSpaRequest())
		  {
			  if (nCmpDrawLvl > 0)
				  (cmpContents.peek()).addContent(cmp.getContent());
		  }
	  }
	  catch (JSONException ex)
	  {
	  }
	}

	private void sendReferer()
	{
		ajax_rsp_assign_hidden("sCallerURL", org.owasp.encoder.Encode.forUri(getReferer()));
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
		if (isSpaRequest())
		{
			writeTextNL("<script>gx.ajax.saveJsonResponse(" + WebUtils.htmlEncode(JSONObject.quote(getJSONResponse()), true) + ");</script>");
		}
		else
		{
			if (drawGridsAtServer())
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
			try(InputStream istream = context.packageClass.getResourceAsStream(path))
			{

				if (istream == null)
				{
					cssContent = "";
				}
				else {
					//BOMInputStream bomInputStream = new BOMInputStream(istream);// Avoid using BOMInputStream because of runtime error (java.lang.NoSuchMethodError: org.apache.commons.io.IOUtils.length([Ljava/lang/Object;)I) issue 94611
					//cssContent = IOUtils.toString(bomInputStream, "UTF-8");
					cssContent = PrivateUtilities.BOMInputStreamToStringUTF8(istream);
				}
			}
			catch ( Exception e) {
				cssContent = "";
			}
			ApplicationContext.getcustomCSSContent().put(getRequest().getServletPath(), cssContent);
		}
		return cssContent;
	}

	public void CloseStyles()
	{
		String cssContent = FetchCustomCSS();
		boolean bHasCustomContent = ! cssContent.isEmpty();
		if (bHasCustomContent && !styleSheets.contains(getRequest().getServletPath()))
		{
			writeTextNL("<style id=\"gx-inline-css\">" + cssContent + "</style>");
			styleSheets.add(getRequest().getServletPath());
		}
		String[] referencedFiles = ThemeHelper.getThemeCssReferencedFiles(PrivateUtilities.removeExtension(themestyleSheet));
		for (int i=0; i<referencedFiles.length; i++)
		{
			String file = referencedFiles[i];
			String extension = PrivateUtilities.getExtension(file);
			if (extension != null)
			{
				if (extension.equals("css"))
					AddStyleSheetFile(file, themeurlBuildNumber, false, bHasCustomContent);
				else if (extension.equals("js"))
					AddDeferredJavascriptSource(file, themeurlBuildNumber);
			}
		}
		for (Object data : this.userStyleSheetFiles)
		{
			String[] sdata = (String[]) data;
			AddStyleSheetFile(sdata[0], sdata[1], false, false);
		}
		AddStyleSheetFile(themekbPrefix + "Resources/" + getLanguage() + "/" + themestyleSheet, themeurlBuildNumber, true, bHasCustomContent);
	}

	public void AddThemeStyleSheetFile(String kbPrefix, String styleSheet)
	{
		AddThemeStyleSheetFile(kbPrefix, styleSheet, "");
	}
	public void AddThemeStyleSheetFile(String kbPrefix, String styleSheet, String urlBuildNumber)
	{
		this.themekbPrefix = kbPrefix;
		this.themestyleSheet = styleSheet;
		this.themeurlBuildNumber = urlBuildNumber;
	}

	public void AddStyleSheetFile(String styleSheet)
	{
		AddStyleSheetFile(styleSheet, "");
	}

	public void AddStyleSheetFile(String styleSheet, String urlBuildNumber)
	{
		urlBuildNumber = getURLBuildNumber(styleSheet, urlBuildNumber);
		userStyleSheetFiles.add(new String[] { styleSheet, urlBuildNumber });
	}

	private String getURLBuildNumber(String resourcePath, String urlBuildNumber)
	{
		if(urlBuildNumber.isEmpty() && !GXutil.isAbsoluteURL(resourcePath) && !GXutil.hasUrlQueryString(resourcePath))
		{
			return "?" + getCacheInvalidationToken();
		}
		else
		{
			return urlBuildNumber;
		}
	}

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

	private JSONObject getGxObject(JSONArray array, String CmpContext, boolean IsMasterPage)
	{
		try
		{
			JSONObject obj;
			int len = array.length();
			for(int i=0; i<len; i++)
			{
				obj = array.getJSONObject(i);
				if (obj.getBoolean("IsMasterPage") == IsMasterPage &&  obj.getString("CmpContext").equals(CmpContext))
				{
					return obj;
				}
			}
			obj = new JSONObject();
			obj.put("CmpContext", CmpContext);
			obj.put("IsMasterPage", new Boolean(IsMasterPage).toString());
			array.put(obj);
			return obj;
		}
		catch (JSONException ex)
		{
		}
		return null;
	}

	public void ajax_rsp_assign_attri( String CmpContext, boolean IsMasterPage, String AttName, Object AttValue)
	{
		if (isJsOutputEnabled)
		{
			if (!isSpaRequest() || (isSpaRequest() && (CmpContext == null || CmpContext.trim().length() == 0)))
			{
				try {
					JSONObject obj = getGxObject(AttValues, CmpContext, IsMasterPage);
					if (obj != null)
					{
						obj.put(AttName, AttValue);
					}
				}
				catch (JSONException ex) {
				}
			}
		}
	}

	private boolean isUndefinedOutParam(String key, Object SdtObj) {
		if (!dynAjaxEventContext.isInputParm(key))
		{
			if (SdtObj instanceof IGXAssigned)
			{
				return !((IGXAssigned)SdtObj).getIsAssigned();
			}
		}
		return false;
	}


	public void ajax_rsp_assign_sdt_attri( String CmpContext, boolean IsMasterPage, String AttName, Object SdtObj)
        {
            if (isJsOutputEnabled)
            {
                if (!isSpaRequest() || (isSpaRequest() && (CmpContext == null || CmpContext.trim().length() == 0)))
                {
                  try {
                      JSONObject obj = getGxObject(AttValues, CmpContext, IsMasterPage);
					  if (obj != null && (dynAjaxEventContext.isParmModified(AttName, SdtObj) || !isUndefinedOutParam( AttName, SdtObj)))
					  {
                        if (SdtObj instanceof IGxJSONAble)
                            obj.put(AttName, ((IGxJSONAble)SdtObj).GetJSONObject());
                        else
                        {
                            if (SdtObj.getClass().isArray())
                            {
                                obj.put(AttName, ObjArrayToJSONArray(SdtObj));
                            }
                        }
                      }
                  }
                  catch (JSONException e) {
					  logger.error(String.format("Could not serialize Object '%s' to JSON", AttName), e);
                  }
                }
            }
        }

        public String ajax_rspGetHiddens()
        {
            return HiddenValues.toString();
        }

        private JSONObject getControlProps(JSONObject obj, String Control)
        {
            JSONObject ctrlProps = null;
            try {
                ctrlProps = obj.optJSONObject(Control);
                if (ctrlProps == null) {
                    ctrlProps = new JSONObject();
                    obj.put(Control, ctrlProps);
                }
            } catch (JSONException e) {
            }
            return ctrlProps;
        }

        public void ajax_rsp_assign_prefixed_prop(String Control, String Property, String Value)
        {
            // Already prefixed control properties are sent in the master page object.
            ajax_rsp_assign_prop("", true, Control, Property, Value, true);
        }

        public void ajax_rsp_assign_prop( String CmpContext, boolean IsMasterPage, String Control, String Property, String Value, boolean SendToAjax)
        {
            if (SendToAjax && shouldLogAjaxControlProperty(Property))
            {
                if (!isSpaRequest() || (isSpaRequest() && (CmpContext == null || CmpContext.trim().length() == 0)))
                {
                    try
                    {
                        // Avoid sending to the client side tmp media directory paths
                        if (Property.equals("URL")) {
                            String tmpMediaDir = com.genexus.ModelContext.getModelContext().getClientPreferences().getTMPMEDIA_DIR().replaceAll("\\\\", "/");
                            if (Value.indexOf(tmpMediaDir) >= 0)
                                return;
                        }
                        if (Control.equals("FORM") && Property.equals("Caption"))
                        {
                            formCaption = Value;
                        }
                        JSONObject obj = getGxObject(PropValues, CmpContext, IsMasterPage);
                        if (obj != null)
                        {
                            JSONObject ctrlProps = getControlProps(obj, Control);
                            if (ctrlProps != null)
                            {
                            ctrlProps.put(Property, Value);
                            }
                        }
                        com.genexus.internet.HttpContext webContext = (HttpContext) com.genexus.ModelContext.getModelContext().getHttpContext();
                        if (webContext != null && !webContext.isAjaxRequest())
                        {
                            ajax_rsp_assign_hidden(Control + "_" + Property.substring(0, 1) + Property.substring(1).toLowerCase(), Value);
                        }
                    }
                    catch (JSONException e) {
                    }
                }
            }
        }

        public void ajax_rsp_assign_uc_prop(String CmpContext, boolean IsMasterPage, String Control, String Property, String Value)
        {
            ajax_rsp_assign_prop(CmpContext, IsMasterPage, Control, Property, Value, true);
            ajax_rsp_assign_prop_as_hidden(Control, Property, Value);
        }

        public void ajax_rsp_assign_boolean_hidden(String Property, Boolean Value)
        {
            ajax_rsp_assign_hidden(Property, (Object)Value);
        }

        public void ajax_rsp_assign_hidden(String Property, String Value)
        {
            ajax_rsp_assign_hidden(Property, (Object)Value);
        }

        private void ajax_rsp_assign_hidden(String Property, Object Value)
        {
          try {
            if (_currentGridRow != null)
                _currentGridRow.AddHidden(Property, Value);
            else
                HiddenValues.put(Property, Value);
          }
          catch (JSONException e) {
          }
        }

        public void ajax_rsp_assign_hidden_sdt( String SdtName, Object SdtObj)
        {
          try {
            if (SdtObj instanceof IGxJSONAble)
            {
                HiddenValues.put(SdtName, ((IGxJSONAble)SdtObj).GetJSONObject());
            }
            else
            {
                if (SdtObj.getClass().isArray())
                {
					try	{
						HiddenValues.put(SdtName, ObjArrayToJSONArray(SdtObj));
					}
					catch(ClassCastException e){
						logger.error(String.format("Could not serialize Object '%s' to JSON", SdtName), e);
						HiddenValues.put(SdtName, SdtObj);
					}
                }
            }
          }
          catch (JSONException e) {
			  logger.error(String.format("Could not serialize Object '%s' to JSON", SdtName), e);
          }
        }

		public void ajax_rsp_assign_grid(String gridName, IGXWebGrid gridObj)
		{
			Object jsonObj = ((IGxJSONAble) gridObj).GetJSONObject();
			Grids.add(jsonObj);
		}

        public void ajax_rsp_assign_grid(String gridName, IGXWebGrid gridObj, String Control)
        {
			Object jsonObj = ((IGxJSONAble) gridObj).GetJSONObject();
			if (DicGrids.containsKey(Control)) {
				Grids.set(DicGrids.get(Control), jsonObj);
			}
			else
			{
				Grids.add(jsonObj);
				DicGrids.put(Control, Grids.size() - 1);
			}
        }

        public void ajax_rsp_clear(){
        	PropValues = new JSONArray();
        }

		private boolean shouldLogAjaxControlProperty(String property)
		{
			return isJsOutputEnabled || (isSpaRequest() && property == "Enabled");
		}

		@Deprecated
		public void AddComponentObject(String cmpCtx, String objName)
		{
			AddComponentObject(cmpCtx, objName, true);
		}

        public void AddComponentObject(String cmpCtx, String objName, boolean justCreated)
        {
            try {
                com.genexus.internet.HttpContext webContext = (HttpContext) com.genexus.ModelContext.getModelContext().getHttpContext();
				if (justCreated)
				{
					try {
						webContext.DeletePostValuePrefix(cmpCtx);
					}
					catch (Exception e) {
						logger.error("Could not delete post value prefix", e);
					}
				}
                ComponentObjects.put(cmpCtx, objName);
            }
          catch (JSONException e) {
          }
        }

        public void SendComponentObjects()
        {
            try {
            HiddenValues.put("GX_CMP_OBJS", ComponentObjects);
          }
          catch (JSONException e) {
          }
        }

        protected void AddThemeHidden(String theme)
        {
          try {
            HiddenValues.put("GX_THEME", theme);
          }
          catch (JSONException e) {
          }
        }

        public void AddStylesheetsToLoad()
        {
            if (StylesheetsToLoad.length() > 0)
            {
                try {
                    HiddenValues.put("GX_STYLE_FILES", StylesheetsToLoad);
                }
                catch (JSONException e) {
                }
            }
        }

        public void SaveComponentMsgList( String sPrefix)
        {
            try {
            	Messages.put(sPrefix, GX_msglist.GetJSONObject());
          }
          catch (JSONException e) {
          }
        }

		public String getJSONContainerResponse(IGxJSONAble Container) {

                GXJSONObject jsonCmdWrapper = new GXJSONObject(isMultipartContent());
                try
                {
					jsonCmdWrapper.put("gxHiddens", HiddenValues);
					jsonCmdWrapper.put("gxContainer", Container.GetJSONObject());
                }
                catch (JSONException e)
                {
                }
                return jsonCmdWrapper.toString();
		}

        protected String getJSONResponsePrivate(String cmpContext)
        {
                GXJSONObject jsonCmdWrapper = new GXJSONObject(isMultipartContent());
                try
                {
						Collections.reverse(Arrays.asList(Grids));
						JSONArray JSONGrids = new JSONArray(Grids);
                        if (commands.AllowUIRefresh())
                        {
							if (cmpContext  == null || cmpContext.equals(""))
							{
								cmpContext = "MAIN";
							}
                            SaveComponentMsgList(cmpContext);
                            jsonCmdWrapper.put("gxProps", PropValues);
                            jsonCmdWrapper.put("gxHiddens", HiddenValues);
                            jsonCmdWrapper.put("gxValues", AttValues);
                            jsonCmdWrapper.put("gxMessages", Messages);
                            jsonCmdWrapper.put("gxComponents", WebComponents);
                            jsonCmdWrapper.put("gxGrids", JSONGrids);
                        }
                        for(Enumeration loadCmds = LoadCommands.keys(); loadCmds.hasMoreElements();)
                        {
                            appendAjaxCommand("load", LoadCommands.get(loadCmds.nextElement()));
                        }
                        if (commands.getCount() > 0)
                        {
                            jsonCmdWrapper.put("gxCommands", commands.getJSONArray());
                        }
                }
                catch (JSONException e)
                {
                }
                return jsonCmdWrapper.toString();
        }

	public String getJSONResponse(String cmpContext)
	{
		if (isCloseCommand() || isRedirected())
                    return "";
		return getJSONResponsePrivate(cmpContext);
	}

	public String getJSONResponse()
	{
		return getJSONResponse("");
	}

	public static Object[] createArrayFromArrayObject(Object o) {
		if(!o.getClass().getComponentType().isPrimitive())
			return (Object[])o;

		int element_count = Array.getLength(o);
		Object elements[] = new Object[element_count];

		for(int i = 0; i < element_count; i++){
			elements[i] = Array.get(o, i);
		}

		return elements;
	}

	public static JSONArray ObjArrayToJSONArray(Object parms)
	{
		return ObjArrayToJSONArray(createArrayFromArrayObject(parms));
	}

	public static JSONArray ObjArrayToJSONArray(Object[] parms)
	{
			JSONArray inputs = new JSONArray();
			for (int i = 0; i < parms.length; i++)
			{
					Object parm = parms[i];
					if (parm instanceof IGxJSONAble)
					{
							inputs.put(((IGxJSONAble)parm).GetJSONObject());
					}
					else if (parm.getClass().isArray())
					{
						inputs.put(ObjArrayToJSONArray((Object[])parm));
					}
					else
					{
							inputs.put(parm);
					}
			}
			return inputs;
	}

	public String getWebReturnParmsJS()
	{
		return ObjArrayToJSONArray(this.getWebReturnParms()).toString();
	}

	public String getWebReturnParmsMetadataJS()
	{
		return ObjArrayToJSONArray(this.getWebReturnParmsMetadata()).toString();
	}

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

	public void writeTextNL(String text)
	{
		writeText(text + "\n");
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

	public boolean getHtmlHeaderClosed() {
		return this.htmlHeaderClosed;
	}

	public void closeHtmlHeader() {
		this.htmlHeaderClosed = true;
		this.writeTextNL("</head>");
	}

	public void redirect_impl(String url, IGXWindow win) {
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

	public void setDrawingGrid(boolean drawingGrid)
	{
		this.drawingGrid = drawingGrid;
	}

	public void windowClosed()
	{
		String popupLevel = getNavigationHelper().getUrlPopupLevel(getRequestNavUrl());
		if (popupLevel.equals("-1"))
			popReferer();
		else
			deleteReferer(popupLevel);
	}

	public boolean isPopUpObject()
	{
		if (wrapped)
			return false;
		return !getNavigationHelper().getUrlPopupLevel(getRequestNavUrl()).equals("-1");
	}

	public int getButtonType()
	{
		if (drawGridsAtServer())
		{
			return TYPE_SUBMIT;
		}
		return TYPE_BUTTON;
	}

	public boolean drawGridsAtServer()
	{
		if (drawGridsAtServer == -1)
		{
			drawGridsAtServer = 0;
			if( (getContext().getPreferences()).propertyExists("DrawGridsAtServer"))
			{
				String prop = (getContext().getPreferences()).getProperty("DrawGridsAtServer", "no");
				if (prop.equalsIgnoreCase("always"))
				{
					drawGridsAtServer = 1;
				}
				else if (prop.equalsIgnoreCase("ie6"))
				{
					if (getBrowserType() == BROWSER_IE)
					{
						if (getBrowserVersion().startsWith("6"))
						{
							drawGridsAtServer = 1;
						}
					}
				}
			}
		}
		return (drawGridsAtServer == 1);
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

	public byte isMobileBrowser()
	{
		String accept = getHeader("HTTP_ACCEPT");

		return (accept.indexOf("wap") >= 0 || accept.indexOf("hdml") >= 0)?0: (byte) 1;
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

	public void popReferer()
	{
		getNavigationHelper().popUrl(getRequestNavUrl());
	}

	public String getCacheInvalidationToken()
	{
		if (CACHE_INVALIDATION_TOKEN == null || CACHE_INVALIDATION_TOKEN.trim().length() == 0)
		{
			String token = (context.getPreferences()).getProperty("CACHE_INVALIDATION_TOKEN", "");
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

	class GXCmpContent
	{
		private String id;
		private String content;

		public GXCmpContent(String id)
		{
			this.id = id;
			this.content = "";
		}

		public String getId()
		{
			return id;
		}

		public void addContent(String content)
		{
			this.content += content;
		}

		public String getContent()
		{
			return content;
		}
	}

	class GXAjaxCommand
	{
		private String[] canManyCmds = new String[] { "print", "load", "popup", "refresh", "ucmethod", "cmp_refresh", "addlines", "set_focus", "calltarget", "exoprop", "exomethod", "refresh_form" };
		private String type;
		private Object data;

		public GXAjaxCommand(String type)
		{
				this.type = type;
				this.data = "";
		}

		public GXAjaxCommand(String type, Object data)
		{
				this.type = type;
				this.data = data;
		}

		public String getType()
		{
				return type;
		}

		public void setData(Object data)
		{
				this.data = data;
		}

		public Object getData()
		{
				return data;
		}

		public JSONObject getJSONObject()
		{
				JSONObject jObj = new JSONObject();
				try {
					jObj.put(type, data);
				} catch (JSONException ex) {
				}
				return jObj;
		}

		public boolean canHaveMany()
		{
				for (int i = 0; i < canManyCmds.length; i++)
				{
						if (type.equals(canManyCmds[i]))
						{
								return true;
						}
				}
				return false;
		}

		public boolean equals(Object obj)
		{
				if (obj instanceof GXAjaxCommand)
				{
						if (!canHaveMany())
						{
								return (type.equalsIgnoreCase(((GXAjaxCommand)obj).getType()));
						}
				}
				return super.equals(obj);
		}

		public String toString()
		{
				return "{ type:" + type + ", data:" + data + " }";
		}
	}

	public class GXUsercontrolMethod implements IGxJSONAble
	{
		JSONObject wrapper;

		public GXUsercontrolMethod(String CmpContext, boolean IsMasterPage, String containerName, String methodName, String output, Object[] parms)
		{
			wrapper = new JSONObject();
			AddObjectProperty("CmpContext", CmpContext);
			AddObjectProperty("IsMasterPage", new Boolean(IsMasterPage));
			AddObjectProperty("Control", containerName);
			AddObjectProperty("Method", methodName);
			AddObjectProperty("Output", output);
			AddObjectProperty("Parms", HttpAjaxContext.ObjArrayToJSONArray(parms));
		}

		public JSONArray GetParmsJArray(Object[] parms)
		{
			JSONArray inputs = new JSONArray();
			for (int i = 0; i < parms.length; i++)
			{
				Object parm = parms[i];
				if (parm instanceof IGxJSONAble)
				{
					inputs.put(((IGxJSONAble)parm).GetJSONObject());
				}
				else
				{
					inputs.put(parm);
				}
			}
			return inputs;
		}

		public void AddObjectProperty(String name, Object prop)
		{
                    try
                    {
			wrapper.put(name, prop);
                    } catch (JSONException ex) {
                    }
		}

		public Object GetJSONObject(boolean includeState)
		{
			return GetJSONObject();
		}

		public Object GetJSONObject()
		{
			return wrapper;
		}

		public void FromJSONObject(IJsonFormattable obj)
		{
		}

		public String ToJavascriptSource()
		{
                    return wrapper.toString();
		}

                public void tojson()
		{
		}
	}

        class GXAjaxCommandCollection
        {
                private ArrayList<GXAjaxCommand> commands;
                private boolean allowUIRefresh;

                public GXAjaxCommandCollection()
                {
                        commands = new ArrayList<>();
                        allowUIRefresh = true;
                }

                public int getCount()
                {
                        return commands.size();
                }

                public boolean AllowUIRefresh()
                {
                        return allowUIRefresh;
                }

                public void AppendCommand(GXAjaxCommand cmd)
                {
                        GXAjaxCommand cmd1 = GetCommand(cmd);
                        if (cmd1 == null)
                        {
                                if (allowUIRefresh)
                                {
                                        allowUIRefresh = cmd.canHaveMany();
                                }
                                commands.add(cmd);
                        }
                        else
                        {
                                cmd1.setData(cmd.getData());
                        }
                }

                private GXAjaxCommand GetCommand(GXAjaxCommand cmd)
                {
                        int cIdx = commands.indexOf(cmd);
                        if (cIdx > 0)
                        {
                                return commands.get(cIdx);
                        }
                        return null;
                }

                public JSONArray getJSONArray()
                {
                        JSONArray jArr = new JSONArray();
                        for(int i=0; i<commands.size(); i++)
                        {
                                GXAjaxCommand cmd = commands.get(i);
                                jArr.put(cmd.getJSONObject());
                        }
                        return jArr;
                }
        }
}
