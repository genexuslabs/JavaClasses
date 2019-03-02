// $Log: HttpAjaxContext.java,v $
package com.genexus.internet;

import java.util.*;
import com.genexus.webpanels.GXWebRow;

import json.org.json.*;

import java.lang.reflect.Array;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

public abstract class HttpAjaxContext
{
		public static final ILogger logger = LogManager.getLogger(HttpAjaxContext.class);
        private JSONArray AttValues = new JSONArray();
        private JSONArray PropValues = new JSONArray();
        protected JSONObject HiddenValues = new JSONObject();
        protected JSONObject Messages = new JSONObject();
        private JSONObject WebComponents = new JSONObject();
        private Hashtable LoadCommands = new Hashtable();
        private JSONArray Grids = new JSONArray();
        private JSONObject ComponentObjects = new JSONObject();
        protected GXAjaxCommandCollection commands = new GXAjaxCommandCollection();
        protected GXWebRow _currentGridRow = null;
        protected JSONArray StylesheetsToLoad = new JSONArray();

        protected boolean bCloseCommand = false;
        protected boolean Redirected = false;
        protected boolean ajaxRefreshAsGET = false;
        protected String formCaption = "";
        private Object[] returnParms = new Object[] {};
        private Object[] returnParmsMetadata = new Object[] {};

        private Stack cmpContents = new Stack();

        public abstract boolean isMultipartContent();
		public abstract void ajax_rsp_assign_prop_as_hidden(String Control, String Property, String Value);

		public abstract boolean isSpaRequest();
		public abstract boolean isSpaRequest(boolean ignoreFlag);
        private boolean isJsOutputEnabled = true;

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
        public MsgList GX_msglist = new MsgList();

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

        public void doAjaxLoad(int SId, GXWebRow row)
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

        protected void ajax_addCmpContent( String content)
        {
            if (nCmpDrawLvl > 0)
                ((GXCmpContent)cmpContents.peek()).addContent(content);
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
              GXCmpContent cmp = (GXCmpContent)cmpContents.pop();
              WebComponents.put(cmp.getId(), cmp.getContent());
              if (isSpaRequest())
              {
                  if (nCmpDrawLvl > 0)
                      ((GXCmpContent)cmpContents.peek()).addContent(cmp.getContent());
              }
          }
          catch (JSONException ex)
          {
          }
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

        public void ajax_rsp_assign_sdt_attri( String CmpContext, boolean IsMasterPage, String AttName, Object SdtObj)
        {
            if (isJsOutputEnabled)
            {
                if (!isSpaRequest() || (isSpaRequest() && (CmpContext == null || CmpContext.trim().length() == 0)))
                {
                  try {
                      JSONObject obj = getGxObject(AttValues, CmpContext, IsMasterPage);
                      if (obj != null)
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
                        com.genexus.internet.HttpContext webContext = com.genexus.ModelContext.getModelContext().getHttpContext();
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

        public void ajax_rsp_assign_grid(String gridName, com.genexus.webpanels.GXWebGrid gridObj)
        {
            try {
                Grids.putIndex(0, gridObj.GetJSONObject());
          }
          catch (JSONException e) {
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
                com.genexus.internet.HttpContext webContext = com.genexus.ModelContext.getModelContext().getHttpContext();
				if (justCreated)
				{
					webContext.DeletePostValuePrefix(cmpCtx);
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

        protected void AddResourceProvider(String provider)
        {
            try {
            HiddenValues.put("GX_RES_PROVIDER", provider);
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
                            jsonCmdWrapper.put("gxGrids", Grids);
                        }
                        for(Enumeration loadCmds = LoadCommands.keys(); loadCmds.hasMoreElements();)
                        {
                            appendAjaxCommand("load", (JSONObject)LoadCommands.get(loadCmds.nextElement()));
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
                private ArrayList commands;
                private boolean allowUIRefresh;

                public GXAjaxCommandCollection()
                {
                        commands = new ArrayList();
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
                                return (GXAjaxCommand)commands.get(cIdx);
                        }
                        return null;
                }

                public JSONArray getJSONArray()
                {
                        JSONArray jArr = new JSONArray();
                        for(int i=0; i<commands.size(); i++)
                        {
                                GXAjaxCommand cmd = (GXAjaxCommand)commands.get(i);
                                jArr.put(cmd.getJSONObject());
                        }
                        return jArr;
                }
        }
}
