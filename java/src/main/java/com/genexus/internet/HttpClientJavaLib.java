package com.genexus.internet;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class HttpClientJavaLib extends GXHttpClient {

	public void setUrl(String url) {
	}

	public void setURL(String stringURL) {
	}

	public void setHost(String host) {
	}

	public String getHost() {
		return "";
	}

	public void setWSDLURL(String WSDLURL) {
	}

	public void setBaseURL(String baseURL) {
	}

	public String getWSDLURL() {
		return "";
	}

	public String getBaseURL() {
		return "";
	}

	public void setPort(int port) {
	}

	public int getPort() {
		return 0;
	}

	public byte getSecure() {
		return 0;
	}

	public void setSecure(int secure) {
	}

	public void setTimeout(int timeout) {
	}

	public int getTimeout() {
		return 0;
	}

	public void setTcpNoDelay(boolean tcpNoDelay) {
	}

	public void addAuthentication(int type, String realm, String name, String value) {
	}

	public void addProxyAuthentication(int type, String realm, String name, String value) {
	}

	public void addCertificate(String fileName) {
	}

	public void addHeader(String name, String value) {
	}

	public void addVariable(String name, String value) {
	}

	public void addBytes(byte[] value) {
	}

	public void addString(String value) {
	}

	public void addFile(String fileName) {
	}

	public void addFile(String fileName, String varName) {
	}

	public void addStringWriter(StringWriter writer, StringBuffer encoding) {
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