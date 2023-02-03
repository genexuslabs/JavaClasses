package com.genexus.cloud.serverless.aws.handler.internal;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;

public class GxAwsLambdaHttpSession implements HttpSession {
	@Override
	public long getCreationTime() {
		return 0;
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public long getLastAccessedTime() {
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		return null;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {

	}

	@Override
	public int getMaxInactiveInterval() {
		return 0;
	}

	@Override
	public HttpSessionContext getSessionContext() {
		return null;
	}

	@Override
	public Object getAttribute(String name) {
		return null;
	}

	@Override
	public Object getValue(String name) {
		return null;
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return null;
	}

	@Override
	public String[] getValueNames() {
		return new String[0];
	}

	@Override
	public void setAttribute(String name, Object value) {
		System.out.println("Setting Session Value");
	}

	@Override
	public void putValue(String name, Object value) {
		System.out.println("Put Session Value");
	}

	@Override
	public void removeAttribute(String name) {

	}

	@Override
	public void removeValue(String name) {

	}

	@Override
	public void invalidate() {

	}

	@Override
	public boolean isNew() {
		return false;
	}
}
