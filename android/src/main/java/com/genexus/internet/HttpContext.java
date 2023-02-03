package com.genexus.internet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import com.genexus.*;

import com.artech.base.services.AndroidContext;
import com.genexus.util.Codecs;
import com.genexus.util.Encryption;

import json.org.json.IJsonFormattable;
import json.org.json.JSONArray;
import json.org.json.JSONException;
import json.org.json.JSONObject;

public abstract class HttpContext extends HttpAjaxContext implements IHttpContext
{
    private static String GX_AJAX_REQUEST_HEADER = "GxAjaxRequest";

    protected boolean PortletMode = false;
    protected boolean AjaxCallMode = false;
    protected boolean AjaxEventMode = false;
	 protected boolean FullAjaxMode = false;
	 public boolean drawingGrid = false;
	
    public void setPortletMode()
    { PortletMode = true; }

    public void setAjaxCallMode()
    { AjaxCallMode = true; }

	 public void setFullAjaxMode()
	 { FullAjaxMode = true; }

	 public void setAjaxEventMode()
    { AjaxEventMode = true; }

    public boolean isPortletMode()
    { return PortletMode; }

    public boolean isAjaxCallMode()
    { return AjaxCallMode; }

    public boolean isAjaxEventMode()
    { return AjaxEventMode; }

	 public boolean isFullAjaxMode()
	 { return FullAjaxMode; }

    public boolean isAjaxRequest()
    { return isAjaxCallMode() || isAjaxEventMode() || isPortletMode() || isFullAjaxMode(); }
    

    public byte wbGlbDoneStart = 0;
				//nSOAPErr
	public HttpResponse GX_webresponse;

	public String wjLoc = "";
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

        private Vector javascriptSources = new Vector();
        private Vector styleSheets = new Vector();
        private boolean responseCommited = false;
        private boolean wrapped = false;
        private int drawGridsAtServer = -1;

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

	public abstract void cleanup();
	public abstract String getResourceRelative(String path);
	public abstract String getResourceRelative(String path, boolean includeBasePath);
	public abstract String getResource(String path);
	public abstract String GetNextPar();
	public abstract HttpContext copy();
	public abstract byte setHeader(String header, String value);
	public abstract void setDateHeader(String header, int value);
	public abstract void setRequestMethod(String method);
	public abstract String getRequestMethod();
	public abstract String getReferer();
	public abstract short setWrkSt(int handle, String wrkst) ;
	public abstract String getWorkstationId(int handle) ;
	public abstract short setUserId(int handle, String user, String dataSource) ;
	public abstract String getUserId(String key, ModelContext context, int handle, String dataSource);
	public abstract String getRemoteAddr();
        public abstract boolean isSmartDevice();
	public abstract int getBrowserType();
	public abstract boolean isIE55();
	public abstract String getDefaultPath();
	public abstract String getBrowserVersion();
	public abstract Object getSessionValue(String name);

	public abstract void webPutSessionValue(String name, Object value);
	public abstract void webPutSessionValue(String name, long value);
	public abstract void webPutSessionValue(String name, double value);
	public abstract void webSessionId(String[] id);
	public abstract String getContextPath();
	public abstract String webSessionId();
	public abstract String getCookie(String name);
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
      public abstract void deletePostValue(String ctrl);
	public abstract HttpResponse getHttpResponse();
	public abstract HttpRequest getHttpRequest();
	public abstract void setHttpRequest(HttpRequest httprequest);
 //       public abstract HttpServletRequest getRequest();
 //       public abstract HttpServletResponse getResponse();
 //       public abstract void setRequest(HttpServletRequest request);
	public abstract Hashtable getPostData();
	public abstract void redirect(String url);
        public abstract void popup( String url);
        public abstract void popup( String url, Object[] returnParms);
	public abstract void setStream() ;
	public abstract void flushStream();

        public abstract void closeHtmlHeader();
        public abstract boolean getHtmlHeaderClosed();
        public abstract void ajax_rsp_command_close();
        public abstract void dispatchAjaxCommands();

		public void AddJavascriptSource(String jsSrc, String urlBuildNumber)
        {
            if(!javascriptSources.contains(jsSrc))
            {
                javascriptSources.add(jsSrc);
                writeTextNL("<script type=\"text/javascript\" src=\"" + oldConvertURL(jsSrc) + urlBuildNumber + "\"></script>") ;
            }
        }

		public void AddThemeStyleSheetFile(String kbPrefix, String styleSheet, String urlBuildNumber)
		{
			AddStyleSheetFile(kbPrefix + "Resources/" + getLanguage() + "/" + styleSheet, urlBuildNumber);
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
			if (!styleSheets.contains(styleSheet))
			{
				styleSheets.add(styleSheet);
                if (!this.getHtmlHeaderClosed() && this.isEnabled)
                {
                    writeTextNL("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + oldConvertURL(styleSheet) + urlBuildNumber + "\" " + htmlEndTag(HTMLElement.LINK));
                }
                else
                {
                    this.StylesheetsToLoad.put(styleSheet);
                }
			}
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
          //  if (getRequest() != null && getRequest().getQueryString() !=null && getRequest().getQueryString().contains("_escaped_fragment_"))
          //  {
          //      return true;
          //  }
            return false;
        }
		public boolean drawGridsAtServer()
		{
			if (this.drawGridsAtServer == -1)
			{
				this.drawGridsAtServer = 0;
				if( this.getContext().getPreferences().propertyExists("DrawGridsAtServer"))
				{
					String prop = this.getContext().getPreferences().getProperty("DrawGridsAtServer", "no");
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
            GXNavigationHelper helper = (GXNavigationHelper)getSessionValue(GX_NAV_HELPER);
            if (helper == null)
            {
                helper = new GXNavigationHelper();
                webPutSessionValue(GX_NAV_HELPER, helper);
            }
            return helper;
        }

        protected String getRequestNavUrl()
        {
            return "";
        }

        public void deleteReferer(String popupLevel)
        {
            getNavigationHelper().deleteStack(popupLevel);
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
        //    String gxHeader = getRequest().getHeader(GX_AJAX_REQUEST_HEADER);
       //     if (gxHeader != null && gxHeader.trim().length() > 0)
        //    {
         //       return true;
        //    }
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
           //     String clientKey = getRequest().getHeader(Encryption.AJAX_SECURITY_TOKEN);
          //      if (clientKey != null && clientKey.trim().length() > 0)
            //    {
            //        boolean candecrypt[]=new boolean[1];
            //        clientKey = Encryption.decryptRijndael(clientKey, Encryption.GX_AJAX_PRIVATE_KEY, candecrypt);
             //       if (candecrypt[0])
            //        {
              //      	webPutSessionValue(Encryption.AJAX_ENCRYPTION_KEY, clientKey);
             //       	return true;
              //      }else
              //      {
              //      	return false;
              //      }
              //  }
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
				sendResponseStatus(403, "Forbidden action");
				return false;
			}
			else if (!insideAjaxCall && isGxAjaxRequest())
			{
				sendResponseStatus(440, "Session timeout");
				return false;
			}            
            return true;
        }
        
        public void sendResponseStatus(int statusCode, String statusDescription)
        {
            //getResponse().setStatus(statusCode);
            //try { getResponse().sendError(statusCode, statusDescription); }
            //catch(Exception e) {}
            //setAjaxCallMode();
            //disableOutput();
        }
        
        private void sendReferer()
        {
        		ajax_rsp_assign_hidden("sCallerURL", getReferer());
        }
        
        public boolean useBase64ViewState()
        {
            return this.context.getPreferences().getProperty("UseBase64ViewState", "n").equals("y");
        }

        public void SendState()
        {
        	   sendReferer();
            AddThemeHidden(this.getTheme());
            AddStylesheetsToLoad();
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
            writeTextNL("'" + htmlEndTag(HTMLElement.INPUT) + "</div>");
            if (this.formCaption != null && !this.formCaption.equals(""))
            {
            		writeTextNL("<script type=\"text/javascript\">gx.fn.setCtrlProperty('FORM', 'Caption', '" + this.formCaption + "');</script>");
            }
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
        
        public boolean IsSameComponent(String oldName, String newName)
        {
            if(oldName.trim().equalsIgnoreCase(newName.trim()))
            {
                return true;
            }
            else if(newName.trim().toLowerCase().startsWith(oldName.trim().toLowerCase() + "?"))
            {
                return true;
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
		boolean lastCharIsSpace = true; // Asumo que al comienzo el lastChar era space
		for (int i = 0; i < text.length(); i++)
		{
			char currentChar = text.charAt(i);
			switch (currentChar)
			{
				case (char) 34:
					writeText("&quot;");
					break;
				case (char) 38:
					writeText("&amp;");
					break;
				case (char) 60:
					writeText("&lt;");
					break;
				case (char) 62:
					writeText("&gt;");
					break;
				case '\t':
					writeText(cvtTabs ? "&nbsp;&nbsp;&nbsp;&nbsp;" : ("" + currentChar));
					break;
				case '\r':
					if (cvtEnters && text.length() > i + 1 && text.charAt(i+1) == '\n'){
						writeText("<br>");
						i++;
					}					
					break;
				case '\n':
					writeText(cvtEnters ? "<br>" : ("" + currentChar));
					break;
				case ' ':
					writeText((lastCharIsSpace && cvtSpaces) ? "&nbsp;" : " ");
					break;
				default:
					writeText("" + currentChar);
			}
			lastCharIsSpace = currentChar == ' ';
		}
	}

	public void writeValue(String text)
	{
		for (int i = 0; i < text.length(); i++)
		{
			char currentChar = text.charAt(i);

			switch (currentChar)
			{
				case (char) 34:
					writeText("&quot;");
					break;
				case (char) 38:
					writeText("&amp;");
					break;
				case (char) 60:
					writeText("&lt;");
					break;
				case (char) 62:
					writeText("&gt;");
					break;
				default:
					writeText("" + currentChar);
			}
		}
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
      if (getResponseCommited())
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

	public void closeOutputStream()	throws IOException
	{
		if	(out != null)
			out.close();
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
	private boolean isDSO = false;

	public String getTheme()
	{
		
		if (theme.trim().length() == 0)
			theme = Preferences.getDefaultPreferences().getDefaultTheme();

		return theme;
	}
	public void setDefaultTheme(String t)
	{
		setDefaultTheme( t, false);
	}

	public void setDefaultTheme(String t, boolean isDSO)
	{
		pushCurrentUrl();
		theme = t;
		this.isDSO = isDSO;
	}
	public int setTheme(String t)
	{
		pushCurrentUrl();
			return 1;
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
				return result;
		}
	}
	public String getBuildNumber(int buildN){
		return context.getClientPreferences().getBUILD_NUMBER(buildN);
	}

	public String convertURL(String file)
	{
		if (file.equals(""))
		{
			return "";
		}

		if (file.indexOf(".") != -1)
		{
			return oldConvertURL(file);
		}
		else
		{
			return oldConvertURL(getImagePath(file, "", getTheme()));
		}
	}

	public String oldConvertURL(String file)
	{
		String out = file.trim();

		if	((file.startsWith("http:")) || (file.startsWith("//")) || (file.length() > 2 && file.charAt(1) == ':'))
		{
			return out;
		}

		if	(file.startsWith("/"))
		{
			if (file.startsWith(getContextPath()))
				return out;
			return getContextPath() + out;
		}

		if	((staticContentBase.startsWith("http")) || (staticContentBase.length() > 2 && staticContentBase.charAt(1) == ':'))
		{
			return staticContentBase + out;
		}

		return getContextPath() + staticContentBase + out;
	}

        public String getMessage(String code, String language)
        {
            if (language==null || language.equals(""))
				return getMessage(code);
			else 
				return getMessage(code, language);
        }
        public String getMessage(String code)
        {
            String _language = getLanguage();
            _language = Application.getClientPreferences().getProperty("language|" + _language, "code", Application.getClientPreferences().getProperty("LANGUAGE", "eng"));
            String resourceName = "messages." + _language + ".txt";
            Messages msgs = com.genexus.Messages.getMessages(resourceName, Application.getClientLocalUtil().getLocale());
            return msgs.getMessage(code);
        }
        public String getLanguageProperty(String property)
        {
            String _language = getLanguage();
            return Application.getClientPreferences().getProperty("language|"+ _language, property, "");
        }
		
		public String getLanguage()
		{
			String deviceLanguage = AndroidContext.ApplicationContext.getLanguageName();
			if (deviceLanguage != null)
				return deviceLanguage;

			// Return the default language on failure.
			return Application.getClientPreferences().getProperty("LANG_NAME", "English");
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
            if (!language.isEmpty() && Application.getClientPreferences().getProperty("language|"+ language, "code", null) != null)
            {
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
            return contentType.toLowerCase().startsWith(contentKey.toLowerCase() + "/");
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
		
		public java.util.Date webcli2server(java.util.Date dt)
		{
            int tzServerOffset = Application.getClientPreferences().getOffsetStorageTimezone();
			if (CommonUtil.nullDate().equals(dt) || tzServerOffset == GX_NULL_TIMEZONEOFFSET)
				return dt;
			return CommonUtil.DateTimeFromUTC(dt);
		}

        public java.util.Date server2webcli(java.util.Date dt)
        {
            int tzServerOffset = Application.getClientPreferences().getOffsetStorageTimezone();
            if (CommonUtil.nullDate().equals(dt) || tzServerOffset == GX_NULL_TIMEZONEOFFSET)
                return dt;
            return com.genexus.GXutil.DateTimeToUTC(dt);
        }

        // in android offline , generated code works as rest service
        public boolean isRestService()
        { return true; }
    
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

		public String getClientId() {
			return "";
		}
}
