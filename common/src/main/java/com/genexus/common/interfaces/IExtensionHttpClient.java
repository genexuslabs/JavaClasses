package com.genexus.common.interfaces;

import java.util.Hashtable;



public interface IExtensionHttpClient {

	void addSDHeaders(String host, String baseURL, Hashtable<String, String> headersToSend);

	String normalizeEncodingName(String charset, String string);

	String beforeAddFile(String fileName);

	void initializeHttpClient(Object client);

}
