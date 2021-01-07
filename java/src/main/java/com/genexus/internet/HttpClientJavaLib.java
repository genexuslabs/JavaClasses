package com.genexus.internet;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.genexus.CommonUtil;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.specific.java.*;
import com.genexus.specific.java.HttpClient;
import com.genexus.webpanels.BlobsCleaner;
import com.sun.istack.NotNull;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.*;
import HTTPClient.*;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.cookie.*;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class HttpClientJavaLib extends GXHttpClient {

	private static HttpClientJavaLib instance = null;
	private HttpClientJavaLib() {init();}
	public static HttpClientJavaLib getInstance() {
		if(instance == null)
			instance = new HttpClientJavaLib();
		return instance;
	}

	private static ConcurrentHashMap<Long,Integer> statusCode;
	private static ConcurrentHashMap<Long,String> reasonLine;
	private static ConcurrentHashMap<Long,HttpClientBuilder> httpClientBuilder;
	private static ConcurrentHashMap<Long,PoolingHttpClientConnectionManager> connManager;
	private static ConcurrentHashMap<Long,HttpClientContext> httpClientContext;
	private static ConcurrentHashMap<Long,CloseableHttpClient> httpClient;
	private static ConcurrentHashMap<Long,CloseableHttpResponse> response;
	private static ConcurrentHashMap<Long,CredentialsProvider> credentialsProvider;
	private static ConcurrentHashMap<Long,RequestConfig> reqConfig;		// Atributo usado en la ejecucion del metodo (por ejemplo, httpGet, httpPost)
	private static ConcurrentHashMap<Long,CookieStore> cookies;

	private void init() {
		httpClient = new ConcurrentHashMap<>();
		httpClientBuilder = new ConcurrentHashMap<>();
		cookies = new ConcurrentHashMap<>();
		statusCode = new ConcurrentHashMap<>();
		reasonLine = new ConcurrentHashMap<>();
		httpClientContext = new ConcurrentHashMap<>();
		response = new ConcurrentHashMap<>();
		credentialsProvider = new ConcurrentHashMap<>();
		reqConfig = new ConcurrentHashMap<>();
		connManager = new ConcurrentHashMap<>();
	}

	private void setConnManager() {
		if (connManager.get(Thread.currentThread().getId()) == null) {
			Registry<ConnectionSocketFactory> socketFactoryRegistry =
				RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", PlainConnectionSocketFactory.INSTANCE).register("https", getSSLSecureInstance())
					.build();
			connManager.put(Thread.currentThread().getId(), new PoolingHttpClientConnectionManager(socketFactoryRegistry));
			connManager.get(Thread.currentThread().getId()).setMaxTotal(100);
			connManager.get(Thread.currentThread().getId()).setDefaultMaxPerRoute(8);
		} else {
			connManager.get(Thread.currentThread().getId()).closeExpiredConnections();
			connManager.get(Thread.currentThread().getId()).closeIdleConnections(30, TimeUnit.SECONDS);        // Seteados 30 de forma estandar
		}
	}

	private void resetExecParams() {
		setConnManager();
		if (httpClientBuilder.get(Thread.currentThread().getId()) == null)
			initBaseAtr();	// Inicializacion de los atributos en la clase base del thread
			httpClientBuilder.put(Thread.currentThread().getId(),
				HttpClients.custom().setConnectionManager(connManager.get(Thread.currentThread().getId())).setConnectionManagerShared(true));
		if (cookies.get(Thread.currentThread().getId()) == null)
			cookies.put(Thread.currentThread().getId(),
				new BasicCookieStore());
		statusCode.put(Thread.currentThread().getId(), 0);
		reasonLine.put(Thread.currentThread().getId(),"");
		resetErrors();
	}


	private void resetErrors()
	{
		if (getErrCode() != 0)
			cleanReqAndRes();
		setErrCode(0);
		setErrDescription("");
	}

	private void resetStateAdapted()
	{
		resetState();
		getheadersToSend().clear();
	}

	private String getURLValid(String url) {
		URI uri;
		try
		{
			uri = new URI(url);		// En caso que la URL pasada por parametro no sea una URL valida, salta una excepcion en esta linea, y se continua haciendo todo el proceso con los datos ya guardados como atributos
			setPrevURLhost(getHost());
			setPrevURLbaseURL(getBaseURL());
			setPrevURLport(getPort());
			setPrevURLsecure(getSecure());
			setIsURL(true);
			setURL(url);

			StringBuilder relativeUri = new StringBuilder();
			if (uri.getPath() != null) {
				relativeUri.append(uri.getPath());
			}
			if (uri.getQueryString() != null) {
				relativeUri.append('?').append(uri.getQueryString());
			}
			if (uri.getFragment() != null) {
				relativeUri.append('#').append(uri.getFragment());
			}
			return relativeUri.toString();
		}
		catch (ParseException e)
		{
			if (url.isEmpty())
				url = "/null";
			return url;
		}
	}


	private SSLConnectionSocketFactory getSSLSecureInstance() {
		try {
			SSLContext sslContext = SSLContextBuilder
				.create()
				.loadTrustMaterial(new TrustSelfSignedStrategy())
				.build();
			return new SSLConnectionSocketFactory(
				sslContext,
				new String[] { "TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3" },
				null,
				SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		return new SSLConnectionSocketFactory(
			SSLContexts.createDefault(),
			new String[] { "TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3" },
			null,
			SSLConnectionSocketFactory.getDefaultHostnameVerifier());
	}

	private CookieStore setAllStoredCookies() {
		if (cookies.get(Thread.currentThread().getId()).getCookies().isEmpty())
			return new BasicCookieStore();

		CookieStore cookiesToSend = new BasicCookieStore();
		cookies.get(Thread.currentThread().getId()).clearExpired(new Date());
		for (Cookie c : cookies.get(Thread.currentThread().getId()).getCookies()) {
			if (getHost().equalsIgnoreCase(c.getDomain()) || (getHost().substring(4).equalsIgnoreCase(c.getDomain())))  	// el substring(4) se debe a que el host puede estar guardado con el "www." previo al host
//				&& (getBaseURL().equalsIgnoreCase(c.getPath()) || (getBaseURL().isEmpty() && c.getPath().equalsIgnoreCase("/"))))
				cookiesToSend.addCookie(c);
		}
		return cookiesToSend;
	}

	private void SetCookieAtr(CookieStore cookiesToSend) {
		for (Cookie c : cookiesToSend.getCookies())
			cookies.get(Thread.currentThread().getId()).addCookie(c);
	}

	public void execute(String method, String url) {
		resetExecParams();

		url = getURLValid(url);		// Funcion genera parte del path en adelante de la URL


		try {
			CookieStore cookiesToSend = null;
			if (getHostChanged()) {
				if (getSecure() == 1 && getPort() == 80) {
					setPort(443);
				}

				SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(getTcpNoDelay()).build();	// Seteo de TcpNoDelay
				this.httpClientBuilder.get(Thread.currentThread().getId()).setDefaultSocketConfig(socketConfig);

				if (!getIncludeCookies())
					cookies.get(Thread.currentThread().getId()).clear();
				cookiesToSend = setAllStoredCookies();
				this.httpClientBuilder.get(Thread.currentThread().getId()).setDefaultCookieStore(cookiesToSend);    // Cookies Seteo CookieStore
			}

			if (getProxyInfoChanged()) {
				HttpHost proxy = new HttpHost(getProxyServerHost(), getProxyServerPort());
				this.httpClientBuilder.get(Thread.currentThread().getId()).setRoutePlanner(new DefaultProxyRoutePlanner(proxy));
				this.reqConfig.put(Thread.currentThread().getId(),RequestConfig.custom()
					.setSocketTimeout(getTimeout() * 1000)    // Se multiplica por 1000 ya que tiene que ir en ms y se recibe en segundos
					.setProxy(proxy)
					.build());
			} else {
				this.httpClientBuilder.get(Thread.currentThread().getId()).setRoutePlanner(null);
				this.reqConfig.put(Thread.currentThread().getId(), RequestConfig.custom()
					.setConnectTimeout(getTimeout() * 1000)		// Se multiplica por 1000 ya que tiene que ir en ms y se recibe en segundos
					.build());
			}

			if (getHostChanged() || getAuthorizationChanged()) { // Si el host cambio o si se agrego alguna credencial
				this.credentialsProvider.put(Thread.currentThread().getId(), new BasicCredentialsProvider());

				for (Enumeration en = getBasicAuthorization().elements(); en.hasMoreElements(); ) {
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					this.credentialsProvider.get(Thread.currentThread().getId()).setCredentials(
//						new AuthScope(getHost(), getPort(), p.realm, AuthSchemes.BASIC),
						AuthScope.ANY,
						new UsernamePasswordCredentials(p.user, p.password));
				}

				for (Enumeration en = getDigestAuthorization().elements(); en.hasMoreElements(); ) {
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					this.credentialsProvider.get(Thread.currentThread().getId()).setCredentials(
						new AuthScope(getHost(), getPort(), p.realm, AuthSchemes.DIGEST),
						new UsernamePasswordCredentials(p.user, p.password));
				}

				for (Enumeration en = getNTLMAuthorization().elements(); en.hasMoreElements(); ) {
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					this.httpClientBuilder.get(Thread.currentThread().getId()).setDefaultAuthSchemeRegistry(RegistryBuilder.<AuthSchemeProvider> create()
						.register(AuthSchemes.NTLM, new NTLMSchemeFactory()).register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory(true)).build());
					try {
						credentialsProvider.get(Thread.currentThread().getId()).setCredentials(
							new AuthScope(getHost(), getPort(), p.realm, AuthSchemes.NTLM),
							new NTCredentials(p.user, p.password, InetAddress.getLocalHost().getHostName(), getHost()));
					} catch (UnknownHostException e) {
						credentialsProvider.get(Thread.currentThread().getId()).setCredentials(
							new AuthScope(getHost(), getPort(), p.realm, AuthSchemes.NTLM),
							new NTCredentials(p.user, p.password, "localhost", getHost()));
					}

				}

			}

			setHostChanged(false);
			setAuthorizationChanged(false); // Desmarco las flags

			if (getProxyInfoChanged() || getAuthorizationProxyChanged()) {    // Si el proxyHost cambio o si se agrego alguna credencial para el proxy
				if (this.credentialsProvider.get(Thread.currentThread().getId()) == null) {
					this.credentialsProvider.put(Thread.currentThread().getId(), new BasicCredentialsProvider());
				}

				for (Enumeration en = getBasicProxyAuthorization().elements(); en.hasMoreElements(); ) {
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					this.credentialsProvider.get(Thread.currentThread().getId()).setCredentials(
						AuthScope.ANY,
//						new AuthScope(getProxyServerHost(), getProxyServerPort(), p.realm, AuthSchemes.BASIC),
						new UsernamePasswordCredentials(p.user, p.password));
				}

				for (Enumeration en = getDigestProxyAuthorization().elements(); en.hasMoreElements(); ) {
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					this.credentialsProvider.get(Thread.currentThread().getId()).setCredentials(
						new AuthScope(getProxyServerHost(), getProxyServerPort(), p.realm, AuthSchemes.DIGEST),
						new UsernamePasswordCredentials(p.user, p.password));
				}

				for (Enumeration en = getNTLMProxyAuthorization().elements(); en.hasMoreElements(); ) {
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					try {
						this.credentialsProvider.get(Thread.currentThread().getId()).setCredentials(
							new AuthScope(getProxyServerHost(), getProxyServerPort(), p.realm, AuthSchemes.NTLM),
							new NTCredentials(p.user, p.password, InetAddress.getLocalHost().getHostName(), getProxyServerHost()));
					} catch (UnknownHostException e) {
						this.credentialsProvider.get(Thread.currentThread().getId()).setCredentials(
							new AuthScope(getProxyServerHost(), getProxyServerPort(), p.realm, AuthSchemes.NTLM),
							new NTCredentials(p.user, p.password, "localhost", getProxyServerHost()));
					}

				}

			}

			setProxyInfoChanged(false); // Desmarco las flags
			setAuthorizationProxyChanged(false);

			if (this.credentialsProvider.get(Thread.currentThread().getId()) != null) {    // En caso que se haya agregado algun tipo de autenticacion (ya sea para el host destino como para el proxy) se agrega al contexto
				httpClientContext.put(Thread.currentThread().getId(), HttpClientContext.create());
				httpClientContext.get(Thread.currentThread().getId()).setCredentialsProvider(credentialsProvider.get(Thread.currentThread().getId()));
			}

			if  (!url.startsWith("/"))
				url = !getBaseURL().startsWith("/")?"/" + getBaseURL() + url:getBaseURL() + url;
			if (getSecure() == 1)   // Se completa con esquema y host
				url = "https://" + getHost() + url;		// La lib de HttpClient agrega el port
			else
				url = "http://" + getHost() + ":" + (getPort() == -1? URI.defaultPort("http"):getPort()) + url;

			httpClient.put(Thread.currentThread().getId(), this.httpClientBuilder.get(Thread.currentThread().getId()).build());

			if (method.equalsIgnoreCase("GET")) {
				HttpGetWithBody httpget = new HttpGetWithBody(url.trim());
				httpget.setConfig(reqConfig.get(Thread.currentThread().getId()));
				Set<String> keys = getheadersToSend().keySet();
				for (String header : keys) {
					httpget.addHeader(header,getheadersToSend().get(header));
				}

				httpget.setEntity(new ByteArrayEntity(getData()));

				response.put(Thread.currentThread().getId(), httpClient.get(Thread.currentThread().getId()).execute(httpget, httpClientContext.get(Thread.currentThread().getId())));

			} else if (method.equalsIgnoreCase("POST")) {
				HttpPost httpPost = new HttpPost(url.trim());
				httpPost.setConfig(reqConfig.get(Thread.currentThread().getId()));
				Set<String> keys = getheadersToSend().keySet();
				boolean hasConentType = false;
				for (String header : keys) {
					httpPost.addHeader(header,getheadersToSend().get(header));
					if (getheadersToSend().get(header).equalsIgnoreCase("Content-type"))
						hasConentType = true;
				}
				if (!hasConentType)		// Si no se setea Content-type, se pone uno default
					httpPost.addHeader("Content-type", "application/x-www-form-urlencoded");

				ByteArrayEntity dataToSend;
				if (!getIsMultipart() && getVariablesToSend().size() > 0)
					dataToSend = new ByteArrayEntity(Codecs.nv2query(hashtableToNVPair(getVariablesToSend())).getBytes());
				else
					dataToSend = new ByteArrayEntity(getData());
				httpPost.setEntity(dataToSend);

				response.put(Thread.currentThread().getId(),httpClient.get(Thread.currentThread().getId()).execute(httpPost, httpClientContext.get(Thread.currentThread().getId())));

			} else if (method.equalsIgnoreCase("PUT")) {
				HttpPut httpPut = new HttpPut(url.trim());
				httpPut.setConfig(reqConfig.get(Thread.currentThread().getId()));
				Set<String> keys = getheadersToSend().keySet();
				for (String header : keys) {
					httpPut.addHeader(header,getheadersToSend().get(header));
				}

				httpPut.setEntity(new ByteArrayEntity(getData()));

				response.put(Thread.currentThread().getId(),httpClient.get(Thread.currentThread().getId()).execute(httpPut, httpClientContext.get(Thread.currentThread().getId())));

			} else if (method.equalsIgnoreCase("DELETE")) {
				HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url.trim());
				httpDelete.setConfig(reqConfig.get(Thread.currentThread().getId()));
				Set<String> keys = getheadersToSend().keySet();
				for (String header : keys) {
					httpDelete.addHeader(header,getheadersToSend().get(header));
				}

				if (getVariablesToSend().size() > 0 || getContentToSend().size() > 0)
					httpDelete.setEntity(new ByteArrayEntity(getData()));

				response.put(Thread.currentThread().getId(),httpClient.get(Thread.currentThread().getId()).execute(httpDelete, httpClientContext.get(Thread.currentThread().getId())));

			} else if (method.equalsIgnoreCase("HEAD")) {
				HttpHeadWithBody httpHead = new HttpHeadWithBody(url.trim());
				httpHead.setConfig(reqConfig.get(Thread.currentThread().getId()));
				Set<String> keys = getheadersToSend().keySet();
				for (String header : keys) {
					httpHead.addHeader(header,getheadersToSend().get(header));
				}

				httpHead.setEntity(new ByteArrayEntity(getData()));

				response.put(Thread.currentThread().getId(),httpClient.get(Thread.currentThread().getId()).execute(httpHead, httpClientContext.get(Thread.currentThread().getId())));

			} else if (method.equalsIgnoreCase("CONNECT")) {
				HttpConnectMethod httpConnect = new HttpConnectMethod(url.trim());
				httpConnect.setConfig(reqConfig.get(Thread.currentThread().getId()));
				Set<String> keys = getheadersToSend().keySet();
				for (String header : keys) {
					httpConnect.addHeader(header,getheadersToSend().get(header));
				}
				response.put(Thread.currentThread().getId(),httpClient.get(Thread.currentThread().getId()).execute(httpConnect, httpClientContext.get(Thread.currentThread().getId())));

			} else if (method.equalsIgnoreCase("OPTIONS")) {
				HttpOptionsWithBody httpOptions = new HttpOptionsWithBody(url.trim());
				httpOptions.setConfig(reqConfig.get(Thread.currentThread().getId()));
				Set<String> keys = getheadersToSend().keySet();
				for (String header : keys) {
					httpOptions.addHeader(header,getheadersToSend().get(header));
				}

				httpOptions.setEntity(new ByteArrayEntity(getData()));

				response.put(Thread.currentThread().getId(),httpClient.get(Thread.currentThread().getId()).execute(httpOptions, httpClientContext.get(Thread.currentThread().getId())));

			} else if (method.equalsIgnoreCase("TRACE")) {		// No lleva payload
				HttpTrace httpTrace = new HttpTrace(url.trim());
				httpTrace.setConfig(reqConfig.get(Thread.currentThread().getId()));
				Set<String> keys = getheadersToSend().keySet();
				for (String header : keys) {
					httpTrace.addHeader(header,getheadersToSend().get(header));
				}
				response.put(Thread.currentThread().getId(),httpClient.get(Thread.currentThread().getId()).execute(httpTrace, httpClientContext.get(Thread.currentThread().getId())));

			} else if (method.equalsIgnoreCase("PATCH")) {
				HttpPatch httpPatch = new HttpPatch(url.trim());
				httpPatch.setConfig(reqConfig.get(Thread.currentThread().getId()));
				Set<String> keys = getheadersToSend().keySet();
				for (String header : keys) {
					httpPatch.addHeader(header,getheadersToSend().get(header));
				}
				ByteArrayEntity dataToSend = new ByteArrayEntity(getData());
				httpPatch.setEntity(dataToSend);
				response.put(Thread.currentThread().getId(),httpClient.get(Thread.currentThread().getId()).execute(httpPatch, httpClientContext.get(Thread.currentThread().getId())));
			}

			statusCode.put(Thread.currentThread().getId(), response.get(Thread.currentThread().getId()).getStatusLine().getStatusCode());
			reasonLine.put(Thread.currentThread().getId(), response.get(Thread.currentThread().getId()).getStatusLine().getReasonPhrase());

			if (cookiesToSend != null)
				SetCookieAtr(cookiesToSend);		// Se setean las cookies devueltas en la lista de cookies

		} catch (IOException e) {
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
		finally {
			if (getIsURL())
			{
				this.setHost(getPrevURLhost());
				this.setBaseURL(getPrevURLbaseURL());
				this.setPort(getPrevURLport());
				this.setSecure(getPrevURLsecure());
				setIsURL(false);
			}
			resetStateAdapted();
		}

	}



	public int getStatusCode() {
		return statusCode.get(Thread.currentThread().getId());
	}

	public String getReasonLine() {
		return reasonLine.get(Thread.currentThread().getId());
	}

	public void getHeader(String name, long[] value) {
		if (response.get(Thread.currentThread().getId()) == null || response.get(Thread.currentThread().getId()).getHeaders(name).length == 0 || response.get(Thread.currentThread().getId()).getHeaders(name) == null)
			return;
		Header[] headers = response.get(Thread.currentThread().getId()).getHeaders(name);
		if (headers == null)
			throw new NumberFormatException("null");
//		for (int i = 0; i< headers.length; i++) {			// Posible solucion en el caso que se quieran poner todos los headers que se obtienen con el name pasado en el parametro value
//			value[i] = Integer.parseInt(headers[i].getValue());
//		}
		value[0] = Integer.parseInt(headers[0].getValue());
	}

	public String getHeader(String name) {
		if (response.get(Thread.currentThread().getId()) == null || response.get(Thread.currentThread().getId()).getHeaders(name).length == 0 || response.get(Thread.currentThread().getId()).getHeaders(name) == null)
			return "";
		return response.get(Thread.currentThread().getId()).getHeaders(name)[0].getValue();
	}

	public void getHeader(String name, String[] value) {
		if (response.get(Thread.currentThread().getId()) == null || response.get(Thread.currentThread().getId()).getHeaders(name).length == 0 || response.get(Thread.currentThread().getId()).getHeaders(name) == null)
			return;
		Header[] headers = response.get(Thread.currentThread().getId()).getHeaders(name);
//		for (int i = 0; i< headers.length; i++) {			// Posible solucion en el caso que se quieran poner todos los headers que se obtienen con el name pasado en el parametro value
//			value[i] = headers[i].getValue();
//		}
		value[0] = headers[0].getValue();
	}

	public void getHeader(String name, java.util.Date[] value) {
		HTTPResponse res = (HTTPClient.HTTPResponse) response.get(Thread.currentThread().getId());
		if (res == null)
			return;
		try
		{
			value[0] = res.getHeaderAsDate(name);
		}
		catch (IOException e)
		{
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
		catch (ModuleException e)
		{
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
	}

	public void getHeader(String name, double[] value) {
		if (response.get(Thread.currentThread().getId()) == null || response.get(Thread.currentThread().getId()).getHeaders(name).length == 0 || response.get(Thread.currentThread().getId()).getHeaders(name) == null)
			return;
		value[0] = CommonUtil.val(response.get(Thread.currentThread().getId()).getHeaders(name)[0].getValue());
	}

	public InputStream getInputStream() throws IOException {
		return response.get(Thread.currentThread().getId()).getEntity().getContent();
	}

	public InputStream getInputStream(String stringURL) throws IOException
	{
		try {
			URI url = new URI(stringURL);
			HttpGet gISHttpGet = new HttpGet(String.valueOf(url));
			CloseableHttpClient gISHttpClient = HttpClients.createDefault();
			return gISHttpClient.execute(gISHttpGet).getEntity().getContent();

		} catch (ParseException e) {
			throw new IOException("Malformed URL " + e.getMessage());
		}
	}

	public String getString() {
		if (response.get(Thread.currentThread().getId()) == null)
			return "";
		try {
			String res = EntityUtils.toString(response.get(Thread.currentThread().getId()).getEntity(), "UTF-8");
			return res;
		} catch (IOException e) {
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		} catch (IllegalArgumentException e) {
		}
		return "";
	}

	public void toFile(String fileName) {
		if (response.get(Thread.currentThread().getId()) == null)
			return;
		try {
			CommonUtil.InputStreamToFile(response.get(Thread.currentThread().getId()).getEntity().getContent(), fileName);
		} catch (IOException e) {
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
	}


	public void cleanup() {
		stcCleanup();
	}

	private void stcCleanup() {
		cleanReqAndRes();
		if (connManager.get(Thread.currentThread().getId()) != null)
			connManager.get(Thread.currentThread().getId()).close();
	}

	private void cleanReqAndRes() {
		if (response.get(Thread.currentThread().getId()) != null) {
			try {
				response.get(Thread.currentThread().getId()).close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (httpClient.get(Thread.currentThread().getId()) != null) {
			try {
				httpClient.get(Thread.currentThread().getId()).close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}