package com.genexus.internet;

public abstract class GXHttpClient implements IHttpClient{
	private final int BASIC  = 0;
	private final int DIGEST = 1;
	private final int NTLM   = 2;

	private int errCode;
	private String errDescription = "";
	private String proxyHost = "";// = HTTPConnection.getDefaultProxyHost() == null ? "" : HTTPConnection.getDefaultProxyHost();
	private int proxyPort = 80;// = HTTPConnection.getDefaultProxyPort() == 0 ? 80 : HTTPConnection.getDefaultProxyPort();
	private boolean includeCookies = true;

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

}
