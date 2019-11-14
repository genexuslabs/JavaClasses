package com.genexus.internet;

import com.genexus.common.interfaces.IGXWSAddressing;
import com.genexus.common.interfaces.IGXWSSecurity;

public class Location
{
	private String host = "";
	private String name  = "";
	private int port = 0;
	private String proxyServerHost = "";
	private int proxyServerPort = 0;
	private String WSDLURL  = "";
	private String baseURL  = "";
	private String resourceName = "";
	private byte secure = -1;
	private short timeout = -1;
	private byte authenticationMethod = -1;
	private String authenticationUser  = "";
	private String authenticationRealm  = "";
	private String authenticationPassword  = "";
	private byte proxyAuthenticationMethod = -1;
	private String proxyAuthenticationUser  = "";
	private String proxyAuthenticationRealm  = "";
	private String proxyAuthenticationPassword  = "";
	private IGXWSAddressing wsAddressing;
	private IGXWSSecurity wsSecurity;
	private String certificate = "";
	private short cancelOnError = 0;
	private String GroupLocation;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getHost()
	{
		return host;
	}

	public int getPort()
	{
		return port;
	}
	
	public String getProxyServerHost()
	{
		return proxyServerHost;
	}

	public int getProxyServerPort()
	{
		return proxyServerPort;
	}

	public String getWSDLURL()
	{
		return WSDLURL;
	}
	public String getBaseURL()
	{
		return baseURL;
	}
	public String getResourceName()
	{
		return resourceName;
	}
	public byte getSecure()
	{
		return secure;
	}

	public short getTimeout()
	{
		return timeout;
	}

	public byte getAuthenticationMethod()
	{
		return authenticationMethod;
	}

 	public String getAuthenticationUser()
	{
		return authenticationUser;
	}

	public String getAuthenticationRealm()
	{
		return authenticationRealm;
	}

	public String getAuthenticationPassword()
	{
		return authenticationPassword;
	}
	
	public byte getProxyAuthenticationMethod()
	{
		return proxyAuthenticationMethod;
	}

 	public String getProxyAuthenticationUser()
	{
		return proxyAuthenticationUser;
	}

	public String getProxyAuthenticationRealm()
	{
		return proxyAuthenticationRealm;
	}

	public String getProxyAuthenticationPassword()
	{
		return proxyAuthenticationPassword;
	}
	
	public IGXWSAddressing getWSAddressing()
	{
		return wsAddressing;
	}
	
	public IGXWSSecurity getWSSecurity()
	{
		return wsSecurity;
	}	

	public String getCertificate()
	{
		return certificate;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public void setPort(int port)
	{
		this.port = port;
	}
	
	public void setProxyServerHost(String proxyServerHost)
	{
		this.proxyServerHost = proxyServerHost;
	}

	public void setProxyServerPort(int proxyServerPort)
	{
		this.proxyServerPort = proxyServerPort;
	}

	public void setWSDLURL(String WSDLURL)
	{
		this.WSDLURL = WSDLURL;
	}

	public void setBaseURL(String baseURL)
	{
		this.baseURL = baseURL;
	}

	public void setResourceName(String resName)
	{
		resourceName = resName;
	}

	public void setSecure(int secure)
	{
		this.secure = (byte) secure;
	}

	public void setTimeout(int timeout)
	{
		this.timeout = (short) timeout;
	}

	public void setAuthenticationMethod(long authenticationMethod)
	{
		this.authenticationMethod = (byte) authenticationMethod;
	}

	public void setAuthenticationUser(String authenticationUser)
	{
		this.authenticationUser = authenticationUser;
	}

	public void setAuthenticationRealm(String authenticationRealm)
	{
		this.authenticationRealm = authenticationRealm;
	}

	public void setAuthenticationPassword(String authenticationPassword)
	{
		this.authenticationPassword = authenticationPassword;
	}
	
	public void setProxyAuthenticationMethod(long authenticationMethod)
	{
		this.proxyAuthenticationMethod = (byte) authenticationMethod;
	}

	public void setProxyAuthenticationUser(String authenticationUser)
	{
		this.proxyAuthenticationUser = authenticationUser;
	}

	public void setProxyAuthenticationRealm(String authenticationRealm)
	{
		this.proxyAuthenticationRealm = authenticationRealm;
	}

	public void setProxyAuthenticationPassword(String authenticationPassword)
	{
		this.proxyAuthenticationPassword = authenticationPassword;
	}
	
	public void setWSAddressing(IGXWSAddressing wsAddressing)
	{
		this.wsAddressing = wsAddressing;
	}	
	
	public void setWSSecurity(IGXWSSecurity wsSecurity)
	{
		this.wsSecurity = wsSecurity;
	}	

	public void setCertificate(String path)
	{
		certificate = path;
	}
	
	int authentication;
	public void setAuthentication(int authentication)
	{
		this.authentication = authentication;
	}

	public byte getAuthentication()
	{
		return (byte) authentication;
	}
	
	int proxyAuthentication;
	public void setProxyAuthentication(int authentication)
	{
		this.proxyAuthentication = authentication;
	}

	public byte getProxyAuthentication()
	{
		return (byte) proxyAuthentication;
	}	

	
	public short getCancelOnError()
	{
		return cancelOnError;
	}

	public void setCancelOnError(short cancelOnError)
	{
		this.cancelOnError = cancelOnError;
	}
	
	public String getGroupLocation()
	{
		return GroupLocation;
	}

	public void setGroupLocation(String GroupLocation)
	{
		this.GroupLocation = GroupLocation;
	}	
}