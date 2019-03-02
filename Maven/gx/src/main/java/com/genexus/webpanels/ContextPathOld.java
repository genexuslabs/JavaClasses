package com.genexus.webpanels;
import javax.servlet.http.HttpServletRequest;

public class ContextPathOld implements IContextPath 
{
	public String getContextPath(HttpServletRequest request)
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
