package com.genexus;

import com.genexus.util.Encryption;

public final class GXutilJava {
	
	public static String getEncryptedSignature( String value, String key)
	{
		return Encryption.encrypt64(CommonUtil.getHash( com.genexus.security.web.WebSecurityHelper.StripInvalidChars(value), com.genexus.cryptography.Constants.SECURITY_HASH_ALGORITHM), key);
	}
	public static boolean checkEncryptedSignature( String value, String hash, String key)
	{
		return CommonUtil.getHash( com.genexus.security.web.WebSecurityHelper.StripInvalidChars(value), com.genexus.cryptography.Constants.SECURITY_HASH_ALGORITHM).equals(Encryption.decrypt64(hash, key));
	}
	
	public static String buildURLFromHttpClient(com.genexus.internet.HttpClient GXSoapHTTPClient, String serviceName, javax.xml.ws.BindingProvider bProvider)
	{
		if (!GXSoapHTTPClient.getProxyServerHost().equals(""))
		{
			bProvider.getRequestContext().put("https.proxyHost", GXSoapHTTPClient.getProxyServerHost());
		}
			
		if (GXSoapHTTPClient.getProxyServerPort() != 80)
		{
			bProvider.getRequestContext().put("https.proxyPort", GXSoapHTTPClient.getProxyServerPort());
		}
		
		String scheme = "http";
		if (GXSoapHTTPClient.getSecure() == 1)
		{
			scheme = "https";
		}
		java.net.URI url = null;
		try
		{
			url = new java.net.URI(scheme, null, GXSoapHTTPClient.getHost(), GXSoapHTTPClient.getPort(), GXSoapHTTPClient.getBaseURL() + serviceName, null, null);
		}
		catch(java.net.URISyntaxException e)
		{
			return "";
		}
		return url.toString();
	}	



}
