package com.genexus.internet;

import java.util.Hashtable;

import com.genexus.servlet.http.IHttpServletRequest;
import com.genexus.servlet.http.IHttpServletResponse;

import com.genexus.Application;
import com.genexus.GXutil;
import com.genexus.GxEjbContext;
import com.genexus.ModelContext;
import com.genexus.common.interfaces.IHttpContextNull;
import com.genexus.db.DBConnectionManager;
import com.genexus.webpanels.WebSession;

import json.org.json.JSONObject;

public class HttpContextNull extends HttpContext implements IHttpContextNull
{
       private HttpRequest httprequest;
       private HttpGXServletRequest request;
	   private WebSession webSession;
	   private String defaultPath = "";
	   private String contextPath = "";
	   private Hashtable<String, String> cookies;	   

	public HttpContext copy()
	{
		HttpContextNull o = new HttpContextNull();
		copyCommon(o);

		return o;
	}

	public HttpContextNull()
	{

		request = null;
		webSession = new WebSession(null);		
		httprequest = new HttpRequestNull(this);
		cookies = new Hashtable<String, String>();
	}

	public String getResourceRelative( String path)
	{
		return path;
	}

	public String getResourceRelative( String path, boolean includeBasePath)
	{
		return path;
	}
	
	public String getResource( String path)
	{
		return "";
	}

	public String getContextPath()
	{
		return contextPath;
	}

	public void setContextPath(String path) {this.contextPath = path;}

	public String getDefaultPath()
	{
		return defaultPath;
	}
	
	public void setDefaultPath(String path)
	{
		this.defaultPath = path;
	}

	public String GetNextPar()
	{
		throw new InternalError();
	}

	public String GetPar(String parameter)
	{
		throw new InternalError();
	}
	public String GetFirstPar(String parameter)
	{
		throw new InternalError();
	}

	public byte setHeader(String header, String value)
	{
		return 0;
	}

	public void setDateHeader(String header, int value)
	{
	}

	public void setRequestMethod(String method)
	{
	}
	public Hashtable getPostData()
	{
		throw new InternalError();
	}

	public String getRequestMethod()
	{
		return "";
	}

	public String getReferer()
	{
		return "";
	}

	public short setWrkSt(int handle, String wrkst)
	{
	  DBConnectionManager.getInstance().getUserInformation(handle).setProperty("WKST_NAME", wrkst.toUpperCase());
	  return 1;
	}

	public String getApplicationId(int handle)
	{
		return "";
	}
	public String getWorkstationId(int handle)
	{
	  String wrkstId = DBConnectionManager.getInstance().getUserInformation(handle).getProperty("WKST_NAME");

	  if	(wrkstId == null || wrkstId.equals(""))
	  {
		  return GXutil.wrkst();
	  }

	  return wrkstId;
	}

       public short setUserId(int handle, String user, String dataSource)
       {
	 DBConnectionManager.getInstance().getUserInformation(handle).setProperty("JAVA_USERID", user.toUpperCase());
	 return 1;
       }

	/** 
	* @deprecated use getUserId(String key, int handle, com.genexus.db.IDataStoreProvider dataStore);
	* */
	public String getUserId(String key, ModelContext context, int handle, String dataSource)
	{
          if (context.getSessionContext() != null) //Si estoy en el contexto de un EJB
             return ((GxEjbContext)context.getSessionContext()).getUserId();

		if	(key.toLowerCase().equals("server") &&  !Application.getUserIdServerAsUserId(handle))
		{
				return GXutil.userId(key, context, handle, dataSource);
		}

		String user = DBConnectionManager.getInstance().getUserInformation(handle).getProperty("JAVA_USERID");

		if	(user == null || user.length() == 0)
			return GXutil.userId("", context, handle, dataSource);

		return user;
	}

	public String getUserId(String key, ModelContext context, int handle, com.genexus.db.IDataStoreProvider dataStore)
	{
        if (context.getSessionContext() != null) //Si estoy en el contexto de un EJB
             return ((GxEjbContext)context.getSessionContext()).getUserId();

		if	(key.toLowerCase().equals("server") &&  !Application.getUserIdServerAsUserId(handle))
		{
				return GXutil.userId(key, context, handle, dataStore);
		}

		String user = DBConnectionManager.getInstance().getUserInformation(handle).getProperty("JAVA_USERID");

		if	(user == null || user.length() == 0)
			return GXutil.userId("", context, handle, dataStore);

		return user;
	}

	public String getRemoteAddr()
	{
		return "";
	}
	public boolean isLocalStorageSupported()
	{
		return false;
	}
	public boolean exposeMetadata()
	{
		return false;
	}

	public boolean isSmartDevice()
	{
		return false;
	}

	public int getBrowserType()
	{
		return 0;
	}

	public boolean isIE55()
	{
		return false;
	}

	public String getBrowserVersion()
	{
		return "";
	}

	public Object getSessionValue(String name)
	{
		return "";
	}

	// ---- Set values
	public void webPutSessionValue(String name, Object value)
	{
	}

	public void webPutSessionValue(String name, long value)
	{
	}

	public void webPutSessionValue(String name, double value)
	{
	}

	public void webSessionId(String[] id)
	{
	}

	public String webSessionId()
	{
		return "0";
	}

	public String getCookie(String name)
	{
		Object o = cookies.get(name);
		if (o != null) 
		{
			return (String)o;
		}
		return "";
	}
	public com.genexus.servlet.http.Cookie[] getCookies()
	{

		com.genexus.servlet.http.Cookie[] cookies = {};
		return cookies;
	}

	public byte setCookieRaw(String name, String value, String path, java.util.Date expiry, String domain, double secure)
	{
		return 0;
	}

	public byte setCookie(String name, String value, String path, java.util.Date expiry, String domain, double secure, Boolean httpOnly)
	{
		return 0;
	}

	public byte setCookie(String name, String value, String path, java.util.Date expiry, String domain, double secure)
	{
		cookies.put(name, value);
		return 0;
	}

	public String getServerName()
	{
          if(request != null)
                  return request.getServerName();

          return "";
	}

	public int getServerPort()
	{
          if	(request != null)
                  return request.getServerPort();

          return 80;
	}

	public String getScriptPath()
	{
          if	(request != null)
          {
                  String path = request.getRequestURI();
                  if	(path.startsWith("http"))
                          path = request.getServletPath();

                  int pos = path.lastIndexOf('/');

                  if	(pos >= 0)
                          return path.substring(0, pos + 1);

                  return path;
          }
          return "";
	}

	public int getHttpSecure()
	{
		return 0;
	}

	public byte setContentType(String type)
	{
		return 1;
	}

	public byte responseContentType (String type)
	{
		return 0;
	}


	public String getHeader(String header)
	{
		return "";
	}


	public void sendError(int error)
	{
	}

	public void setQueryString(String qs)
	{
	}

	public String getQueryString()
	{
		return "";
	}

	public String getPackage()
	{
		return "";
	}

	public String cgiGet(String parm)
	{
		return "";
	}

    public void changePostValue(String ctrl, String value)
    {
    }
    public void deletePostValue(String ctrl)
    {
    }
	public void DeletePostValuePrefix(String sPrefix)
	{
	}
    public void parseGXState(JSONObject tokenValues)
    {
    }
	public boolean isFileParm( String parm)
	{
		return false;
	}
	
	public IHttpServletRequest getRequest()
	{
		if (request == null)
			return null;
		else
			return request.getHttpServletRequest();
		//throw new InternalError();
	}

	public void setRequest(IHttpServletRequest request) {
		this.request = new HttpGXServletRequest();
		this.request.setHttpServletRequest(request);
	}

	@Override
	public void sendResponseStatus(int statusCode, String statusDescription){

	}
	public IHttpServletResponse getResponse()
	{
		throw new InternalError();
	}

	public HttpResponse getHttpResponse()
	{
		return null;
		//throw new InternalError();
	}

	public HttpRequest getHttpRequest()
	{
	       return httprequest;
	      //throw new InternalError();
	}

	public void setHttpRequest(HttpRequest httprequest)
	{
	      this.httprequest = httprequest;
	}


	public WebSession getWebSession()
	{
		return webSession;
	}

	public void redirect(String url) {}
	public void redirect(String url, boolean SkipPushUrl) {}
	public void popup(String url) {}
	public void popup(String url, Object[] returnParms) {}
	public void newWindow(com.genexus.webpanels.GXWindow win) {}
	public void ajax_rsp_command_close(){};
	public void dispatchAjaxCommands() {};
    public void closeHtmlHeader() {};
	public boolean getHtmlHeaderClosed() { return false; }

	public void setStream(){}
	public void flushStream(){}
	public String cgiGetFileName(String parm) {return "";}
	public String cgiGetFileType(String parm) {return "";}
	public void getMultimediaValue(String internalName, String[] blobVar, String[] uriVar) { blobVar[0] = ""; uriVar[0] = ""; }
	public void cleanup() {}
	public boolean isMultipartContent() { return false; }

	public boolean isHttpContextNull() {return true;}
	public boolean isHttpContextWeb() {return false;}
}
