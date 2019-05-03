
package com.genexus.gxmaps;

import com.genexus.CommonUtil;
import com.genexus.GXGeospatial;
import com.genexus.xml.*;
import java.util.*;
import com.genexus.GXSimpleCollection;
import json.org.json.*;
import com.genexus.internet.IGxJSONAble;
import com.genexus.SdtMessages_Message;

public class Directions implements java.io.Serializable, IGxJSONAble {


		
	public Directions()
	{
		this.routes = new GXSimpleCollection<Route>();
		this.messages = new SdtMessages_Message();
	}


	protected JSONObject jsonObj = new JSONObject();

	private GXSimpleCollection<Route> routes;

	public GXSimpleCollection<Route> getRoutes() {
		return routes;
	}

	public void setRoutes(GXSimpleCollection<Route> value) {
		routes = value;
	}

	private SdtMessages_Message messages;		

	public SdtMessages_Message getaRoutes() {
		return messages;
	}

	public void setRoutes(SdtMessages_Message value) {
		messages = value;
	}

	private static java.util.HashMap mapper = new java.util.HashMap();
	static
	{
	}
 
	public String getJsonMap( String value )
	{
	   return (String) mapper.get(value);
	}


	public void tojson( )
	{
	   tojson( true) ;
	}
 
	public void tojson( boolean includeState )
	{
	   tojson( includeState, true) ;
	}
 
	public void tojson( boolean includeState ,
						boolean includeNonInitialized )
	{
	   AddObjectProperty("routes", routes.GetJSONObject());	  
	   AddObjectProperty("messages", messages.GetJSONObject());	   
	}

	
	public void AddObjectProperty(String name, Object prop)
	{
		 try
		 {
			  jsonObj.put(name, prop);
		 }
		 catch(Exception e) {}
	}
 
	public Object GetJSONObject(boolean includeState)
	{
	 return GetJSONObject();
	}

	public Object GetJSONObject()
	{
		 tojson();
		 return jsonObj;
	}

	
	public void FromJSONObject(IJsonFormattable obj)
	{
	}

	public boolean fromJSonString(String s)
	{
		try
		{
			jsonObj = new JSONObject(s);
			FromJSONObject(jsonObj);
			return true;
		}
		catch (JSONException ex)
		{
			
			return false;
		}
	}

	public String ToJavascriptSource()
    {
        return GetJSONObject().toString();
    }

}