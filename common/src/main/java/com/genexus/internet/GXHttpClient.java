package com.genexus.internet;

import HTTPClient.NVPair;
import HTTPClient.ParseException;
import HTTPClient.URI;
import com.genexus.CommonUtil;
import com.genexus.common.interfaces.SpecificImplementation;

import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public abstract class GXHttpClient implements IHttpClient{
	protected final int BASIC  = 0;
	protected final int DIGEST = 1;
	protected final int NTLM   = 2;

	public final int ERROR_IO = 1;

	private ConcurrentHashMap<Long,String> host = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,String> baseURL = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,Integer> secure = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,Integer> timeout = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,Integer> port = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,Boolean> hostChanged = new ConcurrentHashMap<>(); // Indica si el próximo request debe ser realizado en una nueva HTTPConnection (si cambio el host)
	private ConcurrentHashMap<Long,Hashtable<String, String>> headersToSend = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,String> WSDLURL = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,Integer> errCode = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,String> errDescription = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,String> proxyHost = new ConcurrentHashMap<>();// = HTTPConnection.getDefaultProxyHost() == null ? "" : HTTPConnection.getDefaultProxyHost();
	private ConcurrentHashMap<Long,Integer> proxyPort = new ConcurrentHashMap<>();// = HTTPConnection.getDefaultProxyPort() == 0 ? 80 : HTTPConnection.getDefaultProxyPort();
	private ConcurrentHashMap<Long,Boolean> proxyInfoChanged = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,Boolean> includeCookies = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,Boolean> tcpNoDelay = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,Hashtable> variablesToSend = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,Vector> contentToSend = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,Boolean> isMultipart = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,MultipartTemplate> multipartTemplate = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,String> prevURLhost = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,String> prevURLbaseURL = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,Integer> prevURLport = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,Integer> prevURLsecure = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,Boolean> isURL = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,Boolean> authorizationChanged = new ConcurrentHashMap<>(); // Indica si se agregó alguna autorización
	private ConcurrentHashMap<Long,Boolean> authorizationProxyChanged = new ConcurrentHashMap<>(); // Indica si se agregó alguna autorización

	private ConcurrentHashMap<Long,Vector<HttpClientPrincipal>> basicAuthorization = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,Vector<HttpClientPrincipal>> digestAuthorization = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,Vector<HttpClientPrincipal>> NTLMAuthorization = new ConcurrentHashMap<>();

	private ConcurrentHashMap<Long,Vector<HttpClientPrincipal>> basicProxyAuthorization = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,Vector<HttpClientPrincipal>> digestProxyAuthorization = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Long,Vector<HttpClientPrincipal>> NTLMProxyAuthorization = new ConcurrentHashMap<>();
//
	public static boolean issuedExternalHttpClientWarning = false;
	public ConcurrentHashMap<Long,Boolean> usingExternalHttpClient = new ConcurrentHashMap<>();

	private void putOrRemoveIfNull(ConcurrentHashMap atr, Object value) {
		if (value == null)
			atr.remove(Thread.currentThread().getId());
		else
			atr.put(Thread.currentThread().getId(), value);
	}

	protected void initBaseAtr() {
		this.secure.put(Thread.currentThread().getId(), 0);
		this.port.put(Thread.currentThread().getId(), 80);
		this.hostChanged.put(Thread.currentThread().getId(), true);
		this.headersToSend.put(Thread.currentThread().getId(),new Hashtable<>());
		this.proxyHost.put(Thread.currentThread().getId(), "");
		this.proxyPort.put(Thread.currentThread().getId(), 80);
		this.proxyInfoChanged.put(Thread.currentThread().getId(), false);
		this.includeCookies.put(Thread.currentThread().getId(), true);
		this.tcpNoDelay.put(Thread.currentThread().getId(), false);
		this.variablesToSend.put(Thread.currentThread().getId(),  new Hashtable());
		this.contentToSend.put(Thread.currentThread().getId(), new Vector<>());
		this.isMultipart.put(Thread.currentThread().getId(), false);
		this.multipartTemplate.put(Thread.currentThread().getId(), new MultipartTemplate());
		this.isURL.put(Thread.currentThread().getId(), false);
		this.authorizationChanged.put(Thread.currentThread().getId(), false);
		this.authorizationProxyChanged.put(Thread.currentThread().getId(), false);
		this.basicAuthorization.put(Thread.currentThread().getId(), new Vector<>());
		this.digestAuthorization.put(Thread.currentThread().getId(), new Vector<>());
		this.NTLMAuthorization.put(Thread.currentThread().getId(), new Vector<>());
		this.basicProxyAuthorization.put(Thread.currentThread().getId(), new Vector<>());
		this.digestProxyAuthorization.put(Thread.currentThread().getId(), new Vector<>());
		this.NTLMProxyAuthorization.put(Thread.currentThread().getId(), new Vector<>());
		this.usingExternalHttpClient.put(Thread.currentThread().getId(), false);
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
		try {
			return (short) (int) errCode.get(Thread.currentThread().getId());
		} catch (NullPointerException e) {
			errCode.put(Thread.currentThread().getId(), 0);
			return (short) 0;
		}

	}

	public void setErrCode(int errCode) {
		putOrRemoveIfNull(this.errCode, errCode);
	}

	public String getErrDescription()
	{
		return errDescription.get(Thread.currentThread().getId());
	}

	public void setErrDescription(String errDescription) {
		putOrRemoveIfNull(this.errDescription,errDescription);
	}

	public void setProxyServerHost(String host)
	{
		putOrRemoveIfNull(this.proxyHost,host);
		this.proxyInfoChanged.put(Thread.currentThread().getId(), true);
	}

	public void setProxyServerPort(long port)
	{
		putOrRemoveIfNull(this.proxyPort,(int) port);
		this.proxyInfoChanged.put(Thread.currentThread().getId(), true);
	}

	public String getProxyServerHost()
	{
		return proxyHost.get(Thread.currentThread().getId());
	}

	public short getProxyServerPort()
	{
		try {
			return (short) (int) proxyPort.get(Thread.currentThread().getId());
		} catch (NullPointerException e) {
			this.proxyPort.put(Thread.currentThread().getId(), 80);
			return (short) 80;
		}
	}

	public void setIncludeCookies(boolean value)
	{
		this.includeCookies.put(Thread.currentThread().getId(), value);
	}

	public boolean getIncludeCookies()
	{
		return this.includeCookies.get(Thread.currentThread().getId());
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
		if(this.host.get(Thread.currentThread().getId()) == null || !this.host.get(Thread.currentThread().getId()).equalsIgnoreCase(host))
		{ // Si el host ha cambiado, dejo marcado para crear una nueva instancia de HTTPConnection
			putOrRemoveIfNull(this.host,host);
			this.hostChanged.put(Thread.currentThread().getId(), true);

			if (SpecificImplementation.HttpClient != null)
				SpecificImplementation.HttpClient.addSDHeaders(this.host.get(Thread.currentThread().getId()),
					this.baseURL.get(Thread.currentThread().getId()),
					this.headersToSend.get(Thread.currentThread().getId()));
		}
	}

	public String getHost()
	{
		return host.get(Thread.currentThread().getId());
	}

	public void setWSDLURL(String WSDLURL)
	{
		putOrRemoveIfNull(this.WSDLURL,WSDLURL);
	}

	public String getWSDLURL()
	{
		return WSDLURL.get(Thread.currentThread().getId());
	}

	public void setBaseURL(String baseURL)
	{
		putOrRemoveIfNull(this.baseURL,baseURL);
		if (SpecificImplementation.HttpClient != null)
			SpecificImplementation.HttpClient.addSDHeaders(this.host.get(Thread.currentThread().getId()),
				this.baseURL.get(Thread.currentThread().getId()),
				this.headersToSend.get(Thread.currentThread().getId()));
	}

	public String getBaseURL()
	{
		return baseURL.get(Thread.currentThread().getId());
	}

	public void setPort(int port)
	{
		if(this.port.get(Thread.currentThread().getId()) != port)
		{
			putOrRemoveIfNull(this.port,port);
			this.hostChanged.put(Thread.currentThread().getId(), true); // Indico que cambio el Host, pues cambió el puerto
		}
	}

	public int getPort()
	{
		return port.get(Thread.currentThread().getId());
	}

	public byte getSecure()
	{
		return (byte) (int) this.secure.get(Thread.currentThread().getId());

	}

	public void setSecure(int secure)
	{
		if(this.secure.get(Thread.currentThread().getId()) != secure)
		{
			putOrRemoveIfNull(this.secure,secure);
			this.hostChanged.put(Thread.currentThread().getId(), true); // Indico que cambio el Host, pues cambió el protocolo

		}
	}

	public void setTimeout(int timeout)
	{
		putOrRemoveIfNull(this.timeout,timeout);
	}

	public int getTimeout()
	{
		try {
			return timeout.get(Thread.currentThread().getId());
		} catch (NullPointerException e) {
			this.timeout.put(Thread.currentThread().getId(), 30);		// En caso que no se haya seteado un timeout se setea por defecto en 30 segundos
			return 30;
		}
	}

	public void setTcpNoDelay(boolean tcpNoDelay)
	{
		this.tcpNoDelay.put(Thread.currentThread().getId(), tcpNoDelay);
	}

	public boolean getTcpNoDelay() {
		return this.tcpNoDelay.get(Thread.currentThread().getId());
	}

	public Hashtable<String, String> getheadersToSend() {
		return this.headersToSend.get(Thread.currentThread().getId());
	}

	public MultipartTemplate getMultipartTemplate() {
		return this.multipartTemplate.get(Thread.currentThread().getId());
	}

	public void setMultipartTemplate(MultipartTemplate multipartTemplate) {
		putOrRemoveIfNull(this.multipartTemplate,multipartTemplate);
	}

	public boolean getIsMultipart() {
		return this.isMultipart.get(Thread.currentThread().getId());
	}

	public void setIsMultipart(boolean isMultipart) {
		this.isMultipart.put(Thread.currentThread().getId(), isMultipart);
	}

	public Vector<HttpClientPrincipal> getBasicAuthorization() {
		return this.basicAuthorization.get(Thread.currentThread().getId());
	}

	public Vector<HttpClientPrincipal> getDigestAuthorization() {
		return this.digestAuthorization.get(Thread.currentThread().getId());
	}

	public Vector<HttpClientPrincipal> getNTLMAuthorization() {
		return this.NTLMAuthorization.get(Thread.currentThread().getId());
	}

	public Vector<HttpClientPrincipal> getBasicProxyAuthorization() {
		return this.basicProxyAuthorization.get(Thread.currentThread().getId());
	}

	public Vector<HttpClientPrincipal> getDigestProxyAuthorization() {
		return this.digestProxyAuthorization.get(Thread.currentThread().getId());
	}
	public Vector<HttpClientPrincipal> getNTLMProxyAuthorization() {
		return this.NTLMProxyAuthorization.get(Thread.currentThread().getId());
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
				basicAuthorization.get(Thread.currentThread().getId()).addElement(new HttpClientPrincipal(realm, name, value));
				break;
			case DIGEST:
				digestAuthorization.get(Thread.currentThread().getId()).addElement(new HttpClientPrincipal(realm, name, value));
				break;
			case NTLM:
				NTLMAuthorization.get(Thread.currentThread().getId()).addElement(new HttpClientPrincipal(realm, name, value));
				break;
		}
	}

	public void addProxyAuthentication(int type, String realm, String name, String value)
	{
		setAuthorizationProxyChanged(true);
		switch (type)
		{
			case BASIC:
				basicProxyAuthorization.get(Thread.currentThread().getId()).addElement(new HttpClientPrincipal(realm, name, value));
				break;
			case DIGEST :
				digestProxyAuthorization.get(Thread.currentThread().getId()).addElement(new HttpClientPrincipal(realm, name, value));
				break;
			case NTLM :
				NTLMProxyAuthorization.get(Thread.currentThread().getId()).addElement(new HttpClientPrincipal(realm, name, value));
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
				this.isMultipart.put(Thread.currentThread().getId(), true);
				value = multipartTemplate.get(Thread.currentThread().getId()).contentType;
			}
		}
		headersToSend.get(Thread.currentThread().getId()).put(name, value);
	}

	@SuppressWarnings("unchecked")
	public void addVariable(String name, String value)
	{
		variablesToSend.get(Thread.currentThread().getId()).put(name, value);
	}

	@SuppressWarnings("unchecked")
	public void addBytes(byte[] value)
	{
		contentToSend.get(Thread.currentThread().getId()).addElement(value);
	}

	@SuppressWarnings("unchecked")
	public void addString(String value)
	{
		contentToSend.get(Thread.currentThread().getId()).addElement(value);
	}

	@SuppressWarnings("unchecked")
	public void addFile(String fileName)
	{
		fileName = SpecificImplementation.HttpClient.beforeAddFile(fileName);
		contentToSend.get(Thread.currentThread().getId()).addElement(new File(fileName));
	}

	@SuppressWarnings("unchecked")
	public void addFile(String fileName, String varName)
	{
		fileName = SpecificImplementation.HttpClient.beforeAddFile(fileName);
		contentToSend.get(Thread.currentThread().getId()).addElement(new FormFile(fileName, varName));
	}

	@SuppressWarnings("unchecked")
	public void addStringWriter(StringWriter writer, StringBuffer encoding)
	{
		contentToSend.get(Thread.currentThread().getId()).addElement(new Object[]{writer, encoding});
	}

	public boolean getHostChanged() {
		return hostChanged.get(Thread.currentThread().getId());
	}

	public void setHostChanged(boolean hostChanged) {
		this.hostChanged.put(Thread.currentThread().getId(), hostChanged);
	}

	public Vector getContentToSend() {
		return  this.contentToSend.get(Thread.currentThread().getId());
	}

	public void setContentToSend(Vector contentToSend) {
		putOrRemoveIfNull(this.contentToSend,contentToSend);
	}

	protected void resetState()
	{
		this.contentToSend.get(Thread.currentThread().getId()).clear();
		this.variablesToSend.get(Thread.currentThread().getId()).clear();
		this.contentToSend.get(Thread.currentThread().getId()).removeAllElements();
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
		putOrRemoveIfNull(this.variablesToSend,variablesToSend);
	}

	public Hashtable getVariablesToSend() {
		return this.variablesToSend.get(Thread.currentThread().getId());
	}

	public boolean isMultipart() {
		return this.isMultipart.get(Thread.currentThread().getId());
	}

	public void setMultipart(boolean multipart) {
		this.isMultipart.put(Thread.currentThread().getId(), multipart);
	}

	public String getPrevURLhost() {
		return this.prevURLhost.get(Thread.currentThread().getId());
	}

	public void setPrevURLhost(String prevURLhost) {
		putOrRemoveIfNull(this.prevURLhost,prevURLhost);
	}

	public String getPrevURLbaseURL() {
		return this.prevURLbaseURL.get(Thread.currentThread().getId());
	}

	public void setPrevURLbaseURL(String prevURLbaseURL) {
		putOrRemoveIfNull(this.prevURLbaseURL,prevURLbaseURL);
	}

	public int getPrevURLport() {
		return this.prevURLport.get(Thread.currentThread().getId());
	}

	public void setPrevURLport(int prevURLport) {
		putOrRemoveIfNull(this.prevURLport,prevURLport);
	}

	public int getPrevURLsecure() {
		return this.prevURLsecure.get(Thread.currentThread().getId());
	}

	public void setPrevURLsecure(int prevURLsecure) {
		putOrRemoveIfNull(this.prevURLsecure,prevURLsecure);
	}

	public boolean getIsURL() {
		return this.isURL.get(Thread.currentThread().getId());
	}

	public void setIsURL(boolean isURL) {
		putOrRemoveIfNull(this.isURL,isURL);
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
		return this.authorizationChanged.get(Thread.currentThread().getId());
	}

	public void setAuthorizationChanged(boolean authorizationChanged) {
		putOrRemoveIfNull(this.authorizationChanged,authorizationChanged);
	}

	public boolean getAuthorizationProxyChanged() {
		return this.authorizationProxyChanged.get(Thread.currentThread().getId());
	}

	public void setAuthorizationProxyChanged(boolean authorizationProxyChanged) {
		putOrRemoveIfNull(this.authorizationProxyChanged,authorizationProxyChanged);
	}

	protected NVPair[] hashtableToNVPair(Hashtable hashtable)
	{
		NVPair[] ret = new NVPair[hashtable.size()];
		int idx = 0;

		for (Enumeration en = hashtable.keys(); en.hasMoreElements();)
		{
			Object key = en.nextElement();
			ret[idx++] = new NVPair((String) key, (String) hashtable.get(key));
		}

		return ret;
	}

	public boolean getProxyInfoChanged() {
		return proxyInfoChanged.get(Thread.currentThread().getId());
	}

	public void setProxyInfoChanged(boolean proxyInfoChanged) {
		this.proxyInfoChanged.put(Thread.currentThread().getId(), proxyInfoChanged);
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
			return "Content-Disposition: form-data; name=\""+ name + "\"; filename=\""+ fileName + "\"\r\n" + "Content-Type: " + mimeType + "\r\n\r\n";
		}
		String getFormDataTemplate(String varName, String value){
			return "\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + varName + "\";\r\n\r\n" + value;
		}
	}

}
