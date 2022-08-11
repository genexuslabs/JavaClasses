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
import java.util.HashMap;

public class CorsFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

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

	}
}
