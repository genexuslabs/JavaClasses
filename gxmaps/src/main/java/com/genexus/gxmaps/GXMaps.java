package com.genexus.gxmaps;

import java.io.IOException;
import java.util.*;
import com.genexus.CommonUtil;
import com.genexus.GXGeospatial;
import com.genexus.GXSimpleCollection;
import com.genexus.util.GXGeolocation;
import com.genexus.ClientContext;


import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class GXMaps {

    public static Double getLatitude(GXGeospatial Point) {
        return Point.getLatitude();
    }

    public static Double getLongitude(GXGeospatial Point) {
        return Point.getLongitude();
    }

    public static LocationInfo getCurrentLocation(int minAccuracy, int _timeout, boolean includeHAndS) {
        return new LocationInfo();       
    }

    public static LocationInfo getCurrentLocation(int minAccuracy, int _timeout, boolean includeHAndS, Boolean ignoreErrors) {
            return new LocationInfo();       
    }
    
    public static double getDistance(GXGeospatial start, GXGeospatial destination)
    {
        return start.distance(destination);
    }

    public static GXSimpleCollection<GXGeospatial> geocodeAddress(String address)    
	{
        GXSimpleCollection<GXGeospatial> loclist = new GXSimpleCollection<GXGeospatial>();        
        java.util.Vector listPoints = GXGeolocation.getLocation(address);
        for (  Object var : listPoints) {
            GXGeospatial gPoint = new GXGeospatial((String)var);
            loclist.add(gPoint);
        } 
        return loclist;
    }

    public static GXSimpleCollection<String> reverseGeocode(GXGeospatial coordinate) {

        GXSimpleCollection<String> addresses = new GXSimpleCollection<String>();
        String location = coordinate.getLatitude().toString() + "," + coordinate.getLongitude().toString();
        java.util.Vector listaddr =  GXGeolocation.getAddress(location);       
        for (  Object var : listaddr) {
            addresses.add((String)var);
        } 
        return addresses;
    }

    public static Directions calculateDirections(GXGeospatial a, GXGeospatial b)
    {
        return calculateDirections(a, b, "", false);

    }

    public static Directions calculateDirections(GXGeospatial a, GXGeospatial b, String transportType, boolean requestAlternateRoutes)
    {
        Directions directionsCalculated =  new Directions();
        String ApiKey = ClientContext.getModelContext().getClientPreferences().getGOOGLE_API_KEY();
        String tmode = getTransportMode(transportType);
        String queryString = "key=" + ApiKey + "&origin=" + a.getLatitude().toString() + "," + a.getLongitude().toString();
        queryString += "&destination=" + b.getLatitude().toString() + "," +  b.getLongitude().toString();
        if (!tmode.equals(""))
		{
			queryString += "&mode=" + tmode;
		}
		if (requestAlternateRoutes )
		{
			queryString += "&alternatives=true";
		}
        String urlString = "https://maps.googleapis.com/maps/api/directions/json?" + queryString;
        String response = GXGeolocation.getContentFromURL(urlString);
        directionsCalculated = ParseResponse(response);
        return directionsCalculated;
    }

    private static Directions ParseResponse(String response)
	{
		Directions directionsCalculated = new Directions();        
        try {
            JSONObject json = new JSONObject(response);
            if (json.has("routes")) {
                JSONArray results = json.optJSONArray("routes");
                for (int i = 0; i < results.length(); i++) {                    
                    JSONObject jor = results.optJSONObject(i);
                    // each route
                    Double routeDistance = 0.0;
                    Double routeDuration = 0.0;
                    List<String> polyLineData = new LinkedList();
                    String travelMode = "";
                    String description = "";
                    if (jor.has("summary")) {
                        description = jor.getString("summary");
                    }
                    if (jor.has("legs")) {
                        JSONArray legs = jor.optJSONArray("legs");
                        for (int j = 0; j < legs.length(); j++) {
                            JSONObject jol = legs.optJSONObject(j);
                            // each leg
                            if (jol.has("distance")) {
                                routeDistance += jol.getJSONObject("distance").getDouble("value");
                            }
                            if (jol.has("duration")) {
                                routeDuration += jol.getJSONObject("duration").getDouble("value");
                            }
                            if (jol.has("steps")) {
                                JSONArray steps = jol.optJSONArray("steps");
                                for (int k = 0; k < steps.length(); k++) {
                                    JSONObject jos = steps.optJSONObject(k);
                                        if (jos.has("polyline")) {
                                            String encodedLine = jos.getJSONObject("polyline").getString("points");
                                            String decodedLine = DecodePolyLine(encodedLine);
                                            if (decodedLine.length() > 0) {
                                                polyLineData.add(decodedLine);
                                            }
                                        }
                                        travelMode = jos.getString("travel_mode");
                                    }       
                              
                            }
                        }
                        String lineStringPoints = String.join(",", polyLineData);
                        Route currentRoute = new Route();
					    currentRoute.setName( description);
					    currentRoute.setTransportType( travelMode);
					    currentRoute.setDistance(routeDistance);
					    currentRoute.setExpectedTravelTime(routeDuration);
					    currentRoute.setGeoline( new GXGeospatial("LINESTRING(" + lineStringPoints + ")"));
					    directionsCalculated.getRoutes().add(currentRoute);
                    }
                }
            }
        }
        catch(JSONException ex) {

        }
        return directionsCalculated;
    }

        
    private static String getTransportMode(String transportType)
	{
		switch (transportType)
		{
			case "GXM_Driving":
				return "driving";					
			case "GXM_Walking":
				return "walking";
			case "GXM_Transit":
				return "transit";
			case "GXM_Bicycling":
				return "bicycling";
			 default:
				return "";
		}
    }
    
    private static String DecodePolyLine(String encodedPolyline) 
    {
        int len = encodedPolyline.length();
        final java.util.List<String> path = new java.util.ArrayList(len / 2);
        int index = 0;
        int lat = 0;
        int lng = 0;
        
        while (index < len) {
            int result = 1;
            int shift = 0;
            int b;
            do {
      	        b = encodedPolyline.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
            result = 1;
            shift = 0;
            do {
                b = encodedPolyline.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
            path.add(String.valueOf(lng * 1e-5) + " " + String.valueOf(lat * 1e-5));
	    }
	    return  String.join(",", path);
    }

}