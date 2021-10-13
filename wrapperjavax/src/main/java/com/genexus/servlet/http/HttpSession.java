package com.genexus.servlet.http;

import java.util.Enumeration;

public class HttpSession implements IHttpSession{
	private javax.servlet.http.HttpSession session;

	public HttpSession(javax.servlet.http.HttpSession session) {
		this.session = session;
	}

	public javax.servlet.http.HttpSession getWrappedClass() {
		return session;
	}

	public Object getAttribute(String name) {
		return session.getAttribute(name);
	}

	public void setAttribute(String name, Object value) {
		session.setAttribute(name, value);
	}

	public void removeAttribute(String name) {
		session.removeAttribute(name);
	}

	public Enumeration<String> getAttributeNames() {
		return session.getAttributeNames();
	}

	public String getId() {
		return session.getId();
	}

	public void invalidate() {
		session.invalidate();
	}
}
