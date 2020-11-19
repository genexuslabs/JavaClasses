package com.genexus.internet;

import HTTPClient.ParseException;
import HTTPClient.URI;
import com.genexus.CommonUtil;
import com.genexus.common.interfaces.SpecificImplementation;

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

public abstract class GXHttpClient implements IHttpClient{
	protected final int BASIC  = 0;
	protected final int DIGEST = 1;
	protected final int NTLM   = 2;

	public final int ERROR_IO = 1;

	private String host;
	private String baseURL;
	private int secure = 0;
	private int timeout;
	private int port = 80;
	private boolean hostChanged = true; // Indica si el próximo request debe ser realizado en una nueva HTTPConnection (si cambio el host)
	private Hashtable<String, String> headersToSend = new Hashtable<>();
	private String WSDLURL;
	private int errCode;
	private String errDescription = "";
	private String proxyHost = "";// = HTTPConnection.getDefaultProxyHost() == null ? "" : HTTPConnection.getDefaultProxyHost();
	private int proxyPort = 80;// = HTTPConnection.getDefaultProxyPort() == 0 ? 80 : HTTPConnection.getDefaultProxyPort();
	private boolean includeCookies = true;
	private boolean tcpNoDelay = false;
	private Hashtable variablesToSend = new Hashtable();
	private Vector contentToSend = new Vector<>();
	private boolean isMultipart = false;
	private MultipartTemplate multipartTemplate =new MultipartTemplate();
	private String prevURLhost;
	private String prevURLbaseURL;
	private int prevURLport;
	private int prevURLsecure;
	private boolean isURL = false;
	private boolean authorizationChanged = false; // Indica si se agregó alguna autorización
	private boolean authorizationProxyChanged = false; // Indica si se agregó alguna autorización

	private Vector<HttpClientPrincipal> basicAuthorization = new Vector<HttpClientPrincipal>();
	private Vector<HttpClientPrincipal> digestAuthorization = new Vector<>();
	private Vector<HttpClientPrincipal> NTLMAuthorization = new Vector<>();

	private Vector<HttpClientPrincipal> basicProxyAuthorization = new Vector<>();
	private Vector<HttpClientPrincipal> digestProxyAuthorization = new Vector<>();
	private Vector<HttpClientPrincipal> NTLMProxyAuthorization = new Vector<>();

	public static boolean issuedExternalHttpClientWarning = false;
	public boolean usingExternalHttpClient = false;

	protected void resetErrors()
	{
		errCode = 0;
		errDescription = "";
	}

	public byte getBasic()
	{
		return BASIC;
	}

	public byte getDigest()
	{
		return DIGEST;
	}

	public byte getNtlm()
	{
		return NTLM;
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

	boolean proxyInfoChanged = false;
	public void setProxyServerHost(String host)
	{
		this.proxyHost = host;
		proxyInfoChanged = true;
	}

	public String getProxyServerHost()
	{
		return proxyHost;
	}

	public void setProxyServerPort(long port)
	{
		this.proxyPort = (int) port;
		proxyInfoChanged = true;
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

	public void addCertificate(String fileName)
	{
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

	protected String contentEncoding = null;

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

	public abstract void execute(String method, String url);

	public abstract int getStatusCode();

	public abstract String getReasonLine();

	public abstract void getHeader(String name, long[] value);

	public abstract String getHeader(String name);

	public abstract void getHeader(String name, String[] value);

	public abstract void getHeader(String name, java.util.Date[] value);

	public abstract void getHeader(String name, double[] value);

	public abstract InputStream getInputStream() throws IOException;

	public abstract String getString();

	public abstract void toFile(String fileName);

	public abstract void cleanup();

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

	protected void resetState()
	{
		this.contentToSend.clear();
		this.variablesToSend.clear();
		this.contentToSend.removeAllElements();
		setMultipartTemplate(new MultipartTemplate());
		setIsMultipart(false);
		System.out.println();
	}

	protected String getURLValid(String url) {
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
			//No es una URL
			System.out.println("The String url parameter passed is not a valid one.");
			if  (!url.startsWith("/"))		// Este caso sucede cuando salta la excepcion ParseException, determinando que la url pasada por parametro no es una URL valida
				return getBaseURL().trim() + url;
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
	protected byte[] getData()
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


	class FormFile{
		String file;
		String name;
		FormFile(String file, String name){
			this.file = file;
			this.name = name;
		}
	}

	class MultipartTemplate
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

	class HttpClientPrincipal
	{
		String realm;
		String user;
		String password;

		HttpClientPrincipal(String realm, String user, String password)
		{
			this.realm = realm;
			this.user = user;
			this.password = password;
		}
	}

}
