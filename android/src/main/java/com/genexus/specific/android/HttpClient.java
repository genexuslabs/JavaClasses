package com.genexus.specific.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import com.genexus.CommonUtil;
import com.genexus.common.interfaces.IExtensionHttpClient;
import com.genexus.internet.HttpClientManual;
import com.genexus.util.GXFile;

import HTTPClient.HTTPConnection;

import javax.net.ssl.SSLSocket;


public class HttpClient implements IExtensionHttpClient {

	@Override
	public void addSDHeaders(String host, String baseURL, Hashtable<String, String> headersToSend) {
		com.genexus.Preferences.getDefaultPreferences().getProperty("USER_LOG_NAMESPACE", "GeneXusUserLog");
	}

	@Override
	public String normalizeEncodingName(String charset, String string) {
		// change default encoding to Utf-8 in android sdt when not found in charset.
		// seems to be the more used encoding http://en.wikipedia.org/wiki/UTF-8 
		// and net and ios use this as default.
		return CommonUtil.normalizeEncodingName(charset, "UTF-8");
	}

	@Override
	public String beforeAddFile(String fileName) {
		return GXFile.convertToLocalFullPath(fileName);
	}

	@Override
	public com.genexus.internet.IHttpClient initHttpClientImpl() {
		return new HttpClientManual();
	}

	@Override
	public void initializeHttpClient(Object clientObj) {
		com.genexus.internet.HttpClient client = (com.genexus.internet.HttpClient) clientObj;
		try
		{
			Class c = HTTPConnection.class;
			String pHost = (String)(c.getMethod("getDefaultProxyHost", new Class[]{}).invoke(null, new Object[]{}));
			if(pHost != null)
			{
				client.setProxyServerHost(pHost);
			}
			int pPort = ((Number)(c.getMethod("getDefaultProxyPort", new Class[]{}).invoke(null, new Object[]{}))).intValue();
			if(pPort != 0)
			{
				client.setProxyServerPort(pPort);
			}
		}catch(Throwable e)
		{ // No estamos usando NUESTRA version del HttpClient (ej OracleAS)
			client.usingExternalHttpClient = true;
			if(!client.issuedExternalHttpClientWarning)
			{
				System.err.println();
				System.err.println("***************************************************************************");
				System.err.println("** Warning: Not using HttpClient library inside GeneXus standard classes **");
				System.err.println("***************************************************************************");
				System.err.println();
				client.issuedExternalHttpClientWarning = true;
			}
		}
		
	}

	private void setProxyHost(String pHost) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void prepareSSLSocket(SSLSocket socket) {
		// enable TLSv1.1/1.2 if available
		// (see https://github.com/rfc2822/davdroid/issues/229)
		String[] supportedProtocols = socket.getSupportedProtocols();
		ArrayList<String> tempList =  new ArrayList<String>();
		Collections.addAll(tempList, supportedProtocols);
		// remove only olds and unsecure protocols (SSLv3 ), TLS in all version are supported.
		tempList.remove("SSLv3");
		// from : https://blog.dev-area.net/2015/08/13/android-4-1-enable-tls-1-1-and-tls-1-2/
		//  add 1.1 and 1.2 if not yet added, at least in Android 4.x
		if (!tempList.contains("TLSv1.1"))
			tempList.add("TLSv1.1");
		if (!tempList.contains("TLSv1.2"))
			tempList.add("TLSv1.2");

		supportedProtocols = tempList.toArray(new String[tempList.size()]);
		socket.setEnabledProtocols(supportedProtocols);
	}
}
