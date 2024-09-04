package com.genexus.db.driver;

import json.org.json.JSONObject;
import json.org.json.JSONException;

public class ExternalProviderMetadata
{
	public static final int AMAZONS3 = 1;
	public static final int OPENSTACK = 2;
	
	private int provider;
	private String bucket;
	private String folder;
	private String name;
	
	public int getProvider() 
	{
		return provider;
	}
	
	public void setProvider(int provider) 
	{
		this.provider = provider;
	}
	
	public String getBucket() 
	{
		return bucket;
	}
	
	public void setBucket(String bucket) 
	{
		this.bucket = bucket;
	}
	
	public String getFolder() 
	{
		return folder;
	}
	
	public void setFolder(String folder) 
	{
		this.folder = folder;
	}
	
	public String getName() 
	{
		return name;
	}
	
	public void setName(String name) 
	{
		this.name = name;
	}
	
	public String toJson()
	{
		try
		{
			JSONObject jsonString = new JSONObject();
			jsonString.put("Provider", new Integer(getProvider()));
			jsonString.put("Bucket", getBucket());
			jsonString.put("Folder", getFolder());
			jsonString.put("Name", getName());
			return jsonString.toString();
		}
		catch (JSONException e)
		{
			System.err.println("Error creating external provider metadata " + e.getMessage());
			return "";
		}
	}
	
	public void fromJson(String jsonString)
	{
		try
		{		
			JSONObject jsonObject = new JSONObject(jsonString);
			setProvider(new Integer((Integer)jsonObject.get("Provider")).intValue());
			setBucket((String)jsonObject.get("Bucket"));
			setFolder((String)jsonObject.get("Folder"));
			setName((String)jsonObject.get("Name"));
		}
		catch (JSONException e)
		{
			System.err.println("Error reading external provider metadata " + e.getMessage());
		}		
	}
		
}