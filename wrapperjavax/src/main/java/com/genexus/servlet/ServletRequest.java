package com.genexus.servlet;

import com.genexus.servlet.http.HttpServletRequest;
import com.genexus.servlet.http.IHttpServletRequest;

public class ServletRequest implements IServletRequest{
	private javax.servlet.ServletRequest req;

	public ServletRequest(javax.servlet.ServletRequest req) {
		this.req = req;
	}

	public javax.servlet.ServletRequest getWrappedClass() {
		return req;
	}

	public boolean isHttpServletRequest()
	{
		return req instanceof javax.servlet.http.HttpServletRequest;
	}

	public IHttpServletRequest getHttpServletRequest()
	{
		if (req instanceof javax.servlet.http.HttpServletRequest) {
			return new HttpServletRequest((javax.servlet.http.HttpServletRequest)req);
		}
		return null;
	}
	public boolean isSecure() {
		return req.isSecure();
	}
	public String getRemoteAddr() {
		return req.getRemoteAddr();
	}
}
