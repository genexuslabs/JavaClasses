package com.genexus.notifications.properties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import json.org.json.JSONArray;
import json.org.json.JSONObject;

public class Certificates {
	private Hashtable<String,Properties> _properties;
	
	private static Hashtable<String, String> _androidUserTokens = new Hashtable<String,String>();
	
	private static Certificates _instance = new Certificates();
	private Certificates() {
		loadProperties();
	}
	
	public static Certificates getInstance() {
		return _instance;
	}
	
	public String getAndroidUserTokenFor(String applicationId) {
		applicationId = applicationId.toLowerCase();
		if (_androidUserTokens.containsKey(applicationId))
			return (String)_androidUserTokens.get(applicationId);
		
		return null;
	}
	
	public void setAndroidUserTokenFor(String applicationId, String userToken) {
		applicationId = applicationId.toLowerCase();
		if (!_androidUserTokens.containsKey(applicationId))
			_androidUserTokens.put(applicationId, userToken);
	}
	
	public Properties getPropertiesFor(String applicationId) {
		applicationId = applicationId.toLowerCase();
		if (_properties.containsKey(applicationId))
			return (Properties)_properties.get(applicationId);
		
		return null;
	}
	
	private void loadProperties() {
		
		_properties = new Hashtable<String,Properties>();
		
		File f = new File(Certificates.getFilePath("notifications.json"));
		if(f.exists()) {
			try {
				JSONObject json = new JSONObject(Certificates.readTextFile(f));
				JSONArray notifications = json.getJSONArray("Notifications");
				for(int i=0;i<notifications.length();i++) {
					JSONObject obj = notifications.getJSONObject(i);
					
					Properties p = new Properties();
					p.setName(obj.getString("Name"));
					p.setType(obj.getString("Type"));
					
					JSONObject props = obj.getJSONObject("Properties");
					p.setiOScertificate(Certificates.ScanFile(props.getString("iOScertificate")));
					p.setiOScertificatePassword(props.getString("iOScertificatePassword"));
					p.setiOSUserSandboxServer(props.getBoolean("iOSuseSandboxServer"));
					p.setAndroidUser(props.getString("androidSenderId"));
					p.setAndroidAPIKey(props.getString("androidSenderAPIKey"));
					
					_properties.put(p.getName().toLowerCase(), p);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static String ScanFile(String fileName) {
		File file = new File(fileName);
		if (file.exists())
			return fileName;
		file = new File(Certificates.getFilePath(fileName));
		if (file.exists())
			return file.getAbsolutePath();
		
		return "";
	}
	
	private static String readTextFile(File file) throws IOException {
		StringBuffer sb = new StringBuffer(1024);
		BufferedReader reader = new BufferedReader(new FileReader(file));
				
		char[] chars = new char[1024];
		int numRead = 0;
		while( (numRead = reader.read(chars)) > -1){
			sb.append(String.valueOf(chars));	
		}

		reader.close();

		return sb.toString();
	}
	
	private static String getPrivateDirectory() {		if(com.genexus.ApplicationContext.getInstance().isServletEngine())		{
			return com.genexus.ModelContext.getModelContext().getHttpContext().getDefaultPath() + 
			File.separator + "WEB-INF" + File.separatorChar + "private";		}		else		{
			return "private";		}
	}
	
	private static String getFilePath(String fileName) {
		File dir = new File(getPrivateDirectory());
		return new File(dir, fileName).getAbsolutePath();
	}
}
