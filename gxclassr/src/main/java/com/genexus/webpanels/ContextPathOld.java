package com.genexus.webpanels;
import com.genexus.servlet.http.IHttpServletRequest;

public class ContextPathOld implements IContextPath 
{
	public String getContextPath(IHttpServletRequest request)
	{
		try
		{
			return request.getRequestURI().substring(0, request.getRequestURI().length() - request.getServletPath().length());
		}catch(Exception e)
		{
			return "";
		}
	}
	
}
