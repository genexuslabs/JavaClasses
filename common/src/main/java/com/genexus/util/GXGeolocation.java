package com.genexus.util;

import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URLEncoder;
import java.net.URL;

import com.genexus.ClientContext;
import com.genexus.xml.*;
import json.org.json.*;

public class GXGeolocation
{
	private static double getComponent(String geolocation, int item)
	{
		StringTokenizer st = new StringTokenizer(geolocation, ",");
		if (st.countTokens() == 2)
		{
			String token = null;
			for (int currentToken = 0; currentToken <= item; currentToken++)
			{
				token = st.nextToken();
			}
			
			return Double.parseDouble(token);
		}
		else
			return 0;
	}

	public static double getLatitude(String geolocation)
	{
		return GXGeolocation.getComponent(geolocation, 0);
	}

	public static double getLongitude(String geolocation)
	{
		return GXGeolocation.getComponent(geolocation, 1);
	}
	
	private static double degreesToRadians(double degrees)
	{
		return degrees * Math.PI / 180;
	}
	
	public static int getDistance(String location1, String location2)
	{
		// Haversine formula:
		// a = sin�(delta_lat/2) + cos(lat1).cos(lat2).sin�(delta_long/2)
		// c = 2.atan2(sqrt(a), sqrt(1-a))
		// d = R.c
		//   where R is earth�s radius (mean radius = 6,371km);
		// note that angles need to be in radians to pass to trig functions!
		
		double lat1, lon1, lat2, lon2, d_lat, d_lon, a1, a2, a3, a, c, distance;
		
		lat1 = GXGeolocation.getLatitude(location1);
		lon1 = GXGeolocation.getLongitude(location1);
		lat2 = GXGeolocation.getLatitude(location2);
		lon2 = GXGeolocation.getLongitude(location2);
		
		d_lat = GXGeolocation.degreesToRadians(lat2 - lat1);
		d_lon = GXGeolocation.degreesToRadians(lon2 - lon1);
		
		lat1 = GXGeolocation.degreesToRadians(lat1);
		lat2 = GXGeolocation.degreesToRadians(lat2);
		
		a1 = Math.pow( Math.sin(d_lat / 2), 2 );
		a2 = Math.pow( Math.sin(d_lon / 2), 2 );
		a3 = Math.cos(lat1) * Math.cos(lat2);
		a = a1 + a2 * a3;
		
		c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		
		distance = 6371 * c * 1000; // distance in meters
		
		return (int)distance;
	}
	
	public static String getContentFromURL(String urlString)
	{
		HttpURLConnection connection = null;
		BufferedReader rd  = null;
		StringBuffer sb = null;
		String line = null;
		
		String result = null;
    
		try {
			URL url = new URL(urlString);
			
			//Set up the initial connection
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			
			connection.connect();
			
			//read the result from the server
			rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			sb = new StringBuffer();
			while ((line = rd.readLine()) != null)
			{
				sb.append(line);
			}
			
			result = sb.toString();
		} catch (MalformedURLException e) {
		} catch (ProtocolException e) {
		} catch (IOException e)	{
		}
		finally
		{
			//close the connection, set all objects to null
			if (connection!=null)
			{
				connection.disconnect();
				connection = null;
			}
			rd = null;
			sb = null;
		}
		
		return result;
	}

	public static String getAPIKey(){
		return ClientContext.getModelContext().getClientPreferences().getGOOGLE_API_KEY();	
	}

	public static java.util.Vector getAddress(String location)
	{
		String urlString = "https://maps.google.com/maps/api/geocode/json?latlng=" + location.trim() + "&sensor=false";
		// replace space to avoid File not found exception
		// http://stackoverflow.com/questions/19092243/keep-getting-java-io-filenotfoundexception-exeption-at-monodroid-application
		String apiKey = getAPIKey();
    
		if (apiKey.length() > 0) {
			urlString += urlString + "&key=" + apiKey;
		}
		urlString = urlString.replace(" ", "+");

		String response = GXGeolocation.getContentFromURL(urlString);		
		java.util.Vector result = new java.util.Vector();
		
		if (response!=null)
		{
			try {
				JSONObject json = new JSONObject(response);
				if (json.has("results")) {
					JSONArray results = json.optJSONArray("results");
					for (int i = 0; i < results.length(); i++) {
						JSONObject jo = results.optJSONObject(i);
						if (jo.has("formatted_address")) {
							result.add(jo.optString("formatted_address"));
						}
					}
				}
			}
			catch (JSONException ex) {}
		}
		return result;
	}
	
	public static java.util.Vector getLocation(String address)
	{
		java.util.Vector result = new java.util.Vector();
		try {
			
			String urlString = "https://maps.google.com/maps/api/geocode/json?address=" + URLEncoder.encode(address, "utf-8") + "&sensor=false";
			String apiKey = getAPIKey();
			if (apiKey.length() > 0) {
				urlString += urlString + "&key=" + apiKey;
			}
			
			String response = GXGeolocation.getContentFromURL(urlString);
			JSONObject json = new JSONObject(response);
			if (json.has("results")) {
				JSONArray results = json.optJSONArray("results");
				for (int i = 0; i < results.length(); i++) {
					JSONObject jo = results.optJSONObject(i);
					if (jo.has("geometry")) {
						JSONObject geometry = jo.optJSONObject("geometry");
						if (geometry.has("location")) {
							JSONObject location = geometry.optJSONObject("location");
							if ((location.has("lat")) && (location.has("lng"))) {
								String geoloc = location.optString("lat") + "," + location.optString("lng");
								result.add(geoloc);
							}
						}
					}
				}
			}
			else {
				if (json.has("error_message")) {
					String error_message = json.optString("error_message");
					return result;	
				}
			}
		}
		catch (JSONException ex) {}
		catch (java.io.UnsupportedEncodingException e){}
		
		return result;
	}
}
