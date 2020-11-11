
package com.genexus.internet;

import HTTPClient.*;
import java.io.*;
import java.util.Vector;
import java.net.URL;
import java.util.Hashtable;
import java.util.Enumeration;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.CommonUtil;
import com.genexus.internet.IHttpClient;

/**
* Esta clase la usa un cliente que quiere hacer un get/post y recibir el resultado
*/
public class HttpClient
{
	private final int BASIC  = 0;	// AGREGADO EN GXHttpClient
	private final int DIGEST = 1;	// AGREGADO EN GXHttpClient
	private final int NTLM   = 2;	// AGREGADO EN GXHttpClient

	public final int ERROR_IO = 1;

	private String host;	// AGREGADO EN GXHttpClient
	private String WSDLURL;		// AGREGADO EN GXHttpClient
	private String baseURL;		// AGREGADO EN GXHttpClient
	private int port = 80;		// AGREGADO EN GXHttpClient
	private int secure = 0;		// AGREGADO EN GXHttpClient
	private String prevURLhost;		// AGREGADO EN HttpClientManual
	private String prevURLbaseURL;	// AGREGADO EN HttpClientManual
	private int prevURLport;	// AGREGADO EN HttpClientManual
	private int prevURLsecure;	// AGREGADO EN HttpClientManual
	private boolean isURL = false;	// AGREGADO EN HttpClientManual
	private int timeout;		// AGREGADO EN GXHttpClient
	private int errCode;	// AGREGADO EN GXHttpClient
	private String errDescription = "";		// AGREGADO EN GXHttpClient
	private String proxyHost = "";// = HTTPConnection.getDefaultProxyHost() == null ? "" : HTTPConnection.getDefaultProxyHost();   // AGREGADO EN GXHttpClient
	private int proxyPort = 80;// = HTTPConnection.getDefaultProxyPort() == 0 ? 80 : HTTPConnection.getDefaultProxyPort();		// AGREGADO EN GXHttpClient
	private boolean tcpNoDelay = false;		// AGREGADO EN GXHttpClient
	private boolean includeCookies = true;	// AGREGADO EN GXHttpClient

	private HTTPConnection con = null;
	private HTTPResponse res;	// AGREGADO EN HttpClientManual

	private Hashtable<String, String> headersToSend = new Hashtable<>();
	private Hashtable variablesToSend = new Hashtable();	//AGREGADO EN GXHttpClient

	private Vector<HttpClientPrincipal> basicAuthorization = new Vector<>();	// AGREGADO EN HttpClientManual
	private Vector<HttpClientPrincipal> digestAuthorization = new Vector<>();	// AGREGADO EN HttpClientManual
	private Vector<HttpClientPrincipal> NTLMAuthorization = new Vector<>();	// AGREGADO EN HttpClientManual
	
	private Vector<HttpClientPrincipal> basicProxyAuthorization = new Vector<>();	// AGREGADO EN HttpClientManual
	private Vector<HttpClientPrincipal> digestProxyAuthorization = new Vector<>();	// AGREGADO EN HttpClientManual
	private Vector<HttpClientPrincipal> NTLMProxyAuthorization = new Vector<>();	// AGREGADO EN HttpClientManual

	private MultipartTemplate multipartTemplate =new MultipartTemplate();	// AGREGADO EN HttpClientManual
	private boolean isMultipart = false;	// AGREGADO EN HttpClientManual

	private Vector contentToSend = new Vector<>();		// AGREGADO EN GXHttpClient
    private boolean hostChanged = true; // Indica si el próximo request debe ser realizado en una nueva HTTPConnection (si cambio el host)  // AGREGADO EN GXHttpClient
    private boolean authorizationChanged = false; // Indica si se agregó alguna autorización		// AGREGADO EN HttpClientManual
	
	private boolean authorizationProxyChanged = false; // Indica si se agregó alguna autorización	// AGREGADO EN HttpClientManual

	static
	{
		HTTPConnection.setDefaultAllowUserInteraction(false);
        
        if(CommonUtil.isWindows())
        {
            String os = System.getProperty("os.name", "").trim().toUpperCase();
            if(os.endsWith("95") || os.endsWith("98") || os.endsWith("ME"))HTTPConnection.setPipelining(false);
        }             
	}
	public static boolean issuedExternalHttpClientWarning = false;	// AGREGADO EN GXHttpClient
	public boolean usingExternalHttpClient = false;		// AGREGADO EN GXHttpClient

	private IHttpClient session;
	
	public HttpClient()
	{
		session = SpecificImplementation.HttpClient.initHttpClientImpl();	// Se crea la instancia dependiendo si existe o no la implementacion de las librerias de Java
	}

	private void resetState()
	{
		headersToSend.clear();
		variablesToSend.clear();
		contentToSend.removeAllElements();
		multipartTemplate = new MultipartTemplate();
		isMultipart = false;
		System.out.println();
	}

	private void resetErrors()
	{
		errCode = 0;
		errDescription = "";
	}

	public byte getBasic()
	{
		return session.getBasic();
	}

	public byte getDigest()
	{
		return session.getDigest();
	}

	public byte getNtlm()
	{
		return session.getNtlm();
	}

	public short getErrCode()
	{
		return session.getErrCode();
	}

	public String getErrDescription()
	{
		return session.getErrDescription();
	}

	public void setProxyServerHost(String host)	
	{
		session.setProxyServerHost(host);
	}

	public String getProxyServerHost()
	{
		return session.getProxyServerHost();
	}

	public void setProxyServerPort(long port)
	{
		session.setProxyServerPort(port);
	}

	public short getProxyServerPort()
	{
		return session.getProxyServerPort();
	}
	public void setIncludeCookies(boolean value)
	{
		session.setIncludeCookies(value);
	}

	public boolean getIncludeCookies()
	{
		return session.getIncludeCookies();
	}

	public void setUrl(String url)
	{
		setURL(url);
	}
	
	public void setURL(String stringURL)
	{
		session.setURL(stringURL);
	}
	
	public void setHost(String host)
	{
    	session.setHost(host);
	}

	public String getHost()
	{
		return session.getHost();
	}

	public void setWSDLURL(String WSDLURL)
	{
		session.setWSDLURL(WSDLURL);
	}

	public void setBaseURL(String baseURL)
	{
		session.setBaseURL(baseURL);
	}

	public String getWSDLURL()
	{
		return session.getWSDLURL();
	}

	public String getBaseURL()
	{
		return session.getBaseURL();
	}


	public void setPort(int port)
	{
        session.setPort(port);
	}

	public int getPort()
	{
		return session.getPort();
	}

	public byte getSecure()
	{
		return session.getSecure();
	}

	public void setSecure(int secure)
	{
        session.setSecure(secure);
	}

	public void setTimeout(int timeout)
	{
		session.setTimeout(timeout);
	}

	public int getTimeout()
	{
		return session.getTimeout();
	}
	
	public void setTcpNoDelay(boolean tcpNoDelay)
	{
		session.setTcpNoDelay(tcpNoDelay);
	}

	public void addAuthentication(int type, String realm, String name, String value)
	{
        session.addAuthentication(type,realm,name,value);
	}
	
	public void addProxyAuthentication(int type, String realm, String name, String value)
	{
       session.addProxyAuthentication(type,realm,name,value);
	}

	public void addCertificate(String fileName)
	{
		session.addCertificate(fileName);
	}
	
	protected String contentEncoding = null;	// AGREGADO EN GXHttpClient

	public void addHeader(String name, String value)
	{
		session.addHeader(name,value);
	}

	@SuppressWarnings("unchecked")
	public void addVariable(String name, String value)
	{
		session.addVariable(name,value);
	}

	@SuppressWarnings("unchecked")
	public void addBytes(byte[] value)
	{
		session.addBytes(value);
	}

	@SuppressWarnings("unchecked")
	public void addString(String value)
	{
		session.addString(value);
	}

	@SuppressWarnings("unchecked")
	public void addFile(String fileName)
	{
		session.addFile(fileName);
	}

	@SuppressWarnings("unchecked")
	public void addFile(String fileName, String varName)
	{
		session.addFile(fileName,varName);
	}

	@SuppressWarnings("unchecked")
	public void addStringWriter(StringWriter writer, StringBuffer encoding)
	{
		session.addStringWriter(writer,encoding);
	}

	public void execute(String method, String url)
	{
		session.execute(method,url);
	}

	public int getStatusCode()
	{
		return session.getStatusCode();
	}

	public String getReasonLine()
	{
		return session.getReasonLine();
	}

	public void getHeader(String name, long[] value) 
	{
		session.getHeader(name,value);
	}
	
	public String getHeader(String name)
	{
		return session.getHeader(name);
	}


	public void getHeader(String name, String[] value)
	{
		session.getHeader(name,value);
	}

	public void getHeader(String name, java.util.Date[] value)
	{
		session.getHeader(name,value);
	}
	
	public void getHeader(String name, double[] value)
	{
		session.getHeader(name,value);
	}

	public InputStream getInputStream() throws IOException
	{
		return session.getInputStream();
	}

	public String getString()
	{	
		return session.getString();
	}

	public void toFile(String fileName)
	{
		session.toFile(fileName);
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
		session.cleanup();
	}

	class HttpClientPrincipal		// AGREGADO EN HttpClientManual
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
	class FormFile{		// AGREGADO EN GXHttpClient
		String file;
		String name;
		FormFile(String file, String name){
			this.file = file;
			this.name = name;
		}
	}
	class MultipartTemplate		// AGREGADO EN HttpClientManual
	{
		public String boundary;
		public String formdataTemplate;
		public byte[] boundarybytes;
		public byte[] endBoundaryBytes;
		public String contentType;

		public MultipartTemplate()		// AGREGADO EN HttpClientManual
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