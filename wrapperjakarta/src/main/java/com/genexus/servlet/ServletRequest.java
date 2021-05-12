package com.genexus.servlet;

import com.genexus.servlet.http.HttpServletRequest;
import com.genexus.servlet.http.IHttpServletRequest;

public class ServletRequest implements IServletRequest{
	private jakarta.servlet.ServletRequest req;

	public ServletRequest(jakarta.servlet.ServletRequest req) {
		this.req = req;
	}

	public jakarta.servlet.ServletRequest getWrappedClass() {
		return req;
	}

	public boolean isHttpServletRequest()
	{
		return req instanceof jakarta.servlet.http.HttpServletRequest;
	}

	public IHttpServletRequest getHttpServletRequest()
	{
		if (req instanceof jakarta.servlet.http.HttpServletRequest) {
			return new HttpServletRequest((jakarta.servlet.http.HttpServletRequest)req);
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
