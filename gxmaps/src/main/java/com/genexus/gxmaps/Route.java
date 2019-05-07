
package com.genexus.gxmaps;

import com.genexus.CommonUtil;
import com.genexus.GXGeospatial;
import com.genexus.GXSimpleCollection;
import com.genexus.xml.*;
import com.genexus.internet.IGxJSONAble;
import json.org.json.*;
import java.util.*;

public class Route implements java.io.Serializable, IGxJSONAble {

	protected JSONObject jsonObj = new JSONObject();
	
	public void initialize( int remoteHandle )
	{
	   initialize( ) ;
	}

	public void initialize( )
    {
	}

	private String name;

	public String getName() {
		return name;		
	}
	public void setName(String value) {
		name = value;		
	}

	private Double distance;
	public Double getDistance() {
		return distance;		
	}
	public void setDistance(Double value) {
		distance = value;		
	}

	private GXSimpleCollection<String> advisoryNotices;
	public GXSimpleCollection<String> getAdvisoryNotices() {
		return advisoryNotices;		
	}
	public void setAdvisoryNotices(GXSimpleCollection<String> value) {
		advisoryNotices = value;		
	}
	
	private Double expectedTravelTime;
	public Double getExpectedTravelTime() {
		return expectedTravelTime;		
	}
	public void setExpectedTravelTime(Double value) {
		expectedTravelTime = value;		
	}
	private String transportType;
	public String getTransportTypee() {
		return transportType;		
	}
	public void setTransportType(String value) {
		transportType = value;		
	}
	private GXGeospatial geoline;
	public GXGeospatial getGeoline() {
		return geoline;		
	}
	public void setGeoline(GXGeospatial value) {
		geoline = value;		
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
	   AddObjectProperty("name", name);	  
	   AddObjectProperty("distance1", distance);	  
	   AddObjectProperty("expectedTravelTime", expectedTravelTime);	  
	   AddObjectProperty("geoline", geoline);	  	   
	   AddObjectProperty("transportType", transportType);	  
	   AddObjectProperty("advisoryNotices", advisoryNotices);	   
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
		this.setName( jObj.optString("name"));
		this.setDistance( jObj.optDouble("distance"));
		this.setExpectedTravelTime( jObj.optDouble("expectedTravelTime"));
		this.setTransportType( jObj.optString("transportType"));
		/*
		this.setAdvisoryNotices( jObj.optDouble("advisoryNotices"));
		*/
		///this.setTime( );
		this.setGeoline( new GXGeospatial(jObj.optString("geoline")));
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
