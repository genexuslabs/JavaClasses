package com.genexus.servlet.http;

public class HttpSessionBindingEvent extends jakarta.servlet.http.HttpSessionBindingEvent {

	public HttpSessionBindingEvent(HttpSession session, String name) {
		super((jakarta.servlet.http.HttpSession)session, name);
	}
}
