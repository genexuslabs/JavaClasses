package com.genexus.servlet;

import com.genexus.servlet.http.IHttpServletRequest;
import com.genexus.servlet.http.IHttpServletResponse;
import com.genexus.servlet.http.IHttpServletRequestWrapper;
import com.genexus.servlet.http.IHttpServletResponseWrapper;

public interface IFilterChain {
	void doFilter(IServletRequest request, IServletResponse response) throws Exception;
	void doFilter(IHttpServletRequest request, IHttpServletResponse response) throws Exception;
	void doFilter(IServletRequest request, IHttpServletResponseWrapper response) throws Exception;
	void doFilter(IHttpServletRequestWrapper request, IHttpServletResponse response) throws Exception;
}
