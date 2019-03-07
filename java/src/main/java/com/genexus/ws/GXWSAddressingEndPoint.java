package com.genexus.ws;

public class GXWSAddressingEndPoint
{
	private String address;
	private String portType;
	private String serviceName;
	private String properties;
	private String parameters;
	

	public GXWSAddressingEndPoint()
	{
		address = "";
		portType = "";
		serviceName = "";
		properties = "";
		parameters = "";
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address.trim();
	}

	public String getPortType()
	{
		return portType;
	}

	public void setPortType(String portType)
	{
		this.portType = portType.trim();
	}

	public String getServiceName()
	{
		return serviceName;
	}

	public void setServiceName(String serviceName)
	{
		this.serviceName = serviceName.trim();
	}	
	
	public String getProperties()
	{
		return properties;
	}

	public void setProperties(String properties)
	{
		this.properties = properties.trim();
	}

	public String getParameters()
	{
		return parameters;
	}

	public void setParameters(String parameters)
	{
		this.parameters = parameters.trim();
	}	
}

