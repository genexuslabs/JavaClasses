package com.genexus.filters;

import java.util.Map;
import java.util.ArrayList;
import com.genexus.servlet.*;
import com.genexus.servlet.http.IHttpServletRequest;


public class InternalRestServicesFilter extends Filter {
    
    private ArrayList<String> appPath = new ArrayList<String>();

    public void doFilter(IServletRequest request, IServletResponse response, IFilterChain chain) throws Exception {
        if (request.isHttpServletRequest() && response.isHttpServletResponse()) {
            IHttpServletRequest httpRequest = request.getHttpServletRequest();
            String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length()).substring(1);
            String urlString = (path.lastIndexOf("/") > -1)? path.substring(0,path.lastIndexOf("/")).toLowerCase():path.toLowerCase();
            boolean isPath = appPath.contains(urlString);
            if(isPath)
            {
                String fwdURI = "/rest/" + path;
				httpRequest.getRequestDispatcher(fwdURI).forward(request,response);
            }
            else
            {
                chain.doFilter(request, response);
            }
        }
        else
        {
            chain.doFilter(request, response);
        }
    }

    public void init(Map<String, String> headers, String path, String sessionCookieName) throws ServletException {
    	appPath.add("gxmulticall");
    }

    public void destroy() {
    }
}