package com.genexus.specific.java;

import java.util.Hashtable;

import com.genexus.CommonUtil;
import com.genexus.common.interfaces.IExtensionHttpClient;

import javax.net.ssl.SSLSocket;

public class HttpClient implements IExtensionHttpClient {

	@Override
	public void addSDHeaders(String host, String baseURL, Hashtable headersToSend) {

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
}
