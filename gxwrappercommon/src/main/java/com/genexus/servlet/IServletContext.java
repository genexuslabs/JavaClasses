package com.genexus.servlet;

public interface IServletContext {
	String getRealPath(String path);
	String getServerInfo();
	int getMajorVersion();
	int	getMinorVersion();
	String getTEMPDIR();
	Object getAttribute(String name);
	String getInitParameter(String name);
}
