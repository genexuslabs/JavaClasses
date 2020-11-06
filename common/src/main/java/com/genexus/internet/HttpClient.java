
package com.genexus.internet;

import HTTPClient.*;
import java.io.*;
import java.util.Vector;
import java.net.URL;
import java.util.Hashtable;
import java.util.Enumeration;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.CommonUtil;

/**
* Esta clase la usa un cliente que quiere hacer un get/post y recibir el resultado
*/
public class HttpClient implements IHttpClient
{
	private final int BASIC  = 0;
	private final int DIGEST = 1;
	private final int NTLM   = 2;

	public final int ERROR_IO = 1;

	private String host;
	private String WSDLURL;
	private String baseURL;
	private int port = 80;
	private int secure = 0;
	private String prevURLhost;
	private String prevURLbaseURL;
	private int prevURLport;
	private int prevURLsecure;	
	private boolean isURL = false;
	private int timeout;
	private int errCode;
	private String errDescription = "";
	private String proxyHost = "";// = HTTPConnection.getDefaultProxyHost() == null ? "" : HTTPConnection.getDefaultProxyHost();
	private int proxyPort = 80;// = HTTPConnection.getDefaultProxyPort() == 0 ? 80 : HTTPConnection.getDefaultProxyPort();
	private boolean tcpNoDelay = false;
	private boolean includeCookies = true;

	private HTTPConnection con = null;
	private HTTPResponse res;

	private Hashtable<String, String> headersToSend = new Hashtable<>();
	private Hashtable variablesToSend = new Hashtable();

	private Vector<HttpClientPrincipal> basicAuthorization = new Vector<>();
	private Vector<HttpClientPrincipal> digestAuthorization = new Vector<>();
	private Vector<HttpClientPrincipal> NTLMAuthorization = new Vector<>();
	
	private Vector<HttpClientPrincipal> basicProxyAuthorization = new Vector<>();
	private Vector<HttpClientPrincipal> digestProxyAuthorization = new Vector<>();
	private Vector<HttpClientPrincipal> NTLMProxyAuthorization = new Vector<>();

	private MultipartTemplate multipartTemplate =new MultipartTemplate();
	private boolean isMultipart = false;

	private Vector contentToSend = new Vector<>();
    private boolean hostChanged = true; // Indica si el próximo request debe ser realizado en una nueva HTTPConnection (si cambio el host)
    private boolean authorizationChanged = false; // Indica si se agregó alguna autorización
	
	private boolean authorizationProxyChanged = false; // Indica si se agregó alguna autorización

	static
	{
		HTTPConnection.setDefaultAllowUserInteraction(false);
        
        if(CommonUtil.isWindows())
        {
            String os = System.getProperty("os.name", "").trim().toUpperCase();
            if(os.endsWith("95") || os.endsWith("98") || os.endsWith("ME"))HTTPConnection.setPipelining(false);
        }             
	}
	public static boolean issuedExternalHttpClientWarning = false;
	public boolean usingExternalHttpClient = false;
	
	public HttpClient()
	{
		SpecificImplementation.HttpClient.initializeHttpClient(this);
	}

	private void resetState()
	{
		headersToSend.clear();
		variablesToSend.clear();
		contentToSend.removeAllElements();
		multipartTemplate = new MultipartTemplate();
		isMultipart = false;
	}

	private void resetErrors()
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

	public String getErrDescription()
	{
		return errDescription;
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

	public void setUrl(String url)
	{
		setURL(url);
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

	public void addAuthentication(int type, String realm, String name, String value)
	{
        authorizationChanged = true;
		switch (type)
		{
			case BASIC:
				basicAuthorization.addElement(new HttpClientPrincipal(realm, name, value));
				break;
			case DIGEST :
				digestAuthorization.addElement(new HttpClientPrincipal(realm, name, value));
				break;
			case NTLM :
				NTLMAuthorization.addElement(new HttpClientPrincipal(realm, name, value));
				break;
		}
	}
	
	public void addProxyAuthentication(int type, String realm, String name, String value)
	{
        authorizationProxyChanged = true;
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
	public void addCertificate(String fileName)
	{
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

	public void execute(String method, String url)
	{
		resetErrors();
		
		URI uri;
		try
		{
		    uri = new URI(url);
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
		    url = relativeUri.toString();
		}
		catch (ParseException e)
		{
			//No es una URL
		}		

		try
		{
            if(hostChanged) // Si el host cambio, creo una nueva instancia de HTTPConnection
            {
				if (con != null)
				{
					con.stop();
				}
            		if (secure == 1 && port == 80)
            		{
            			port = 443;
            		}
                con = new HTTPConnection(secure == 0?"http":"https", host, port);
                if (secure != 0)
                    con.setSSLConnection(SSLManager.getSSLConnection());
				
				con.setTcpNoDelay(tcpNoDelay);
				con.setIncludeCookies(includeCookies);
            }

			con.setTimeout(timeout * 1000); // Este puede variar sin cambiar de instancia
			if(proxyInfoChanged)
			{ 
				con.setCurrentProxy(proxyHost, proxyPort); // Este puede variar sin cambiar de instancia
			}

            if(hostChanged || authorizationChanged)
            { // Si el host cambio o si se agrego alguna credencial
                for (Enumeration en = basicAuthorization.elements(); en.hasMoreElements(); )
                {
                    HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
                    con.addBasicAuthorization(p.realm, p.user, p.password);
                }

                for (Enumeration en = digestAuthorization.elements(); en.hasMoreElements(); )
                {
                    HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
                    con.addDigestAuthorization(p.realm, p.user, p.password);
                }

                for (Enumeration en = NTLMAuthorization.elements(); en.hasMoreElements(); )
                {
                    HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
                    //p.addBasicAuthorization(p.realm, p.user, p.password);
                }
            }
            
            hostChanged = authorizationChanged = false; // Desmarco las flags
			
            if(proxyInfoChanged || authorizationProxyChanged)
            { // Si el poxyHost cambio o si se agrego alguna credencial para el proxy
                for (Enumeration en = basicProxyAuthorization.elements(); en.hasMoreElements(); )
                {
                    HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
                    con.addBasicProxyAuthorization(p.realm, p.user, p.password);
                }

                for (Enumeration en = digestProxyAuthorization.elements(); en.hasMoreElements(); )
                {
                    HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
                    con.addDigestProxyAuthorization(p.realm, p.user, p.password);
                }

                for (Enumeration en = NTLMProxyAuthorization.elements(); en.hasMoreElements(); )
                {
                    HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
                    //p.addBasicAuthorization(p.realm, p.user, p.password);
                }
            }
            
            proxyInfoChanged = authorizationProxyChanged = false; // Desmarco las flags			

			if  (!url.startsWith("/"))
				url = baseURL + url;

			if	(method.equalsIgnoreCase("GET"))
			{
				if (contentToSend.size() > 0)
					res = con.Get(url, "", hashtableToNVPair(headersToSend), getData());
				else
					res = con.Get(url, "", hashtableToNVPair(headersToSend));
			}
			else if (method.equalsIgnoreCase("POST"))
			{
				if	(!isMultipart && variablesToSend.size() > 0)
				{
					res = con.Post(url, hashtableToNVPair(variablesToSend), hashtableToNVPair(headersToSend));
				}
				else
				{
					res = con.Post(url, getData(), hashtableToNVPair(headersToSend));
				}
			}
			else if (method.equalsIgnoreCase("PUT"))
			{
				res = con.Put(url, getData(), hashtableToNVPair(headersToSend));
			}
			else if (method.equalsIgnoreCase("DELETE"))
			{
				if (variablesToSend.size() > 0 || contentToSend.size() > 0)
				{
					res = con.Delete(url, getData(), hashtableToNVPair(headersToSend));
				}
				else
				{				
					res = con.Delete(url, hashtableToNVPair(headersToSend));
				}
			}			
			else 
			{
				res = con.ExtensionMethod(method, url, getData(), hashtableToNVPair(headersToSend));
			}
		}
		catch (ProtocolNotSuppException e)
		{
			System.err.println(e);
		}
		catch (IOException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
		catch (ModuleException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
		finally
		{
			getStatusCode();
			if (isURL)
			{
				this.setHost(prevURLhost);
				this.setBaseURL(prevURLbaseURL);
				this.setPort(prevURLport);
		 		this.setSecure(prevURLsecure);
		 		this.isURL = false;
			}						
		}

		resetState();
	}

	public int getStatusCode()
	{
		if	(res == null)
		{
			return 0;
		}

		try
		{
			return res.getStatusCode();
		}
		catch (IOException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
			res = null;
		}
		catch (ModuleException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
		
		return 0;
	}

	public String getReasonLine()
	{
		if	(res == null)
		{
			return "";
		}

		try
		{
			return res.getReasonLine();
		}
		catch (IOException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
			return "";
		}
		catch (ModuleException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
			return "";
		}
	}

	public void getHeader(String name, long[] value) 
	{
		if	(res == null)
		{
			return;
		}

		try
		{
			value[0] = res.getHeaderAsInt(name);
		}
		catch (IOException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
		catch (ModuleException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
	}
	
	public String getHeader(String name)
	{
		if	(res == null)
		{
			return "";
		}

		try
		{
			return res.getHeader(name);
		}
		catch (IOException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
		catch (ModuleException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}

		return "";
	}


	public void getHeader(String name, String[] value)
	{
		if	(res == null)
		{
			return;
		}

		try
		{
			value[0] = res.getHeader(name);
		}
		catch (IOException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
		catch (ModuleException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
	}

	public void getHeader(String name, java.util.Date[] value)
	{
		if	(res == null)
		{
			return;
		}

		try
		{
			value[0] = res.getHeaderAsDate(name);
		}
		catch (IOException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
		catch (ModuleException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
	}
	
	public void getHeader(String name, double[] value)
	{
		if	(res == null)
		{
			return;
		}

		try
		{
			value[0] = CommonUtil.val(res.getHeader(name));
		}
		catch (IOException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
		catch (ModuleException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
	}

	public InputStream getInputStream() throws IOException
	{
		try
		{
			return res.getInputStream();
		}
		catch (ModuleException e)
		{
			throw new IOException("Module/ " + e.getMessage());
		}
		catch (Exception e)
		{
			throw new IOException("Module/ " + e.getMessage());
		}

	}

	public String getString()
	{	
		if	(res == null)
		{
			return "";
		}

		try
		{
			return res.getText();
			//return new String(PrivateUtilities.readToByteArray(res.getInputStream()));
		}
		catch (ModuleException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
		catch (IOException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
		catch (Exception e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}		

		return "";
	}

	public void toFile(String fileName)
	{
		if	(res == null)
		{
			return;
		}

		try
		{
			CommonUtil.InputStreamToFile(res.getInputStream(), fileName);
		}
		catch (IOException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
		catch (ModuleException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
	}

	private NVPair[] getHeaders()
	{
		NVPair[] ret = new NVPair[headersToSend.size()];
		int idx = 0;

		for (Enumeration en = headersToSend.keys(); en.hasMoreElements();)
		{
			Object key = en.nextElement();
			ret[idx++] = new NVPair((String) key, (String) headersToSend.get(key));
		}

		return ret;
	}

	private NVPair[] hashtableToNVPair(Hashtable hashtable)
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

	@SuppressWarnings("unchecked")
	private byte[] getData()
	{
		byte[] out = new byte[0];

        for (Object key: variablesToSend.keySet())
        {
	   		String value = multipartTemplate.getFormDataTemplate((String)key, (String)variablesToSend.get(key));
			contentToSend.add(0, value); //Variables al principio
        }

		for (int idx = 0; idx < contentToSend.size(); idx++)
		{
			Object curr = contentToSend.elementAt(idx);
			
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
					errCode = ERROR_IO;
					errDescription = e.getMessage();
				}
				catch (IOException e)
				{
					errCode = ERROR_IO;
					errDescription = e.getMessage();
				}
			}
		}
		out = endMultipartBoundary(out);
		return out;
	}
	private byte[] startMultipartFile(byte[] in, String name, String fileName)
	{
		if (isMultipart && CommonUtil.fileExists(fileName)==1)
		{
			if (name==null || name=="")
			{
				name = CommonUtil.getFileName(fileName);
			}
			byte[] out = addToArray(in, multipartTemplate.boundarybytes);
			String mimeType = SpecificImplementation.Application.getContentType(fileName);
			String header = multipartTemplate.getHeaderTemplate(name, fileName, mimeType);
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
		if (isMultipart){
			return addToArray(in, multipartTemplate.endBoundaryBytes);
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

	public static InputStream getInputStream(String stringURL) throws IOException
	{ // for this request always create a new HTTPConnection
		try
		{
		    URI url = new URI(stringURL);
			return new HTTPConnection(url.getScheme(), url.getHost(), url.getPort()).Get(url.getPathAndQuery()).getInputStream();
		}
		catch (ParseException e)
		{
			throw new IOException("Malformed URL " + e.getMessage());
		}
		catch (ModuleException e)
		{
			throw new IOException("ModuleException " + e.getMessage());
		}
	}
	
	public void cleanup()
	{
		if (con != null)
		{
			con.stop();
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

}