package com.genexus.servlet.http;

import java.util.Enumeration;

public interface IHttpSession {
	Object getAttribute(String name);
	void setAttribute(String name, Object value);
	void removeAttribute(String name);
	Enumeration<String> getAttributeNames();
	String getId();
	void invalidate();
}
