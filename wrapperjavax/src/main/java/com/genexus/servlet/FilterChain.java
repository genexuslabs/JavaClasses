package com.genexus.servlet;

import com.genexus.servlet.http.*;
import java.io.IOException;

public class FilterChain implements IFilterChain{
	private javax.servlet.FilterChain fc;

	public FilterChain(javax.servlet.FilterChain fc) {
		this.fc = fc;
	}

	public void doFilter(IServletRequest request, IServletResponse response) throws IOException, ServletException {
		try {
			fc.doFilter(((ServletRequest) request).getWrappedClass(), ((ServletResponse)response).getWrappedClass());
		} catch (javax.servlet.ServletException e) {
			throw (ServletException)e;
		}
	}

	
	public void doFilter(IHttpServletRequest request, IHttpServletResponse response) throws IOException, ServletException {
		try {
			fc.doFilter(((HttpServletRequest)request).getWrappedClass(), ((HttpServletResponse)response).getWrappedClass());
		} catch (javax.servlet.ServletException e) {
			throw (ServletException)e;
		}
	}

	public void doFilter(IServletRequest request, IHttpServletResponseWrapper response) throws IOException, ServletException {
		try {
			fc.doFilter(((ServletRequest)request).getWrappedClass(), (HttpServletResponseWrapper)response);
		} catch (javax.servlet.ServletException e) {
			throw (ServletException)e;
		}
	}

	public void doFilter(IHttpServletRequestWrapper request, IHttpServletResponse response) throws IOException, ServletException {
		try {
			fc.doFilter(((ServletRequest)request.getServletRequest()).getWrappedClass(), ((HttpServletResponse)response).getWrappedClass());
		} catch (javax.servlet.ServletException e) {
			throw (ServletException)e;
		}
	}
}
