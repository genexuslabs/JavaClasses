package com.genexus.specific.java;

import org.apache.http.client.methods.HttpRequestBase;

import java.net.URI;

public class HttpConnectMethod extends HttpRequestBase {
	public static final String METHOD_NAME = "CONNECT";

	public HttpConnectMethod() {
	}

	public HttpConnectMethod(URI uri) {
		this.setURI(uri);
	}

	public HttpConnectMethod(String uri) {
		this.setURI(URI.create(uri));
	}

	public String getMethod() {
		return METHOD_NAME;
	}
}
