package com.genexus.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Filter implements jakarta.servlet.Filter {
	protected abstract void doFilter(IServletRequest request, IServletResponse response, IFilterChain chain) throws Exception;
	protected abstract void init(Map<String, String> headers, String path, String sessionCookieName) throws ServletException;

	public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, jakarta.servlet.FilterChain chain) throws IOException, jakarta.servlet.ServletException {
		try {
			doFilter(new ServletRequest(request), new ServletResponse(response), new FilterChain(chain));
		} catch (Exception e) {
			if (e instanceof IOException) {
				throw new IOException(e);
			}
			if (e instanceof ServletException) {
				throw new ServletException(e);
			}
		}
	}

	public void init(jakarta.servlet.FilterConfig filterConfig) throws jakarta.servlet.ServletException {
		String path = filterConfig.getServletContext().getRealPath("/");
		String sessionCookieName = filterConfig.getServletContext().getSessionCookieConfig().getName();
		Map<String, String> headers = new LinkedHashMap<String, String>();
		for (Enumeration<String> names = filterConfig.getInitParameterNames(); names.hasMoreElements();) {
			String name = names.nextElement();
			String value = filterConfig.getInitParameter(name);
			try {
				headers.put(name, value);
			} catch (RuntimeException e) {
				throw new ServletException("Exception processing configuration parameter '" + name + "':'" + value + "'", e);
			}
		}
		init(headers, path, sessionCookieName);
	}
}
