package com.genexus.servlet;


import com.genexus.cors.CORSHelper;
import jakarta.servlet.*;
import jakarta.servlet.Filter;
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
		Filter.super.init(filterConfig);
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;

		HashMap<String, String> corsHeaders = CORSHelper.getCORSHeaders(request.getHeader("Access-Control-Request-Headers"));
		if (corsHeaders != null) {
			HttpServletResponse response = (HttpServletResponse) servletResponse;
			for (String headerName : corsHeaders.keySet()) {
				if (!response.containsHeader(headerName)) {
					response.setHeader(headerName, corsHeaders.get(headerName));
				}
			}
		}
		if (!request.getMethod().equalsIgnoreCase("OPTIONS")) {
			filterChain.doFilter(servletRequest, servletResponse);
		}
	}

	@Override
	public void destroy() {
		Filter.super.destroy();
	}
}
