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
	private String baseURL = "/";
	private int secure = 0;
	private int timeout = 30;
	private int port = 80;
	private boolean  hostChanged = true; // Indica si el próximo request debe ser realizado en una nueva HTTPConnection (si cambio el host)
	private Hashtable<String, String> headersToSend = new Hashtable<>();
	private String WSDLURL;
	private int errCode;
	private String errDescription = "";
	private String proxyHost = "";// = HTTPConnection.getDefaultProxyHost() == null ? "" : HTTPConnection.getDefaultProxyHost();
	private int proxyPort = 80;// = HTTPConnection.getDefaultProxyPort() == 0 ? 80 : HTTPConnection.getDefaultProxyPort();
	private boolean proxyInfoChanged = false;
	private boolean includeCookies = true;
	private boolean tcpNoDelay = false;
	private Hashtable variablesToSend = new Hashtable();
	private Vector contentToSend = new Vector<>();
	private boolean isMultipart = false;
	private MultipartTemplate multipartTemplate = new MultipartTemplate();
	private String prevURLhost;
	private String prevURLbaseURL;
	private int prevURLport;
	private int prevURLsecure;
	private boolean isURL = false;
	private boolean authorizationChanged = false; // Indica si se agregó alguna autorización
	private boolean authorizationProxyChanged = false; // Indica si se agregó alguna autorización

	private Vector<HttpClientPrincipal> basicAuthorization = new Vector<>();
	private Vector<HttpClientPrincipal> digestAuthorization = new Vector<>();
	private Vector<HttpClientPrincipal> NTLMAuthorization = new Vector<>();

	private Vector<HttpClientPrincipal> basicProxyAuthorization = new Vector<>();
	private Vector<HttpClientPrincipal> digestProxyAuthorization = new Vector<>();
	private Vector<HttpClientPrincipal> NTLMProxyAuthorization = new Vector<>();
//
	public static boolean issuedExternalHttpClientWarning = false;
	public boolean usingExternalHttpClient = false;

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
		try {
			return (short) errCode;
		} catch (NullPointerException e) {
			errCode = 0;
			return (short) 0;
		}

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
		this.proxyHost = host == null ? host : host.trim();
		this.proxyInfoChanged = true;
	}

	public void setProxyServerPort(long port)
	{
		this.proxyPort = (int) port;
		this.proxyInfoChanged = true;
	}

	public String getProxyServerHost()
	{
		return proxyHost;
	}

	public short getProxyServerPort()
	{
		try {
			return (short) (int) proxyPort;
		} catch (NullPointerException e) {
			this.proxyPort = 80;
			return (short) 80;
		}
	}

	public void setIncludeCookies(boolean value)
	{
		this.includeCookies = value;
		this.hostChanged = true;
	}

	public boolean getIncludeCookies()
	{
		return this.includeCookies;
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
			this.host = host == null ? host : host.trim();
			this.hostChanged = true;

			if (SpecificImplementation.HttpClient != null)
				SpecificImplementation.HttpClient.addSDHeaders(this.host,
					this.baseURL,
					this.headersToSend);
		}
	}

	public String getHost()
	{
		return host;
	}

	public void setWSDLURL(String WSDLURL)
	{
		this.WSDLURL = WSDLURL == null ? WSDLURL : WSDLURL.trim();
	}

	public String getWSDLURL()
	{
		return WSDLURL;
	}

	public void setBaseURL(String baseURL)
	{
		this.baseURL = baseURL == null ? baseURL : baseURL.trim();
		if (SpecificImplementation.HttpClient != null)
			SpecificImplementation.HttpClient.addSDHeaders(this.host,
				this.baseURL,
				this.headersToSend);
	}

	public String getBaseURL()
	{
		return baseURL;
	}

	public void setPort(int port)
	{
		if(this.port != port)
		{
			this.port = port;
			this.hostChanged = true; // Indico que cambio el Host, pues cambió el puerto
		}
	}

	public int getPort()
	{
		return port;
	}

	public byte getSecure()
	{
		return (byte) (int) this.secure;

	}

	public void setSecure(int secure)
	{
		if(this.secure != secure)
		{
			this.secure = secure;
			this.hostChanged = true; // Indico que cambio el Host, pues cambió el protocolo

		}
	}

	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	public int getTimeout()
	{
		try {
			return timeout;
		} catch (NullPointerException e) {
			this.timeout = 30;		// En caso que no se haya seteado un timeout se setea por defecto en 30 segundos
			return 30;
		}
	}

	public void setTcpNoDelay(boolean tcpNoDelay)
	{
		this.tcpNoDelay = tcpNoDelay;
		this.hostChanged = true;
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
				if (this.basicAuthorization == null)
					this.basicAuthorization = new Vector<>();
				basicAuthorization.addElement(new HttpClientPrincipal(realm, name, value));
				break;

			case DIGEST:
				if (this.digestAuthorization == null)
					this.digestAuthorization = new Vector<>();
				digestAuthorization.addElement(new HttpClientPrincipal(realm, name, value));
				break;

			case NTLM:
				if (this.NTLMAuthorization == null)
					this.NTLMAuthorization = new Vector<>();
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
				if (this.basicProxyAuthorization == null)
					this.basicProxyAuthorization = new Vector<>();
				basicProxyAuthorization.addElement(new HttpClientPrincipal(realm, name, value));
				break;

			case DIGEST :
				if (this.digestProxyAuthorization == null)
					this.digestProxyAuthorization = new Vector<>();
				digestProxyAuthorization.addElement(new HttpClientPrincipal(realm, name, value));
				break;

			case NTLM :
				if (this.NTLMProxyAuthorization == null)
					this.NTLMProxyAuthorization = new Vector<>();
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
				this.isMultipart = true;
				value = multipartTemplate.contentType;
			}
		}
		if (this.headersToSend == null)
			this.headersToSend =new Hashtable<>();
		for (String elem: headersToSend.keySet()) {
			if (elem.equalsIgnoreCase(name)) {
				headersToSend.put(elem, value);
				return;
			}
		}
		headersToSend.put(name, value);
	}

	@SuppressWarnings("unchecked")
	public void addVariable(String name, String value)
	{
		if (this.variablesToSend == null)
			this.variablesToSend =  new Hashtable();
		variablesToSend.put(name, value);
	}

	@SuppressWarnings("unchecked")
	public void addBytes(byte[] value)
	{
		if (this.contentToSend == null)
			this.contentToSend = new Vector<>();
		contentToSend.addElement(value);
	}

	@SuppressWarnings("unchecked")
	public void addString(String value)
	{
		if (this.contentToSend == null)
			this.contentToSend = new Vector<>();
		this.contentToSend.addElement(value);
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

	protected void resetState()
	{
		this.contentToSend.clear();
		this.variablesToSend.clear();
		this.contentToSend.removeAllElements();
		setMultipartTemplate(new MultipartTemplate());
		setIsMultipart(false);
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

	public abstract InputStream getInputStream(String stringURL) throws IOException;

	public abstract String getString();

	public abstract void toFile(String fileName);

	public abstract void cleanup();

	public void setVariablesToSend(Hashtable variablesToSend) {
		this.variablesToSend = variablesToSend;
	}

	public Hashtable getVariablesToSend() {
		return this.variablesToSend;
	}

	public boolean isMultipart() {
		return this.isMultipart;
	}

	public void setMultipart(boolean multipart) {
		this.isMultipart = multipart;
	}

	public String getPrevURLhost() {
		return this.prevURLhost;
	}

	public void setPrevURLhost(String prevURLhost) {
		this.prevURLhost = prevURLhost;
	}

	public String getPrevURLbaseURL() {
		return this.prevURLbaseURL;
	}

	public void setPrevURLbaseURL(String prevURLbaseURL) {
		this.prevURLbaseURL = prevURLbaseURL;
	}

	public int getPrevURLport() {
		return this.prevURLport;
	}

	public void setPrevURLport(int prevURLport) {
		this.prevURLport = prevURLport;
	}

	public int getPrevURLsecure() {
		return this.prevURLsecure;
	}

	public void setPrevURLsecure(int prevURLsecure) {
		this.prevURLsecure = prevURLsecure;
	}

	public boolean getIsURL() {
		return this.isURL;
	}

	public void setIsURL(boolean isURL) {
		this.isURL = isURL;
	}

	protected String setPathUrl(String url) {
		if (!getIsURL()) {		// Si no es URL absoluta
			if (!getBaseURL().isEmpty()) {
				if (!getBaseURL().startsWith("/") && !getBaseURL().startsWith("http"))
					setBaseURL("/" + getBaseURL());
				if (url.isEmpty())
					url = getBaseURL();
				else {
					if (!url.startsWith("/"))
						url = getBaseURL() + (getBaseURL().endsWith("/") ? url.trim() : "/" + url.trim());
					else
						url = getBaseURL() + (getBaseURL().endsWith("/") ? url.substring(1).trim() : url.trim());
				}
			} else {
				if (!url.startsWith("/"))
					url = "/" + url.trim();
			}
		}
		return url;
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

	protected void setExceptionsCatch(Exception e) {
		setErrCode(ERROR_IO);
		setErrDescription(e.getMessage());
	}

	public boolean getAuthorizationChanged() {
		return this.authorizationChanged;
	}

	public void setAuthorizationChanged(boolean authorizationChanged) {
		this.authorizationChanged = authorizationChanged;
	}

	public boolean getAuthorizationProxyChanged() {
		return this.authorizationProxyChanged;
	}

	public void setAuthorizationProxyChanged(boolean authorizationProxyChanged) {
		this.authorizationProxyChanged = authorizationProxyChanged;
	}

	public boolean getProxyInfoChanged() {
		return proxyInfoChanged;
	}

	public void setProxyInfoChanged(boolean proxyInfoChanged) {
		this.proxyInfoChanged = proxyInfoChanged;
	}


	class FormFile{
		String file;
		String name;
		FormFile(String file, String name){
			this.file = file;
			this.name = name;
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
			fileName = new File(fileName).getName();
			return "Content-Disposition: form-data; name=\""+ name + "\"; filename=\""+ fileName + "\"\r\n" + "Content-Type: " + mimeType + "\r\n\r\n";
		}
		String getFormDataTemplate(String varName, String value){
			return "\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + varName + "\";\r\n\r\n" + value;
		}
	}

}
