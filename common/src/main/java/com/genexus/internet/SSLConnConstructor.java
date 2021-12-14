package com.genexus.internet;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class SSLConnConstructor {

	public static SSLConnectionSocketFactory getSSLSecureInstance(String[] supportedProtocols) {
		try {
			SSLContext sslContext = SSLContextBuilder
				.create()
				.loadTrustMaterial(new TrustSelfSignedStrategy())
				.build();
			return new SSLConnectionSocketFactory(
				sslContext,
				supportedProtocols,
				null,
				SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		} catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
			e.printStackTrace();
		}
		return new SSLConnectionSocketFactory(
			SSLContexts.createDefault(),
			supportedProtocols,
			null,
			SSLConnectionSocketFactory.getDefaultHostnameVerifier());
	}
}
