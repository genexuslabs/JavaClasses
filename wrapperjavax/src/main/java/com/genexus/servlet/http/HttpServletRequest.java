package com.genexus.servlet.http;

import com.genexus.servlet.*;

import java.io.BufferedReader;
import java.util.Enumeration;

public class HttpServletRequest implements IHttpServletRequest{
	private javax.servlet.http.HttpServletRequest req;

	public HttpServletRequest(javax.servlet.http.HttpServletRequest req)
	{
		this.req = req;
	}

	public javax.servlet.http.HttpServletRequest getWrappedClass() {
		return req;
	}

	public String getContextPath() {
		return req.getContextPath();
	}

	public StringBuffer getRequestURL() {
		return req.getRequestURL();
	}

	public String getQueryString() {
		return req.getQueryString();
	}

	public String getContentType() {
		return req.getContentType();
	}

	public String getServletPath() {
		return req.getServletPath();
	}

	public String getRequestURI() {
		return req.getRequestURI();
	}

	public Object getAttribute(String name) {
		return req.getAttribute(name);
	}

	public void setAttribute(String name, Object o) {
		req.setAttribute(name, o);
	}

	public String getRemoteAddr() {
		return req.getLocalAddr();
	}

	public String getRemoteUser() {
		return req.getRemoteUser();
	}

	public String getRemoteHost() {
		return req.getRemoteHost();
	}

	public IHttpSession	getSession(boolean create) {
		javax.servlet.http.HttpSession session = req.getSession(create);
		if (session != null) {
		return new HttpSession(req.getSession(create));
		}
		return null;
	}

	public int getServerPort() {
		return req.getServerPort();
	}

	public String getServerName() {
		return req.getServerName();
	}

	public String getScheme() {
		return req.getScheme();
	}

	public IRequestDispatcher getRequestDispatcher(String path) {
		return new RequestDispatcher(req.getRequestDispatcher(path));
	}

	public String getRequestedSessionId() {
		return req.getRequestedSessionId();
	}

	public boolean isRequestedSessionIdValid() {
		return req.isRequestedSessionIdValid();
	}

	public Enumeration<String> getParameterNames() {
		return req.getParameterNames();
	}

	public String[] getParameterValues(String name) {
		return req.getParameterValues(name);
	}

	public String getCharacterEncoding() {
		return req.getCharacterEncoding();
	}

	public boolean isSecure() {
		return req.isSecure();
	}

	public int getContentLength() {
		return req.getContentLength();
	}

	public IServletInputStream getInputStream() throws java.io.IOException {
		return new ServletInputStream(req.getInputStream());
	}

	public ICookie[]	getCookies()
	{
		javax.servlet.http.Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			ICookie[] gxCookies = new Cookie[cookies.length];
			for (int i = 0; i < cookies.length; i++){
				gxCookies[i] = new Cookie(cookies[i].getName(), cookies[i].getValue());
			}
			return gxCookies;
		}
		return null;
	}

	public String getHeader(String name){
		return req.getHeader(name);
	}

	public Enumeration<String> getHeaders(String name) {
		return req.getHeaders(name);
	}

	public Enumeration<String> getHeaderNames() {
		return req.getHeaderNames();
	}


	public String getMethod() {
		return req.getMethod();
	}

	public IServletContext getServletContext() {
		return new ServletContext(req.getServletContext());
	}

	public BufferedReader getReader() throws java.io.IOException {
		return req.getReader();
	}
}
