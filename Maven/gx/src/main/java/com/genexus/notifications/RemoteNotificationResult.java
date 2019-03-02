package com.genexus.notifications;

public class RemoteNotificationResult
{
	int errorCode;
	String errorDescription;
	byte deviceType;
	String deviceToken;
	
	public RemoteNotificationResult()
	{
	}
	
	public int getErrorCode()
	{
		return errorCode;
	}
	
	public String getErrorDescription()
	{
		return errorDescription;
	}
	
	public byte getDeviceType()
	{
		return deviceType;
	}
	
	public String getDeviceToken()
	{
		return deviceToken;
	}
}