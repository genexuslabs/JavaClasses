package com.genexus.servlet.http;

public class HttpSessionEvent{
	jakarta.servlet.http.HttpSessionEvent hse;
	public HttpSessionEvent(jakarta.servlet.http.HttpSessionEvent hse) {
		this.hse = hse;
	}

	public IHttpSession getSession() {
		return new HttpSession(hse.getSession());
	}
}
