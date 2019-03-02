package com.genexus.notifications.properties;

public class Properties {
	private String _type = "";
	private String _name = "";
	private String _iOScertificate = "";
	private String _iOScertificatePassword = "";
	private boolean _iOSUseSandboxServer = true;
	
	private String _androidUser = "";
	private String _androidAPIKey = "";
	
	public String getType() {
		return _type;
	}
	
	void setType(String type) {
		_type = type;
	}
	
	public String getName() {
		return _name;
	}
	
	void setName(String name) {
		_name = name;
	}
	
	public String getiOScertificate() {
		return _iOScertificate;
	}
	
	void setiOScertificate(String certificate) {
		_iOScertificate = certificate;
	}
	
	public String getiOScertificatePassword() {
		return _iOScertificatePassword;
	}
	
	void setiOScertificatePassword(String password) {
		_iOScertificatePassword = password;
	}
	
	public boolean getiOSUseSandboxServer() {
		return _iOSUseSandboxServer;
	}
	
	void setiOSUserSandboxServer(boolean useSandbox) {
		_iOSUseSandboxServer = useSandbox;
	}
	
	public String getAndroidUser() {
		return _androidUser;
	}
	
	void setAndroidUser(String user) {
		_androidUser = user;
	}
	
	public String getAndroidAPIKey() {
		return _androidAPIKey;
	}
	
	void setAndroidAPIKey(String apiKey) {
		_androidAPIKey = apiKey;
	}
	
}
