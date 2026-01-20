package com.genexus.filters;

import java.util.Arrays;
import java.util.Map;

import com.genexus.WrapperUtils;
import com.genexus.servlet.*;
import com.genexus.servlet.http.*;

public class SessionFilter extends Filter{
	String JSESSIONID="JSESSIONID";
    @Override
    public void init(Map<String, String> headers, String path, String sessionCookieName) throws ServletException {
        if (sessionCookieName!=null && !sessionCookieName.equals("")) {
			JSESSIONID=sessionCookieName;
		}
    }

    @Override
    public void doFilter(IServletRequest request, IServletResponse response, IFilterChain chain) throws Exception {
        IHttpServletRequest req = request.getHttpServletRequest();
        IHttpServletResponse res = response.getHttpServletResponse();
		if (WrapperUtils.isSecureConnection(req))
		{
			chain.doFilter(request, new SecureCookieHttpServletResponseWrapper(res, JSESSIONID));
		}
		else{
			chain.doFilter(req, res);
		}
    }

    @Override
    public void destroy() {
    }

}