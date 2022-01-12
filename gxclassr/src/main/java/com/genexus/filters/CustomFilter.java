package com.genexus.filters;

import java.util.Map;

import com.genexus.servlet.*;
import com.genexus.servlet.http.IHttpServletRequest;
import com.genexus.servlet.http.IHttpServletResponse;

public class CustomFilter extends Filter {
	public void doFilter(IServletRequest request, IServletResponse response, IFilterChain chain) throws Exception {
		if (request != null && request.isHttpServletRequest()) {
			IHttpServletRequest req = request.getHttpServletRequest();
			String url = req.getRequestURL().toString();
			if (url.contains("apple-app-site-association")) {
				IHttpServletResponse resp = response.getHttpServletResponse();
				resp.setContentType("application/json");
			}
		}		
		// pass the request along the filter chain
		chain.doFilter(request, response);
	}

	public void destroy() {
	}

	public void init(Map<String, String> headers, String path, String sessionCookieName) throws ServletException {
	}

}