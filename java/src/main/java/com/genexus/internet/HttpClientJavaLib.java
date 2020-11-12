package com.genexus.internet;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import org.apache.http.client.*;

public class HttpClientJavaLib extends GXHttpClient {

	public void addAuthentication(int type, String realm, String name, String value) {
	}

	public void addProxyAuthentication(int type, String realm, String name, String value) {
	}


	public void addHeader(String name, String value) {
	}

	public void execute(String method, String url) {
	}

	public int getStatusCode() {
		return 0;
	}

	public String getReasonLine() {
		return "";
	}

	public void getHeader(String name, long[] value) {
	}

	public String getHeader(String name) {
		return "";
	}

	public void getHeader(String name, String[] value) {
	}

	public void getHeader(String name, java.util.Date[] value) {
	}

	public void getHeader(String name, double[] value) {
	}

	public InputStream getInputStream() throws IOException {
		return new InputStream() {
			@Override
			public int read() throws IOException {

				return 0;
			}
		};
	}

	public String getString() {
		return "";
	}

	public void toFile(String fileName) {
	}


	public void cleanup() {
	}
}