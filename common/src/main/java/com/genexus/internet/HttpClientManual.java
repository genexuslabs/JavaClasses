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

//	private Vector<HttpClientPrincipal> basicAuthorization = new Vector<HttpClientPrincipal>();
//	private Vector<HttpClientPrincipal> digestAuthorization = new Vector<>();
//	private Vector<HttpClientPrincipal> NTLMAuthorization = new Vector<>();
//
//	private Vector<HttpClientPrincipal> basicProxyAuthorization = new Vector<>();
//	private Vector<HttpClientPrincipal> digestProxyAuthorization = new Vector<>();
//	private Vector<HttpClientPrincipal> NTLMProxyAuthorization = new Vector<>();
//	private String prevURLhost;
//	private String prevURLbaseURL;
//	private int prevURLport;
//	private int prevURLsecure;
//	private boolean isURL = false;
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

	@Override
	public void execute(String method, String url)
	{
		resetErrors();

		// ESTE BLOQUE COMENTADO FUE REEMPLAZADO POR LA FUNCION getURLValid
//		URI uri;
//		try
//		{
//			uri = new URI(url);		// En caso que la URL pasada por parametros no sea una URL valida, salta una excepcion en esta linea, y se continua haciendo todo el proceso con los datos ya guardado como atributos
//			prevURLhost = this.getHost();
//			prevURLbaseURL = this.getBaseURL();
//			prevURLport = this.getPort();
//			prevURLsecure = this.getSecure();
//			isURL = true;
//			setURL(url);
//
//			StringBuilder relativeUri = new StringBuilder();
//			if (uri.getPath() != null) {
//				relativeUri.append(uri.getPath());
//			}
//			if (uri.getQueryString() != null) {
//				relativeUri.append('?').append(uri.getQueryString());
//			}
//			if (uri.getFragment() != null) {
//				relativeUri.append('#').append(uri.getFragment());
//			}
//			url = relativeUri.toString();
//		}
//		catch (ParseException e)
//		{
//			//No es una URL
//		}
		url = getURLValid(url);

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

			if(getHostChanged() || getAuthorizationChanged())
			{ // Si el host cambio o si se agrego alguna credencial
				for (Enumeration en = getBasicAuthorization().elements(); en.hasMoreElements(); )
				{
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					con.addBasicAuthorization(p.realm, p.user, p.password);
				}

				for (Enumeration en = getDigestAuthorization().elements(); en.hasMoreElements(); )
				{
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					con.addDigestAuthorization(p.realm, p.user, p.password);
				}

				for (Enumeration en = getNTLMAuthorization().elements(); en.hasMoreElements(); )
				{
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					//p.addBasicAuthorization(p.realm, p.user, p.password);
				}
			}

			setHostChanged(false);
			setAuthorizationChanged(false); // Desmarco las flags

			if(proxyInfoChanged || getAuthorizationProxyChanged())
			{ // Si el poxyHost cambio o si se agrego alguna credencial para el proxy
				for (Enumeration en = getBasicProxyAuthorization().elements(); en.hasMoreElements(); )
				{
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					con.addBasicProxyAuthorization(p.realm, p.user, p.password);
				}

				for (Enumeration en = getDigestProxyAuthorization().elements(); en.hasMoreElements(); )
				{
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					con.addDigestProxyAuthorization(p.realm, p.user, p.password);
				}

				for (Enumeration en = getNTLMProxyAuthorization().elements(); en.hasMoreElements(); )
				{
					HttpClientPrincipal p = (HttpClientPrincipal) en.nextElement();
					//p.addBasicAuthorization(p.realm, p.user, p.password);
				}
			}

			proxyInfoChanged = false; // Desmarco las flags
			setAuthorizationProxyChanged(false);


			if  (!url.startsWith("/"))		// Este caso sucede cuando salta la excepcion ParseException, determinando que la url pasada por parametro no es una URL valida
				url = getBaseURL().trim() + url;

			if	(method.equalsIgnoreCase("GET"))
			{
				if (getContentToSend().size() > 0)
					res = con.Get(url, "", hashtableToNVPair(getheadersToSend()), getData());
				else
					res = con.Get(url, "", hashtableToNVPair(getheadersToSend()));
			}
			else if (method.equalsIgnoreCase("POST"))
			{
				if	(!getIsMultipart() && getVariablesToSend().size() > 0)
				{
					res = con.Post(url, hashtableToNVPair(getVariablesToSend()), hashtableToNVPair(getheadersToSend()));
				}
				else
				{
					res = con.Post(url, getData(), hashtableToNVPair(getheadersToSend()));
				}
			}
			else if (method.equalsIgnoreCase("PUT"))
			{
				res = con.Put(url, getData(), hashtableToNVPair(getheadersToSend()));
			}
			else if (method.equalsIgnoreCase("DELETE"))
			{
				if (getVariablesToSend().size() > 0 || getContentToSend().size() > 0)
				{
					res = con.Delete(url, getData(), hashtableToNVPair(getheadersToSend()));
				}
				else
				{
					res = con.Delete(url, hashtableToNVPair(getheadersToSend()));
				}
			}
			else
			{
				res = con.ExtensionMethod(method, url, getData(), hashtableToNVPair(getheadersToSend()));
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
			if (getIsURL())
			{
				this.setHost(getPrevURLhost());
				this.setBaseURL(getPrevURLbaseURL());
				this.setPort(getPrevURLport());
				this.setSecure(getPrevURLsecure());
				setIsURL(false);
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

}
