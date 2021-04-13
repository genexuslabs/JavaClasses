package com.genexus.webpanels;
import com.genexus.servlet.http.IHttpServletRequest;

public class ContextPathNew implements IContextPath 
{
	public String getContextPath(IHttpServletRequest request)
	{
		String path = request.getContextPath();
		return path == null?"":path;
	}
	
}
