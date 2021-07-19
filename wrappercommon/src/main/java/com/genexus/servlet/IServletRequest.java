package com.genexus.servlet;

import com.genexus.servlet.http.IHttpServletRequest;

public interface IServletRequest {
	boolean isHttpServletRequest();
	IHttpServletRequest getHttpServletRequest();
	boolean isSecure();
	String getRemoteAddr();
}
