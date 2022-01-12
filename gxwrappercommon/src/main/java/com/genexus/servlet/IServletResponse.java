package com.genexus.servlet;

import com.genexus.servlet.http.IHttpServletResponse;

public interface IServletResponse {
	boolean isHttpServletResponse();
	IHttpServletResponse getHttpServletResponse();
	boolean isCommitted();
}
