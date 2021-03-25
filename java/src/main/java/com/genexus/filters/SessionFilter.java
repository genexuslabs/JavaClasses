package com.genexus.filters;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SessionFilter implements Filter{
	String JSESSIONID="JSESSIONID";
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String sessionCookieName = filterConfig.getServletContext().getSessionCookieConfig().getName();
        if (sessionCookieName!=null && !sessionCookieName.equals("")) {
			JSESSIONID=sessionCookieName;
		}
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
		Cookie session=null;
        Cookie[] allCookies = req.getCookies();
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