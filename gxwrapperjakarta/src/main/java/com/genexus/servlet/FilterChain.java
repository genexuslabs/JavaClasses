package com.genexus.servlet;

import com.genexus.servlet.http.*;
import java.io.IOException;

public class FilterChain implements IFilterChain{
	private jakarta.servlet.FilterChain fc;

	public FilterChain(jakarta.servlet.FilterChain fc) {
		this.fc = fc;
	}

	public void doFilter(IServletRequest request, IServletResponse response) throws IOException, ServletException {
		try {
			fc.doFilter(((ServletRequest) request).getWrappedClass(), ((ServletResponse)response).getWrappedClass());
		} catch (jakarta.servlet.ServletException e) {
			throw (ServletException)e;
		}
	}

	public void doFilter(IHttpServletRequest request, IHttpServletResponse response) throws IOException, ServletException {
		try {
			fc.doFilter(((HttpServletRequest)request).getWrappedClass(), ((HttpServletResponse)response).getWrappedClass());
		} catch (jakarta.servlet.ServletException e) {
			throw (ServletException)e;
		}
	}

	public void doFilter(IServletRequest request, IHttpServletResponseWrapper response) throws IOException, ServletException {
		try {
			fc.doFilter(((ServletRequest)request).getWrappedClass(), (HttpServletResponseWrapper)response);
		} catch (jakarta.servlet.ServletException e) {
			throw (ServletException)e;
		}
	}

	public void doFilter(IHttpServletRequestWrapper request, IHttpServletResponse response) throws IOException, ServletException {
		try {
			fc.doFilter(((ServletRequest)request.getServletRequest()).getWrappedClass(), ((HttpServletResponse)response).getWrappedClass());
		} catch (jakarta.servlet.ServletException e) {
			throw (ServletException)e;
		}
	}
}
