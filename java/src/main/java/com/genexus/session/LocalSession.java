package com.genexus.session;

import com.genexus.servlet.http.IHttpSession;

import java.util.Enumeration;
import java.util.Hashtable;

public class LocalSession implements IHttpSession {
	private Hashtable<String, Object> sessionValues;

	@Override
	public Object getAttribute(String name) {
		return null;
	}

	@Override
	public void setAttribute(String name, Object value) {

	}

	@Override
	public void removeAttribute(String name) {

	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return null;
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public void invalidate() {

	}
}
