package com.genexus.db;

import java.lang.reflect.Method;


public class SQLAndroidSQLiteHelper 
{
	   private static String clazzFullName = "com.artech.layers.LocalUtils";
	   
	   public static void beginTransaction()
	    {
	        try {
	            Class<?> clazz = Class.forName(clazzFullName);
	            Method method = clazz.getMethod("beginTransaction");
	            
	            method.invoke(null);
	            
	        } catch ( Exception e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } 
	    }
	    
	    public static void endTransaction()
	    {
	        try {
	            Class<?> clazz = Class.forName(clazzFullName);
	            Method method = clazz.getMethod("endTransaction");
	            
	            method.invoke(null);
	            
	        } catch ( Exception e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } 
	        
	    }
	    
    
}
