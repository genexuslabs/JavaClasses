
package com.genexus.internet;

import java.io.*;
import com.genexus.common.interfaces.SpecificImplementation;

/**
* Esta clase la usa un cliente que quiere hacer un get/post y recibir el resultado
*/
public class HttpClient
{
	private IHttpClient session;
	
	public HttpClient()
	{
		session = SpecificImplementation.HttpClient.initHttpClientImpl();	// Se crea la instancia dependiendo si existe o no la implementacion de las librerias de Java
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

	public String getString()
	{	
		return session.getString();
	}

	public String readChunk()
	{
		return session.readChunk();
	}

	public boolean getEof()
	{
		return session.getEof();
	}

	public void toFile(String fileName)
	{
		session.toFile(fileName);
	}
	
	public void cleanup()
	{
		session.cleanup();
	}

}