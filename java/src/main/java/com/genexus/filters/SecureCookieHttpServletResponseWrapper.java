package com.genexus.filters;

import com.genexus.servlet.IServletOutputStream;
import com.genexus.servlet.http.ICookie;
import com.genexus.servlet.http.Cookie;
import com.genexus.servlet.http.IHttpServletResponse;
import com.genexus.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;

public class SecureCookieHttpServletResponseWrapper extends HttpServletResponseWrapper {

	IHttpServletResponse response;
	String cookieId;
    public SecureCookieHttpServletResponseWrapper(IHttpServletResponse response, String cookieId) {
        super(response);
		this.response = response;
        this.cookieId = cookieId.toLowerCase();
    }
	@Override
	public void addCookie(ICookie cookie) {
		if (!((Cookie)cookie).getSecure() && ((Cookie)cookie).getName().toLowerCase()==cookieId){
			((Cookie)cookie).setSecure(true);
		}
		super.addCookie(cookie);
	}

	public IServletOutputStream getWrapperOutputStream() throws IOException {
    	return response.getOutputStream();
	}
}