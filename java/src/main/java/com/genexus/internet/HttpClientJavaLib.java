package com.genexus.internet;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.genexus.util.IniFile;
import org.apache.http.HttpResponse;
import com.genexus.CommonUtil;
import com.genexus.specific.java.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
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
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;

public class HttpClientJavaLib extends GXHttpClient {

	public HttpClientJavaLib() {
		getPoolInstance();
		ConnectionKeepAliveStrategy myStrategy = generateKeepAliveStrategy();
		httpClientBuilder = HttpClients.custom().setConnectionManager(connManager).setConnectionManagerShared(true).setKeepAliveStrategy(myStrategy);
		cookies = new BasicCookieStore();
		logger.info("Using apache http client implementation");
	}

	private static void getPoolInstance() {
		if(connManager == null) {
			Registry<ConnectionSocketFactory> socketFactoryRegistry =
				RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", PlainConnectionSocketFactory.INSTANCE).register("https", getSSLSecureInstance())
					.build();
			connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			connManager.setMaxTotal((int) CommonUtil.val(clientCfg.getProperty("Client","HTTPCLIENT_MAX_SIZE","1000")));
			connManager.setDefaultMaxPerRoute((int) CommonUtil.val(clientCfg.getProperty("Client","HTTPCLIENT_MAX_PER_ROUTE","1000")));
		}
	}

	private ConnectionKeepAliveStrategy generateKeepAliveStrategy() {
		return new ConnectionKeepAliveStrategy() {
			@Override
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				HeaderElementIterator it = new BasicHeaderElementIterator
					(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
				while (it.hasNext()) {
					HeaderElement he = it.nextElement();
					String param = he.getName();
					String value = he.getValue();
					if (value != null && param.equalsIgnoreCase
						("timeout")) {
						return Long.parseLong(value) * 1000;
					}
				}
				return getTimeout() * 1000;
			}
		};
	}

	@Override
	public void setTimeout(int timeout)
	{
		super.setTimeout(timeout);
		ConnectionKeepAliveStrategy myStrategy = generateKeepAliveStrategy();	// Cuando se actualiza el timeout, se actualiza el KeepAliveStrategy ya que el mismo de basa el el timeout seteado
		httpClientBuilder.setKeepAliveStrategy(myStrategy);
	}

	private static Logger logger = org.apache.logging.log4j.LogManager.getLogger(HttpClientJavaLib.class);

	private static PoolingHttpClientConnectionManager connManager = null;
	private Integer statusCode = 0;
	private String reasonLine = "";
	private HttpClientBuilder httpClientBuilder;
	private HttpClientContext httpClientContext = null;
	private CloseableHttpClient httpClient = null;
	private CloseableHttpResponse response = null;
	private CredentialsProvider credentialsProvider = null;
	private RequestConfig reqConfig = null;		// Atributo usado en la ejecucion del metodo (por ejemplo, httpGet, httpPost)
	private CookieStore cookies;
	private static IniFile clientCfg = new com.genexus.ModelContext(com.genexus.ModelContext.getModelContextPackageClass()).getPreferences().getIniFile();


	private void resetExecParams() {
		statusCode = 0;
		reasonLine = "";
		resetErrorsAndConnParams();
		setErrCode(0);
		setErrDescription("");
	}


	private void resetErrorsAndConnParams()
	{
		if (response != null) {
			try {
				EntityUtils.consume(response.getEntity());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
			uri = new URI(url);		// En caso que la URL pasada por parametro no sea una URL valida (en este caso seria que no sea un URL absoluta), salta una excepcion en esta linea, y se continua haciendo todo el proceso con los datos ya guardados como atributos
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
			return url;
		}
	}

	private static SSLConnectionSocketFactory getSSLSecureInstance() {
		try {
			SSLContext sslContext = SSLContextBuilder
				.create()
				.loadTrustMaterial(new TrustSelfSignedStrategy())
				.build();
			return new org.apache.http.conn.ssl.SSLConnectionSocketFactory(
				sslContext,
				new String[] { "TLSv1", "TLSv1.1", "TLSv1.2" },
				null,
				SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		} catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
			e.printStackTrace();
			return new org.apache.http.conn.ssl.SSLConnectionSocketFactory(
				SSLContexts.createDefault(),
				new String[] { "TLSv1", "TLSv1.1", "TLSv1.2"},
				null,
				SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		} catch (Exception e) {
			e.printStackTrace();
			return new org.apache.http.conn.ssl.SSLConnectionSocketFactory(
				SSLContexts.createDefault(),
				new String[] { "TLSv1", "TLSv1.1", "TLSv1.2"},
				null,
				SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		}

	}

	private CookieStore setAllStoredCookies() {
		if (cookies.getCookies().isEmpty())
			return new BasicCookieStore();

		CookieStore cookiesToSend = new BasicCookieStore();
		cookies.clearExpired(new Date());
		for (Cookie c : cookies.getCookies()) {
			if (getHost().equalsIgnoreCase(c.getDomain()) || (getHost().substring(4).equalsIgnoreCase(c.getDomain())))  	// el substring(4) se debe a que el host puede estar guardado con el "www." previo al host
				cookiesToSend.addCookie(c);
		}
		return cookiesToSend;
	}

	private void SetCookieAtr(CookieStore cookiesToSend) {
		for (Cookie c : cookiesToSend.getCookies())
			cookies.addCookie(c);
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
				this.httpClientBuilder.setDefaultSocketConfig(socketConfig);
				if (!getIncludeCookies())
					cookies.clear();
				cookiesToSend = setAllStoredCookies();
				this.httpClientBuilder.setDefaultCookieStore(cookiesToSend);    // Cookies Seteo CookieStore
			}

			if (getProxyInfoChanged()) {
				HttpHost proxy = new HttpHost(getProxyServerHost(), getProxyServerPort());
				this.httpClientBuilder.setRoutePlanner(new DefaultProxyRoutePlanner(proxy));
				this.reqConfig = RequestConfig.custom()
					.setSocketTimeout(getTimeout() * 1000)    // Se multiplica por 1000 ya que tiene que ir en ms y se recibe en segundos
					.setProxy(proxy)
					.build();
			} else {
				this.httpClientBuilder.setRoutePlanner(null);
				this.reqConfig = RequestConfig.custom()
					.setConnectTimeout(getTimeout() * 1000)   	// Se multiplica por 1000 ya que tiene que ir en ms y se recibe en segundos
					.build();
			}

			if (getHostChanged() || getAuthorizationChanged()) { // Si el host cambio o si se agrego alguna credencial
				this.credentialsProvider = new BasicCredentialsProvider();

				for (Enumeration en = getBasicAuthorization().elements(); en.hasMoreElements(); ) {
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					this.credentialsProvider.setCredentials(
						AuthScope.ANY,
						new UsernamePasswordCredentials(p.user, p.password));
				}

				for (Enumeration en = getDigestAuthorization().elements(); en.hasMoreElements(); ) {
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					this.credentialsProvider.setCredentials(
						new AuthScope(getHost(), getPort(), p.realm, AuthSchemes.DIGEST),
						new UsernamePasswordCredentials(p.user, p.password));
				}

				for (Enumeration en = getNTLMAuthorization().elements(); en.hasMoreElements(); ) {
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					this.httpClientBuilder.setDefaultAuthSchemeRegistry(RegistryBuilder.<AuthSchemeProvider> create()
						.register(AuthSchemes.NTLM, new NTLMSchemeFactory()).register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory(true)).build());
					try {
						credentialsProvider.setCredentials(
							new AuthScope(getHost(), getPort(), p.realm, AuthSchemes.NTLM),
							new NTCredentials(p.user, p.password, InetAddress.getLocalHost().getHostName(), getHost()));
					} catch (UnknownHostException e) {
						credentialsProvider.setCredentials(
							new AuthScope(getHost(), getPort(), p.realm, AuthSchemes.NTLM),
							new NTCredentials(p.user, p.password, "localhost", getHost()));
					}

				}

			}

			setHostChanged(false);
			setAuthorizationChanged(false); // Desmarco las flags

			if (getProxyInfoChanged() || getAuthorizationProxyChanged()) {    // Si el proxyHost cambio o si se agrego alguna credencial para el proxy
				if (this.credentialsProvider == null) {
					this.credentialsProvider = new BasicCredentialsProvider();
				}

				for (Enumeration en = getBasicProxyAuthorization().elements(); en.hasMoreElements(); ) {
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					this.credentialsProvider.setCredentials(
						AuthScope.ANY,
						new UsernamePasswordCredentials(p.user, p.password));
				}

				for (Enumeration en = getDigestProxyAuthorization().elements(); en.hasMoreElements(); ) {
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					this.credentialsProvider.setCredentials(
						new AuthScope(getProxyServerHost(), getProxyServerPort(), p.realm, AuthSchemes.DIGEST),
						new UsernamePasswordCredentials(p.user, p.password));
				}

				for (Enumeration en = getNTLMProxyAuthorization().elements(); en.hasMoreElements(); ) {
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

			setProxyInfoChanged(false); // Desmarco las flags
			setAuthorizationProxyChanged(false);

			if (this.credentialsProvider != null) {    // En caso que se haya agregado algun tipo de autenticacion (ya sea para el host destino como para el proxy) se agrega al contexto
				httpClientContext = HttpClientContext.create();
				httpClientContext.setCredentialsProvider(credentialsProvider);
			}

			url = setPathUrl(url);
			url = com.genexus.CommonUtil.escapeUnsafeChars(url);

			if (getSecure() == 1)   // Se completa con esquema y host
				url = "https://" + getHost() + url;		// La lib de HttpClient agrega el port
			else
				url = "http://" + getHost() + ":" + (getPort() == -1? URI.defaultPort("http"):getPort()) + url;


			httpClient = this.httpClientBuilder.build();

			if (method.equalsIgnoreCase("GET")) {
				HttpGetWithBody httpget = new HttpGetWithBody(url.trim());
				httpget.setConfig(reqConfig);
				Set<String> keys = getheadersToSend().keySet();
				for (String header : keys) {
					httpget.addHeader(header,getheadersToSend().get(header));
				}

				httpget.setEntity(new ByteArrayEntity(getData()));

				response = httpClient.execute(httpget, httpClientContext);

			} else if (method.equalsIgnoreCase("POST")) {
				HttpPost httpPost = new HttpPost(url.trim());
				httpPost.setConfig(reqConfig);
				Set<String> keys = getheadersToSend().keySet();
				boolean hasConentType = false;
				for (String header : keys) {
					httpPost.addHeader(header,getheadersToSend().get(header));
					if (header.equalsIgnoreCase("Content-type"))
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

				response = httpClient.execute(httpPost, httpClientContext);

			} else if (method.equalsIgnoreCase("PUT")) {
				HttpPut httpPut = new HttpPut(url.trim());
				httpPut.setConfig(reqConfig);
				Set<String> keys = getheadersToSend().keySet();
				for (String header : keys) {
					httpPut.addHeader(header,getheadersToSend().get(header));
				}

				httpPut.setEntity(new ByteArrayEntity(getData()));

				response = httpClient.execute(httpPut, httpClientContext);

			} else if (method.equalsIgnoreCase("DELETE")) {
				HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url.trim());
				httpDelete.setConfig(reqConfig);
				Set<String> keys = getheadersToSend().keySet();
				for (String header : keys) {
					httpDelete.addHeader(header,getheadersToSend().get(header));
				}

				if (getVariablesToSend().size() > 0 || getContentToSend().size() > 0)
					httpDelete.setEntity(new ByteArrayEntity(getData()));

				response = httpClient.execute(httpDelete, httpClientContext);

			} else if (method.equalsIgnoreCase("HEAD")) {
				HttpHeadWithBody httpHead = new HttpHeadWithBody(url.trim());
				httpHead.setConfig(reqConfig);
				Set<String> keys = getheadersToSend().keySet();
				for (String header : keys) {
					httpHead.addHeader(header,getheadersToSend().get(header));
				}

				httpHead.setEntity(new ByteArrayEntity(getData()));

				response = httpClient.execute(httpHead, httpClientContext);

			} else if (method.equalsIgnoreCase("CONNECT")) {
				HttpConnectMethod httpConnect = new HttpConnectMethod(url.trim());
				httpConnect.setConfig(reqConfig);
				Set<String> keys = getheadersToSend().keySet();
				for (String header : keys) {
					httpConnect.addHeader(header,getheadersToSend().get(header));
				}
				response = httpClient.execute(httpConnect, httpClientContext);

			} else if (method.equalsIgnoreCase("OPTIONS")) {
				HttpOptionsWithBody httpOptions = new HttpOptionsWithBody(url.trim());
				httpOptions.setConfig(reqConfig);
				Set<String> keys = getheadersToSend().keySet();
				for (String header : keys) {
					httpOptions.addHeader(header,getheadersToSend().get(header));
				}

				httpOptions.setEntity(new ByteArrayEntity(getData()));

				response = httpClient.execute(httpOptions, httpClientContext);

			} else if (method.equalsIgnoreCase("TRACE")) {		// No lleva payload
				HttpTrace httpTrace = new HttpTrace(url.trim());
				httpTrace.setConfig(reqConfig);
				Set<String> keys = getheadersToSend().keySet();
				for (String header : keys) {
					httpTrace.addHeader(header,getheadersToSend().get(header));
				}
				response = httpClient.execute(httpTrace, httpClientContext);

			} else if (method.equalsIgnoreCase("PATCH")) {
				HttpPatch httpPatch = new HttpPatch(url.trim());
				httpPatch.setConfig(reqConfig);
				Set<String> keys = getheadersToSend().keySet();
				for (String header : keys) {
					httpPatch.addHeader(header,getheadersToSend().get(header));
				}
				ByteArrayEntity dataToSend = new ByteArrayEntity(getData());
				httpPatch.setEntity(dataToSend);
				response = httpClient.execute(httpPatch, httpClientContext);
			}

			statusCode =  response.getStatusLine().getStatusCode();
			reasonLine =  response.getStatusLine().getReasonPhrase();


			if (cookiesToSend != null)
				SetCookieAtr(cookiesToSend);		// Se setean las cookies devueltas en la lista de cookies

		} catch (IOException e) {
			setExceptionsCatch(e);
			this.statusCode = 0;
			this.reasonLine = "";
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
		return statusCode;
	}

	public String getReasonLine() {
		return reasonLine;
	}

	public void getHeader(String name, long[] value) {
		if (response == null || response.getHeaders(name).length == 0 || response.getHeaders(name) == null)
			return;
		Header[] headers = response.getHeaders(name);
		if (headers == null)
			throw new NumberFormatException("null");
		value[0] = Integer.parseInt(headers[0].getValue());
	}

	public String getHeader(String name) {
		if (response == null || response.getHeaders(name).length == 0 || response.getHeaders(name) == null)
			return "";
		return response.getHeaders(name)[0].getValue();
	}

	public void getHeader(String name, String[] value) {
		if (response == null || response.getHeaders(name).length == 0 || response.getHeaders(name) == null)
			return;
		Header[] headers = response.getHeaders(name);
//		for (int i = 0; i< headers.length; i++) {			// Posible solucion en el caso que se quieran poner todos los headers que se obtienen con el name pasado en el parametro value
//			value[i] = headers[i].getValue();
//		}
		value[0] = headers[0].getValue();
	}

	public void getHeader(String name, java.util.Date[] value) {
		HTTPResponse res = (HTTPClient.HTTPResponse) response;
		if (res == null)
			return;
		try
		{
			value[0] = res.getHeaderAsDate(name);
		}
		catch (IOException | ModuleException e)
		{
			setExceptionsCatch(e);
		}
	}

	public void getHeader(String name, double[] value) {
		if (response == null || response.getHeaders(name).length == 0 || response.getHeaders(name) == null)
			return;
		value[0] = CommonUtil.val(response.getHeaders(name)[0].getValue());
	}

	public InputStream getInputStream() throws IOException {
		if (response != null)
			return response.getEntity().getContent();
		else
			return null;
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
		if (response == null)
			return "";
		try {
			String res = EntityUtils.toString(response.getEntity(), "UTF-8");
			return res;
		} catch (IOException e) {
			setExceptionsCatch(e);
		} catch (IllegalArgumentException e) {
		}
		return "";
	}

	public void toFile(String fileName) {
		if (response == null)
			return;
		try {
			CommonUtil.InputStreamToFile(response.getEntity().getContent(), fileName);
		} catch (IOException e) {
			setExceptionsCatch(e);
		}
	}


	public void cleanup() {
		resetErrorsAndConnParams();
	}

}