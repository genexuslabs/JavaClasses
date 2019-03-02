// $Log: HttpAjaxContext.java,v $
package com.genexus.internet;

import java.util.*;

import json.org.json.*;

public abstract class HttpAjaxContext
{
        private JSONArray AttValues = new JSONArray();
        private JSONArray PropValues = new JSONArray();
        protected JSONObject HiddenValues = new JSONObject();
        protected JSONObject Messages = new JSONObject();
        private JSONObject WebComponents = new JSONObject();
        private JSONArray Grids = new JSONArray();
        private JSONObject ComponentObjects = new JSONObject();
        protected GXAjaxCommandCollection commands = new GXAjaxCommandCollection();
        protected JSONArray StylesheetsToLoad = new JSONArray();

        protected boolean bCloseCommand = false;
        protected boolean Redirected = false;
        protected boolean ajaxRefreshAsGET = false;
        protected String formCaption = "";
        private Object[] returnParms = new Object[] {};

        private Stack cmpContents = new Stack();

        public abstract boolean isMultipartContent();
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

        public void doAjaxRefresh()
        {
            try
            {
            		String refreshMethod = "POST";
            		if (ajaxRefreshAsGET)
            		{
            			refreshMethod = "GET";
            		}
                appendAjaxCommand("refresh", refreshMethod);
            }
            catch (JSONException ex)
            {
            }
        }
        
        protected Object[] getWebReturnParms()
        {
            return this.returnParms;
        }

        public void setWebReturnParms(Object[] retParms)
        {
            this.returnParms = retParms;
        }

        public void appendAjaxCommand(String cmdType, Object cmdData) throws JSONException
        {
            commands.AppendCommand(new GXAjaxCommand(cmdType, cmdData));
        }
        
        public void executeUsercontrolMethod(String CmpContext, boolean IsMasterPage, String containerName, String methodName, String input, Object[] parms)
        {
                GXUsercontrolMethod method = new GXUsercontrolMethod(CmpContext, IsMasterPage, containerName, methodName, input, parms);
                commands.AppendCommand(new GXAjaxCommand("ucmethod", method.GetJSONObject()));
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
          try
          {
            WebComponents.put(CmpId, "");
          }
          catch (JSONException ex) { }
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
              if (nCmpDrawLvl > 0)
                  ((GXCmpContent)cmpContents.peek()).addContent(cmp.getContent());
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
                for(int i=0; i<array.length(); i++)
                {
                    obj = array.getJSONObject(i);
                    if (obj.get("CmpContext").toString().equals(CmpContext) && obj.get("IsMasterPage").toString().equals(new Boolean(IsMasterPage).toString()))
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

        public void ajax_rsp_assign_attri( String CmpContext, boolean IsMasterPage, String AttName, String AttValue)
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

        public void ajax_rsp_assign_sdt_attri( String CmpContext, boolean IsMasterPage, String AttName, Object SdtObj)
        {
          try {
              JSONObject obj = getGxObject(AttValues, CmpContext, IsMasterPage);
              if (obj != null)
              {
                  obj.put(AttName, ((IGxJSONAble)SdtObj).GetJSONObject());
              }
          }
          catch (JSONException ex) {
          }
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

        public void ajax_rsp_assign_prop( String Control, String Property, String Value)
        {
            ajax_rsp_assign_prop( "", false, Control, Property, Value);
        }
        
        public void ajax_rsp_assign_prop( String CmpContext, boolean IsMasterPage, String Control, String Property, String Value)
        {
          try {
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
          }
          catch (JSONException e) {
          }
        }

        public void ajax_rsp_assign_hidden( String Property, String Value)
        {
          try {
                HiddenValues.put(Property, Value);
          }
          catch (JSONException e) {
          }
        }

        public void ajax_rsp_assign_hidden_sdt( String SdtName, Object SdtObj)
        {
          try {
            HiddenValues.put(SdtName, ((IGxJSONAble)SdtObj).GetJSONObject());
          }
          catch (JSONException e) {
          }
        }

        public void AddComponentObject(String cmpCtx, String objName)
        {
            try {
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

        protected String getJSONResponsePrivate()
        {
                GXJSONObject jsonCmdWrapper = new GXJSONObject(isMultipartContent());
                try
                {
                        if (commands.AllowUIRefresh())
                        {
                            SaveComponentMsgList("MAIN");
                            jsonCmdWrapper.put("gxProps", PropValues);
                            jsonCmdWrapper.put("gxHiddens", HiddenValues);
                            jsonCmdWrapper.put("gxValues", AttValues);
                            jsonCmdWrapper.put("gxMessages", Messages);
                            jsonCmdWrapper.put("gxComponents", WebComponents);
                            jsonCmdWrapper.put("gxGrids", Grids);
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

	public String getJSONResponse()
	{
		if (isCloseCommand() || isRedirected())
                    return "";
		return getJSONResponsePrivate();
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
                private String[] canManyCmds = new String[] { "popup", "refresh", "ucmethod" };
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
