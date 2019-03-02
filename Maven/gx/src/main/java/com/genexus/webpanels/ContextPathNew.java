package com.genexus.webpanels;
import javax.servlet.http.HttpServletRequest;

public class ContextPathNew implements IContextPath 
{
	public String getContextPath(HttpServletRequest request)
	{
		String path = request.getContextPath();
		return path == null?"":path;
	}
	
}
