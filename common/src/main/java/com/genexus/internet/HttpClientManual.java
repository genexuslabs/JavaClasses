package com.genexus.internet;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;

public class HttpClientManual extends GXHttpClient {

	@Override
	public void setUrl(String url) {

	}

	@Override
	public void setURL(String stringURL) {

	}

	@Override
	public void setHost(String host) {

	}

	@Override
	public String getHost() {
		return null;
	}

	@Override
	public void setWSDLURL(String WSDLURL) {

	}

	@Override
	public void setBaseURL(String baseURL) {

	}

	@Override
	public String getWSDLURL() {
		return null;
	}

	@Override
	public String getBaseURL() {
		return null;
	}

	@Override
	public void setPort(int port) {

	}

	@Override
	public int getPort() {
		return 0;
	}

	@Override
	public byte getSecure() {
		return 0;
	}

	@Override
	public void setSecure(int secure) {

	}

	@Override
	public void setTimeout(int timeout) {

	}

	@Override
	public int getTimeout() {
		return 0;
	}

	@Override
	public void setTcpNoDelay(boolean tcpNoDelay) {

	}

	@Override
	public void addAuthentication(int type, String realm, String name, String value) {

	}

	@Override
	public void addProxyAuthentication(int type, String realm, String name, String value) {

	}

	@Override
	public void addCertificate(String fileName) {

	}

	@Override
	public void addHeader(String name, String value) {

	}

	@Override
	public void addVariable(String name, String value) {

	}

	@Override
	public void addBytes(byte[] value) {

	}

	@Override
	public void addString(String value) {

	}

	@Override
	public void addFile(String fileName) {

	}

	@Override
	public void addFile(String fileName, String varName) {

	}

	@Override
	public void addStringWriter(StringWriter writer, StringBuffer encoding) {

	}

	@Override
	public void execute(String method, String url) {

	}

	@Override
	public int getStatusCode() {
		return 0;
	}

	@Override
	public String getReasonLine() {
		return null;
	}

	@Override
	public void getHeader(String name, long[] value) {

	}

	@Override
	public String getHeader(String name) {
		return null;
	}

	@Override
	public void getHeader(String name, String[] value) {

	}

	@Override
	public void getHeader(String name, Date[] value) {

	}

	@Override
	public void getHeader(String name, double[] value) {

	}

	@Override
	public InputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public String getString() {
		return null;
	}

	@Override
	public void toFile(String fileName) {

	}

	@Override
	public void cleanup() {

	}
}
