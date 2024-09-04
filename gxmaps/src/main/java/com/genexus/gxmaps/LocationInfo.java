

package com.genexus.gxmaps;

import com.genexus.CommonUtil;
import com.genexus.GXGeospatial;

import java.text.DateFormat;
import java.util.*;
import com.genexus.xml.*;

import json.org.json.*;
import com.genexus.internet.IGxJSONAble;

public final class LocationInfo implements java.io.Serializable, IGxJSONAble {

	protected JSONObject jsonObj = new JSONObject();
	
	
	private GXGeospatial location;
	
	public GXGeospatial getLocation() {
		return location;
	}
	
	public void setLocation(GXGeospatial value) {
		location = value;
	}

	private String description;

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String value) {
		description = value;
	}

	private Date time;

	public Date getTime() {
		return time;
	}
	
	public void setTime(Date value) {
		time = value;
	}


	private Double precision;
	
	public Double getPrecision() {
		return precision;
	}
	
	public void setPrecision(Double value) {
		precision = value;
	}


	private Double heading;
	
	public Double getHeading() {
		return heading;
	}
	
	public void setHeading(Double value) {
		heading = value;
	}

	private Double speed;

	public Double getSpeed() {
		return speed;
	}
	
	public void setSpeed(Double value) {
		speed = value;
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
	   AddObjectProperty("location", location);	  
	   AddObjectProperty("description", description);	   
	   AddObjectProperty("time", time);	   
	   AddObjectProperty("precision", precision);	   
	   AddObjectProperty("heading", heading);	   
	   AddObjectProperty("speed", speed);	   
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
		JSONObject jObj = (JSONObject) obj;	
		this.setDescription( jObj.optString("description"));
		this.setHeading( jObj.optDouble("heading"));
		this.setSpeed( jObj.optDouble("speed"));
		this.setPrecision( jObj.optDouble("precision"));
		///this.setTime( );
		this.setLocation( new GXGeospatial(jObj.optString("location")));
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
