package com.genexus.internet;

import HTTPClient.*;
import com.genexus.CommonUtil;
import com.genexus.common.interfaces.SpecificImplementation;

import java.io.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class HttpClientManual extends GXHttpClient {

	private Vector<HttpClientPrincipal> basicAuthorization = new Vector<HttpClientPrincipal>();
	private Vector<HttpClientPrincipal> digestAuthorization = new Vector<>();
	private Vector<HttpClientPrincipal> NTLMAuthorization = new Vector<>();

	private Vector<HttpClientPrincipal> basicProxyAuthorization = new Vector<>();
	private Vector<HttpClientPrincipal> digestProxyAuthorization = new Vector<>();
	private Vector<HttpClientPrincipal> NTLMProxyAuthorization = new Vector<>();
	private boolean authorizationChanged = false; // Indica si se agregó alguna autorización
	private boolean authorizationProxyChanged = false; // Indica si se agregó alguna autorización
	private boolean isMultipart = false;
	private MultipartTemplate multipartTemplate =new MultipartTemplate();
	private Hashtable<String, String> headersToSend = new Hashtable<>();
	private String prevURLhost;		// AGREGADO EN HttpClientManual
	private String prevURLbaseURL;	// AGREGADO EN HttpClientManual
	private int prevURLport;	// AGREGADO EN HttpClientManual
	private int prevURLsecure;	// AGREGADO EN HttpClientManual
	private boolean isURL = false;	// AGREGADO EN HttpClientManual
	private HTTPConnection con = null;
	private HTTPResponse res;

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

	@SuppressWarnings("unchecked")
	private byte[] getData()
	{
		byte[] out = new byte[0];

		for (Object key: getVariablesToSend().keySet())
		{
			String value = multipartTemplate.getFormDataTemplate((String)key, (String)getVariablesToSend().get(key));
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
				if (curr instanceof HttpClient.FormFile)
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


	@Override
	public void addAuthentication(int type, String realm, String name, String value)
	{
		authorizationChanged = true;
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

	@Override
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

	protected String contentEncoding = null;

	@Override
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

	private void resetState()
	{
		headersToSend.clear();
		getVariablesToSend().clear();
		getContentToSend().removeAllElements();
		multipartTemplate = new MultipartTemplate();
		isMultipart = false;
		System.out.println();
	}

	@Override
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
			if(getHostChanged()) // Si el host cambio, creo una nueva instancia de HTTPConnection
			{
				if (con != null)
				{
					con.stop();
				}
				if (getSecure() == 1 && getPort() == 80)
				{
					setPort(443);
				}
				con = new HTTPConnection(getSecure() == 0?"http":"https", getHost(), getPort());
				if (getSecure() != 0)
					con.setSSLConnection(SSLManager.getSSLConnection());

				con.setTcpNoDelay(getTcpNoDelay());
				con.setIncludeCookies(getIncludeCookies());
			}

			con.setTimeout(getTimeout() * 1000); // Este puede variar sin cambiar de instancia
			if(proxyInfoChanged)
			{
				con.setCurrentProxy(getProxyServerHost(), getProxyServerPort()); // Este puede variar sin cambiar de instancia
			}

			if(getHostChanged() || authorizationChanged)
			{ // Si el host cambio o si se agrego alguna credencial
				for (Enumeration en = basicAuthorization.elements(); en.hasMoreElements(); )
				{
					HttpClient.HttpClientPrincipal p = (HttpClient.HttpClientPrincipal) en.nextElement();
					con.addBasicAuthorization(p.realm, p.user, p.password);
				}

				for (Enumeration en = digestAuthorization.elements(); en.hasMoreElements(); )
				{
					HttpClient.HttpClientPrincipal p = (HttpClient.HttpClientPrincipal) en.nextElement();
					con.addDigestAuthorization(p.realm, p.user, p.password);
				}

				for (Enumeration en = NTLMAuthorization.elements(); en.hasMoreElements(); )
				{
					HttpClient.HttpClientPrincipal p = (HttpClient.HttpClientPrincipal) en.nextElement();
					//p.addBasicAuthorization(p.realm, p.user, p.password);
				}
			}

			setHostChanged(false);
			authorizationChanged = false; // Desmarco las flags

			if(proxyInfoChanged || authorizationProxyChanged)
			{ // Si el poxyHost cambio o si se agrego alguna credencial para el proxy
				for (Enumeration en = basicProxyAuthorization.elements(); en.hasMoreElements(); )
				{
					HttpClient.HttpClientPrincipal p = (HttpClient.HttpClientPrincipal) en.nextElement();
					con.addBasicProxyAuthorization(p.realm, p.user, p.password);
				}

				for (Enumeration en = digestProxyAuthorization.elements(); en.hasMoreElements(); )
				{
					HttpClient.HttpClientPrincipal p = (HttpClient.HttpClientPrincipal) en.nextElement();
					con.addDigestProxyAuthorization(p.realm, p.user, p.password);
				}

				for (Enumeration en = NTLMProxyAuthorization.elements(); en.hasMoreElements(); )
				{
					HttpClient.HttpClientPrincipal p = (HttpClient.HttpClientPrincipal) en.nextElement();
					//p.addBasicAuthorization(p.realm, p.user, p.password);
				}
			}

			proxyInfoChanged = authorizationProxyChanged = false; // Desmarco las flags

			if  (!url.startsWith("/"))
				url = getBaseURL().trim() + url;

			if	(method.equalsIgnoreCase("GET"))
			{
				if (getContentToSend().size() > 0)
					res = con.Get(url, "", hashtableToNVPair(headersToSend), getData());
				else
					res = con.Get(url, "", hashtableToNVPair(headersToSend));
			}
			else if (method.equalsIgnoreCase("POST"))
			{
				if	(!isMultipart && getVariablesToSend().size() > 0)
				{
					res = con.Post(url, hashtableToNVPair(getVariablesToSend()), hashtableToNVPair(headersToSend));
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
				if (getVariablesToSend().size() > 0 || getContentToSend().size() > 0)
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
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
		catch (ModuleException e)
		{
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
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

	@Override
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
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
			res = null;
		}
		catch (ModuleException e)
		{
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}

		return 0;
	}

	@Override
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
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
			return "";
		}
		catch (ModuleException e)
		{
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
			return "";
		}
	}

	@Override
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
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
		catch (ModuleException e)
		{
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
	}

	@Override
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
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
		catch (ModuleException e)
		{
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}

		return "";
	}

	@Override
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
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
		catch (ModuleException e)
		{
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
	}

	@Override
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
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
		catch (ModuleException e)
		{
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
	}

	@Override
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
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
		catch (ModuleException e)
		{
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
	}

	@Override
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

	@Override
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
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
		catch (IOException e)
		{
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
		catch (Exception e)
		{
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}

		return "";
	}

	@Override
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
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
		catch (ModuleException e)
		{
			setErrCode(ERROR_IO);
			setErrDescription(e.getMessage());
		}
	}


	@Override
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
