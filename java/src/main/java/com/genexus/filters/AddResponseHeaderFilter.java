package com.genexus.filters;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AddResponseHeaderFilter implements Filter {
    private Map<String, String> headers = new LinkedHashMap<String, String>();

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            for (Map.Entry<String, String> header : this.headers.entrySet()) {
                httpResponse.setHeader(header.getKey(), header.getValue());
            }
        }
        chain.doFilter(request, response);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        for (Enumeration<String> names = filterConfig.getInitParameterNames(); names.hasMoreElements();) {
            String name = names.nextElement();
            String value = filterConfig.getInitParameter(name);
            try {
                this.headers.put(name, value);
            } catch (RuntimeException e) {
                throw new ServletException("Exception processing configuration parameter '" + name + "':'" + value + "'", e);
            }
        }
    }

    public void destroy() {
    }
}