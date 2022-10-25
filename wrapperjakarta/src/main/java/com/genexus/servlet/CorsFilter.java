package com.genexus.servlet;

import com.genexus.cors.CORSHelper;
import jakarta.servlet.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;

public class CorsFilter implements jakarta.servlet.Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;

		HashMap<String, String> corsHeaders = CORSHelper.getCORSHeaders(request.getMethod(), request.getHeader(CORSHelper.REQUEST_METHOD_HEADER_NAME), request.getHeader(CORSHelper.REQUEST_HEADERS_HEADER_NAME));
		if (corsHeaders != null) {
			HttpServletResponse response = (HttpServletResponse) servletResponse;
			for (String headerName : corsHeaders.keySet()) {
				if (!response.containsHeader(headerName)) {
					response.setHeader(headerName, corsHeaders.get(headerName));
				}
			}
		}
		filterChain.doFilter(servletRequest, servletResponse);
	}

	@Override
	public void destroy() {

	}
}
