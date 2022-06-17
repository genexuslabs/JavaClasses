package com.genexus;

import com.genexus.util.*;
import java.util.Map;

public class GXRuntime {
  static public short getEnvironment() {
    return 1; // RuntimeEnvironment.Server
  }
  static int exitCode;

  static public void exit() {
	if (exitCode!=0){
		System.exit(exitCode);
	 }
  }
  static public void setExitCode(int value) {
    exitCode = value; 
  }
  static public int getExitCode() {
    return exitCode; 
  }

  public static String GetEnvironmentVariable(String key) {
  	String value = "";
  	if (key != null && key.length() > 0){
		value = System.getenv(key);
	}
  		return value;
  }
  public static GXProperties GetEnvironmentVariables() {

	  Map<String, String> env = System.getenv();
	  GXProperties gxProperties = new GXProperties();
	  for (String envName : env.keySet()) {
		  gxProperties.add(envName,env.get(envName) );
	  }
	  return gxProperties;
	}

  public static boolean HasEnvironmentVariable(String key) {
  	if (key != null && key.length() > 0){
  		String value = System.getenv(key);
  		if (value != null && value.length() > 0)
  			return true;
  	}
  	return false;
  }
}
