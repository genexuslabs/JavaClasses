package com.genexus.common.interfaces;

public interface IGXWSAddressingEndPoint
{
	String getAddress();
	void setAddress(String address);
	String getPortType();
	void setPortType(String portType);
	String getServiceName();
	void setServiceName(String serviceName);
	String getProperties();
	void setProperties(String properties);
	String getParameters();
	void setParameters(String parameters);	
}

