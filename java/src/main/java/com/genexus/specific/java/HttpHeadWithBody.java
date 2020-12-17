package com.genexus.specific.java;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

public class HttpHeadWithBody extends HttpEntityEnclosingRequestBase {
	public static final String METHOD_NAME = "HEAD";

	public String getMethod() {
		return METHOD_NAME;
	}

	public HttpHeadWithBody(final String uri) {
		super();
		setURI(URI.create(uri));
	}

	public HttpHeadWithBody(final URI uri) {
		super();
		setURI(uri);
	}

	public HttpHeadWithBody() {
		super();
	}
}
