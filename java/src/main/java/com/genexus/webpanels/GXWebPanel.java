package com.genexus.webpanels;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import com.genexus.*;
import com.genexus.db.UserInformation;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.internet.HttpContext;
import com.genexus.internet.IGxJSONSerializable;
import com.genexus.security.GXSecurityProvider;

import json.org.json.IJsonFormattable;
import json.org.json.JSONArray;
import json.org.json.JSONException;
import json.org.json.JSONObject;

class DataIntegrityException extends Exception
{
    public DataIntegrityException(String message)
    {
		super(message);
    }
}

public abstract class GXWebPanel extends GXWebObjectBase
{

	protected abstract void createObjects();
	public abstract void initialize();

	public void initialize_properties(){};

	public void initializeDynEvents(){};
	public boolean supportAjaxEvent(){ return false;};

	public String ajaxOnSessionTimeout(){ return "Ignore";};

	public void setPrefix(String prefix){};

	//protected MsgList GX_msglist;
	public static final ILogger logger = LogManager.getLogger(GXWebPanel.class);

	protected static boolean isStaticGeneration = false;
	protected boolean isStatic = false;
	protected static String staticDir;
	private Hashtable<String, String> eventsMetadata = new Hashtable<>();
	protected boolean fullAjaxMode = false;
	private Hashtable<String, String> callTargetsByObject = new Hashtable<String, String>();

	private static String GX_FULL_AJAX_REQUEST_HEADER = "X-FULL-AJAX-REQUEST";

	public GXWebPanel(HttpContext httpContext)
	{
		super(httpContext);
	}

	/**
	 * Constructor para los estáticos.
	 *
	 */

	public GXWebPanel(int remoteHandle, ModelContext context)
	{
		super(remoteHandle, context);
		ui.setAutoDisconnect(true);

		isStaticGeneration = true;
	}

	public void setFullAjaxMode()
	{
		fullAjaxMode = true;
	}

	public boolean isMasterPage()
	{
		return false;
	}


	public boolean isFullAjaxMode()
	{
		return fullAjaxMode;
	}

	public boolean isAjaxCallMode()
	{
		return httpContext.isAjaxCallMode();
	}

	public void addString(String value) {
		httpContext.GX_webresponse.addString(value);
	}

	private static String IMPL_CLASS_SUFFIX = "_impl";

	public GXMasterPage createMasterPage(int remoteHandle, String fullClassName) {
		if (fullClassName.equals("(none)")) // none Master Page
		{
			return new NoneMasterPage((HttpContext) this.context.getHttpContext());
		}

		if (fullClassName.equals("(default)")) // Is the default
		{
			String masterPage = this.context.getPreferences().getProperty("MasterPage", "");
			if (masterPage.isEmpty())
			{
				logger.error("The default master page is not present on the client.cfg file, please add the MasterPage key to the client.cfg.");
				return null;
			}
			String namespace = this.context.getPreferences().getProperty("NAME_SPACE", "");
			fullClassName = namespace.isEmpty() ? masterPage.toLowerCase() + IMPL_CLASS_SUFFIX : namespace.trim() + "." + masterPage.toLowerCase() + IMPL_CLASS_SUFFIX;
		}

		fullClassName = fullClassName + IMPL_CLASS_SUFFIX;

		try {
			Class<?> masterPageClass = Class.forName(fullClassName);
			return (GXMasterPage) masterPageClass.getConstructor(new Class<?>[] {int.class, ModelContext.class}).newInstance(remoteHandle, context.copy());
		} catch (ClassNotFoundException e) {
			logger.error("MasterPage class not found: " + fullClassName, e);
			throw new RuntimeException(e);
		} catch (Exception e) {
			logger.error("MasterPage could not be loaded: " + fullClassName, e);
			throw new RuntimeException(e);
		}
	}


	protected Object dyncall(String MethodName)
	{
		Method m = getMethod(getClass(), MethodName);
		if (m == null) {
			logger.error("DynCall - Method not found: " + MethodName);
			return null;
		}

		Class[] pars = m.getParameterTypes();
		int ParmsCount = pars.length;
		Object[] convertedparms = new Object[ParmsCount];

		for (int i = 0; i < ParmsCount; i++) {
			convertedparms[i] = convertparm(pars, i, WebUtils.decryptParm(httpContext.GetNextPar(), ""));
		}

		try
		{
			return m.invoke(this, convertedparms);
		}
		catch (java.lang.Exception e)
		{
			logger.error("DynCall - Invoke error " + MethodName, e);
		}

		return null;
	}

        protected Object convertparm(Class<?>[] pars, int i, Object value) {
            try {
				String parmTypeName = pars[i].getName();
                if (parmTypeName.equals("java.util.Date"))
                {
                    String strVal = value.toString();
                    if (strVal.length() > 8)
                        return localUtil.parseDTimeParm(strVal);
                    else
                        return localUtil.parseDateParm(strVal);
                }
                if(IGxJSONSerializable.class.isAssignableFrom(pars[i]))
                {
                    IGxJSONSerializable parmObj = null;
                    if(com.genexus.xml.GXXMLSerializable.class.isAssignableFrom(pars[i]))
                    {
                        parmObj = (com.genexus.xml.GXXMLSerializable)pars[i].getConstructor(new Class<?>[] { ModelContext.class }).newInstance(new Object[] { context });
                    }
                    else
                    {
                        parmObj = (IGxJSONSerializable)pars[i].getConstructor(new Class<?>[] {}).newInstance(new Object[] {});
                    }
                    parmObj.fromJSonString(value.toString());
                    return parmObj;
                }
				//Control Properties values (ServerSide: int, clientSide: bool)
				if ((parmTypeName.equals("int") || parmTypeName.equals("java.lang.Integer")) && value != null)
				{
					if (value.toString().equalsIgnoreCase("true"))
						return 1;
					if (value.toString().equalsIgnoreCase("false"))
						return 0;
				}

                return com.genexus.GXutil.convertObjectTo(value, pars[i], false);
            } catch (Exception e)
            {
                return value;
            }
        }

	protected void setEventMetadata(String eventName, String metadata) {
		if (eventsMetadata.containsKey(eventName))
			eventsMetadata.put(eventName,  eventsMetadata.get(eventName) + metadata);
		else
			eventsMetadata.put(eventName, metadata);
	}

	protected void initState(ModelContext context, UserInformation ui)
	{
		super.initState(context, ui);

		staticDir	   = context.getClientPreferences().getWEB_STATIC_DIR();
		//GX_msglist     = context.getHttpContext().GX_msglist;

		createObjects();
		initialize();
		httpContext.setStream();

	}

	public static byte isStaticGeneration()
	{
		return isStaticGeneration?(byte)1:0;
	}

	public static boolean getStaticGeneration()
	{
		return isStaticGeneration;
	}

	public static String prefixURL(String file)
	{
		// Esto solo se llama cuando tengo un link a un webpanel estático.
		String out = file.trim();

		if	(isStaticGeneration)
		{
			return out;
		}

		return staticDir + out;
	}

	public String getresponse(String a)
	{
		return "no content";
	}

	protected void createFile(String fileName)
	{
	  	try
	  	{
			((HttpContext)context.getHttpContext()).setOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
	  	}
	  	catch (IOException e)
	  	{
			throw new RuntimeException(e.getMessage());
	  	}
	}

	/*
	* Los metodos que vienen ahora son los que permiten ser un 'WebComponent Container'
	*/

	public void setparameters(Object[] parms)
	{
		executeMethod("setparameters", parms);
	}

	/**
	*	El mecanismo para esto es medio horrible:
	*
	*		- busco un Method que se llame como methodName
	*		- convierto los parametros del Object[] a los parametros que espera methodName
	*
	*	Sin esto no hay forma de poder hacer los componentes 'dinamicos' y que no sean del mismo
	* 	tipo los parametros.
	*/

	protected void executeMethod(String methodName, Object[] parms)
	{
		try
		{
			Method m = getMethod(getClass(), methodName);
			Class[] parmTypes = m.getParameterTypes();

			for (int i = 0; i < parms.length; i++)
			{
				mapClass(parmTypes[i], i, parms);
			}

			m.invoke(this, parms);
		}
		catch (Exception e)
		{
			throw new GXRuntimeException(e);
		}
	}

	/**
	*	Me devuelve un metodo de la clase source que se llama name, y no es de esta clase (GXWebComponent)
	*/

	private static Method getMethod(Class source, String name)
	{
		Method[] m = source.getMethods();
		Method lastFound = null;
		for (int i = 0 ; i < m.length; i++)
		{
			if (m[i].getName().equals(name))
			{
				lastFound = m[i];
			}
			// Esta chanchada es porque hay dos metodos con el mismo nombre, uno el de la superclase, que tiene
			// Object[] como parametro y otro el de la implementacion, que tienelos parametros reales
			if 	(
					m[i].getName().equals(name) &&
					!(m[i].getDeclaringClass().getName().equals(com.genexus.webpanels.GXWebComponent.class.getName())) &&
					!(m[i].getDeclaringClass().getName().equals(com.genexus.webpanels.GXWebPanel.class.getName()))
				)
			{
				return m[i];
			}
		}
		return lastFound;
	}

	private static void mapClass(Class dest, int idx, Object[] parms)
	{
		if	(dest.equals(byte.class))
		{
			parms[idx] = new Byte( ((Number) parms[idx]).byteValue());
		}
		else if	(dest.equals(short.class))
		{
			parms[idx] = new Short( ((Number) parms[idx]).shortValue());
		}
		else if	(dest.equals(int.class))
		{
			parms[idx] = new Integer( ((Number) parms[idx]).intValue());
		}
		else if	(dest.equals(float.class))
		{
			parms[idx] = new Float( ((Number) parms[idx]).floatValue());
		}
		else if	(dest.equals(double.class))
		{
			parms[idx] = new Double( ((Number) parms[idx]).doubleValue());
		}
		else if	(dest.equals(long.class))
		{
			parms[idx] = new Long( ((Number) parms[idx]).longValue());
		}
	}

	public void DeleteReferer(String popupLevel)
	{
		httpContext.deleteReferer(popupLevel);
	}

	protected boolean IsAuthorized(String permissionPrefix)
	{
		boolean[] flag = new boolean[]{false};
		boolean[] permissionFlag = new boolean[]{false};
		String reqUrl = httpContext.getRequest().getRequestURL().toString();
		String queryString = httpContext.getRequest().getQueryString();
		if (queryString != null)
		{
			reqUrl += "?"+queryString;
		}
		GXSecurityProvider.getInstance().checksessionprm(-2, context, reqUrl, permissionPrefix, flag, permissionFlag);
		if (permissionFlag[0])
		{
			return true;
		}
		else
		{
			if (flag[0])
			{
				return false;
			}
			else
			{
				String loginObject = Application.getClientContext().getClientPreferences().getProperty("IntegratedSecurityLoginWeb", "");
				httpContext.redirect(formatLink(GXutil.getClassName(loginObject)));
				return false;
			}
		}
	}

	private boolean isFullAjaxRequest()
	{
		String contentType = httpContext.getRequest().getContentType();
		String fullAjaxReqHeader = httpContext.getHeader(GX_FULL_AJAX_REQUEST_HEADER);
		boolean supportAjaxEvent = supportAjaxEvent() || fullAjaxReqHeader.compareTo("1") == 0;
		boolean fullAjaxRequest = httpContext.isGxAjaxRequest() && supportAjaxEvent && contentType!=null && contentType.contains("application/json");
		String ajaxEventData = httpContext.cgiGet(GX_AJAX_MULTIPART_ID);
		fullAjaxRequest = fullAjaxRequest || (httpContext.isMultipartContent() && ajaxEventData!= null && !ajaxEventData.equals(""));
		return fullAjaxRequest;
	}

	public void webExecuteEx() throws Exception
	{
		if (isFullAjaxRequest())
		{
			boolean sessionExpired = ajaxOnSessionTimeout().equalsIgnoreCase("Warn") && WebSession.isSessionExpired(httpContext.getRequest());
			if (sessionExpired)
			{
				httpContext.sendResponseStatus(440, "Session Timeout");
			}
			else
			{
				try {
					webAjaxEvent();
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}
			}
		}
		else
			webExecute();
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
				webExecuteEx();
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

	public void webAjaxEvent() throws Exception {
		httpContext.setAjaxCallMode();
		httpContext.setFullAjaxMode();
		DynAjaxEvent dynAjaxEvent = new DynAjaxEvent(httpContext.getDynAjaxEventContext());
		String jsonRequest;
		if (httpContext.isMultipartContent())
			jsonRequest = httpContext.cgiGet(GX_AJAX_MULTIPART_ID);
		else
			jsonRequest = httpContext.getHttpRequest().getString();
		String jsonResponse = dynAjaxEvent.invoke(jsonRequest, this);

		Boolean isMultipartRequest = httpContext.isMultipartContent();

		if (isMultipartRequest) {
			httpContext.setContentType("text/html");
		}
		else
			httpContext.setContentType("application/json");

		if (!redirect((HttpContextWeb) httpContext)){
			((HttpContextWeb)httpContext).sendFinalJSONResponse(jsonResponse);
		}
		httpContext.setResponseCommited();
	}

	private boolean redirect(HttpContextWeb context)
	{
		if (context.wjLoc!=null && !context.wjLoc.equals("") )
		{
			context.redirect( context.wjLoc );
			context.dispatchAjaxCommands();
			return true ;
		}
		else if ( context.nUserReturn == 1 )
		{
			context.ajax_rsp_command_close();
			context.dispatchAjaxCommands();
			return true;
		}
		return false;
	}

	public interface IDynAjaxEventContext
	{
		void Clear();
		void ClearParmsMetadata();
		boolean isInputParm(String key);
		void SetParmHash(String fieldName, Object value);
		boolean isParmModified(String fieldName, Object value);

	}

	protected class DynAjaxEvent {
		JSONArray events = new JSONArray();
		GXWebPanel targetObj;
		String[] eventHandlers;
		boolean[] eventUseInternalParms;
		String cmpContext = "";
		int grid;
		String row;
		String pRow = "";
		boolean anyError;
		JSONArray inParmsValues = new JSONArray();
		JSONArray inHashValues = new JSONArray();
		DynAjaxEventContext dynAjaxEventContext;
		DynAjaxEvent( DynAjaxEventContext dynAjaxEventContext)
		{
			this.dynAjaxEventContext = dynAjaxEventContext;
		}

		private void parseInputJSonMessage(String jsonMessage, GXWebPanel targetObj) throws JSONException {
			try {
				JSONObject objMessage = new JSONObject(jsonMessage);
				if (objMessage.has("parms"))
					inParmsValues = objMessage.getJSONArray("parms");
				if (objMessage.has("hsh"))
					inHashValues = objMessage.getJSONArray("hsh");
				if (objMessage.has("events"))
					events = objMessage.getJSONArray("events");
				if (objMessage.has("cmpCtx"))
					cmpContext = objMessage.getString("cmpCtx");
				this.targetObj = targetObj;
				String pckgName = (objMessage.has("pkgName") && objMessage.getString("pkgName").length() > 0) ? objMessage.getString("pkgName") + "." : "";
				if (objMessage.has("MPage") && objMessage.getBoolean("MPage")) {
					if (objMessage.has("objClass")) {
						String fullClassName = pckgName + objMessage.getString("objClass") + "_impl";
						Class<?> webComponentClass = targetObj.getClass().forName(fullClassName);
						GXWebPanel webComponent = (GXWebPanel) webComponentClass.getConstructor(new Class<?>[]{int.class, ModelContext.class}).newInstance(new Object[]{new Integer(remoteHandle), context});
						this.targetObj = webComponent;
					}
				} else {
					if (!cmpContext.equals("") && objMessage.has("objClass")) {
						String fullClassName = pckgName + objMessage.getString("objClass") + "_impl";
						GXWebComponent webComponent = WebUtils.getWebComponent(getClass(), fullClassName, remoteHandle, context);
						this.targetObj = webComponent;
					}
				}
				if (objMessage.has("grids"))
					parseGridsDataParms((JSONObject) objMessage.get("grids"));
				if (objMessage.has("grid"))
					grid = objMessage.getInt("grid");
				else
					grid = 0;
				if (objMessage.has("row"))
					row = objMessage.getString("row");
				else
					row = "";
				if (objMessage.has("pRow"))
					pRow = objMessage.getString("pRow");
				if (objMessage.has("gxstate")) {
					parseGXStateParms(objMessage.getJSONObject("gxstate"));
				}
				if (objMessage.has("fullPost")) {
					parseGXStateParms(objMessage.getJSONObject("fullPost"));
				}
			} catch (Exception e) {
				e.printStackTrace();
				this.targetObj = new GXWebComponentNull(remoteHandle, context);
			}
		}

		private void parseGridsDataParms(JSONObject gxGrids)
		{
			JSONArray gridNames = gxGrids.names();
			if (gridNames != null)
			{
				for (int i = 0; i < gridNames.length(); i++) {
					try {
						JSONObject grid = (JSONObject)gxGrids.get(gridNames.getString(i));
						if (grid.getInt("id") != 0 && !grid.getString("lastRow").equals(""))
						{
							int lastRow = grid.getInt("lastRow") + 1;
							try{
								SetFieldValue("sGXsfl_" + grid.getString("id") + "_idx", String.format("%04d", lastRow) + pRow);
								SetFieldValue("nGXsfl_" + grid.getString("id") + "_idx", String.valueOf(lastRow));
							}
							catch(Exception ex1)
							{
								logger.error("Error parseGridsDataParms row parameter ", ex1);
							}
						}
					}
					catch (JSONException e) {
						System.out.println(e.getMessage());
					}
				}
			}
		}

		private void parseGXStateParms(JSONObject gxState) {
			JSONArray names = gxState.names();
			if (names != null)
			{
				for (int i = 0; i < names.length(); i++) {
					String key;
					try {
						key = names.getString(i);
						String value = gxState.getString(key);
						if ((!targetObj.httpContext.isFileParm( key)) && (!value.equals(""))) {
							targetObj.httpContext.changePostValue(key, value);
						}
					} catch (JSONException e) {
						System.out.println(e.getMessage());
					}
				}
			}
		}

		private String buildOutputJSonMessage() {
			return targetObj.httpContext.getJSONResponse(cmpContext);
		}

        private boolean IsInternalParm(JSONObject parm)
        {
            return parm.has("sPrefix") || parm.has("sSFPrefix") || parm.has("sCompEvt");
        }

		private void addParmsMetadata(JSONObject inputParm, JSONArray ParmsList, HashSet<String> ParmsListHash) throws JSONException {
			String key = "";
			if (inputParm.has("av") && inputParm.has("ctrl") && inputParm.has("prop"))
			{
				key = inputParm.getString("av") + inputParm.getString("ctrl") + inputParm.getString("prop");
			}
			else if (inputParm.has("av"))
			{
				key = inputParm.getString("av");
			}
			else if (inputParm.has("ctrl") && inputParm.has("prop"))
			{
				key = inputParm.getString("ctrl") + inputParm.getString("prop");
			}
			else if (inputParm.has("ctrl"))
			{
				key = inputParm.getString("ctrl");
			}
			if (key==null || key.equals("") || !ParmsListHash.contains(key)) {
				ParmsList.put(inputParm);
				if (key!=null && !key.equals("")) {
					ParmsListHash.add(key);
				}
			}
		}

		private void parseMetadata() {
			try {
				dynAjaxEventContext.ClearParmsMetadata();
				eventHandlers = new String[events.length()];
                eventUseInternalParms = new boolean[events.length()];
				int eventCount = 0;
				for (int i=0; i< events.length(); i++ )
				{
					String eventName = events.getString(i);
					JSONObject eventMetadata = new JSONObject(targetObj.eventsMetadata.get(eventName));
					eventHandlers[eventCount] = eventMetadata.getString("handler");
					JSONArray eventInputParms = eventMetadata.getJSONArray("iparms");
					for (int j=0; j< eventInputParms.length(); j++ )
					{
						addParmsMetadata(eventInputParms.getJSONObject(j), dynAjaxEventContext.inParmsMetadata, dynAjaxEventContext.inParmsMetadataHash);
                        eventUseInternalParms[eventCount] = eventUseInternalParms[eventCount] || IsInternalParm(eventInputParms.getJSONObject(j));
					}
					JSONArray eventOutputParms = eventMetadata.getJSONArray("oparms");
					for (int j = 0; j < eventOutputParms.length(); j++) {
						addParmsMetadata(eventOutputParms.getJSONObject(j), dynAjaxEventContext.outParmsMetadata, dynAjaxEventContext.outParmsMetadataHash);
					}
					eventCount++;
				}
			} catch (Exception ex) {
				logger.error("Failed to parse event metadata", ex);
				anyError = true;
			}
		}

		private void SetNullableScalarOrCollectionValue(JSONObject parm, Object value, JSONArray columnValues) throws JSONException, Exception
		{
			String nullableAttribute = parm.optString("nullAv", null);
			if (nullableAttribute != null && value.toString().length() == 0)
			{
				SetScalarOrCollectionValue(nullableAttribute, null, true, null);
			}
			else
			{
				SetScalarOrCollectionValue(parm.has("av") ? parm.getString("av") : null, parm.has("prop") ? parm.getString("prop") : null, value, columnValues);
			}
		}

        private void SetScalarOrCollectionValue(String fieldName, String propName, Object value, JSONArray values) throws JSONException, Exception
        {
			Field field = fieldName == null ? null : PrivateUtilities.getField(targetObj, fieldName);
			Object targetVar = targetObj;
            if (field == null)
            {
				targetVar = PrivateUtilities.getFieldTarget(targetObj, fieldName);
				if (targetVar != null && propName != null)
				{
					field = PrivateUtilities.getField(targetVar, propName);
					fieldName = propName;
				}
			}
            if (field != null)
            {
				Class fieldType = field.getType();
				if (fieldType != null)
				{
					if ( fieldType.getSuperclass() == GXBaseCollection.class || fieldType.getSuperclass() == GXSimpleCollection.class || fieldType.getSuperclass() == java.util.Vector.class)
						SetCollectionFieldValue(fieldName, values);
					else
						SetFieldValue( targetVar, fieldName, value);
				}
			}
        }

        Object getFieldValue( String fieldName, String propName) throws Exception
        {
			Field field = fieldName == null ? null : PrivateUtilities.getField(targetObj, fieldName);
			Object targetVar = targetObj;
            if (field == null)
            {
				targetVar = PrivateUtilities.getFieldTarget(targetObj, fieldName);
				if (targetVar != null && propName != null)
				{
					field = PrivateUtilities.getField(targetVar, propName);
					fieldName = propName;
				}
			}
			if (field != null)
			{
				field.setAccessible(true);
				return field.get(targetVar);
			}
			return null;

        }

        private void SetCollectionFieldValue(String fieldName, JSONArray values)  throws JSONException, Exception
        {
			Field field = PrivateUtilities.getField(targetObj, fieldName);
            if (field != null)
            {
				Class[] cArg = new Class[1];
				cArg[0] = IJsonFormattable.class;
   				Method mth = field.getType().getMethod("FromJSONObject", cArg);
                if (mth != null)
                {
                	Object fieldInstance = PrivateUtilities.getFieldValue(targetObj, fieldName);
                	mth.invoke(fieldInstance , new Object[]{values});
                }
            }
        }

		private void SetFieldValue(String fieldName, Object value) throws JSONException, Exception {
			SetFieldValue( targetObj, fieldName, value);
		}

		private void SetFieldValue(Object targetObj, String fieldName, Object value) throws JSONException, Exception {
			Field field = PrivateUtilities.getField(targetObj, fieldName);
			if (field != null)
            {
				Class fieldType = field.getType();
				if (IGxJSONSerializable.class.isAssignableFrom(field.getType()))
				{
					Class[] cArg = new Class[1];
					cArg[0] = String.class;
					Object fieldInstance = PrivateUtilities.getFieldValue(targetObj, fieldName);
					Method mth;
					if (value instanceof IJsonFormattable)
					{
						mth = field.getType().getMethod("FromJSONObject", new Class[]{IJsonFormattable.class});
						mth.invoke(fieldInstance , new Object[]{value});
					}
					else
					{
						mth = field.getType().getMethod("fromJSonString", cArg);
						mth.invoke(fieldInstance , new Object[]{value.toString()});
					}

					PrivateUtilities.setFieldValue(targetObj, field.getName(), fieldInstance);
				}
				else
				{
					try
					{
						if (fieldType.isArray())
						{
							Object tempArray = getArrayFieldValue(fieldType, value);
							if (tempArray != null)
							{
								value = tempArray;
							}
						}
						else {
							if (fieldType == java.util.Date.class)
								value = localUtil.ctot(value.toString(), 0);
							else
								value = GXutil.convertObjectTo(value, fieldType);
						}
						PrivateUtilities.setFieldValue(targetObj, field.getName(), value);
					}
					catch (Exception e)
					{
					}
				}
			}
		}

		private Object getArrayFieldValue(Class fieldType, Object value) throws JSONException, Exception {
			if (value instanceof JSONArray)
			{
				JSONArray jArray = (JSONArray)value;
				int len = jArray.length();
				if (len > 0)
				{
					Object returnArray = Array.newInstance(fieldType.getComponentType(), len);
					for (int i=0; i<len; i++)
					{
						Object itemValue;
						if (jArray.get(i) instanceof JSONArray)
						{
							itemValue = getArrayFieldValue(fieldType.getComponentType(), jArray.get(i));
						}
						else
						{
							itemValue = GXutil.convertObjectTo(jArray.get(i), fieldType.getComponentType());
						}
						Array.set(returnArray, i, itemValue);
					}
					return returnArray;
				}
			}

			return null;
		}

		private void initializeOutParms() throws JSONException {
			dynAjaxEventContext.Clear();
			for (int j = 0; j < dynAjaxEventContext.outParmsMetadata.length(); j++) {
				JSONObject parm = dynAjaxEventContext.outParmsMetadata.getJSONObject(j);
				String parmName = "";
				if (parm.has("av")) {
					parmName = parm.getString("av");
					if (!parmName.isEmpty()) {
						Object TypedValue = null;
						try {
							TypedValue = getFieldValue(parmName, parmName);
							dynAjaxEventContext.SetParmHash(parmName, TypedValue);
							if (!dynAjaxEventContext.isInputParm(parmName) && TypedValue instanceof IGXAssigned) {
								((IGXAssigned) TypedValue).setIsAssigned(false);
							}
						} catch (java.lang.Exception e) {
							logger.error(String.format("initializeOutParms param:'%s'", parmName, e));
						}
					}
				}
			}
		}

		private Object[] beforeInvoke() throws JSONException, Exception {
			ArrayList<Object> MethodParms = new ArrayList<Object>();
			int hash_i = 0;
            int parm_i = 0;
			int hashValuesLen = inHashValues.length();
			if (!anyError) {
				int nParm = 0;
				int len = dynAjaxEventContext.inParmsMetadata.length();
				boolean multipart = targetObj.httpContext.isMultipartContent();
				for (int i = 0; i < len; i++) {
					JSONObject parm = (JSONObject) dynAjaxEventContext.inParmsMetadata.getJSONObject(i);
					try{
						if (parm.has("postForm"))
						{
						}
						else
						{
							Object value = inParmsValues.get(nParm);
							JSONObject jValue = inParmsValues.optJSONObject(nParm);
							JSONArray allCollData = inParmsValues.optJSONArray(nParm);
							nParm++;
							if (multipart)
							{
								String fld = null;
								if (parm.has("fld"))
									fld = String.format("%s%s", cmpContext, parm.getString("fld"));

								if ((value instanceof String) && (value==null || value.equals("")) && fld!=null && !targetObj.httpContext.cgiGetFileName(fld).equals(""))
									value = targetObj.httpContext.cgiGet(fld);
							}

							if (IsInternalParm(parm))
							{
								MethodParms.add(value);
							}
							else
							{
								JSONArray columnValues = new JSONArray();
								JSONArray hashValues = new JSONArray();
								JSONArray hideCodeValues;
                                String picture = parm.has("pic") ? parm.getString("pic") : "";
								if (parm.has("grid"))
								{
									String parentRow = "";
									//Case for each line command or collection based grid
									int colDataLen = (allCollData == null) ? 0 : allCollData.length();
									for (int k = 0; k < colDataLen; k++) {
										JSONObject columnData = (JSONObject)allCollData.get(k);
										parentRow = (String)columnData.get("pRow");
										columnValues = (JSONArray)columnData.get("c");
										if (columnData.has("hsh")) {
											hashValues = (JSONArray)columnData.get("hsh");
										}
										hashValuesLen = hashValues.length();
										value = columnData.get("v");
										int rowIdx = 1;
										int colValuesLen = columnValues.length();
										String strValue;
										Object objValue;
										for (int j = 0; j < colValuesLen; j++) {
											String varName = String.format("%s%s_%s%s", cmpContext, (String)parm.get("fld"), String.format("%04d", rowIdx), parentRow);
											objValue = columnValues.get(j);
											if (objValue.getClass() == Double.class)
											{
												DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(localUtil.getLocale());
												df.setMaximumFractionDigits(Integer.MAX_VALUE);
												strValue = df.format(objValue);
											}
											else
											{
												strValue = columnValues.getString(j);
											}
											targetObj.httpContext.changePostValue(varName, strValue);
											rowIdx++;
										}
                                       if (parm.has("hsh"))
                                       {
											try {
												rowIdx = 1;
												for (int j = 0; j < hashValuesLen; j++) {
													String hashName = String.format("%sgxhash_%s_%s%s", cmpContext, (String)parm.get("fld"), String.format("%04d", rowIdx), parentRow);
													String sRow = String.format("%s%s", String.format("%04d", rowIdx), parentRow);
													String columnHash = hashValues.getString(j);
													targetObj.httpContext.changePostValue(hashName, columnHash);
													if (httpContext.useSecurityTokenValidation()) {
														SetScalarOrCollectionValue(parm.has("av") ? parm.getString("av") : null, parm.has("prop") ? parm.getString("prop") : null, columnValues.get(j), columnValues);
													    Object TypedValue = getFieldValue(parm.has("av") ? parm.getString("av") : null, parm.has("prop") ? parm.getString("prop") : null);
														checkParmIntegrity(TypedValue, columnHash, sRow, dynAjaxEventContext.inParmsMetadata.get(parm_i), hash_i, picture);
													}
													rowIdx++;
												}
											}
                                            catch (Exception ex)
                                            {
                                                anyError = true;
												logger.error("Failed checkParmsIntegrity 403 Forbidden action Exception", ex);
												httpContext.sendResponseStatus(403, "Forbidden action");
												throw new DataIntegrityException("Failed checkParmIntegrity 403 Forbidden action Exception");
                                            }
										}
										if (columnData.has("hc"))
										{
											hideCodeValues = (JSONArray)columnData.get("hc");
											rowIdx = 1;
											int hideCodeValuesLen = hideCodeValues.length();
											for (int j = 0; j < hideCodeValuesLen; j++) {
												String varName = String.format("%sGXHC%s_%s%s", cmpContext, (String)parm.get("fld"), String.format("%04d", rowIdx), parentRow);
												objValue = hideCodeValues.get(j);
												if (objValue.getClass() == Double.class)
												{
													DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(localUtil.getLocale());
													df.setMaximumFractionDigits(Integer.MAX_VALUE);
													strValue = df.format(objValue);
												}
												else
												{
													strValue = hideCodeValues.getString(j);
												}
												targetObj.httpContext.changePostValue(varName, strValue);
												rowIdx++;
											}
										}
										int gridId = parm.getInt("grid");
										String pRowRCSuffix = (!parentRow.equals("")) ? String.format("_%s", parentRow) : "";
										targetObj.httpContext.changePostValue(String.format("%snRC_GXsfl_%s%s",cmpContext, new Integer(gridId).toString(), pRowRCSuffix), new Integer(columnValues.length()).toString());
									}
									if (parm.has("prop") && parm.getString("prop").equals("GridRC"))
									{
			                            String sRC = "";
                                        String rowsufix = "";
										String varname = "";
                                        if (jValue != null)
                                        {
											sRC = jValue.optString("gridRC","");
                                            rowsufix = jValue.optString("rowSuffix","");
											varname = parm.optString("av");
                                        }
										targetObj.httpContext.changePostValue(String.format("%s%s%s",cmpContext, varname, rowsufix), sRC);
										value = null;
									}
								}
								else {
									columnValues = (value instanceof JSONArray ? (JSONArray)value : null);
									if (parm.has("hsh") && httpContext.useSecurityTokenValidation()) {
										try {
											JSONObject hashObj = (JSONObject)((hash_i < inHashValues.length()) ? inHashValues.get(hash_i) : new JSONObject());
											String sRow = "";
											String hash = "";
											try {
												sRow = hashObj.has("row") ? hashObj.getString("row") : "";
												hash = hashObj.has("hsh") ? hashObj.getString("hsh") : "";
											}
											catch (JSONException e) {
												System.out.println(e.getMessage());
											}
											SetScalarOrCollectionValue(parm.has("av") ? parm.getString("av") : null, parm.has("prop") ? parm.getString("prop") : null, value, columnValues);
											Object TypedValue = getFieldValue(parm.has("av") ? parm.getString("av") : null, parm.has("prop") ? parm.getString("prop") : null);
											checkParmIntegrity(TypedValue, hash, sRow, dynAjaxEventContext.inParmsMetadata.get(parm_i), hash_i, picture);
										}
                                        catch (Exception ex)
                                        {
                                            anyError = true;

											logger.error("Failed checkParmsIntegrity 403 Forbidden action Exception", ex);
											httpContext.sendResponseStatus(403, "Forbidden action");
											throw new DataIntegrityException("Failed checkParmIntegrity 403 Forbidden action Exception");
                                        }
                                    }
									if (parm.has("hsh"))
									{
										hash_i++;
									}
								}
								if (value != null)
								{
									SetNullableScalarOrCollectionValue(parm, value, columnValues);
								}
							}
						}
					}
                    catch (DataIntegrityException ex)
                    {
	                    anyError = true;
						break;
                    }
					catch(Exception ex)
					{
						logger.error("Error DynAjaxEvent parameter: " + (parm.has("av") ? parm.getString("av") : "null") + "  beforeInvoke " + ex.getMessage(), ex);
					}
                    if (!parm.has("postForm"))
                    {
                        parm_i++;
                    }
				}
				if (grid != 0 && row!=null && !row.equals(""))
				{
					try{
						SetFieldValue("sGXsfl_" + new Integer(grid).toString() + "_idx", row);
						SetFieldValue("nGXsfl_" + new Integer(grid).toString() + "_idx", row);
					}
					catch(Exception ex1)
					{
						logger.error("Error DynAjaxEvent grid and row parameter ", ex1);
					}
				}
				SetFieldValue("wbLoad", true);
			}
			initializeOutParms();
			return MethodParms.toArray();
		}

        private void checkParmIntegrity(Object parm, String jwt, String sRow, Object inParmMetadata, int hash_i, String picture) throws DataIntegrityException
        {
		    String sContext = targetObj.isMasterPage() ? "gxmpage_" : cmpContext;
            if (!targetObj.verifySecureSignedToken(sContext + sRow, parm, picture, jwt) && !targetObj.verifySecureSignedToken(sContext, parm, picture, jwt))
            {
				logger.error("Failed checkParmIntegrity 403 Forbidden action with parm:" + inParmMetadata);
				logger.error(String.format("ParmValue: '%s' [%s]", parm.toString(), parm.getClass().toString()));
				logger.error("row:" + sRow);
				logger.error("hash_i:" + hash_i + " inHashValues.Length:" + inHashValues.length());
				logger.error("Received jwt:" + jwt);
				httpContext.sendResponseStatus(403, "Forbidden action");
                throw new DataIntegrityException("Failed checkParmIntegrity 403 Forbidden action with parm:" + inParmMetadata);
            }
        }

		private void afterInvoke() {
			this.targetObj.httpContext.AddStylesheetsToLoad();
			this.targetObj.httpContext.SendComponentObjects();
		}

		private void doInvoke(Object [] MethodParms) throws IllegalArgumentException,
				IllegalAccessException, InvocationTargetException, Exception {
			if (!anyError){
				for (int i=0; i< eventHandlers.length; i++ )
				{
					String handler = eventHandlers[i];
					Method mth = getMethod(targetObj.getClass(), handler);
					try{
						if (i > 0)
						{
							targetObj.PrepareForReuse();
						}
						mth.invoke(targetObj, (eventUseInternalParms[i] ? MethodParms : null));

					}catch(Exception iex)
					{
						logger.error("Error doInvoke class: " + targetObj.getClass() + " handler:" + handler + " exMessage:" , iex);
						throw iex;
					}
				}
			}
		}

		public String invoke(String JsonMessage, GXWebPanel targetObj)
				throws Exception {
			parseInputJSonMessage(JsonMessage, targetObj);
  			this.targetObj.setFullAjaxMode();
			this.targetObj.createObjects();
			this.targetObj.initialize();
			this.targetObj.initializeDynEvents();
			this.targetObj.setPrefix(cmpContext);
			this.targetObj.initialize_properties();
			this.targetObj.httpContext.setAjaxEventMode();
			this.targetObj.httpContext.ajax_rsp_clear();
			this.targetObj.httpContext.initClientId();
			String response = "";

			boolean iSecEnabled = this.targetObj.IntegratedSecurityEnabled();
			if (this.targetObj.validateObjectAccess(cmpContext) && (iSecEnabled ? this.targetObj.CheckCmpSecurityAccess(): true))
			{
				parseMetadata();
				doInvoke(beforeInvoke());
				afterInvoke();
				response = buildOutputJSonMessage();
			}
			this.targetObj.cleanup();
			if (!this.targetObj.isMasterPage() && this.targetObj != targetObj)
			{
				targetObj.cleanup();//CloseConnection en Webpanel entry point cuando this.targetObj es un webcomponent.
			}
			return response;
		}
	}

	protected void setCallTarget(String objClass, String target)
	{
		callTargetsByObject.put(objClass.toLowerCase().replaceAll("\\\\", "."), target.toLowerCase());
	}

	public void PrepareForReuse() {
		httpContext.ClearJavascriptSources();

	}
	private String getCallTargetFromUrl(String url)
	{
		int queryStringPos = url.indexOf('?');
		int slashPos = url.lastIndexOf('/');
		if (queryStringPos > 0)
			slashPos = url.substring(0, queryStringPos).lastIndexOf('/');
		int startPos = (slashPos >= 0) ? slashPos + 1 : 0;
		if (queryStringPos < 0)
			queryStringPos = url.length();
		String objClass = url.substring(startPos, queryStringPos).toLowerCase();
		if (objClass.startsWith(context.getNAME_SPACE()))
		{
			objClass = objClass.substring(context.getNAME_SPACE().length() + 1);
		}
		String target = callTargetsByObject.get(objClass);
		if (shouldLoadTarget(target))
			return target;
		return "";
	}

	protected void callWebObject(String url)
	{
		String target = getCallTargetFromUrl(url);
		if (target == null || target.length() == 0)
		{
			httpContext.wjLoc = url;
		}
		else
		{
			try
			{
				JSONObject jsonCmd = new JSONObject();
				jsonCmd.put("url", url);
				jsonCmd.put("target", target);
				httpContext.appendAjaxCommand("calltarget", jsonCmd);
			}
			catch (JSONException ex)
			{
			}
		}
	}

	private boolean shouldLoadTarget(String target)
	{
		return target != null && (target.equalsIgnoreCase("top") || target.equalsIgnoreCase("right") || target.equalsIgnoreCase("bottom") || target.equalsIgnoreCase("left"));
	}

}
