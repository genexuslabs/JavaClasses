package com.genexus.servlet;

import com.genexus.cors.CORSHelper;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class CorsFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		String origin = request.getHeader(CORSHelper.ORIGIN_HEADER_NAME);
		String requestedMethod = request.getHeader(CORSHelper.REQUEST_METHOD_HEADER_NAME);
		String requestedHeaders = request.getHeader(CORSHelper.REQUEST_HEADERS_HEADER_NAME);

		HashMap<String, String> corsHeaders = CORSHelper.getCORSHeaders(request.getMethod(), origin, requestedMethod, requestedHeaders);
		if (corsHeaders != null) {
			for (String headerName : corsHeaders.keySet()) {
				if (!response.containsHeader(headerName)) {
					response.setHeader(headerName, corsHeaders.get(headerName));
				}
			}
			if (CORSHelper.isPreflight(request.getMethod(), origin, requestedMethod)) {
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				return;
			}
		}
		filterChain.doFilter(servletRequest, servletResponse);
	}

	@Override
	public void destroy() {

	}
}
