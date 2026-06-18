package com.genexus.filters;

import com.genexus.servlet.IServletOutputStream;
import com.genexus.servlet.http.ICookie;
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
		if (!cookie.getSecure() && cookie.getName().toLowerCase().equals(cookieId)){
			cookie.setSecure(true);
		}
		super.addCookie(cookie);
	}

	@Override
	public void addHeader(String name, String value) {
		if (name.equalsIgnoreCase("Set-Cookie") && value.toLowerCase().startsWith(cookieId.toLowerCase() + "=")) {
			if (!value.toLowerCase().contains("secure")) {
				value += "; Secure";
			}
		}
		super.addHeader(name, value);
	}

	public IServletOutputStream getWrapperOutputStream() throws IOException {
    	return response.getOutputStream();
	}
}