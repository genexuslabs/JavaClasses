package com.genexus.filters;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class SecureCookieHttpServletResponseWrapper extends HttpServletResponseWrapper {

	String cookieId;
    public SecureCookieHttpServletResponseWrapper(HttpServletResponse response, String cookieId) {
        super(response);
		this.cookieId = cookieId.toLowerCase();
    }
	@Override
	public void addCookie(Cookie cookie) {
		if (!cookie.getSecure() && cookie.getName().toLowerCase()==cookieId){
			cookie.setSecure(true);
		}
		super.addCookie(cookie);
	}
}