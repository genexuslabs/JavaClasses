package com.genexus.notifications;

public class NotificationsConfiguration
{
	String iOScertificate;
	String iOScertificatePassword;
	boolean iOSuseSandboxServer;
	String androidSenderId;
	String androidSenderAPIKey;
	String WNSPackageSecurityIdentifier;
	String WNSClientSecret;
	
	public NotificationsConfiguration()
	{
	}
	
	public String getiOScertificate()
	{
		return iOScertificate;
	}
	
	public void setiOScertificate(String iOScertificate)
	{
		this.iOScertificate = iOScertificate;
	}	
	
	public String getiOScertificatePassword()
	{
		return iOScertificatePassword;
	}
	
	public void setiOScertificatePassword(String iOScertificatePassword)
	{
		this.iOScertificatePassword = iOScertificatePassword;
	}	
	
	public boolean getiOSuseSandboxServer()
	{
		return iOSuseSandboxServer;
	}	
	
	public void setiOSuseSandboxServer(boolean iOSuseSandboxServer)
	{
		this.iOSuseSandboxServer = iOSuseSandboxServer;
	}	
	
	public String getandroidSenderId()
	{
		return androidSenderId;
	}
	
	public void setandroidSenderId(String androidSenderId)
	{
		this.androidSenderId = androidSenderId;
	}	
	
	public String getandroidSenderAPIKey()
	{
		return androidSenderAPIKey;
	}
	
	public void setandroidSenderAPIKey(String androidSenderAPIKey)
	{
		this.androidSenderAPIKey = androidSenderAPIKey;
	}	
	
	public String getWNSPackageSecurityIdentifier()
	{
		return WNSPackageSecurityIdentifier;
	}

	public void setWNSPackageSecurityIdentifier(String WNSPackageSecurityIdentifier)
	{
		this.WNSPackageSecurityIdentifier = WNSPackageSecurityIdentifier;
	}	
	
	public String getWNSClientSecret()
	{
		return WNSClientSecret;
	}	
	
	public void getWNSClientSecret(String WNSClientSecret)
	{
		this.WNSClientSecret = WNSClientSecret;
	}
}