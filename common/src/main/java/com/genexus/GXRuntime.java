package com.genexus;

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
}
