package com.genexus.specific.java;

import java.util.Hashtable;

import com.genexus.CommonUtil;
import com.genexus.common.interfaces.IExtensionHttpClient;
import com.genexus.common.interfaces.SpecificImplementation;
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

	private com.genexus.internet.IHttpClient useHttpClientOldImplementation() {
		com.genexus.internet.IHttpClient client = new HttpClientManual();
		SpecificImplementation.HttpClient.initializeHttpClient(client);
		return client;
	}

	@Override
	public com.genexus.internet.IHttpClient initHttpClientImpl() {
		com.genexus.internet.IHttpClient client = null;
		try {
			if (com.genexus.ResourceReader.getResourceAsStream("useoldhttpclient.txt") == null)
				client = new HttpClientJavaLib();
			else {
				client = useHttpClientOldImplementation();
			}

		} catch (Throwable e) {

			client = useHttpClientOldImplementation();

		} finally {

			return client;

		}
	}
}
