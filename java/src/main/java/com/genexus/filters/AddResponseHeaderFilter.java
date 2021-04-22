package com.genexus.filters;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import com.genexus.servlet.*;
import com.genexus.servlet.http.IHttpServletResponse;

public class AddResponseHeaderFilter extends Filter {
    private Map<String, String> headers = new LinkedHashMap<String, String>();

    public void doFilter(IServletRequest request, IServletResponse response, IFilterChain chain) throws Exception {
        if (request.isHttpServletRequest() && response.isHttpServletResponse()) {
            IHttpServletResponse httpResponse = response.getHttpServletResponse();
            for (Map.Entry<String, String> header : this.headers.entrySet()) {
                httpResponse.setHeader(header.getKey(), header.getValue());
            }
        }
        chain.doFilter(request, response);
    }

    public void init(Map<String, String> headers, String path, String sessionCookieName) throws ServletException {
		this.headers = headers;
    }

    public void destroy() {
    }
}