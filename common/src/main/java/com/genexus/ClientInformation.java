package com.genexus;

import com.genexus.common.interfaces.SpecificImplementation;


public class ClientInformation
{
	static public String getAppVersionCode()
	{
		String id = "";
		if (SpecificImplementation.Application.getModelContext().getHttpContext() != null)
			id = SpecificImplementation.Application.getModelContext().getHttpContext().getHeader("GXAppVersionCode");
		return id;
	}
	static public String getAppVersionName()
	{
		String id = "";
		if (SpecificImplementation.Application.getModelContext().getHttpContext() != null)
			id = SpecificImplementation.Application.getModelContext().getHttpContext().getHeader("GXAppVersionName");
		return id;
	}
	static public String getId()
	{
		String id = "";
		IHttpContext ctx = SpecificImplementation.Application.getModelContext().getHttpContext();
		if (ctx != null) {
			id = ctx.getHeader("DeviceId");
		if (id == null || id.equals(""))
			id = ctx.getClientId();
		}
		return id;
	}

	static public byte getDeviceType()
	{
		String type = "";
		if (SpecificImplementation.Application.getModelContext().getHttpContext() != null)
			type = SpecificImplementation.Application.getModelContext().getHttpContext().getHeader("DeviceType");
		try
		{
			return Byte.valueOf(type).byteValue();
		}
		catch(Exception ex)
		{
			return 0;
		}
	}
	static public String getOSName()
	{
		return SpecificImplementation.Application.getModelContext().getHttpContext().getHeader("DeviceOSName");
	}

	static public String getOSVersion()
	{
		return SpecificImplementation.Application.getModelContext().getHttpContext().getHeader("DeviceOSVersion");
	}

	static public String getNetworkID()
	{
		return SpecificImplementation.Application.getModelContext().getHttpContext().getHeader("DeviceNetworkId");
	}

	static public String getLanguage()
	{
		return SpecificImplementation.Application.getModelContext().getHttpContext().getHeader("Accept-Language");
	}
	static public String getPlatformName()
	{
		return SpecificImplementation.Application.getModelContext().getHttpContext().getHeader("PlatformName");
	}
}
