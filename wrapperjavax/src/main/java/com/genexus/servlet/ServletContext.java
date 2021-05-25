package com.genexus.servlet;

public class ServletContext implements IServletContext{
	private javax.servlet.ServletContext sc;

	public ServletContext(javax.servlet.ServletContext sc) {
		this.sc = sc;
	}

	public javax.servlet.ServletContext getWrappedClass() {
		return sc;
	}

	public String getRealPath(String path) {
		return sc.getRealPath(path);
	}

	public String getServerInfo() {
		return sc.getServerInfo();
	}

	public int getMajorVersion() {
		return sc.getMajorVersion();
	}

	public int	getMinorVersion() {
		return sc.getMinorVersion();
	}

	public String getTEMPDIR() {
		return sc.TEMPDIR;
	}

	public Object getAttribute(String name) {
		return sc.getAttribute(name);
	}

	public String getInitParameter(String name) {
		return sc.getInitParameter(name);
	}
}
