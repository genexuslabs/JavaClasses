package com.genexus.specific.java;

import java.io.InputStream;
import java.util.Hashtable;

import com.genexus.CommonUtil;
import com.genexus.common.interfaces.IExtensionHttpClient;
import com.genexus.internet.HttpClientJavaLib;
import com.genexus.internet.HttpClientManual;

import javax.net.ssl.SSLSocket;

public class HttpClient implements IExtensionHttpClient {

	@Override
	public void addSDHeaders(String host, String baseURL, Hashtable<String, String> headersToSend) {

	}

	@Override
	public String normalizeEncodingName(String charset, String string) {
		return CommonUtil.normalizeEncodingName(charset);
	}

	@Override
	public String beforeAddFile(String fileName) {
		// TODO Auto-generated method stub
		return fileName;
	}

	@Override
	public void initializeHttpClient(Object client) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void prepareSSLSocket(SSLSocket sock) {

	}

	@Override
	public com.genexus.internet.IHttpClient initHttpClientImpl() {
		com.genexus.internet.IHttpClient client = null;
		try (InputStream is = com.genexus.ResourceReader.getResourceAsStream("useoldhttpclient.txt")){
			if (is == null) {
				Class.forName("org.apache.http.impl.conn.PoolingHttpClientConnectionManager");    // httpclient-4.5.14.jar dectected by reflection
				client = new HttpClientJavaLib();
			} else
				client = useHttpClientOldImplementation();
		} catch (ClassNotFoundException e) {
			client = useHttpClientOldImplementation();
		} finally {
			return client;
		}
	}
}
