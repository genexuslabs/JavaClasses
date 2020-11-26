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

public abstract class GXHttpClient implements IHttpClient{
	protected final int BASIC  = 0;
	protected final int DIGEST = 1;
	protected final int NTLM   = 2;

	public final int ERROR_IO = 1;

//	private String host;
//	private String baseURL;
//	private int secure = 0;
//	private int timeout;
//	private int port = 80;
//	private boolean hostChanged = true; // Indica si el próximo request debe ser realizado en una nueva HTTPConnection (si cambio el host)
//	private Hashtable<String, String> headersToSend = new Hashtable<>();
//	private String WSDLURL;
//	private int errCode;
//	private String errDescription = "";
//	private String proxyHost = "";// = HTTPConnection.getDefaultProxyHost() == null ? "" : HTTPConnection.getDefaultProxyHost();
//	private int proxyPort = 80;// = HTTPConnection.getDefaultProxyPort() == 0 ? 80 : HTTPConnection.getDefaultProxyPort();
//	private boolean includeCookies = true;
//	private boolean tcpNoDelay = false;
//	private Hashtable variablesToSend = new Hashtable();
//	private Vector contentToSend = new Vector<>();
//	private boolean isMultipart = false;
//	private MultipartTemplate multipartTemplate =new MultipartTemplate();
//	private String prevURLhost;
//	private String prevURLbaseURL;
//	private int prevURLport;
//	private int prevURLsecure;
//	private boolean isURL = false;
//	private boolean authorizationChanged = false; // Indica si se agregó alguna autorización
//	private boolean authorizationProxyChanged = false; // Indica si se agregó alguna autorización
//
//	private Vector<HttpClientPrincipal> basicAuthorization = new Vector<HttpClientPrincipal>();
//	private Vector<HttpClientPrincipal> digestAuthorization = new Vector<>();
//	private Vector<HttpClientPrincipal> NTLMAuthorization = new Vector<>();
//
//	private Vector<HttpClientPrincipal> basicProxyAuthorization = new Vector<>();
//	private Vector<HttpClientPrincipal> digestProxyAuthorization = new Vector<>();
//	private Vector<HttpClientPrincipal> NTLMProxyAuthorization = new Vector<>();
//
//	public static boolean issuedExternalHttpClientWarning = false;
//	public boolean usingExternalHttpClient = false;

//	protected void resetErrors()
//	{
//		errCode = 0;
//		errDescription = "";
//	}

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

	public abstract short getErrCode();

	public abstract void setErrCode(int errCode);

	public abstract String getErrDescription();

	public abstract void setErrDescription(String errDescription);

//	boolean proxyInfoChanged = false;
	public abstract void setProxyServerHost(String host);

	public abstract String getProxyServerHost();

	public abstract void setProxyServerPort(long port);

	public abstract short getProxyServerPort();

	public abstract void setIncludeCookies(boolean value);

	public abstract boolean getIncludeCookies();

	public abstract void setURL(String stringURL);

	public abstract void setHost(String host);

	public abstract String getHost();

	public abstract void setWSDLURL(String WSDLURL);

	public abstract void setBaseURL(String baseURL);

	public abstract String getWSDLURL();

	public abstract String getBaseURL();

	public abstract void setPort(int port);

	public abstract int getPort();

	public abstract byte getSecure();

	public abstract void setSecure(int secure);

	public abstract void setTimeout(int timeout);

	public abstract int getTimeout();

	public abstract void setTcpNoDelay(boolean tcpNoDelay);

	public abstract boolean getTcpNoDelay();

	public abstract Hashtable<String, String> getheadersToSend();

//	public abstract MultipartTemplate getMultipartTemplate();
//
//	public abstract void setMultipartTemplate(MultipartTemplate multipartTemplate);

	public abstract boolean getIsMultipart();

	public abstract void setIsMultipart(boolean isMultipart);

	public abstract Vector<HttpClientPrincipal> getBasicAuthorization();

	public abstract Vector<HttpClientPrincipal> getDigestAuthorization();

	public abstract Vector<HttpClientPrincipal> getNTLMAuthorization();

	public abstract Vector<HttpClientPrincipal> getBasicProxyAuthorization();

	public abstract Vector<HttpClientPrincipal> getDigestProxyAuthorization();

	public abstract Vector<HttpClientPrincipal> getNTLMProxyAuthorization();

	public void addCertificate(String fileName)
	{
	}

	public abstract void addAuthentication(int type, String realm, String name, String value);

	public abstract void addProxyAuthentication(int type, String realm, String name, String value);

//	protected String contentEncoding = null;

	public abstract void addHeader(String name, String value);

	public abstract void addVariable(String name, String value);

	public abstract void addBytes(byte[] value);

	public abstract void addString(String value);

	public abstract void addFile(String fileName);

	public abstract void addFile(String fileName, String varName);

	public abstract void addStringWriter(StringWriter writer, StringBuffer encoding);

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

	public abstract boolean getHostChanged();

	public abstract void setHostChanged(boolean hostChanged);

	public abstract Vector getContentToSend();

	public abstract void setContentToSend(Vector contentToSend);

	public abstract void setVariablesToSend(Hashtable variablesToSend);

	public abstract Hashtable getVariablesToSend();

	public abstract boolean isMultipart();

	public abstract void setMultipart(boolean multipart);

	public abstract String getPrevURLhost();

	public abstract void setPrevURLhost(String prevURLhost);

	public abstract String getPrevURLbaseURL();

	public abstract void setPrevURLbaseURL(String prevURLbaseURL);

	public abstract int getPrevURLport();

	public abstract void setPrevURLport(int prevURLport);

	public abstract int getPrevURLsecure();

	public abstract void setPrevURLsecure(int prevURLsecure);

	public abstract boolean getIsURL();

	public abstract void setIsURL(boolean isURL);

	public abstract boolean getAuthorizationChanged();

	public abstract void setAuthorizationChanged(boolean authorizationChanged);

	public abstract boolean getAuthorizationProxyChanged();

	public abstract void setAuthorizationProxyChanged(boolean authorizationProxyChanged);

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

}
