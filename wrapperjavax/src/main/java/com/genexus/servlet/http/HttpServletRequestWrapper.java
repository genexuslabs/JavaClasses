package com.genexus.servlet.http;

import com.genexus.servlet.IServletRequest;
import com.genexus.servlet.ServletRequest;

public class HttpServletRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper implements javax.servlet.ServletRequest, IHttpServletRequestWrapper {

	public HttpServletRequestWrapper(IHttpServletRequest request) {
		super(((HttpServletRequest)request).getWrappedClass());
	}
	public IServletRequest getServletRequest() {
		return new ServletRequest(this);
	}
}
