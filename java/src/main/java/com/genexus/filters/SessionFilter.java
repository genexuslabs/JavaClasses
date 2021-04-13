package com.genexus.filters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

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
		Cookie session=null;
        Cookie[] allCookies = (Cookie[]) req.getCookies();
		if (allCookies != null) {
			session = Arrays.stream(allCookies).filter(x -> x.getName().equals(JSESSIONID)).findFirst().orElse(null);
		}
		if (session!=null && req.isSecure() && !session.getSecure())
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