package com.genexus.internet;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.*;
import HTTPClient.*;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.cookie.*;

import javax.net.ssl.SSLSocketFactory;

public class HttpClientJavaLib extends GXHttpClient {

	private static HttpClientJavaLib httpClientJavaLib = new HttpClientJavaLib();
	private HttpClientJavaLib() {}
	public static HttpClientJavaLib getInstance() {
		return httpClientJavaLib;
	}
	







	private int statusCode;
	private String reasonLine;
	private HttpClientBuilder httpClientBuilder;
//	private PoolingHttpClientConnectionManager connManager = null;		// USAR ESTO SI EN UN FUTURO SE QUIEREN MANEJAR LAS CONEXIONES CON MULTITHREADING
	private HttpClientContext httpClientContext = null;
	private CredentialsProvider credentialsProvider = null;
	private RequestConfig reqConfig = null;		// Atributo usado en la ejecucion del metodo (por ejemplo, httpGet, httpPost)
	private CookieStore cookies = new BasicCookieStore();
	private CookieStore cookiesToSend = null;


	private void resetExecParams() {
		this.statusCode = 0;
		this.reasonLine = "";
//		this.connManager = null;
		this.reqConfig = null;
		this.httpClientContext = null;
		this.credentialsProvider = null;
		this.cookiesToSend = null;
		resetErrors();
	}

	private SSLConnectionSocketFactory getSSLSecureInstance() {
		return new SSLConnectionSocketFactory(
			SSLContexts.createDefault(),
			new String[] { "TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3" },
			null,
			SSLConnectionSocketFactory.getDefaultHostnameVerifier());
	}

	private CookieStore setAllStoredCookies() {
		if (this.cookies.getCookies().isEmpty())
			return null;

		CookieStore cookiesToSend = new BasicCookieStore();
		this.cookies.clearExpired(new Date());
		for (Cookie c : this.cookies.getCookies()) {
			if (getHost().equalsIgnoreCase(c.getDomain()) && getBaseURL().equalsIgnoreCase(c.getPath()))
				cookiesToSend.addCookie(c);
		}
		return cookiesToSend;
	}

	private void SetCookieAtr(CookieStore cookiesToSend) {
		for (Cookie c : cookiesToSend.getCookies())
			this.cookies.addCookie(c);
	}

	public void execute(String method, String url) {
		resetExecParams();

		this.httpClientBuilder = HttpClients.custom();
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;

			url = getURLValid(url);		// Funcion genera parte del path en adelante de la URL
		if (getSecure() == 1)		// Se completa con esquema y host
			url = "https://" + getHost() + url.trim();
		else
			url = "http://" + getHost() + url.trim();

		try {
			if (getHostChanged()) {
				if (getSecure() == 1 && getPort() == 80) {
					setPort(443);
				}
				if (getSecure() != 0) {
					SSLConnectionSocketFactory sslsf = getSSLSecureInstance();
					this.httpClientBuilder.setSSLSocketFactory(sslsf);        // seteo SSL en HttpClientBuilder
				}
				SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(getTcpNoDelay()).build();	// Seteo de TcpNoDelay
//				this.connManager = new PoolingHttpClientConnectionManager();
//				connManager.setDefaultSocketConfig(socketConfig);
//				this.httpClientBuilder.setConnectionManager(connManager);
				this.httpClientBuilder.setDefaultSocketConfig(socketConfig);

				if (!getIncludeCookies())
					this.cookies.clear();
				this.cookiesToSend = setAllStoredCookies();
				this.httpClientBuilder.setDefaultCookieStore(this.cookiesToSend);    // CookiesSeteo CookieStore
			}

			if (proxyInfoChanged)
				this.reqConfig = RequestConfig.custom()
					.setConnectionRequestTimeout(getTimeout() * 1000)	// Se multiplica por 1000 ya que tiene que ir en ms y se recibe en segundos
					.setProxy(new HttpHost(getProxyServerHost(), getProxyServerPort()))
					.build();
			else
				this.reqConfig = RequestConfig.custom()
					.setConnectionRequestTimeout(getTimeout() * 1000)
					.build();

			if (getHostChanged() || getAuthorizationChanged()) { // Si el host cambio o si se agrego alguna credencial
				this.credentialsProvider = new BasicCredentialsProvider();

				for (Enumeration en = getBasicAuthorization().elements(); en.hasMoreElements(); ) {
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					this.credentialsProvider.setCredentials(
						new AuthScope(getHost(), getPort(), p.realm, AuthSchemes.BASIC),
						new UsernamePasswordCredentials(p.user, p.password));
				}

				for (Enumeration en = getDigestAuthorization().elements(); en.hasMoreElements(); ) {
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					this.credentialsProvider.setCredentials(
						new AuthScope(getHost(), getPort(), p.realm, AuthSchemes.DIGEST),
						new UsernamePasswordCredentials(p.user, p.password));
				}

				for (Enumeration en = getDigestAuthorization().elements(); en.hasMoreElements(); ) {
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					try {
						this.credentialsProvider.setCredentials(
							new AuthScope(getHost(), getPort(), p.realm, AuthSchemes.NTLM),
							new NTCredentials(p.user, p.password, InetAddress.getLocalHost().getHostName(), getHost()));
					} catch (UnknownHostException e) {
						this.credentialsProvider.setCredentials(
							new AuthScope(getHost(), getPort(), p.realm, AuthSchemes.NTLM),
							new NTCredentials(p.user, p.password, "localhost", getHost()));
					}

				}

			}

			setHostChanged(false);
			setAuthorizationChanged(false); // Desmarco las flags

			if (proxyInfoChanged || getAuthorizationProxyChanged()) {    // Si el proxyHost cambio o si se agrego alguna credencial para el proxy
				if (this.credentialsProvider == null) {
					this.credentialsProvider = new BasicCredentialsProvider();
				}

				for (Enumeration en = getBasicProxyAuthorization().elements(); en.hasMoreElements(); ) {
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					this.credentialsProvider.setCredentials(
						new AuthScope(getProxyServerHost(), getProxyServerPort(), p.realm, AuthSchemes.BASIC),
						new UsernamePasswordCredentials(p.user, p.password));
				}

				for (Enumeration en = getDigestAuthorization().elements(); en.hasMoreElements(); ) {
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					this.credentialsProvider.setCredentials(
						new AuthScope(getProxyServerHost(), getProxyServerPort(), p.realm, AuthSchemes.DIGEST),
						new UsernamePasswordCredentials(p.user, p.password));
				}

				for (Enumeration en = getDigestAuthorization().elements(); en.hasMoreElements(); ) {
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					try {
						this.credentialsProvider.setCredentials(
							new AuthScope(getProxyServerHost(), getProxyServerPort(), p.realm, AuthSchemes.NTLM),
							new NTCredentials(p.user, p.password, InetAddress.getLocalHost().getHostName(), getProxyServerHost()));
					} catch (UnknownHostException e) {
						this.credentialsProvider.setCredentials(
							new AuthScope(getProxyServerHost(), getProxyServerPort(), p.realm, AuthSchemes.NTLM),
							new NTCredentials(p.user, p.password, "localhost", getProxyServerHost()));
					}

				}

			}

			proxyInfoChanged = false; // Desmarco las flags
			setAuthorizationProxyChanged(false);

			if (this.credentialsProvider != null) {    // En caso que se haya agregado algun tipo de autenticacion (ya sea para el host destino como para el proxy) se agrega al contexto
				this.httpClientContext.setCredentialsProvider(this.credentialsProvider);
			}

			httpClient = this.httpClientBuilder.build();

			if (method.equalsIgnoreCase("GET")) {
				HttpGet httpget = new HttpGet(url.trim());
				httpget.setConfig(reqConfig);
				response = httpClient.execute(httpget, this.httpClientContext);

			} else if (method.equalsIgnoreCase("POST")) {

			} else if (method.equalsIgnoreCase("PUT")) {

			} else if (method.equalsIgnoreCase("DELETE")) {

			} else {
				// VER COMO TRATAR ACA LOS OTROS METODOS QUE PUEDEN VENIR
			}

			SetCookieAtr(this.cookiesToSend);		// Se setean las cookies devueltas en la lista de cookies

		} catch (IOException e) {
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
		finally {		// Se cierra la conexion siempre al final
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			resetState();
		}

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