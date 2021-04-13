package com.genexus.servlet.http;

import com.genexus.servlet.IServletRequest;
import com.genexus.servlet.ServletRequest;

public class HttpServletRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper implements jakarta.servlet.ServletRequest, IHttpServletRequestWrapper {

	public HttpServletRequestWrapper(IHttpServletRequest request) {
		super(((HttpServletRequest)request).getWrappedClass());
	}

	public IServletRequest getServletRequest() {
		return new ServletRequest(this);
	}
}
