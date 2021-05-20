package com.genexus.gxmaps;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import com.genexus.CommonUtil;
import com.genexus.GXGeospatial;
import com.genexus.GXSimpleCollection;
import com.genexus.util.GXGeolocation;
import com.genexus.xml.GXXMLSerializable;
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

    public static GXXMLSerializable calculateDirections(GXGeospatial a, GXGeospatial b)
    {
        return calculateDirections(a, b, "", false);
    }

    private static final String DIRECTIONS_PARAMETERS_SDT_CLASS_NAME = "com.genexuscore.genexus.common.SdtDirectionsRequestParameters";

    public static GXXMLSerializable calculateDirections(GXGeospatial a, GXGeospatial b, String transportType, boolean requestAlternateRoutes)
    {
       try {
        Class<?> DirectionsParametersSDTClass = Class.forName(DIRECTIONS_PARAMETERS_SDT_CLASS_NAME);
        Object directionsParametersSDT = DirectionsParametersSDTClass.getDeclaredConstructor().newInstance();
       
        DirectionsParametersSDTClass.getMethod("setgxTv_SdtDirectionsRequestParameters_Sourcelocation", GXGeospatial.class).invoke(directionsParametersSDT, a);
        DirectionsParametersSDTClass.getMethod("setgxTv_SdtDirectionsRequestParameters_Destinationlocation", GXGeospatial.class).invoke(directionsParametersSDT, b);
        DirectionsParametersSDTClass.getMethod("setgxTv_SdtDirectionsRequestParameters_Transporttype", String.class).invoke(directionsParametersSDT, transportType);
        DirectionsParametersSDTClass.getMethod("setgxTv_SdtDirectionsRequestParameters_Requestalternateroutes", Boolean.TYPE).invoke(directionsParametersSDT, requestAlternateRoutes);
        
        return calculateDirections(directionsParametersSDT);
    } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (InstantiationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (IllegalArgumentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (InvocationTargetException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (NoSuchMethodException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (SecurityException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
        return null;
    }

    private static final String DIRECTIONS_SERVICE_INTERNAL_PROCEDURE_CLASS_NAME = "com.genexuscore.genexus.common.googlemapsdirectionsserviceinternal";

    public static GXXMLSerializable calculateDirections(Object DirectionsParametersSDTArray) 
    {
        try {
            Object DirectionsParametersSDT = ((Object[]) DirectionsParametersSDTArray)[0];
            Class<?> DirectionsServiceProcedureClass = Class.forName(DIRECTIONS_SERVICE_INTERNAL_PROCEDURE_CLASS_NAME);
            Object DirectionsServiceProcedure = DirectionsServiceProcedureClass.getDeclaredConstructor(Integer.TYPE).newInstance(-1);

            return (GXXMLSerializable)DirectionsServiceProcedureClass.getMethod("executeUdp", DirectionsParametersSDT.getClass()).invoke(DirectionsServiceProcedure, DirectionsParametersSDT);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       
        return null;
    }
}