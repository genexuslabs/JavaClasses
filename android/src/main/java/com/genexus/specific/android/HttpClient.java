package com.genexus.specific.android;

import java.util.Hashtable;

import com.genexus.CommonUtil;
import com.genexus.common.interfaces.IExtensionHttpClient;
import com.genexus.util.GXFile;

import HTTPClient.HTTPConnection;


public class HttpClient implements IExtensionHttpClient {

	@Override
	public void addSDHeaders(String host, String baseURL, Hashtable headersToSend) {
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

}
