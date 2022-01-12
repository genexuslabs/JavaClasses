package com.genexus.webpanels;

import java.util.ArrayList;

import com.genexus.ModelContext;
import com.genexus.internet.HttpContext;
import com.genexus.internet.IGxJSONAble;

import json.org.json.IJsonFormattable;
import json.org.json.JSONArray;
import json.org.json.JSONException;
import json.org.json.JSONObject;

public class GXWebGrid implements IGxJSONAble
{
    ModelContext context;
    boolean wrapped;
    boolean isFreestyle;
    JSONArray _ColsProps;
    JSONObject _Rows;
    int _Count;
    ArrayList<GXWebRow> _rowObjs;
	ArrayList<JSONArray> _ColsPropsCommon;
    int _PageSize;
    boolean writingTableContent;

    public GXWebGrid()
    {
        _Rows = new JSONObject();
        _ColsProps = new JSONArray();
        _rowObjs = new ArrayList<>();
		_ColsPropsCommon = new ArrayList<>();
        _Count = 0;
    }

    public GXWebGrid(ModelContext context)
    {
        this();
        this.context = context;
    }

    public boolean isFreestyle()
    {
    		return this.isFreestyle;
    	}

    public void SetIsFreestyle(boolean isFreestyle)
    {
    		this.isFreestyle = isFreestyle;
    }

    public boolean isWritingTableContent()
    {
        return this.writingTableContent;
    }

    public void setWritingTableContent(boolean writingTableContent)
    {
        this.writingTableContent = writingTableContent;
    }

    public void SetWrapped(int wrapped)
    {
        HttpContext httpContext = (HttpContext) this.context.getHttpContext();
        this.wrapped = (wrapped==1);
        if (!this.wrapped)
        {
            this.wrapped = httpContext.drawGridsAtServer();
        }
        if (!this.wrapped)
        {
            this.wrapped = httpContext.getWrapped();
        }
        httpContext.setWrapped(this.wrapped);
    }

    public int GetWrapped()
    {
        if (this.wrapped)
        {
            return 1;
        }
        return 0;
    }

	public ArrayList<JSONArray> GetColsPropsCommon(){
		return this._ColsPropsCommon;
	}

    public void CloseTag(String tag)
    {
        if (this.isFreestyle() && tag.equalsIgnoreCase("row") && !this.isWritingTableContent())
        {
                return;
        }
        GXWebStdMethods.closeTag(this.context, tag);
        if (this.isFreestyle() && tag.equalsIgnoreCase("table"))
        {
                this.setWritingTableContent(false);
        }
    }

    public void setPageSize(int pagesize)
    {
        _PageSize=pagesize;
    }

    public void Clear()
    {
        _Rows.clear();
        _ColsProps.clear();
        _rowObjs.clear();
		_ColsPropsCommon.clear();
        _Count = 0;
        writingTableContent = false;
    }

    public void ClearRows()
	{
		_Rows.clear();
        _rowObjs.clear();
        _Count = 0;
    }

	private void ResetRows()
    {
        for (int i = 0; i < _Count; i++)
        {
            _Rows.remove(String.valueOf(_Count));
        }
        _rowObjs.clear();
        _Count = 0;
    }

    public void AddRow(GXWebRow row)
    {
        if (!this.isFreestyle() && this.wrapped && this.context != null)
        {
            GXWebStdMethods.closeTag(this.context, "row");
        }
        else
        {
            if (_PageSize>0 && _Count + 1 > _PageSize)
                ResetRows();
            _rowObjs.add(row);
            AddObjectProperty(String.valueOf(_Count), row.GetJSONObject());
            _Count++;
        }
    }

    public String ToJavascriptSource()
    {
        return GetJSONObject().toString();
    }

    public String GridValuesHidden()
    {
        HttpContext httpContext = (HttpContext) this.context.getHttpContext();
        String values = GetValues().toString();
        if (!httpContext.isAjaxRequest() && !httpContext.isSpaRequest())
        {
            values = WebUtils.htmlEncode(values, true);
        }
        return values;
    }

    public JSONArray GetValues()
    {
        JSONArray values = new JSONArray();
        if (!this.wrapped)
        {
            for (int i=0; i<_rowObjs.size(); i++)
            {
                values.put(_rowObjs.get(i).GetValues());
            }
        }
        return values;
    }

	public String getGridName()
	{
		try
		{
		if (_Rows != null) return (String)_Rows.get("GridName"); else return "";
		}
		catch (JSONException e) { return "";}
	}

    public void tojson()
    {
        AddObjectProperty("Wrapped", new Boolean(this.wrapped));
        if (!this.wrapped)
        {
            AddObjectProperty("Columns", _ColsProps);
            AddObjectProperty("Count", new Integer(_Count));
        }
    }
    public void AddObjectProperty(String name, int prop)
    {
        AddObjectProperty(name, new Integer(prop));
    }
    public void AddObjectProperty(String name, byte prop)
    {
        AddObjectProperty(name, prop==1 ? new Boolean(true):new Boolean(false));
    }
    public void AddObjectProperty(String name, Object prop)
    {
        try
        {
        		if (IGxJSONAble.class.isAssignableFrom(prop.getClass()))
					prop = ((IGxJSONAble)prop).GetJSONObject();
             _Rows.put(name, prop);
        }
        catch (JSONException e) {}
    }

    public void AddColumnProperties(Object colProps)
    {
        _ColsProps.put(((IGxJSONAble)colProps).GetJSONObject());
    }

    public Object GetJSONObject(boolean includeState)
    {
		return GetJSONObject();
    }

    public Object GetJSONObject()
    {
        tojson();
        return _Rows;
    }

    public void FromJSONObject(IJsonFormattable obj)
    {
    }
}
