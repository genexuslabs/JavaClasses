package com.genexus.webpanels;

import com.genexus.internet.IGxJSONAble;

import json.org.json.*;

public class GXWebColumn implements IGxJSONAble
{
	private JSONObject _Properties;

    public GXWebColumn()
    {
        _Properties = new JSONObject();
    }

    public JSONObject GetProperties()
    {
        return _Properties;
    }

    public void Clear()
    {
        _Properties.clear();
    }

    public static GXWebColumn GetNew()
    {
            return GetNew(false);
    }

    public static GXWebColumn GetNew(boolean includeValue)
    {
        GXWebColumn col = new GXWebColumn();
        return col;
    }

    public void tojson()
    {
    }

	public String ToJavascriptSource()
	{
		return GetJSONObject().toString();
	}

    public void AddObjectProperty(String name, Object prop)
    {
        try
        {
			if (IGxJSONAble.class.isAssignableFrom(prop.getClass()))
				prop = ((IGxJSONAble)prop).GetJSONObject();
			_Properties.put(name, prop);
        }
        catch (JSONException e) {}
    }

    public Object GetJSONObject(boolean includeState)
    {
		return GetJSONObject();
    }
    public Object GetJSONObject()
    {
    	   tojson();
        return _Properties;
    }

    public void FromJSONObject(IJsonFormattable obj) {
    }
}
