package com.genexus.internet;

import java.io.*;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.*;
import java.net.URI;
import javax.net.ssl.SSLContext;
import org.apache.http.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.protocol.HttpContext;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.*;
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
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import com.genexus.webpanels.HttpContextWeb;
import com.genexus.ModelContext;
import com.genexus.management.HTTPConnectionJMX;
import com.genexus.management.HTTPPoolJMX;
import com.genexus.util.IniFile;
import com.genexus.Application;
import com.genexus.CommonUtil;
import com.genexus.specific.java.*;

public class HttpClientJavaLib extends GXHttpClient implements IConnectionObserver {

	public HttpClientJavaLib() {
		getPoolInstance();
		ConnectionKeepAliveStrategy myStrategy = generateKeepAliveStrategy();
		httpClientBuilder = HttpClients.custom().setConnectionManager(connManager).setConnectionManagerShared(true).setKeepAliveStrategy(myStrategy);
		cookies = new BasicCookieStore();
		logger.info("Using apache http client implementation");
		streamsToClose = new Vector<>();
		connManager.addObserver(this);
	}

	private static void getPoolInstance() {
		if(connManager == null) {
			Registry<ConnectionSocketFactory> socketFactoryRegistry =
				RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", PlainConnectionSocketFactory.INSTANCE).register("https", getSSLSecureInstance())
					.build();
			connManager = new CustomPoolingHttpClientConnectionManager(socketFactoryRegistry);
			connManager.setMaxTotal((int) CommonUtil.val(clientCfg.getProperty("Client", "HTTPCLIENT_MAX_SIZE", "1000")));
			connManager.setDefaultMaxPerRoute((int) CommonUtil.val(clientCfg.getProperty("Client", "HTTPCLIENT_MAX_PER_ROUTE", "1000")));

			if (Application.isJMXEnabled())
				HTTPPoolJMX.CreateHTTPPoolJMX(connManager);
		}
		else {
			connManager.closeExpiredConnections();
		}
	}

	@Override
	public void onConnectionCreated(HttpRoute route) {
		if (Application.isJMXEnabled())
			HTTPConnectionJMX.CreateHTTPConnectionJMX(route);
	}

	@Override
	public void onConnectionDestroyed(HttpRoute route) {
		if (Application.isJMXEnabled())
			HTTPConnectionJMX.DestroyHTTPConnectionJMX(route);
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

	private static CustomPoolingHttpClientConnectionManager connManager = null;
	private Integer statusCode = 0;
	private String reasonLine = "";
	private HttpClientBuilder httpClientBuilder;
	private HttpClientContext httpClientContext = null;
	private CloseableHttpResponse response = null;
	private CredentialsProvider credentialsProvider = null;
	private RequestConfig reqConfig = null;		// Atributo usado en la ejecucion del metodo (por ejemplo, httpGet, httpPost)
	private CookieStore cookies;
	private ByteArrayEntity entity = null;	// Para mantener el stream luego de cerrada la conexion en la lectura de la response
	BufferedReader reader = null;
	private Boolean lastAuthIsBasic = null;
	private Boolean lastAuthProxyIsBasic = null;
	private static IniFile clientCfg = new ModelContext(ModelContext.getModelContextPackageClass()).getPreferences().getIniFile();
	private static final String SET_COOKIE = "Set-Cookie";
	private static final String COOKIE = "Cookie";

	private java.util.Vector<InputStream> streamsToClose;

	private void closeOpenedStreams()
	{
		Enumeration<InputStream> e = streamsToClose.elements();
		while(e.hasMoreElements())
		{
			try
			{
				(e.nextElement()).close();
			}
			catch(java.io.IOException ioex)
			{
				logger.error("Error closing stream: " + ioex.getMessage());
			}
		}
		streamsToClose.removeAllElements();
	}

	private void resetExecParams() {
		statusCode = 0;
		reasonLine = "";
		resetErrorsAndConnParams();
		setErrCode(0);
		setErrDescription("");
		entity = null;
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

	@Override
	public void addAuthentication(int type, String realm, String name, String value) {	// Metodo overriden por tratarse de forma distinta el pasaje de auth Basic y el resto
		if (type == BASIC)
			lastAuthIsBasic = true;
		else
			lastAuthIsBasic = false;
		super.addAuthentication(type,realm,name,value);
	}

	@Override
	public void addProxyAuthentication(int type, String realm, String name, String value) {	// Metodo overriden por tratarse de forma distinta el pasaje de auth Basic y el resto
		if (type == BASIC)
			lastAuthProxyIsBasic = true;
		else
			lastAuthProxyIsBasic = false;
		super.addProxyAuthentication(type,realm,name,value);
	}

	private void resetStateAdapted()
	{
		resetState();
		getheadersToSend().clear();
	}

	@Override
	public void setURL(String stringURL) {
		try
		{
			URI url = new URI(stringURL);
			setHost(url.getHost());
			setPort(url.getPort());
			setBaseURL(url.getPath());
			setSecure(url.getScheme().equalsIgnoreCase("https") ? 1 : 0);
		}
		catch (URISyntaxException e)
		{
			System.err.println("E " + e + " " + stringURL);
			e.printStackTrace();
		}
	}

	private String getURLValid(String url) {
		try
		{
			URI uri;
			try {
				uri = new URI(url);
			}
			catch (URISyntaxException _) {
				url = CommonUtil.escapeUnsafeChars(url);
				uri = new URI(url);
			}
			if (!uri.isAbsolute()) {        // En caso que la URL pasada por parametro no sea una URL valida (en este caso seria que no sea un URL absoluta), salta una excepcion en esta linea, y se continua haciendo todo el proceso con los datos ya guardados como atributos
				return url;
			}
			setPrevURLhost(getHost());
			setPrevURLbaseURL(getBaseURL());
			setPrevURLport(getPort());
			setPrevURLsecure(getSecure());
			setIsURL(true);
			setURL(url);

			StringBuilder relativeUri = new StringBuilder();
			if (uri.getRawPath() != null) {
				relativeUri.append(uri.getRawPath());
			}
			if (uri.getRawQuery() != null) {
				relativeUri.append('?').append(uri.getRawQuery());
			}
			if (uri.getRawFragment() != null) {
				relativeUri.append('#').append(uri.getRawFragment());
			}
			return relativeUri.toString();
		}
		catch (URISyntaxException _)
		{
		}
		return url;
	}

	private static SSLConnectionSocketFactory getSSLSecureInstance() {
		try {
			SSLContextBuilder sslContextBuilder = SSLContextBuilder
				.create()
				.loadTrustMaterial(new TrustSelfSignedStrategy());

			String pathToKeystore = System.getProperty("javax.net.ssl.keyStore");
			String keystorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
			if (pathToKeystore != null && keystorePassword != null)
				sslContextBuilder.loadKeyMaterial(new File(pathToKeystore), keystorePassword.toCharArray(), keystorePassword.toCharArray());

			String pathToTruststore = System.getProperty("javax.net.ssl.trustStore");
			String truststorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
			if (pathToTruststore != null && truststorePassword != null)
				sslContextBuilder.loadTrustMaterial(new File(pathToTruststore), truststorePassword.toCharArray());

			SSLContext sslContext = sslContextBuilder.build();

			return new SSLConnectionSocketFactory(
				sslContext,
				new String[] { "TLSv1", "TLSv1.1", "TLSv1.2" },
				null,
				NoopHostnameVerifier.INSTANCE);
		} catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | UnrecoverableKeyException | CertificateException | IOException e) {
			e.printStackTrace();
		}
		return new SSLConnectionSocketFactory(
			SSLContexts.createDefault(),
			new String[] { "TLSv1", "TLSv1.1", "TLSv1.2"},
			null,
			SSLConnectionSocketFactory.getDefaultHostnameVerifier());
	}

	private CookieStore setAllStoredCookies() {
		CookieStore cookiesToSend = new BasicCookieStore();
		if (!ModelContext.getModelContext().isNullHttpContext()) { 	// Caso de ejecucion de varias instancia de HttpClientJavaLib, por lo que se obtienen cookies desde sesion web del browser

			String selfWebCookie = ((HttpContextWeb) ModelContext.getModelContext().getHttpContext()).getCookie(SET_COOKIE);
			if (!selfWebCookie.isEmpty())
				this.addHeader(COOKIE, selfWebCookie.replace("+",";"));

		} else {	// Caso se ejecucion de una misma instancia HttpClientJavaLib mediante command line
			if (!getIncludeCookies())
				cookies.clear();
			if (cookies.getCookies().isEmpty())
				return new BasicCookieStore();

			cookies.clearExpired(new Date());
			for (Cookie c : cookies.getCookies()) {
				if (getHost().equalsIgnoreCase(c.getDomain()) || (getHost().substring(4).equalsIgnoreCase(c.getDomain())))  	// el substring(4) se debe a que el host puede estar guardado con el "www." previo al host
					cookiesToSend.addCookie(c);
			}
		}
		return cookiesToSend;
	}

	private void SetCookieAtr(CookieStore cookiesToSend) {
		if (cookiesToSend != null) {
			if (ModelContext.getModelContext().isNullHttpContext()) {
				for (Cookie c : cookiesToSend.getCookies())
					cookies.addCookie(c);
			} else {
				try {
					HttpContextWeb webcontext = ((HttpContextWeb) ModelContext.getModelContext().getHttpContext());

					Header[] headers = this.response.getHeaders(SET_COOKIE);
					if (headers.length > 0) {
						String webcontextCookieHeader = "";
						for (Header header : headers) {
							String[] cookieParts = header.getValue().split(";");
							String[] cookieKeyAndValue = cookieParts[0].split("=");
							webcontextCookieHeader += cookieKeyAndValue[0];
							if (cookieKeyAndValue.length > 1) {
								webcontextCookieHeader += "=" + cookieKeyAndValue[1] + "; ";
							}
							else {
								webcontextCookieHeader += "; ";
							}
						}
						webcontextCookieHeader = webcontextCookieHeader.trim().substring(0,webcontextCookieHeader.length()-2);	// Se quita el espacio y la coma al final
						webcontext.setCookie(SET_COOKIE,webcontextCookieHeader,"",CommonUtil.nullDate(),"",this.getSecure());
					}
					ModelContext.getModelContext().setHttpContext(webcontext);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void addBasicAuthHeader(String user, String password, Boolean isProxy) {
		Boolean typeAuth = isProxy ? this.lastAuthProxyIsBasic : this.lastAuthIsBasic;
		if (typeAuth) {
			String auth = user + ":" + password;
			String authHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.ISO_8859_1));
			addHeader(isProxy ? HttpHeaders.PROXY_AUTHORIZATION : HttpHeaders.AUTHORIZATION, authHeader);
		}
	}

	public void execute(String method, String url) {
		resetExecParams();

		url = getURLValid(url).trim();		// Funcion genera parte del path en adelante de la URL

		try {
			CookieStore cookiesToSend = null;
			if (getHostChanged()) {
				if (getSecure() == 1 && getPort() == 80) {
					setPort(443);
				}

				SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(getTcpNoDelay()).build();	// Seteo de TcpNoDelay
				this.httpClientBuilder.setDefaultSocketConfig(socketConfig);

				cookiesToSend = setAllStoredCookies();

				this.httpClientBuilder.setDefaultCookieStore(cookiesToSend);    // Cookies Seteo CookieStore
			}

			int msTimeout = getTimeout() * 1000;

			RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
				.setCookieSpec(CookieSpecs.STANDARD)
				.setSocketTimeout(msTimeout)
				.setConnectionRequestTimeout(msTimeout)
				.setConnectTimeout(msTimeout);

			this.httpClientBuilder.setRoutePlanner(null);

			if (getProxyInfoChanged() && !getProxyServerHost().isEmpty() && getProxyServerPort() != 0) {
				HttpHost proxy = new HttpHost(getProxyServerHost(), getProxyServerPort());
				this.httpClientBuilder.setRoutePlanner(new DefaultProxyRoutePlanner(proxy));
				requestConfigBuilder.setProxy(proxy);
			}

			this.reqConfig = requestConfigBuilder.build();

			if (getHostChanged() || getAuthorizationChanged()) { // Si el host cambio o si se agrego alguna credencial
				this.credentialsProvider = new BasicCredentialsProvider();

				for (Enumeration en = getBasicAuthorization().elements(); en.hasMoreElements(); ) {	// No se puede hacer la autorizacion del tipo Basic con el BasicCredentialsProvider porque esta funcionando bien en todos los casos
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					addBasicAuthHeader(p.user,p.password,false);
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

				for (Enumeration en = getBasicProxyAuthorization().elements(); en.hasMoreElements(); ) { // No se puede hacer la autorizacion del tipo Basic con el BasicCredentialsProvider porque esta funcionando bien en todos los casos
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					addBasicAuthHeader(p.user,p.password,true);
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
			url = CommonUtil.escapeUnsafeChars(url);

			if (getSecure() == 1)   // Se completa con esquema y host
				url = url.startsWith("https://") ? url : "https://" + getHost()+ (getPort() != 443?":"+getPort():"")+ url;		// La lib de HttpClient agrega el port
			else
				url = url.startsWith("http://") ? url : "http://" + getHost() + ":" + (getPort() == -1? "80" :getPort()) + url;

			try (CloseableHttpClient httpClient = this.httpClientBuilder.build()) {
				if (method.equalsIgnoreCase("GET")) {
					HttpGetWithBody httpget = new HttpGetWithBody(url.trim());
					httpget.setConfig(reqConfig);
					Set<String> keys = getheadersToSend().keySet();
					for (String header : keys) {
						httpget.addHeader(header, getheadersToSend().get(header));
					}

					httpget.setEntity(new ByteArrayEntity(getData()));

					response = httpClient.execute(httpget, httpClientContext);

				} else if (method.equalsIgnoreCase("POST")) {
					HttpPost httpPost = new HttpPost(url.trim());
					httpPost.setConfig(reqConfig);
					Set<String> keys = getheadersToSend().keySet();
					boolean hasConentType = false;
					for (String header : keys) {
						httpPost.addHeader(header, getheadersToSend().get(header));
						if (header.equalsIgnoreCase("Content-type"))
							hasConentType = true;
					}
					if (!hasConentType)        // Si no se setea Content-type, se pone uno default
						httpPost.addHeader("Content-type", "application/x-www-form-urlencoded");

					ByteArrayEntity dataToSend;
					if (!getIsMultipart() && getVariablesToSend().size() > 0)
						dataToSend = new ByteArrayEntity(CommonUtil.hashtable2query(getVariablesToSend()).getBytes());
					else
						dataToSend = new ByteArrayEntity(getData());
					httpPost.setEntity(dataToSend);

					response = httpClient.execute(httpPost, httpClientContext);

				} else if (method.equalsIgnoreCase("PUT")) {
					HttpPut httpPut = new HttpPut(url.trim());
					httpPut.setConfig(reqConfig);
					Set<String> keys = getheadersToSend().keySet();
					for (String header : keys) {
						httpPut.addHeader(header, getheadersToSend().get(header));
					}

					httpPut.setEntity(new ByteArrayEntity(getData()));

					response = httpClient.execute(httpPut, httpClientContext);

				} else if (method.equalsIgnoreCase("DELETE")) {
					HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url.trim());
					httpDelete.setConfig(reqConfig);
					Set<String> keys = getheadersToSend().keySet();
					for (String header : keys) {
						httpDelete.addHeader(header, getheadersToSend().get(header));
					}

					if (getVariablesToSend().size() > 0 || getContentToSend().size() > 0)
						httpDelete.setEntity(new ByteArrayEntity(getData()));

					response = httpClient.execute(httpDelete, httpClientContext);

				} else if (method.equalsIgnoreCase("HEAD")) {
					HttpHeadWithBody httpHead = new HttpHeadWithBody(url.trim());
					httpHead.setConfig(reqConfig);
					Set<String> keys = getheadersToSend().keySet();
					for (String header : keys) {
						httpHead.addHeader(header, getheadersToSend().get(header));
					}

					httpHead.setEntity(new ByteArrayEntity(getData()));

					response = httpClient.execute(httpHead, httpClientContext);

				} else if (method.equalsIgnoreCase("CONNECT")) {
					HttpConnectMethod httpConnect = new HttpConnectMethod(url.trim());
					httpConnect.setConfig(reqConfig);
					Set<String> keys = getheadersToSend().keySet();
					for (String header : keys) {
						httpConnect.addHeader(header, getheadersToSend().get(header));
					}
					response = httpClient.execute(httpConnect, httpClientContext);

				} else if (method.equalsIgnoreCase("OPTIONS")) {
					HttpOptionsWithBody httpOptions = new HttpOptionsWithBody(url.trim());
					httpOptions.setConfig(reqConfig);
					Set<String> keys = getheadersToSend().keySet();
					for (String header : keys) {
						httpOptions.addHeader(header, getheadersToSend().get(header));
					}

					httpOptions.setEntity(new ByteArrayEntity(getData()));

					response = httpClient.execute(httpOptions, httpClientContext);

				} else if (method.equalsIgnoreCase("TRACE")) {        // No lleva payload
					HttpTrace httpTrace = new HttpTrace(url.trim());
					httpTrace.setConfig(reqConfig);
					Set<String> keys = getheadersToSend().keySet();
					for (String header : keys) {
						httpTrace.addHeader(header, getheadersToSend().get(header));
					}
					response = httpClient.execute(httpTrace, httpClientContext);

				} else if (method.equalsIgnoreCase("PATCH")) {
					HttpPatch httpPatch = new HttpPatch(url.trim());
					httpPatch.setConfig(reqConfig);
					Set<String> keys = getheadersToSend().keySet();
					for (String header : keys) {
						httpPatch.addHeader(header, getheadersToSend().get(header));
					}
					ByteArrayEntity dataToSend = new ByteArrayEntity(getData());
					httpPatch.setEntity(dataToSend);
					response = httpClient.execute(httpPatch, httpClientContext);
				}
			}
			statusCode =  response.getStatusLine().getStatusCode();
			reasonLine =  response.getStatusLine().getReasonPhrase();

			SetCookieAtr(cookiesToSend);		// Se setean las cookies devueltas en la lista de cookies

			if (response.containsHeader("Transfer-Encoding")) {
				isChunkedResponse = response.getFirstHeader("Transfer-Encoding").getValue().equalsIgnoreCase("chunked");
			}

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
		if (response == null)
			return;
		try
		{
			value[0] = CommonUtil.getHeaderAsDate(response.getFirstHeader(name).getValue());
		}
		catch (IOException e)
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
		if (response != null) {
			this.setEntity();
			InputStream content = entity.getContent();
			streamsToClose.addElement(content);
			return content;
		} else
			return null;
	}

	private void setEntity() throws IOException {
		if (entity == null)
			entity = new ByteArrayEntity(EntityUtils.toByteArray(response.getEntity()));
	}

	private void setEntityReader() throws IOException {
		if (reader == null)
			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	}

	public String getString() {
		if (response == null)
			return "";
		try {
			this.setEntity();
			String res = EntityUtils.toString(entity, "UTF-8");
			eof = true;
			return res;
		} catch (IOException e) {
			setExceptionsCatch(e);
		} catch (IllegalArgumentException e) {
		}
		return "";
	}

	private	boolean eof;
	public boolean getEof() {
		return eof;
	}

	public String readChunk() {
		if (!isChunkedResponse)
			return getString();

		if (response == null)
			return "";
		try {
			this.setEntityReader();
			String res = reader.readLine();
			if (res == null) {
				eof = true;
				res = "";
			}
			return res;
		} catch (IOException e) {
			setExceptionsCatch(e);
		}
		return "";
	}

	public void toFile(String fileName) {
		if (response == null)
			return;
		try {
			this.setEntity();
			CommonUtil.InputStreamToFile(entity.getContent(), fileName);
		} catch (IOException e) {
			setExceptionsCatch(e);
		}
	}

	public void cleanup() {
		resetErrorsAndConnParams();
	}

	@Override
	protected void finalize()
	{
		this.closeOpenedStreams();
	}

}