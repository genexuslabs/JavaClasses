package com.genexus;

public class AndroidLog 
{
	  // log levels
    static final int OFF = 0;
	static final int ERROR = 1;
	static final int WARNING = 2;
	static final int INFO = 3;
	static final int DEBUG = 4;
	
    public static int LEVEL = ERROR;
 	
    
    public static void debug(String message) 
    {
        if (LEVEL>=DEBUG) System.out.println(message);
    }

    public static void warning(String message) 
    {
        if (LEVEL>=WARNING) System.out.println(message);
    }
    
    public static void error(String message) 
    {
        if (LEVEL>=ERROR) System.err.println(message);
    }

    public static void info(String message) 
    {
        if (LEVEL>=INFO) System.out.println(message);
    }

    /*static void v(String message) 
    {
        if (LEVEL<=VERBOSE) System.out.println(message);
    }*/

}