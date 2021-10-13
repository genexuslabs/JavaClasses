package com.genexus.servlet.http;


public class HttpSessionEvent{

	javax.servlet.http.HttpSessionEvent hse;

	public HttpSessionEvent(javax.servlet.http.HttpSessionEvent hse) {
		this.hse = hse;
	}

	public IHttpSession getSession() {
		return new HttpSession(hse.getSession());
	}
}
