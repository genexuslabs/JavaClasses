package com.genexus.internet;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import com.genexus.CommonUtil;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.specific.java.HttpClient;
import com.sun.istack.NotNull;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.*;
import HTTPClient.*;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.cookie.*;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLSocketFactory;

public class HttpClientJavaLib extends GXHttpClient {

	private static String host;
	private static  String baseURL;
	private static  int secure;
	private static  int timeout;
	private static  int port;
	private static  boolean hostChanged;
	private static  Hashtable<String, String> headersToSend;
	private static  String WSDLURL;
	private static  int errCode;
	private static  String errDescription;
	private static  String proxyHost;
	private static  int proxyPort;
	private static boolean proxyInfoChanged;
	private static  boolean includeCookies;
	private static  boolean tcpNoDelay;
	private static  Hashtable variablesToSend;
	private static  Vector contentToSend;
	private static  boolean isMultipart;
	private static  MultipartTemplate multipartTemplate;
	private static  String prevURLhost;
	private static  String prevURLbaseURL;
	private static  int prevURLport;
	private static  int prevURLsecure;
	private static  boolean isURL;
	private static  boolean authorizationChanged; // Indica si se agregó alguna autorización
	private static  boolean authorizationProxyChanged; // Indica si se agregó alguna autorización

	private static  Vector<HttpClientPrincipal> basicAuthorization;
	private static  Vector<HttpClientPrincipal> digestAuthorization;
	private static  Vector<HttpClientPrincipal> NTLMAuthorization;

	private static  Vector<HttpClientPrincipal> basicProxyAuthorization;
	private static  Vector<HttpClientPrincipal> digestProxyAuthorization;
	private static  Vector<HttpClientPrincipal> NTLMProxyAuthorization;

	// Definicion de Singleton

	private static HttpClientJavaLib httpClientJavaLib = new HttpClientJavaLib();
	private HttpClientJavaLib() {}
	public static HttpClientJavaLib getInstance() {
		init();
		return httpClientJavaLib;
	}

	private static int statusCode;
	private static String reasonLine;
	private static HttpClientBuilder httpClientBuilder;
	private static PoolingHttpClientConnectionManager connManager;
	private static HttpClientContext httpClientContext;
	private static CloseableHttpClient httpClient;
	private static CloseableHttpResponse response;
	private static CredentialsProvider credentialsProvider;
	private static RequestConfig reqConfig;		// Atributo usado en la ejecucion del metodo (por ejemplo, httpGet, httpPost)
	private static CookieStore cookies;
	private static CookieStore cookiesToSend;

	private static void init() {
		secure = 0;
		port = 80;
		hostChanged = true; // Indica si el próximo request debe ser realizado en una nueva HTTPConnection (si cambio el host)
		headersToSend = new Hashtable<>();
		errDescription = "";
		proxyHost = "";// = HTTPConnection.getDefaultProxyHost() == null ? "" : HTTPConnection.getDefaultProxyHost();
		proxyPort = 80;// = HTTPConnection.getDefaultProxyPort() == 0 ? 80 : HTTPConnection.getDefaultProxyPort();
		proxyInfoChanged = false;
		includeCookies = true;
		tcpNoDelay = false;
		variablesToSend = new Hashtable();
		contentToSend = new Vector<>();
		isMultipart = false;
		multipartTemplate =new MultipartTemplate();
		isURL = false;
		authorizationChanged = false; // Indica si se agregó alguna autorización
		authorizationProxyChanged = false; // Indica si se agregó alguna autorización
		basicAuthorization = new Vector<HttpClientPrincipal>();
		digestAuthorization = new Vector<>();
		NTLMAuthorization = new Vector<>();
		basicProxyAuthorization = new Vector<>();
		digestProxyAuthorization = new Vector<>();
		NTLMProxyAuthorization = new Vector<>();
		contentEncoding = null;
		httpClientBuilder = HttpClients.custom();
		httpClient = null;
		connManager = new PoolingHttpClientConnectionManager();
		httpClientBuilder.setConnectionManager(connManager);
		cookies = new BasicCookieStore();
		resetExecParams();
	}

	private static void resetExecParams() {
		statusCode = 0;
		reasonLine = "";
		reqConfig = null;	// Atributo usado en la ejecucion del metodo (por ejemplo, httpGet, httpPost)
		httpClientContext = null;
		credentialsProvider = null;
		cookiesToSend = null;
		response = null;
		resetErrors();
	}


	private static void resetErrors()
	{
		errCode = 0;
		errDescription = "";
	}

	public short getErrCode()
	{
		return (short) errCode;
	}

	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}

	public String getErrDescription()
	{
		return errDescription;
	}

	public void setErrDescription(String errDescription) {
		this.errDescription = errDescription;
	}

	public void setProxyServerHost(String host)
	{
		this.proxyHost = host;
		proxyInfoChanged = true;
	}

	public void setProxyServerPort(long port)
	{
		this.proxyPort = (int) port;
		proxyInfoChanged = true;
	}

	public String getProxyServerHost()
	{
		return proxyHost;
	}

	public short getProxyServerPort()
	{
		return (short) proxyPort;
	}

	public void setIncludeCookies(boolean value)
	{
		this.includeCookies = value;
	}

	public boolean getIncludeCookies()
	{
		return includeCookies;
	}

	public void setURL(String stringURL)
	{
		try
		{
			URI url = new URI(stringURL);
			setHost(url.getHost());
			setPort(url.getPort());
			setBaseURL(url.getPath());
			setSecure(url.getScheme().equalsIgnoreCase("https") ? 1 : 0);
		}
		catch (ParseException e)
		{
			System.err.println("E " + e + " " + stringURL);
			e.printStackTrace();
		}
	}

	public void setHost(String host)
	{
		if(this.host == null || !this.host.equalsIgnoreCase(host))
		{ // Si el host ha cambiado, dejo marcado para crear una nueva instancia de HTTPConnection
			this.host = host;
			hostChanged = true;

			if (SpecificImplementation.HttpClient != null)
				SpecificImplementation.HttpClient.addSDHeaders(this.host, this.baseURL, this.headersToSend);
		}
	}

	public String getHost()
	{
		return host;
	}

	public void setWSDLURL(String WSDLURL)
	{
		this.WSDLURL = WSDLURL;
	}

	public void setBaseURL(String baseURL)
	{
		this.baseURL = baseURL;
		if (SpecificImplementation.HttpClient != null)
			SpecificImplementation.HttpClient.addSDHeaders(this.host, this.baseURL, this.headersToSend);
	}

	public String getWSDLURL()
	{
		return WSDLURL;
	}

	public String getBaseURL()
	{
		return baseURL;
	}

	public void setPort(int port)
	{
		if(this.port != port)
		{
			hostChanged = true; // Indico que cambio el Host, pues cambió el puerto
			this.port = port;
		}
	}

	public int getPort()
	{
		return port;
	}

	public byte getSecure()
	{
		return (byte) secure;
	}

	public void setSecure(int secure)
	{
		if(this.secure != secure)
		{
			hostChanged = true; // Indico que cambio el Host, pues cambió el protocolo
			this.secure = secure;
		}
	}

	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	public int getTimeout()
	{
		return timeout;
	}

	public void setTcpNoDelay(boolean tcpNoDelay)
	{
		this.tcpNoDelay = tcpNoDelay;
	}

	public boolean getTcpNoDelay() {
		return this.tcpNoDelay;
	}

	public Hashtable<String, String> getheadersToSend() {
		return this.headersToSend;
	}

	public MultipartTemplate getMultipartTemplate() {
		return this.multipartTemplate;
	}

	public void setMultipartTemplate(MultipartTemplate multipartTemplate) {
		this.multipartTemplate = multipartTemplate;
	}

	public boolean getIsMultipart() {
		return this.isMultipart;
	}

	public void setIsMultipart(boolean isMultipart) {
		this.isMultipart = isMultipart;
	}

	public Vector<HttpClientPrincipal> getBasicAuthorization() {
		return this.basicAuthorization;
	}

	public Vector<HttpClientPrincipal> getDigestAuthorization() {
		return this.digestAuthorization;
	}

	public Vector<HttpClientPrincipal> getNTLMAuthorization() {
		return this.NTLMAuthorization;
	}

	public Vector<HttpClientPrincipal> getBasicProxyAuthorization() {
		return this.basicProxyAuthorization;
	}

	public Vector<HttpClientPrincipal> getDigestProxyAuthorization() {
		return this.digestProxyAuthorization;
	}
	public Vector<HttpClientPrincipal> getNTLMProxyAuthorization() {
		return this.NTLMProxyAuthorization;
	}

	public void addAuthentication(int type, String realm, String name, String value)
	{
		setAuthorizationChanged(true);
		switch (type)
		{
			case BASIC:
				basicAuthorization.addElement(new HttpClientPrincipal(realm, name, value));
				break;
			case DIGEST:
				digestAuthorization.addElement(new HttpClientPrincipal(realm, name, value));
				break;
			case NTLM:
				NTLMAuthorization.addElement(new HttpClientPrincipal(realm, name, value));
				break;
		}
	}

	public void addProxyAuthentication(int type, String realm, String name, String value)
	{
		setAuthorizationProxyChanged(true);
		switch (type)
		{
			case BASIC:
				basicProxyAuthorization.addElement(new HttpClientPrincipal(realm, name, value));
				break;
			case DIGEST :
				digestProxyAuthorization.addElement(new HttpClientPrincipal(realm, name, value));
				break;
			case NTLM :
				NTLMProxyAuthorization.addElement(new HttpClientPrincipal(realm, name, value));
				break;
		}
	}

	private static String contentEncoding;

	public void addHeader(String name, String value)
	{
		if(name.equalsIgnoreCase("Content-Type"))
		{
			try
			{
				int index = value.toLowerCase().lastIndexOf("charset");
				int equalsIndex = value.indexOf('=', index) + 1;
				String charset = value.substring(equalsIndex).trim();
				int lastIndex = charset.indexOf(' ');
				if(lastIndex != -1)
				{
					charset = charset.substring(0, lastIndex);
				}
				charset = charset.replace('\"', ' ').replace('\'', ' ').trim();

				contentEncoding = SpecificImplementation.HttpClient.normalizeEncodingName(charset, "UTF-8");
			}catch(Exception e)
			{
			}

			if (value.toLowerCase().startsWith("multipart/form-data")){
				isMultipart = true;
				value = multipartTemplate.contentType;
			}
		}
		headersToSend.put(name, value);
	}

	@SuppressWarnings("unchecked")
	public void addVariable(String name, String value)
	{
		variablesToSend.put(name, value);
	}

	@SuppressWarnings("unchecked")
	public void addBytes(byte[] value)
	{
		contentToSend.addElement(value);
	}

	@SuppressWarnings("unchecked")
	public void addString(String value)
	{
		contentToSend.addElement(value);
	}

	@SuppressWarnings("unchecked")
	public void addFile(String fileName)
	{
		fileName = SpecificImplementation.HttpClient.beforeAddFile(fileName);
		contentToSend.addElement(new File(fileName));
	}

	@SuppressWarnings("unchecked")
	public void addFile(String fileName, String varName)
	{
		fileName = SpecificImplementation.HttpClient.beforeAddFile(fileName);
		contentToSend.addElement(new FormFile(fileName, varName));
	}

	@SuppressWarnings("unchecked")
	public void addStringWriter(StringWriter writer, StringBuffer encoding)
	{
		contentToSend.addElement(new Object[]{writer, encoding});
	}

	public boolean getHostChanged() {
		return hostChanged;
	}

	public void setHostChanged(boolean hostChanged) {
		this.hostChanged = hostChanged;
	}

	public Vector getContentToSend() {
		return  this.contentToSend;
	}

	public void setContentToSend(Vector contentToSend) {
		this.contentToSend = contentToSend;
	}

	public void setVariablesToSend(Hashtable variablesToSend) {
		this.variablesToSend = variablesToSend;
	}

	public Hashtable getVariablesToSend() {
		return this.variablesToSend;
	}

	public boolean isMultipart() {
		return isMultipart;
	}

	public void setMultipart(boolean multipart) {
		isMultipart = multipart;
	}

	private void resetState()
	{
		this.contentToSend.clear();
		this.variablesToSend.clear();
		this.contentToSend.removeAllElements();
		setMultipartTemplate(new MultipartTemplate());
		setIsMultipart(false);
		System.out.println();
	}

	private String getURLValid(String url) {
		URI uri;
		try
		{
			uri = new URI(url);		// En caso que la URL pasada por parametro no sea una URL valida, salta una excepcion en esta linea, y se continua haciendo todo el proceso con los datos ya guardados como atributos
			prevURLhost = this.getHost();
			prevURLbaseURL = this.getBaseURL();
			prevURLport = this.getPort();
			prevURLsecure = this.getSecure();
			isURL = true;
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

	public String getPrevURLhost() {
		return prevURLhost;
	}

	public void setPrevURLhost(String prevURLhost) {
		this.prevURLhost = prevURLhost;
	}

	public String getPrevURLbaseURL() {
		return prevURLbaseURL;
	}

	public void setPrevURLbaseURL(String prevURLbaseURL) {
		this.prevURLbaseURL = prevURLbaseURL;
	}

	public int getPrevURLport() {
		return prevURLport;
	}

	public void setPrevURLport(int prevURLport) {
		this.prevURLport = prevURLport;
	}

	public int getPrevURLsecure() {
		return prevURLsecure;
	}

	public void setPrevURLsecure(int prevURLsecure) {
		this.prevURLsecure = prevURLsecure;
	}

	public boolean getIsURL() {
		return isURL;
	}

	public void setIsURL(boolean isURL) {
		this.isURL = isURL;
	}

	@SuppressWarnings("unchecked")
	private byte[] getData()
	{
		byte[] out = new byte[0];

		for (Object key: getVariablesToSend().keySet())
		{
			String value = getMultipartTemplate().getFormDataTemplate((String)key, (String)getVariablesToSend().get(key));
			getContentToSend().add(0, value); //Variables al principio
		}

		for (int idx = 0; idx < getContentToSend().size(); idx++)
		{
			Object curr = getContentToSend().elementAt(idx);

			if	(curr instanceof String)
			{
				try
				{
					if(contentEncoding != null)
					{
						out = addToArray(out, ((String)curr).getBytes(contentEncoding));
					}else
					{
						out = addToArray(out, (String) curr);
					}
				}catch(UnsupportedEncodingException e)
				{
					System.err.println(e.toString());
					out = addToArray(out, (String) curr);
				}
			}
			else if	(curr instanceof Object[])
			{
				StringWriter writer = (StringWriter)((Object[])curr)[0];
				StringBuffer encoding = (StringBuffer)((Object[])curr)[1];

				if(encoding == null || encoding.length() == 0)
				{
					encoding = new StringBuffer("UTF-8");
				}
				try
				{
					out = addToArray(out, writer.toString().getBytes(encoding.toString()));
				}
				catch(UnsupportedEncodingException e)
				{
					out = addToArray(out, writer.toString());
				}
			}
			else if	(curr instanceof byte[])
			{
				out = addToArray(out, (byte[]) curr);
			}
			else //File or FormFile
			{
				File file;
				if (curr instanceof FormFile)
				{
					FormFile formFile = (FormFile)curr;
					out = startMultipartFile(out, formFile.name, formFile.file);
					file = new File(formFile.file);
				}
				else
				{
					file = (File) curr;
				}
				try
				{
					out = addToArray(out, CommonUtil.readToByteArray(new java.io.BufferedInputStream(new FileInputStream(file))));
				}
				catch (FileNotFoundException e)
				{
					setErrCode(ERROR_IO);
					setErrDescription(e.getMessage());
				}
				catch (IOException e)
				{
					setErrCode(ERROR_IO);
					setErrDescription(e.getMessage());
				}
			}
		}
		out = endMultipartBoundary(out);
		return out;
	}

	private byte[] startMultipartFile(byte[] in, String name, String fileName)
	{
		if (getIsMultipart() && CommonUtil.fileExists(fileName)==1)
		{
			if (name==null || name=="")
			{
				name = CommonUtil.getFileName(fileName);
			}
			byte[] out = addToArray(in, getMultipartTemplate().boundarybytes);
			String mimeType = SpecificImplementation.Application.getContentType(fileName);
			String header = getMultipartTemplate().getHeaderTemplate(name, fileName, mimeType);
			try{
				byte[] headerbytes = header.getBytes("UTF8");
				out = addToArray(out,headerbytes);
			} catch( java.io.UnsupportedEncodingException uee)
			{
				byte[] headerbytes = header.getBytes();
				out = addToArray(out,headerbytes);
			}

			return out;
		}else{
			return in;
		}
	}

	private byte[] endMultipartBoundary(byte[] in)
	{
		if (getIsMultipart()){
			return addToArray(in, getMultipartTemplate().endBoundaryBytes);
		}else{
			return in;
		}
	}

	private byte[] addToArray(byte[] in, String val)
	{
		byte[] out = new byte[in.length + val.length()];

		System.arraycopy(in, 0, out, 0, in.length);
		out = val.getBytes();

		return out;
	}

	private byte[] addToArray(byte[] in, byte[] val)
	{
		byte[] out = new byte[in.length + val.length];

		System.arraycopy(in, 0, out, 0, in.length);
		System.arraycopy(val, 0, out, in.length, val.length);

		return out;
	}

	public boolean getAuthorizationChanged() {
		return authorizationChanged;
	}

	public void setAuthorizationChanged(boolean authorizationChanged) {
		this.authorizationChanged = authorizationChanged;
	}

	public boolean getAuthorizationProxyChanged() {
		return authorizationProxyChanged;
	}

	public void setAuthorizationProxyChanged(boolean authorizationProxyChanged) {
		this.authorizationProxyChanged = authorizationProxyChanged;
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
//		System.out.println("nuevito");
		resetExecParams();

		url = getURLValid(url);		// Funcion genera parte del path en adelante de la URL
		if  (!url.startsWith("/"))
			url = !getBaseURL().startsWith("/")?"/" + getBaseURL() + url:getBaseURL() + url;
		if (getSecure() == 1)        // Se completa con esquema y host
			url = "https://" + getHost() + ":" + getPort() + url;
		else
			url = "http://" + getHost() + ":" + getPort() + url;

		try {
			if (getHostChanged()) {
				if (getSecure() == 1 && getPort() == 80) {
					setPort(443);
				}
				if (getSecure() != 0) {
					SSLConnectionSocketFactory sslsf = getSSLSecureInstance();
					httpClientBuilder.setSSLSocketFactory(sslsf);        // seteo SSL en HttpClientBuilder
				}
				SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(getTcpNoDelay()).build();	// Seteo de TcpNoDelay
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
				httpClientContext = HttpClientContext.create();
				httpClientContext.setCredentialsProvider(credentialsProvider);
			}

			httpClient = this.httpClientBuilder.build();

			if (method.equalsIgnoreCase("GET")) {		// No se le agrega ningun body al envio (caso excepcional segun RFC 7231)
				HttpGet httpget = new HttpGet(url.trim());
				httpget.setConfig(reqConfig);
				Set<String> keys = headersToSend.keySet();
				for (String header : keys) {
					httpget.addHeader(header,headersToSend.get(header));
				}
				response = httpClient.execute(httpget, this.httpClientContext);
				statusCode = response.getStatusLine().getStatusCode();
				reasonLine = response.getStatusLine().getReasonPhrase();



			} else if (method.equalsIgnoreCase("POST")) {
				HttpPost httpPost = new HttpPost(url.trim());
				httpPost.setConfig(reqConfig);
				Set<String> keys = headersToSend.keySet();
				for (String header : keys) {
					httpPost.addHeader(header,headersToSend.get(header));
				}
				ByteArrayEntity dataToSend = null;
				if (!getIsMultipart() && getVariablesToSend().size() > 0)
					dataToSend = new ByteArrayEntity(Codecs.nv2query(hashtableToNVPair(getVariablesToSend())).getBytes());
				else
					dataToSend = new ByteArrayEntity(getData());
				httpPost.setEntity(dataToSend);
				response = httpClient.execute(httpPost, httpClientContext);
				statusCode = response.getStatusLine().getStatusCode();
				reasonLine = response.getStatusLine().getReasonPhrase();

			} else if (method.equalsIgnoreCase("PUT")) {
				HttpPut httpPut = new HttpPut(url.trim());
				httpPut.setConfig(reqConfig);
				Set<String> keys = headersToSend.keySet();
				for (String header : keys) {
					httpPut.addHeader(header,headersToSend.get(header));
				}
				ByteArrayEntity dataToSend = new ByteArrayEntity(getData());
				httpPut.setEntity(dataToSend);
				response = httpClient.execute(httpPut,httpClientContext);
				statusCode = response.getStatusLine().getStatusCode();
				reasonLine = response.getStatusLine().getReasonPhrase();

			} else if (method.equalsIgnoreCase("DELETE")) {		// No se le agrega ningun body al envio (caso excepcional segun RFC 7231)
				HttpDelete httpDelete = new HttpDelete(url.trim());
				httpDelete.setConfig(reqConfig);
				Set<String> keys = headersToSend.keySet();
				for (String header : keys) {
					httpDelete.addHeader(header,headersToSend.get(header));
				}
//				ByteArrayEntity dataToSend;
//				if (getVariablesToSend().size() > 0 || getContentToSend().size() > 0) {
//					dataToSend = new ByteArrayEntity(getData());
//					httpDelete.
//				}
				response = httpClient.execute(httpDelete,httpClientContext);
				statusCode = response.getStatusLine().getStatusCode();
				reasonLine = response.getStatusLine().getReasonPhrase();

			} else if (method.equalsIgnoreCase("HEAD")) {		// No se le agrega ningun body al envio (caso excepcional segun RFC 7231)
				HttpHead httpHead = new HttpHead(url.trim());
				httpHead.setConfig(reqConfig);
				Set<String> keys = headersToSend.keySet();
				for (String header : keys) {
					httpHead.addHeader(header,headersToSend.get(header));
				}
//				ByteArrayEntity dataToSend = new ByteArrayEntity(getData());

				response = httpClient.execute(httpHead,httpClientContext);
				statusCode = response.getStatusLine().getStatusCode();
				reasonLine = response.getStatusLine().getReasonPhrase();


			} else if (method.equalsIgnoreCase("CONNECT")) {		// No se le agrega ningun body al envio (caso excepcional segun RFC 7231)


			} else if (method.equalsIgnoreCase("OPTIONS")) {		// No se le agrega ningun body al envio (caso excepcional segun RFC 7231)
				HttpOptions httpOptions = new HttpOptions(url.trim());
				httpOptions.setConfig(reqConfig);
				Set<String> keys = headersToSend.keySet();
				for (String header : keys) {
					httpOptions.addHeader(header,headersToSend.get(header));
				}
//				ByteArrayEntity dataToSend = new ByteArrayEntity(getData());
				response = httpClient.execute(httpOptions,httpClientContext);
				statusCode = response.getStatusLine().getStatusCode();
				reasonLine = response.getStatusLine().getReasonPhrase();

			} else if (method.equalsIgnoreCase("TRACE")) {		// No lleva payload
				HttpTrace httpTrace = new HttpTrace(url.trim());
				httpTrace.setConfig(reqConfig);
				Set<String> keys = headersToSend.keySet();
				for (String header : keys) {
					httpTrace.addHeader(header,headersToSend.get(header));
				}
				response = httpClient.execute(httpTrace,httpClientContext);
				statusCode = response.getStatusLine().getStatusCode();
				reasonLine = response.getStatusLine().getReasonPhrase();

			} else if (method.equalsIgnoreCase("PATCH")) {
				HttpPatch httpPatch = new HttpPatch(url.trim());
				httpPatch.setConfig(reqConfig);
				Set<String> keys = headersToSend.keySet();
				for (String header : keys) {
					httpPatch.addHeader(header,headersToSend.get(header));
				}
				ByteArrayEntity dataToSend = new ByteArrayEntity(getData());
				httpPatch.setEntity(dataToSend);
				response = httpClient.execute(httpPatch,httpClientContext);
				statusCode = response.getStatusLine().getStatusCode();
				reasonLine = response.getStatusLine().getReasonPhrase();
			}

			if (cookiesToSend != null)
				SetCookieAtr(this.cookiesToSend);		// Se setean las cookies devueltas en la lista de cookies

		} catch (IOException e) {
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
		finally {		// Se cierra la conexion siempre al final
			getStatusCode();
			if (getIsURL())
			{
				this.setHost(getPrevURLhost());
				this.setBaseURL(getPrevURLbaseURL());
				this.setPort(getPrevURLport());
				this.setSecure(getPrevURLsecure());
				setIsURL(false);
			}
			resetState();
		}

	}



	public int getStatusCode() {
		return statusCode;
	}

	public String getReasonLine() {
		return reasonLine;
	}

	public void getHeader(String name, long[] value) {
		if (response == null)
			return;
		Header[] headers = response.getHeaders(name);
		if (headers == null)
			throw new NumberFormatException("null");
//		for (int i = 0; i< headers.length; i++) {			// Posible solucion en el caso que se quieran poner todos los headers que se obtienen con el name pasado en el parametro value
//			value[i] = Integer.parseInt(headers[i].getValue());
//		}
		value[0] = Integer.parseInt(headers[0].getValue());
	}

	public String getHeader(String name) {
		if (response == null || response.getHeaders(name) == null)
			return "";
		return response.getHeaders(name)[0].getValue();
	}

	public void getHeader(String name, String[] value) {
		if (response == null || response.getHeaders(name) == null)
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
		if (response == null || response.getHeaders(name) == null)
			return;
		value[0] = CommonUtil.val(response.getHeaders(name)[0].getValue());
	}

	public InputStream getInputStream() throws IOException {
		return response.getEntity().getContent();
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
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
		return "";
	}

	public void toFile(String fileName) {
		if (response == null)
			return;
		try {
			CommonUtil.InputStreamToFile(response.getEntity().getContent(), fileName);
		} catch (IOException e) {
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
	}


	public void cleanup() {
		if (response != null) {
			try {
				response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (httpClient != null) {
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (connManager != null)
			connManager.close();
	}

	static class MultipartTemplate
	{
		public String boundary;
		public String formdataTemplate;
		public byte[] boundarybytes;
		public byte[] endBoundaryBytes;
		public String contentType;

		public MultipartTemplate()
		{
			boundary = "----------------------------" + CommonUtil.now(false,false).getTime();
			contentType = "multipart/form-data; boundary=" + boundary;
			String boundaryStr = "\r\n--" + boundary + "\r\n";
			String endBoundaryStr = "\r\n--" + boundary + "--";
			try{
				boundarybytes = boundaryStr.getBytes("ASCII");
				endBoundaryBytes = endBoundaryStr.getBytes("ASCII");
			} catch( java.io.UnsupportedEncodingException uee)
			{
				boundarybytes = boundaryStr.getBytes();
				endBoundaryBytes = endBoundaryStr.getBytes();
			}
		}
		String getHeaderTemplate(String name, String fileName, String mimeType){
			return "Content-Disposition: form-data; name=\""+ name + "\"; filename=\""+ fileName + "\"\r\n" + "Content-Type: " + mimeType + "\r\n\r\n";
		}
		String getFormDataTemplate(String varName, String value){
			return "\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + varName + "\";\r\n\r\n" + value;
		}
	}
}