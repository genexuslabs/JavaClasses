package com.genexus.performance;

public interface IApplication {
	boolean isJMXEnabled();
	static boolean staticIsJMXEnabled(){ return false; }
}
