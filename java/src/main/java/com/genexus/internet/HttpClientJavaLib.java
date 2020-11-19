package com.genexus.internet;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.*;
import HTTPClient.*;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLSocketFactory;

public class HttpClientJavaLib extends GXHttpClient {

	private int statusCode;
	private String reasonLine;
	private PoolingHttpClientConnectionManager connManager = null;
	private HttpClientContext httpClientContext = null;
	private CredentialsProvider credentialsProvider = null;
	private RequestConfig reqConfig = null;		// Atributo usado en la ejecucion del metodo (por ejemplo, httpGet, httpPost)


	private void resetExecParams() {
		this.statusCode = 0;
		this.reasonLine = "";
		this.connManager = null;
		this.reqConfig = null;
		this.httpClientContext = null;
		this.credentialsProvider = null;
		resetErrors();
	}

	private SSLConnectionSocketFactory getSSLSecureInstance() {
		return new SSLConnectionSocketFactory(
			SSLContexts.createDefault(),
			new String[] { "TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3" },
			null,
			SSLConnectionSocketFactory.getDefaultHostnameVerifier());
	}

	public void execute(String method, String url) {
		resetExecParams();


		url = getURLValid(url);		// Funcion genera parte del path en adelante de la URL
		if (getSecure() == 1)		// Se completa con esquema y host
			url = "https://" + getHost();
		else
			url = "http://" + getHost();

//		try {
			if (getHostChanged()) {
				if (getSecure() == 1 && getPort() == 80)
				{
					setPort(443);
				}
				if (getSecure() != 0) {
					SSLConnectionSocketFactory sslsf = getSSLSecureInstance();
				}
				SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(getTcpNoDelay()).build();
				this.connManager = new PoolingHttpClientConnectionManager();
				connManager.setDefaultSocketConfig(socketConfig);

				CookieStore cookieStore = null;
				if (getIncludeCookies())
					cookieStore = new BasicCookieStore();
			}

			if (proxyInfoChanged)
				this.reqConfig = RequestConfig.custom()
					.setConnectionRequestTimeout(getTimeout()*1000)
					.setProxy(new HttpHost(getProxyServerHost(),getProxyServerPort()))
					.build();
			else
				this.reqConfig = RequestConfig.custom()
					.setConnectionRequestTimeout(getTimeout()*1000)
					.build();

			if(getHostChanged() || getAuthorizationChanged()) { // Si el host cambio o si se agrego alguna credencial
				this.credentialsProvider = new BasicCredentialsProvider();

				for (Enumeration en = getBasicAuthorization().elements(); en.hasMoreElements(); )
				{
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					this.credentialsProvider.setCredentials(
						new AuthScope(getHost(),getPort(),p.realm, AuthSchemes.BASIC),
						new UsernamePasswordCredentials(p.user,p.password));
				}

				for (Enumeration en = getDigestAuthorization().elements(); en.hasMoreElements(); )
				{
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					this.credentialsProvider.setCredentials(
						new AuthScope(getHost(),getPort(),p.realm,AuthSchemes.DIGEST),
						new UsernamePasswordCredentials(p.user,p.password));
				}

				for (Enumeration en = getDigestAuthorization().elements(); en.hasMoreElements(); )
				{
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					try {
						this.credentialsProvider.setCredentials(
							new AuthScope(getHost(),getPort(),p.realm,AuthSchemes.NTLM),
							new NTCredentials(p.user,p.password, InetAddress.getLocalHost().getHostName(),getHost()));
					} catch (UnknownHostException e) {
						this.credentialsProvider.setCredentials(
							new AuthScope(getHost(),getPort(),p.realm,AuthSchemes.NTLM),
							new NTCredentials(p.user,p.password, "localhost",getHost()));
					}

				}

			}

			setHostChanged(false);
			setAuthorizationChanged(false); // Desmarco las flags

			if(proxyInfoChanged || getAuthorizationProxyChanged()) { 	// Si el proxyHost cambio o si se agrego alguna credencial para el proxy
				for (Enumeration en = getBasicProxyAuthorization().elements(); en.hasMoreElements(); )
				{
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					this.credentialsProvider.setCredentials(
						new AuthScope(getProxyServerHost(),getProxyServerPort(),p.realm,AuthSchemes.BASIC),
						new UsernamePasswordCredentials(p.user,p.password));
				}

				for (Enumeration en = getDigestAuthorization().elements(); en.hasMoreElements(); )
				{
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					this.credentialsProvider.setCredentials(
						new AuthScope(getProxyServerHost(),getProxyServerPort(),p.realm,AuthSchemes.DIGEST),
						new UsernamePasswordCredentials(p.user,p.password));
				}

				for (Enumeration en = getDigestAuthorization().elements(); en.hasMoreElements(); )
				{
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					try {
						this.credentialsProvider.setCredentials(
							new AuthScope(getProxyServerHost(),getProxyServerPort(),p.realm,AuthSchemes.NTLM),
							new NTCredentials(p.user,p.password, InetAddress.getLocalHost().getHostName(),getProxyServerHost()));
					} catch (UnknownHostException e) {
						this.credentialsProvider.setCredentials(
							new AuthScope(getProxyServerHost(),getProxyServerPort(),p.realm,AuthSchemes.NTLM),
							new NTCredentials(p.user,p.password, "localhost",getProxyServerHost()));
					}

				}

			}

			proxyInfoChanged = false; // Desmarco las flags
			setAuthorizationProxyChanged(false);

			// Al final de cada ejecucion se setean los atributos

			if (method.equalsIgnoreCase("GET")) {


			} else if (method.equalsIgnoreCase("POST")) {

			} else if (method.equalsIgnoreCase("PUT")) {

			} else if (method.equalsIgnoreCase("DELETE")) {

			} else {
				// VER COMO TRATAR ACA LOS OTROS METODOS QUE PUEDEN VENIR
			}

//		} catch {
//
//		}



		resetState();

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