package com.genexus.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CustomFilter implements Filter {	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {			
		if (request != null && request instanceof HttpServletRequest) {
			HttpServletRequest req = (HttpServletRequest) request;
			String url = req.getRequestURL().toString();
			if (url.contains("apple-app-site-association")) {
				HttpServletResponse resp = (HttpServletResponse) response;
				resp.setContentType("application/json");
			}
		}		
		// pass the request along the filter chain
		chain.doFilter(request, response);
	}

	public void destroy() {
	}

	public void init(FilterConfig arg0) throws ServletException
	{
		// TODO Auto-generated method stub
		
	}

}