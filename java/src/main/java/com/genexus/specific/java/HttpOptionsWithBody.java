package com.genexus.specific.java;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.util.Args;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class HttpOptionsWithBody extends HttpEntityEnclosingRequestBase {
	public static final String METHOD_NAME = "OPTIONS";

	public String getMethod() {
		return METHOD_NAME;
	}

	public Set<String> getAllowedMethods(HttpResponse response) {
		Args.notNull(response, "HTTP response");
		HeaderIterator it = response.headerIterator("Allow");
		HashSet methods = new HashSet();

		while(it.hasNext()) {
			Header header = it.nextHeader();
			HeaderElement[] elements = header.getElements();
			HeaderElement[] arr$ = elements;
			int len$ = elements.length;

			for(int i$ = 0; i$ < len$; ++i$) {
				HeaderElement element = arr$[i$];
				methods.add(element.getName());
			}
		}

		return methods;
	}

	public HttpOptionsWithBody(final String uri) {
		super();
		setURI(URI.create(uri));
	}

	public HttpOptionsWithBody(final URI uri) {
		super();
		setURI(uri);
	}

	public HttpOptionsWithBody() {
		super();
	}
}
