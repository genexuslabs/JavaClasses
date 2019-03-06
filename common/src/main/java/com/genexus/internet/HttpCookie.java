package com.genexus.internet;
import java.util.Date;
import com.genexus.CommonUtil;
import com.genexus.common.interfaces.SpecificImplementation;

public class HttpCookie
{

	String name;
	String value;
	String path;
	Date expirationDate;
	String domain;
	boolean secure;
	boolean httpOnly = true;
	
	public HttpCookie()
	{
		name = "";
		value = "";
		path = "";
		expirationDate = CommonUtil.resetTime( CommonUtil.nullDate());
		domain = "";
		secure = false;
		if (SpecificImplementation.HttpCookie != null)
			httpOnly = SpecificImplementation.HttpCookie.getHttpOnly();
		else
			httpOnly = false;
	}

	public String getName()
	{
		return name;
	}
	
	public void setName( String name)
	{			
		this.name = name;
	}

	public String getValue()
	{
		return value;
	}
	
	public void setValue( String value)
	{
		this.value = value;
	}
	
	public void setPath( String path)
	{
		this.path = path;
	}
	
	public String getPath()
	{
		return path;
	}

	public void setExpirationdate( Date expirationDate)
	{
		this.expirationDate = expirationDate;
	}
	
	public Date getExpirationdate()
	{
		return expirationDate;
	}

	public void setDomain( String domain)
	{
		this.domain = domain;
	}
	
	public String getDomain()
	{
		return domain;
	}

	public void setSecure( boolean secure)
	{
		this.secure = secure;
	}
	
	public boolean getSecure()
	{
		return secure;
	}

	public void setHttponly( boolean httpOnly)
	{
		this.httpOnly = httpOnly;
	}
	
	public boolean getHttponly()
	{
		return httpOnly;
	}
}