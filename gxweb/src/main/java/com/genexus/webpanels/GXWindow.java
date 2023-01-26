package com.genexus.webpanels;

import com.genexus.internet.HttpAjaxContext;
import com.genexus.internet.IGxJSONAble;
import com.genexus.common.interfaces.IGXWindow;

import json.org.json.IJsonFormattable;
import json.org.json.JSONArray;

public class GXWindow implements IGxJSONAble, IGXWindow
{
    private JSONArray jArr;
    private String _url;
    private int _autoresize;
	private String _cssClassName;
    private int _width;
    private int _height;
    private int _position;
    private int _top;
    private int _left;
    private Object[] _onlcoseCmds;
    private Object[] _returnParms;


    public GXWindow()
    {
        jArr = new JSONArray();
        _url = "";
        _autoresize = 1;
		_cssClassName = "";
        _width = 0;
        _height = 0;
        _position = 0; //0: Centered, 1: Absolute
        _top = 0;
        _left = 0;
        _onlcoseCmds = new Object[] {};
        _returnParms = new Object[] {};
    }

    public void setUrl(String url)
    {
        _url = url;
    }

    public String getUrl()
    {
        return _url;
    }

    public void setAutoresize(int autoresize)
    {
        _autoresize = autoresize;
    }

	public void setThemeClass(String themeClass) 
	{
		_cssClassName = themeClass;
	}
	
	public String getThemeClass()
	{
		return _cssClassName;
	}
	
    public int getAutoresize()
    {
        return _autoresize;
    }

    public void setWidth(int width)
    {
        _width = width;
    }

    public int getWidth()
    {
        return _width;
    }

    public void setHeight(int height)
    {
        _height = height;
    }

    public int getHeight()
    {
        return _height;
    }

    public void setPosition(int position)
    {
        _position = position;
    }

    public int getPosition()
    {
        return _position;
    }

    public void setTop(int top)
    {
        _top = top;
    }

    public int getTop()
    {
        return _top;
    }

    public void setLeft(int left)
    {
        _left = left;
    }

    public int getLeft()
    {
        return _left;
    }
    
    public void setReturnParms(Object[] retParms)
    {
        _returnParms = retParms;
    }

    public void tojson()
    {
        jArr.clear();
        jArr.put(_url);
        jArr.put(_autoresize);
        jArr.put(_width);
        jArr.put(_height);
        jArr.put(_position);
        jArr.put(_top);
        jArr.put(_left);
        jArr.put(HttpAjaxContext.ObjArrayToJSONArray(_onlcoseCmds));
        jArr.put(HttpAjaxContext.ObjArrayToJSONArray(_returnParms));
		jArr.put(_cssClassName);
    }

    public void AddObjectProperty(String name, Object prop)
    {
    }

    public Object GetJSONObject(boolean includeState)
    {
		return GetJSONObject();
    }

    public Object GetJSONObject()
    {
        tojson();
        return jArr;
    }

    public void FromJSONObject(IJsonFormattable obj)
    {
    }

    public String ToJavascriptSource()
    {
        tojson();
        return jArr.toString();
    }
}
