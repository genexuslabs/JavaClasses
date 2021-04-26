package com.genexus.servlet.http;

import java.io.BufferedReader;
import java.util.Enumeration;

import com.genexus.servlet.IRequestDispatcher;
import com.genexus.servlet.IServletInputStream;
import com.genexus.servlet.IServletContext;

public interface IHttpServletRequest {
	String getContextPath();
	StringBuffer getRequestURL();
	String getQueryString();
	String getContentType();
	String getServletPath();
	String getRequestURI();
	Object getAttribute(String name);
	void setAttribute(String name, Object o);
	String getRemoteAddr();
	String getRemoteUser();
	String getRemoteHost();
	IHttpSession getSession(boolean create);
	int getServerPort();
	String getServerName();
	String getScheme();
	IRequestDispatcher getRequestDispatcher(String path);
	String getRequestedSessionId();
	boolean isRequestedSessionIdValid();
	Enumeration<String> getParameterNames();
	String[] getParameterValues(String name);
	String getCharacterEncoding();
	boolean isSecure();
	int getContentLength();
	IServletInputStream getInputStream() throws java.io.IOException;
	ICookie[]	getCookies();
	String getHeader(String name);
	Enumeration<String> getHeaders(String name);
	Enumeration<String> getHeaderNames();
	String getMethod();
	IServletContext getServletContext();
	BufferedReader getReader() throws java.io.IOException;
	String getRealPath(String path);
}
